package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.calculate.util.DistanceCalculator;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.TorsionResult;

public class CalculateSandwiches implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateSandwiches.class.getName());
    
    private double GridUnitSize = 50;
    
    /*
     * Function to detect beta sandwich structures re-written by DW 26/02/97
     */
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Searching for beta sandwiches");

        // detect and form sandwiches //
        List<SSE[]> sandwiches = new ArrayList<SSE[]>();
        for (SSE r : chain.getSSEs()) {
            if (r.isStrand() && r.hasFixedType(FixedType.FT_SHEET)) {
                String rDomain = this.FindDomain(r);
                for (SSE s : chain.iterNext(r)) {
                    String sDomain = this.FindDomain(s);
                    if (s.isStrand() && s.hasFixedType(FixedType.FT_SHEET) && sDomain == rDomain) { 
                        if (this.isSandwich(chain, r, s)) {
//                            System.out.println(String.format("Sandwich detected between %s and %s" , r, s));
                            sandwiches.add(new SSE[] {r, s});
                            this.makeSandwich(chain, r, s);
                        }
                    }
                }
            }
        }


        // turn the sandwiches into fixed structures //
        for (SSE[] sandwich : sandwiches) {
            chain.moveFixed(sandwich[0], sandwich[1]);
        }
    }

    // FIXME! : need to implement domains asap
    public String FindDomain(SSE sse) {
        return "YES!";
    }

    
    /**
    Function to determine whether two sheets constitute a sandwich.
    Both must have more than two strands, there must be at least a certain number of 
    pairwise contacts - this number is the the number of strands in the smallest sheet - ,
    and these contacts must span at least two strands of the larger sheet 
    **/
    public boolean isSandwich(Chain chain, SSE r, SSE s) {
        int ncontacts = 0;
        double MaxContactSeparation = 13.0;
        double MinOverLap = 2 * this.GridUnitSize;

        SSE p = chain.findFixedStart(r);
        SSE q = chain.findFixedStart(s);

        double s1 = chain.fixedSpan(p, this.GridUnitSize);
        double s2 = chain.fixedSpan(q, this.GridUnitSize);

        double MinContacts = 0;
        if (s1 < s2) {
            double tmp = s2;
            s2 = s1;
            s1 = tmp;
            SSE tmpC = q;
            q = p;
            p = tmpC;
            MinContacts = s1;
        } else {
            MinContacts = s2;
        }

        if ((p == q) || (s1 < 3) || (s2 < 3) 
                || (!p.hasFixedType(FixedType.FT_SHEET) 
                || (!q.hasFixedType(FixedType.FT_SHEET)))) {
            return false;
        }

        int LeftOverlap = Integer.MAX_VALUE;
        int RightOverlap = Integer.MAX_VALUE;
        for (SSE p1 : chain.iterFixed(p)) {
            for (SSE q1 : chain.iterFixedInclusive(q)) {
                if (p1.separation(q1) <= MaxContactSeparation || q1.separation(p1) <= MaxContactSeparation) {
                    ncontacts += 1;
                    int p1x = p1.getCartoonX();
                    if (p1x < LeftOverlap) LeftOverlap = p1x;
                    if (p1x > RightOverlap) RightOverlap = p1x;
                }
            }
        }

        int OverLap = RightOverlap - LeftOverlap;
        if (ncontacts >= MinContacts && OverLap >= MinOverLap) {
            return true;
        } else {
            return false;
        }
    }

    public void makeSandwich(Chain chain, SSE r, SSE s) {
        // determine relative sizes of sheets """
        SSE rSheetStart = chain.findFixedStart(r);
        SSE sSheetStart = chain.findFixedStart(s);

        double spanR = chain.fixedSpan(rSheetStart, this.GridUnitSize);
        double spanS = chain.fixedSpan(sSheetStart, this.GridUnitSize);

        SSE longSheetStart, shortSheetStart;
        if (spanR > spanS) {
            longSheetStart = rSheetStart;
            shortSheetStart = sSheetStart;
        } else {   
            longSheetStart = sSheetStart;
            shortSheetStart = rSheetStart;
        }

        // leave the longest alone and move the shortest """

        // first determine left and right most strands in the short sheet """
        SSE shortSheetLeftMost = chain.leftMost(shortSheetStart);
        SSE shortSheetRightMost = chain.rightMost(shortSheetStart);

        // determine the left most and right most strands in the long sheet """
        SSE longSheetLeftMost = chain.leftMost(longSheetStart);
        SSE longSheetRightMost = chain.rightMost(longSheetStart);

        // is the short sheet the right way round?, if not turn it """
        double leftSeparation = DistanceCalculator.secStrucSeparation(shortSheetLeftMost, longSheetLeftMost);
        double rightSeparation = DistanceCalculator.secStrucSeparation(shortSheetRightMost, longSheetLeftMost);
        if (leftSeparation > rightSeparation) {
            chain.flipSymbols(shortSheetStart);
            SSE tmp = shortSheetRightMost;
            shortSheetRightMost = shortSheetLeftMost;
            shortSheetLeftMost = tmp;
        }

        // now check directions of symbols """
        // this is done geometrically using the longest strand in each sheet """
        SSE longestStrandInShortSheet = chain.longestInFixed(rSheetStart);
        SSE longestStrandInLongSheet = chain.longestInFixed(sSheetStart);
        TorsionResult torsionResult = longestStrandInLongSheet.ClosestApproach(longestStrandInShortSheet);
        if (Math.abs(torsionResult.torsion) > 90.0) {
            if (longestStrandInShortSheet.getDirection() == longestStrandInLongSheet.getDirection()) {
                for (SSE sse : chain.iterFixed(shortSheetStart)) {
                    sse.flip();
                }
            }
        } else {
            if (longestStrandInShortSheet.getDirection() != longestStrandInLongSheet.getDirection()) {
                for (SSE sse : chain.iterFixed(shortSheetStart)) {
                    sse.flip();
                }
            }
        }
        
        // now reposition the short sheet, x direction try to get best position of left and right most strands in the
        //   short sheet subject with the constraint that the short sheet must lie within the x range of the long sheet  """
        SSE closestLongShortLeft = chain.closestInFixed(longSheetStart, shortSheetLeftMost);
        SSE closestLongShortRight = chain.closestInFixed(longSheetStart, shortSheetRightMost);
        double xmove = (closestLongShortLeft.getCartoonX() - shortSheetLeftMost.getCartoonX() + closestLongShortRight.getCartoonX() - shortSheetRightMost.getCartoonX()) / 2;
        double loff = longSheetLeftMost.getCartoonX() - shortSheetLeftMost.getCartoonX() - xmove;
        double roff = shortSheetRightMost.getCartoonX() + xmove - longSheetRightMost.getCartoonX();
        if (loff > 0) xmove += loff;
        if (roff > 0) xmove -= roff;

        double ymove = chain.lowestPos(longSheetStart) - this.GridUnitSize - shortSheetLeftMost.getCartoonY();

        for (SSE sse : chain.iterFixed(shortSheetStart)) {
            sse.setCartoonX((int)(sse.getCartoonX() + xmove));
            sse.setCartoonY((int) (sse.getCartoonY() + ymove));
        }
        

        // set type of fixed structure """
        for (SSE sse : chain.iterFixed(shortSheetStart)) {
            sse.setFixedType(FixedType.FT_SANDWICH);
        }
        for (SSE sse : chain.iterFixed(longSheetStart)) {
            sse.setFixedType(FixedType.FT_SANDWICH);
        }
    }

    @Override
    public void setParameter(String key, double value) {
        if (key.equals("gridUnitSize")) {
            GridUnitSize = value;
        }
    }

}
