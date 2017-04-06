package tops.port.calculate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Vector3d;

import tops.port.model.BridgePartner;
import tops.port.model.BridgeType;
import tops.port.model.Chain;
import tops.port.model.SSE;
import tops.port.model.TorsionResult;

/** 
 * Merge structures if necessary 
 * The conditions are these: p and p.From are strands separated by less than minMergeStrandSeparation residues
 * they are part of the same sheet, either on the same side of a common bridge partner, or so far separated that
 * merging will create a barrel of at least 6 strands, or are not in the same sheet but separated by a very short loop
 * they satisfy geometric criteria (NB ClosestApproach returns a torsion angle).
 **/
public class CalculateMergedStrands implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateMergedStrands.class.getName());
    
    private double minMergeStrandSeparation = 5;
    private boolean mergeBetweenSheets = false;

    @Override
    public void calculate(Chain chain) {
        if (minMergeStrandSeparation <= 0) return;
        
        log.log(Level.INFO, "STEP: Searching for strand merges");
        List<SSE> sses = chain.getSSEs(); 
        SSE prev = null;
        for (int index = 1; index < sses.size(); index++) {
            SSE p = sses.get(index);
            int connectLoopLen = getLoopLength(p, prev);
            if (p.isStrand() && prev != null && prev.isStrand() && connectLoopLen < minMergeStrandSeparation) {
                int shortLoop = 1;
                int cbpd = this.connectBPDistance(p, prev);
                boolean sheetMerge = this.mergeBetweenSheets && connectLoopLen <= shortLoop;
                if ((cbpd == 2 && p.sameBPSide(prev)) || (cbpd > 5) ||sheetMerge ) {
                    TorsionResult result = p.ClosestApproach(prev);
                    if (Math.abs(result.torsion) < 90.0) { 
//                        System.out.println(String.format("Merging Strands %d %d\n", p.From.getSymbolNumber(), p.getSymbolNumber()));
                        this.joinToLast(p, prev, chain);
                        p.sortBridgePartners(); // XXX not sure why we have to sort
                    }
                }
            }
            prev = p;
        }
    }
    
    private int getLoopLength(SSE sse, SSE prev) {
        return sse.sseData.SeqStartResidue - prev.sseData.SeqFinishResidue - 1;
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

    /*
    function join_to_last

    Tom F. August 1992

    Function to join an sse to the previous one. returns if the
    secondary structures are different or the vectors are less than
    ninety degrees ie. antiparallel.
     */
    public void joinToLast(SSE sse, SSE lastSSE, Chain chain) {

        if (!lastSSE.isSameType(sse)) return;

        // Merge bridge partners 
        for (BridgePartner bridgePartner : sse.getBridgePartners()) {
            SSE toJoinPartner = bridgePartner.partner;
            BridgePartner common = null;
            for (BridgePartner other : toJoinPartner.getBridgePartners()) {
                // Does the bridge partner already have a bond to lastSSE */
                if (other.partner == lastSSE) {
                    common = other;
                    break;
                }
            }

            // If Bridge partner doesn't already exist create it */
            if (common == null) {
                for (BridgePartner bp : toJoinPartner.getBridgePartners()) {
                    if (bp.partner == sse) {
                        bp.partner = lastSSE;
                        BridgeType type = bridgePartner.bridgeType;
                        lastSSE.addBridgePartner(new BridgePartner(
                                toJoinPartner, bridgePartner.rangeMin, bridgePartner.rangeMax, type, null));
                        break;
                    }
                }
            } else {

                // Coalesce common bridge partner 
                for (BridgePartner lastBridgePartner : lastSSE.getBridgePartners()) {
                    if (lastBridgePartner.partner == toJoinPartner) {
                        lastBridgePartner.NumberBridgePartners += bridgePartner.NumberBridgePartners;
                        common.NumberBridgePartners = lastBridgePartner.NumberBridgePartners;
                        lastBridgePartner.rangeMax = bridgePartner.rangeMax;
                        toJoinPartner.removeBridgePartner(sse);
                    }
                }
            }
        }

        // Switch ends 
        lastSSE.axis.AxisFinishPoint = (Vector3d)sse.axis.AxisFinishPoint.clone();

        // Merge residues - don't include fixed 
        lastSSE.sseData.SeqFinishResidue = sse.sseData.SeqFinishResidue;
        lastSSE.sseData.PDBFinishResidue = sse.sseData.PDBFinishResidue;

        lastSSE.incrementMerges();

        // FIXME - merge ranges only seem to be needed for chiral
        //        p.MergeRanges.append(new int[] {q.SeqStartResidue, q.SeqFinishResidue});
    }
    
    public void fixNeighbours(Chain chain) {
     // This is yet another hack for the time being to deal with neighbours */
        for (SSE sse : chain.getSSEs()) {
            // TODO
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
