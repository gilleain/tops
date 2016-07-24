package tops.port.calculate.util;

import javax.vecmath.Vector3d;

import tops.port.model.Axis;
import tops.port.model.Chain;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class ChiralityCalculator {

    /**
     * Calculates chiralities for 2D TOPS cartoon
     */
    public static Hand hand2D(Chain chain, SSE p) {
        if (p.Chirality != Hand._no_hand) {
            SSE q = topsChiralPartner(chain, p);
            if (q != null) return chiral2d(chain, p, q);
        }
        return Hand._no_hand;
    }

    /**
     * This could be a Cartoon method if it was recast as finding the sign of the determinant of the matrix
     *   of course, this would mean not re-using the Torsion method so cunningly, but hey.
     */
    public static Hand chiral2d(Chain chain, SSE p, SSE q) {
        Hand hand = Hand._unk_hand;
        Hand lasthand = Hand._unk_hand;

        Vector3d a, b, c, d;
        if (p.getDirection() == 'U') a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 1.0);
        else a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        b = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        c = new Vector3d(q.getCartoonX(), q.getCartoonY(), 0.0);

        int i = 0;
        for (SSE r : chain.range(p.To, q)) {
            d = new Vector3d(r.getCartoonX(), r.getCartoonY(), 0.0);
            lasthand = hand;
            double theta = angleBetweenLines(b, c, d);
            if (theta < 0.5 || theta > 179.5) {
                hand = Hand._unk_hand;
            } else {
                double torsion = Axis.Torsion(a, b, c, d);
                if (torsion < 0.0) {
                    hand = Hand._Left;
                } else {
                    hand = Hand._Right;
                }
            }
            if (i > 0 && hand != lasthand) { 
                return Hand._unk_hand;
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
    
    private static Vector3d diff(Vector3d a, Vector3d b) {
        Vector3d ab = a;
        ab.sub(b);
        return ab;
    }

    
    /*
     * Hand calculator Updated to call T. Slidel's chirality calculation by D.
     * Westhead 20/5/97 If handedness is uncertain, or an error occurrs, right
     * handed is assumed an sse method
     */
    public static Hand hand3D(Chain chain, SSE p) {

        SSE q = topsChiralPartner(chain, p);
        if (q != null) {
            Hand chir = chiral3d(p, q);
            if (chir == Hand._unk_hand) {
                if (p.isStrand()) chir = Hand._Right;
                else if (p.isHelix()) chir = Hand._no_hand;
                else chir = Hand._no_hand;
            }
            return chir;
        } else {
            return Hand._no_hand;
        }
    }
    

    public static Hand chiral3d(SSE sse, SSE other) {

        int a1s, a1f, a2s, a2f;
        // XXX TODO - what is this merge range stuff?
//        if (sse.getM > 0) {
//            a1s = this.MergeRanges[this.Merges - 1][0];
//            a1f = this.MergeRanges[this.Merges - 1][1];
//        } else {
            a1s = sse.sseData.SeqStartResidue;
            a1f = sse.sseData.SeqFinishResidue;
//        }
//
//        if (other.Merges > 0) {
//            a2s = other.MergeRanges[0][0];
//            a2f = other.MergeRanges[0][1];
//        } else {
            a2s = other.sseData.SeqStartResidue;
            a2f = other.sseData.SeqFinishResidue;
//        }

        return motifChirality(a1s, a1f, a2s, a2f);
    }

    //FIXME!! : link in the slidel code!
    public static Hand motifChirality(int a, int b, int c, int d) { 
        return Hand._unk_hand;
    }
    
    public static SSE topsChiralPartner(Chain chain, SSE p) {
        /*
        this piece of code finds sequences of ss elements to which a chirality should be attached
        for TOPS this is two parallel strands in the same fixed structure with a connection of at least one and no 
        more than five ss elements, none of which should be in the same sheet,
        OR two parallel helices each of more than 12 residues connected by at least one and no more than 2 other helices.
        */
        int startIndex = chain.getSSEs().indexOf(p) + 1;

        if (p.isStrand()) {
            int endIndex = startIndex + 5;
            for (int i = startIndex; i < endIndex; i++) {
                SSE q;
                if (i >= chain.getSSEs().size()) return null;
                else q = chain.getSSEs().get(i);
                if ((q.isStrand()) && (chain.findFixedStart(q) == chain.findFixedStart(p))) {
                    if (q.getDirection() == p.getDirection()) return q;
                    else return null;
                }
            }
        } else if ((p.isHelix()) && (p.SecStrucLength() > 12)) {
            int endIndex = startIndex + 2;
            for (int i = startIndex; i < endIndex; i++) {
                SSE q;
                if (i >= chain.getSSEs().size()) return null;
                else q = chain.getSSEs().get(i);
                if (!q.isHelix()) return null;
                if ((q.getDirection() == p.getDirection()) && (q.SecStrucLength() > 12)) return q;
            }
        }
        return null;    // XXX added to satisfy compiler
    }
}
