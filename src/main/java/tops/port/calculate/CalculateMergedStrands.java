package tops.port.calculate;

import java.util.HashMap;
import java.util.Map;

import tops.port.model.Chain;
import tops.port.model.SSE;
import tops.port.model.TorsionResult;

public class CalculateMergedStrands implements Calculation {
    
    private double MergeStrands = 5;
    private boolean MergeBetweenSheets = false;

    
    /** 
     * Merge structures if necessary 
     * The conditions are these: p and p.From are strands separated by less than minMergeStrandSeparation residues
     * they are part of the same sheet, either on the same side of a common bridge partner, or so far separated that
     * merging will create a barrel of at least 6 strands, or are not in the same sheet but separated by a very short loop
     * they satisfy geometric criteria (NB ClosestApproach returns a torsion angle).
     **/
    public void mergeStrands(int minMergeStrandSeparation, Chain chain) {
        System.out.println("Searching for strand merges");
        for (SSE p : chain.getSSEs()) {
            int connectLoopLen = p.sseData.SeqStartResidue - p.From.sseData.SeqFinishResidue - 1;
            if (p.isStrand() && p.From != null && p.From.isStrand() && connectLoopLen < minMergeStrandSeparation) {
                int shortLoop = 1;
                int cbpd = this.connectBPDistance(p, p.From);
                boolean sheetMerge = this.MergeBetweenSheets && connectLoopLen <= shortLoop;
                if ((cbpd == 2 && p.sameBPSide(p.From)) || (cbpd > 5) ||sheetMerge ) {
                    TorsionResult result = p.ClosestApproach(p.From);
                    if (Math.abs(result.torsion) < 90.0) { 
                        System.out.println(String.format("Merging Strands %d %d\n", p.From.getSymbolNumber(), p.getSymbolNumber()));
                        this.joinToLast(chain, p);
                        p.sortBridgePartners(); // XXX not sure why we have to sort
                    }
                }
            }
        }
    }
    

    public int connectBPDistance(SSE p, SSE q) {
        Map<SSE, Integer> distanceMap = new HashMap<SSE, Integer>();
        this.listBPConnected(p, 0, distanceMap);
        if (distanceMap.containsKey(q)) {
            return distanceMap.get(q);
        } else {
            return -1;
        }
    }
    
    public void listBPConnected(SSE p, int currentDistance, Map<SSE, Integer> distances) {
        if (distances.containsKey(p)) {
            if (currentDistance < distances.get(p)) {
                distances.put(p, currentDistance);
            }
        } else {
            distances.put(p, currentDistance);
            for (SSE q : p.getPartners()) {
                if (q == null) break;
                this.listBPConnected(q, currentDistance + 1, distances);
            }
        }
    }

    
    public void joinToLast(Chain chain, SSE sseToJoin) {
        sseToJoin.joinToLast();
        // This is yet another hack for the time being to deal with neighbours */
        for (SSE sse : chain.getSSEs()) {
            //            for (int j = 0; j < r.Neighbour.size(); j++) {
            //                if (r.Neighbour[j] == q) r.Neighbour[j] = p;
            //            }
        }
    }


    @Override
    public void calculate(Chain chain) {
        if (this.MergeStrands > 0) {
            this.mergeStrands((int)this.MergeStrands, chain);
        }        
    }


    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
        
    }
}
