package tops.port.calculate.chirality;

import static tops.port.model.Direction.UP;

import javax.vecmath.Vector3d;

import tops.port.model.Axis;
import tops.port.model.Chain;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class ChiralityCalculator {
    
    private ChiralityCalculator() {
        // prevent instantiation
    }

    /**
     * Calculates chiralities for 2D TOPS cartoon
     */
    public static Hand hand2D(Chain chain, SSE p) {
        if (p.getChirality() != Hand.NONE) {
            SSE q = topsChiralPartner(chain, p);
            if (q != null) return chiral2d(chain, p, q);
        }
        return Hand.NONE;
    }

    /**
     * This could be a Cartoon method if it was recast as finding the sign of the determinant of the matrix
     *   of course, this would mean not re-using the Torsion method so cunningly, but hey.
     */
    public static Hand chiral2d(Chain chain, SSE p, SSE q) {
        Hand hand = Hand.UNKNOWN;

        Vector3d a;
        Vector3d b;
        Vector3d c;
        Vector3d d;
        if (p.getDirection() == UP) {
            a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 1.0);
        } else {
            a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);
        }

        b = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        c = new Vector3d(q.getCartoonX(), q.getCartoonY(), 0.0);

        int i = 0;
        for (SSE r : chain.range(chain.getNext(p), q)) {
            d = new Vector3d(r.getCartoonX(), r.getCartoonY(), 0.0);
            Hand lasthand = hand;
            double theta = angleBetweenLines(b, c, d);
            if (theta < 0.5 || theta > 179.5) {
                hand = Hand.UNKNOWN;
            } else {
                double torsion = Axis.torsion(a, b, c, d);
                if (torsion < 0.0) {
                    hand = Hand.LEFT;
                } else {
                    hand = Hand.RIGHT;
                }
            }
            if (i > 0 && hand != lasthand) { 
                return Hand.UNKNOWN;
            }
        }
        return hand;
    }
    

    //might not be as accurate as the original
    public static double angleBetweenLines(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d ba = diff(b, a);
        Vector3d bc = diff(b, c);
        if (ba.length() == 0.0 || bc.length() == 0.0) return 0.0;
        return Math.toDegrees(ba.angle(bc));
    }
    
    private static Vector3d diff(Vector3d aVec, Vector3d bVec) {
        Vector3d ab = aVec;
        ab.sub(bVec);
        return ab;
    }

    
    /*
     * Hand calculator Updated to call T. Slidel's chirality calculation by D.
     * Westhead 20/5/97 If handedness is uncertain, or an error occurrs, right
     * handed is assumed an sse method
     */
    public static Hand hand3D(Chain chain, SSE p) {
        // TODO : inject
        ChiralityInterface chiral = new SimpleChirality();
        
        SSE q = topsChiralPartner(chain, p);
        if (q != null) {
            Hand chir = chiral.chiral3d(chain, p, q);
            if (chir == Hand.UNKNOWN) {
                if (p.isStrand()) chir = Hand.RIGHT;
                else if (p.isHelix()) chir = Hand.NONE;
                else chir = Hand.NONE;
            }
            return chir;
        } else {
            return Hand.NONE;
        }
    }
    
    
    public static SSE topsChiralPartner(Chain chain, SSE sse) {
        /*
        this piece of code finds sequences of ss elements to which a chirality should be attached
        for TOPS this is two parallel strands in the same fixed structure with a connection of at least one and no 
        more than five ss elements, none of which should be in the same sheet,
        OR two parallel helices each of more than 12 residues connected by at least one and no more than 2 other helices.
        */
        int startIndex = chain.getSSEs().indexOf(sse) + 1;

        if (sse.isStrand()) {
            return findStrandPartner(chain, startIndex, sse);
        } else if ((sse.isHelix()) && (sse.secStrucLength() > 12)) {
            return findHelixPartner(chain, startIndex, sse);
        }
        return null; // none found
    }
    
    public static SSE findStrandPartner(Chain chain, int startIndex, SSE sse) {
        int endIndex = startIndex + 5;
        for (int i = startIndex; i < endIndex; i++) {
            SSE partner;
            if (i >= chain.getSSEs().size()) return null;
            else partner = chain.getSSEs().get(i);
            if ((partner.isStrand()) && (chain.findFixedStart(partner) == chain.findFixedStart(sse))) {
                if (partner.getDirection() == sse.getDirection()) return partner;
                else return null;
            }
        }
        return null;
    }
    
    public static SSE findHelixPartner(Chain chain, int startIndex, SSE sse) {
        int endIndex = startIndex + 2;
        for (int i = startIndex; i < endIndex; i++) {
            SSE partner;
            if (i >= chain.getSSEs().size()) return null;
            else partner = chain.getSSEs().get(i);
            if (!partner.isHelix()) return null;
            if ((partner.getDirection() == sse.getDirection()) && (partner.secStrucLength() > 12)) return partner;
        }
        return null;
    }
}
