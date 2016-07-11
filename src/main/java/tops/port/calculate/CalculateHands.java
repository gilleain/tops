package tops.port.calculate;

import tops.port.model.Chain;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class CalculateHands implements Calculation {

    public void calculate(Chain chain) {
        System.out.println("Calculating chiralities");
        for (SSE p : chain.getSSEs()) {
            p.Chirality = this.hand3D(chain, p);
        }
    }
    
    /*
     * Hand calculator Updated to call T. Slidel's chirality calculation by D.
     * Westhead 20/5/97 If handedness is uncertain, or an error occurrs, right
     * handed is assumed an sse method
     */
    public Hand hand3D(Chain chain, SSE p) {

        SSE q = this.topsChiralPartner(chain, p);
        if (q != null) {
            Hand chir = p.Chiral3d(q);
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
    
    public SSE topsChiralPartner(Chain chain, SSE p) {
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

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
        
    }

}
