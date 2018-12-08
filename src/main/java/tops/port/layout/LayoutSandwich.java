package tops.port.layout;

import tops.port.calculate.util.DistanceCalculator;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.TorsionResult;
import tops.port.model.tse.BaseTSE;

public class LayoutSandwich implements TSELayout {
    
    private double GridUnitSize = 50;
    

    @Override
    public void layout(Chain chain, BaseTSE tse) {
        // TODO Auto-generated method stub
        
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
        TorsionResult torsionResult = longestStrandInLongSheet.closestApproach(longestStrandInShortSheet);
        if (Math.abs(torsionResult.getTorsion()) > 90.0) {
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
            sse.setFixedType(FixedType.SANDWICH);
        }
        for (SSE sse : chain.iterFixed(longSheetStart)) {
            sse.setFixedType(FixedType.SANDWICH);
        }
    }


}
