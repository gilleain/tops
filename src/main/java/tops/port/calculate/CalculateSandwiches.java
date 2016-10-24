package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.tse.BaseTSE;
import tops.port.model.tse.Sandwich;
import tops.port.model.tse.Sheet;

public class CalculateSandwiches implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateSandwiches.class.getName());
    
    private double GridUnitSize = 50;
    
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Searching for beta sandwiches");
        for (Sandwich sandwich : findSandwiches(chain)) {
            chain.addTSE(sandwich);
        }
    }
    
    /*
     * Function to detect beta sandwich structures re-written by DW 26/02/97
     */
    public void calculateOld(Chain chain) {
        log.log(Level.INFO, "STEP : Searching for beta sandwiches");

        // detect and form sandwiches //
        List<SSE[]> sandwiches = new ArrayList<SSE[]>();
        for (SSE r : chain.getSSEs()) {
            if (r.isStrand() && r.hasFixedType(FixedType.SHEET)) {
                String rDomain = this.FindDomain(r);
                for (SSE s : chain.iterNext(r)) {
                    String sDomain = this.FindDomain(s);
                    if (s.isStrand() && s.hasFixedType(FixedType.SHEET) && sDomain == rDomain) { 
                        if (this.isSandwich(chain, r, s)) {
//                            System.out.println(String.format("Sandwich detected between %s and %s" , r, s));
                            sandwiches.add(new SSE[] {r, s});
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
    
    private List<Sandwich> findSandwiches(Chain chain) {
        List<Sandwich> sandwiches = new ArrayList<Sandwich>();
        List<BaseTSE> tses = chain.getTSEs();
        for (int tseIndex = 0; tseIndex < tses.size(); tseIndex++) {
            BaseTSE tse1 = tses.get(tseIndex);
            if (tse1 instanceof Sheet) {
                Sheet sheet1 = (Sheet) tse1;
                String domain1 = findDomain(sheet1);
                for (int tseIndex2 = tseIndex + 1; tseIndex2 < tses.size(); tseIndex2++) {
                    BaseTSE tse2 = tses.get(tseIndex2);
                    if (tse2 instanceof Sheet) {
                        Sheet sheet2 = (Sheet) tse2;
                        log.info("Examining " + sheet1 + " against " + sheet2);
                        String domain2 = findDomain(sheet2);
                        if (!domain1.equals(domain2)) continue;
                        if (isSandwich(chain, sheet1, sheet2)) {
                            sandwiches.add(new Sandwich(sheet1, sheet2));
                        }
                    }
                }
            }
        }
        return sandwiches;
    }

    private boolean isSandwich(Chain chain, Sheet sheet1, Sheet sheet2) {
        int minDistance = 13;   // XXX magic number...
        int span1 = sheet1.span();
        int span2 = sheet2.span();
        
        int minContacts = Math.min(span1, span2);
        int contacts = 0;
        for (SSE strandP : sheet1.getElements()) {
            for (SSE strandQ : sheet2.getElements()) {
                if (strandP.separation(strandQ) < minDistance) {
                    contacts++;
                }
            }
        }
        log.log(Level.INFO, String.format("contacts = %s, min = %s", contacts, minContacts));
        return contacts > minContacts;
    }

    // FIXME! : need to implement domains asap
    public String FindDomain(SSE sse) {
        return "YES!";
    }
    
    // FIXME! : need to implement domains asap
    public String findDomain(Sheet sheet) {
        return "YES!";
    }

    
    /**
     * Function to determine whether two sheets constitute a sandwich.
     * Both must have more than two strands, there must be at least a certain number of 
     * pairwise contacts - this number is the the number of strands in the smallest sheet - ,
     * and these contacts must span at least two strands of the larger sheet.
     * 
     * @param chain
     * @param r
     * @param s
     * @return
     */
    public boolean isSandwich(Chain chain, SSE r, SSE s) {
        double maxContactSeparation = 13.0;
        double minOverlap = 2 * this.GridUnitSize;

        SSE p = chain.findFixedStart(r);
        SSE q = chain.findFixedStart(s);

        double s1 = chain.fixedSpan(p, this.GridUnitSize);
        double s2 = chain.fixedSpan(q, this.GridUnitSize);

        double minContacts = 0;
        if (s1 < s2) {
            double tmp = s2;
            s2 = s1;
            s1 = tmp;
            SSE tmpC = q;
            q = p;
            p = tmpC;
            minContacts = s1;
        } else {
            minContacts = s2;
        }

        if ((p == q) || (s1 < 3) || (s2 < 3) 
                || (!p.hasFixedType(FixedType.SHEET) 
                || (!q.hasFixedType(FixedType.SHEET)))) {
            return false;
        }

        int leftOverlap = Integer.MAX_VALUE;
        int rightOverlap = Integer.MAX_VALUE;
        int ncontacts = 0;
        for (SSE p1 : chain.iterFixed(p)) {
            for (SSE q1 : chain.iterFixedInclusive(q)) {
                if (p1.separation(q1) <= maxContactSeparation || q1.separation(p1) <= maxContactSeparation) {
                    ncontacts += 1;
                    int p1x = p1.getCartoonX();
                    if (p1x < leftOverlap) leftOverlap = p1x;
                    if (p1x > rightOverlap) rightOverlap = p1x;
                }
            }
        }

        int overlap = rightOverlap - leftOverlap;
        if (ncontacts >= minContacts && overlap >= minOverlap) {
            return true;
        } else {
            return false;
        }
    }
    

    @Override
    public void setParameter(String key, double value) {
        if (key.equals("gridUnitSize")) {
            GridUnitSize = value;
        }
    }

}
