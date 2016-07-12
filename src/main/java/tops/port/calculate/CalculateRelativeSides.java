package tops.port.calculate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.SSE;
import tops.port.model.TorsionResult;

public class CalculateRelativeSides implements Calculation {

    private double MergeStrands = 5;
    private boolean MergeBetweenSheets = false;

    @Override
    public void calculate(Chain chain) {
        /* 
         * At this point we assign relative position of bridge partner strands  
         * Although this is already set from the dssp file it is reset here since the latter is unreliable
         */

        System.out.println("Assigning relative sides to bridge partner strands");
        for (SSE p : chain.getSSEs()) {
            assignRelativeSides(p);
        }
        if (this.MergeStrands > 0) {
            this.mergeStrands((int)this.MergeStrands, chain);
        }
    }

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


    public void joinToLast(Chain chain, SSE sseToJoin) {
        sseToJoin.joinToLast();
        // This is yet another hack for the time being to deal with neighbours */
        for (SSE sse : chain.getSSEs()) {
            //            for (int j = 0; j < r.Neighbour.size(); j++) {
            //                if (r.Neighbour[j] == q) r.Neighbour[j] = p;
            //            }
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

    public void assignRelativeSides(SSE sse) {
        if (sse.isStrand()) {
            for (BridgePartner bp : sse.getBridgePartners()) {
                bp.side = BridgePartner.Side.UNKNOWN;
            }
            int RefBP = sse.LongestBridgeRange();
            BridgePartner BP1 = sse.getBridgePartners().get(RefBP);
            if (RefBP >= 0 && BP1 != null) {
                BP1.side = BridgePartner.Side.LEFT;
                // Assign side for those 'BridgeOverlap'ing with the RefBP //
                for (int j = 0; j < sse.getBridgePartners().size(); j++) {
                    BridgePartner BP2 = sse.getBridgePartners().get(j);
                    if (j != RefBP && BP2 != null) {
                        if (bridgeOverlap(sse, BP1.partner, BP2.partner)) {
                            BP2.side = BridgePartner.Side.RIGHT;
                        }
                    }
                }
                // Sort out any other sides which can be calculated by BridgeOverlaps with other than the RefBP //
                for (int j = 0; j < sse.getBridgePartners().size(); j++) {
                    BridgePartner BP2 = sse.getBridgePartner(j);
                    if (BP2 != null && BP2.side == BridgePartner.Side.UNKNOWN) {
                        for (int k = 0; k < sse.getBridgePartners().size(); k++) {
                            BridgePartner BP3 = sse.getBridgePartners().get(k);
                            if (k != j && BP3 != null && BP3.side == BridgePartner.Side.UNKNOWN) {
                                if (bridgeOverlap(sse, BP2.partner, BP3.partner)) {
                                    if (BP3.side == BridgePartner.Side.LEFT)
                                        BP2.side = BridgePartner.Side.RIGHT;
                                } else {
                                    BP2.side = BridgePartner.Side.LEFT;
                                }
                            }
                        }
                    }
                }

                // The rest have to be done geometrically //
                for (int j = 0; j < sse.getBridgePartners().size(); j++) {
                    BridgePartner BP2 = sse.getBridgePartners().get(j);
                    if (BP2 != null && BP2.side == BridgePartner.Side.UNKNOWN) {
                        if (geometricSameSide(sse, BP1.partner, BP2.partner)) {
                            BP2.side = BridgePartner.Side.LEFT;
                        } else {
                            BP2.side = BridgePartner.Side.RIGHT;
                        }
                    }
                }
            }
        }
    }

    /*
    Function to determine whether the bridge partners of p overlap.
    ie. are the same residues hydrogen bonding to q and r?
     */
    public boolean bridgeOverlap(SSE p, SSE q, SSE r) {

        // Find the index to q //
        int i = p.FindBPIndex(q);

        // Find the index to r //
        int j = p.FindBPIndex(r);

        // Compare ranges //
        int a = p.getBridgeRange(i).start;
        int b = p.getBridgeRange(i).end;
        int x = p.getBridgeRange(j).start;
        int y = p.getBridgeRange(j).end;

        // Not a---b x---y or x---y a---b //
        return !(b < x || y < a);  
    }

    /*
     * A function to decide geometrically whether strand p lies on the same or
     * opposite side of strand q as strand r
     */
    public boolean geometricSameSide(SSE q, SSE r, SSE p) {

        Vector3d v1 = q.axis.getVector();
        Point3d midr = r.axis.getCentroid();
        Point3d midp = p.axis.getCentroid();

        Vector3d v2 = new Vector3d(midr);
        v2.sub(q.axis.AxisStartPoint);

        Vector3d v3 = new Vector3d(midp);
        v3.sub(q.axis.AxisStartPoint);

        double normal2 = cross(v2, v1).length();
        double normal1 = cross(v1, v3).length();
        double costheta = -(normal1 * normal2);

        return costheta > 0.0;
    }

    private Vector3d cross(Vector3d a, Vector3d b) {    // I really hate vecmath api
        Vector3d tmp = new Vector3d();
        tmp.cross(a, b);
        return tmp;
    }

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub

    }

}
