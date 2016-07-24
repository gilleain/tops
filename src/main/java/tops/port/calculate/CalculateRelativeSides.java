package tops.port.calculate;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.SSE;

/**
 * At this point we assign relative position of bridge partner strands  
 * Although this is already set from the dssp file it is reset here since the latter is unreliable
 * 
 */
public class CalculateRelativeSides implements Calculation {


    @Override
    public void calculate(Chain chain) {
        System.out.println("STEP : Assigning relative sides to bridge partner strands");
        for (SSE p : chain.getSSEs()) {
            assignRelativeSides(p);
        }
    }

  
    public void assignRelativeSides(SSE sse) {
        if (sse.isStrand()) {
            for (BridgePartner bp : sse.getBridgePartners()) {
                bp.setUnknown();
            }
            BridgePartner bridgePartner1 = sse.longestBridgeRange();
            if (bridgePartner1 != null) {
                bridgePartner1.setLeft();
                // Assign side for those 'BridgeOverlap'ing with the RefBP //
                for (int j = 0; j < sse.getBridgePartners().size(); j++) {
                    BridgePartner bridgePartner2 = sse.getBridgePartners().get(j);
                    if (bridgePartner2 != null && !bridgePartner2.equals(bridgePartner1)) {
                        if (bridgeOverlap(bridgePartner1, bridgePartner2)) {
                            bridgePartner2.setRight();
                        }
                    }
                }
                // Sort out any other sides which can be calculated by 
                // BridgeOverlaps with other than the RefBP
                for (int j = 0; j < sse.getBridgePartners().size(); j++) {
                    BridgePartner bridgePartner2 = sse.getBridgePartner(j);
                    if (bridgePartner2 != null && bridgePartner2.isUnknownSide()) {
                        for (int k = 0; k < sse.getBridgePartners().size(); k++) {
                            BridgePartner bridgePartner3 = sse.getBridgePartners().get(k);
                            if (k != j && bridgePartner3 != null && bridgePartner3.isUnknownSide()) {
                                if (bridgeOverlap(bridgePartner2, bridgePartner3)) {
                                    if (bridgePartner3.isLeft()) {
                                        bridgePartner2.setRight();
                                    }
                                } else {
                                    bridgePartner2.setLeft();
                                }
                            }
                        }
                    }
                }

                // The rest have to be done geometrically //
                for (int j = 0; j < sse.getBridgePartners().size(); j++) {
                    BridgePartner bridgePartner2 = sse.getBridgePartners().get(j);
                    if (bridgePartner2 != null && bridgePartner2.isUnknownSide()) {
                        if (geometricSameSide(sse, bridgePartner1, bridgePartner2)) {
                            bridgePartner2.setLeft();
                        } else {
                            bridgePartner2.setRight();
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
    public boolean bridgeOverlap(BridgePartner bpQ, BridgePartner bpR) {
        int a = bpQ.rangeMin;
        int b = bpQ.rangeMax;
        int x = bpR.rangeMin;
        int y = bpR.rangeMax;

        // Not a---b x---y or x---y a---b //
        return !(b < x || y < a);  
    }

    /*
     * A function to decide geometrically whether strand p lies on the same or
     * opposite side of strand q as strand r
     */
    public boolean geometricSameSide(SSE q, BridgePartner bpR, BridgePartner bpP) {
        SSE r = bpR.partner;
        SSE p = bpP.partner;
        
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
