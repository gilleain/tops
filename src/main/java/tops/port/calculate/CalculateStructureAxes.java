package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;

import tops.port.model.Chain;
import tops.port.model.SSE;
import tops.port.model.TorsionResult;

public class CalculateStructureAxes implements Calculation {
    
    private double MergeStrands = 5;
    private double MergeBetweenSheets = 0;
    
    public void calculate(Chain chain) {

        // Calculate axes //
        System.out.println("Calculating secondary structure vectors");
        for (SSE sse : chain.getSSEs()) {
            if (!sse.isStrand() && !sse.isHelix()) continue;
            SecondaryStructureAxis(chain, sse);
        }

        /* 
         * At this point we assign relative position of bridge partner strands  
         * Although this is already set from the dssp file it is reset here since the latter is unreliable
         */

        System.out.println("Assigning relative sides to bridge partner strands");
        for (SSE p : chain.getSSEs()) {
            p.assignRelativeSides();
        }
        if (this.MergeStrands > 0) {
            this.mergeStrands((int)this.MergeStrands, chain);
        }
    }
    

    public void SecondaryStructureAxis(Chain chain, SSE sse) {
        sse.setAxis(
                chain.secondaryStructureAxis(
                        sse.sseData.SeqStartResidue, sse.sseData.SeqFinishResidue));
    }
    

    /** 
     * Merge structures if necessary 
     * The conditions are these: p and p.From are strands separated by less than MergeStrands residues
     * they are part of the same sheet, either on the same side of a common bridge partner, or so far separated that
     * merging will create a barrel of at least 6 strands, or are not in the same sheet but separated by a very short loop
     * they satisfy geometric criteria (NB ClosestApproach returns a torsion angle).
     **/
    public void mergeStrands(int MergeStrands, Chain chain) {
        System.out.println("Searching for strand merges");
        for (SSE p : chain.getSSEs()) {
            int ConnectLoopLen = p.sseData.SeqStartResidue - p.From.sseData.SeqFinishResidue - 1;
            if (p.isStrand() && p.From != null && p.From.isStrand() && ConnectLoopLen < MergeStrands) {
                int VShortLoop = 1;
                int cbpd = this.ConnectBPDistance(p, p.From);
                boolean sheetMerge = this.MergeBetweenSheets != 0 && ConnectLoopLen <= VShortLoop;
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
    

    public int ConnectBPDistance(SSE p, SSE q) {
        List<SSE> connectList = new ArrayList<SSE>();
        List<Integer> distances = new ArrayList<Integer>();
        this.ListBPConnected(p, 0, connectList, distances);
        if (connectList.contains(q)) {
            return distances.get(connectList.indexOf(q));
        } else {
            return -1;
        }
    }

    public void ListBPConnected(SSE p, int currentDistance, List<SSE> connectList, List<Integer> distances) {
        if (connectList.contains(p)) {
            int listPos = connectList.indexOf(p);
            if (currentDistance < distances.get(listPos)) {
                distances.set(listPos, currentDistance);
            }
            return;
        } else {
            connectList.add(p);
            distances.add(currentDistance);
            currentDistance += 1;
            for (SSE q : p.getPartners()) {
                if (q == null) break;
                this.ListBPConnected(q, currentDistance, connectList, distances);
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
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
        
    }


}
