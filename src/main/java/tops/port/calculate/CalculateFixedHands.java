package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

import tops.port.model.Axis;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class CalculateFixedHands implements Calculation {
    
    private List<FixedType> allowedTypes = new ArrayList<FixedType>() {{
        add(FixedType.FT_BARREL); 
        add(FixedType.FT_SANDWICH);
        add(FixedType.FT_SHEET);    // XXX not in original code, not sure why?
        add(FixedType.FT_CURVED_SHEET); 
        add(FixedType.FT_V_CURVED_SHEET); 
    }};

    
    public void calculate(Chain chain) {
        System.out.println("STEP : calculating fixed hands");
        for (SSE sse : chain.getSSEs()) {
            if (sse.hasFixed()) this.setFixedHand(chain, sse);
        }
    }
    
    /**
     * This function checks that a given fixed structure is drawn with correct
     * handedness. It only works where the handedness is easy to calculate (ie.
     * we can use TS's routine) that being where we can find an appropriate
     * Beta-x-Beta unit. Note that TIM barrels do not lie in this category but
     * are already drawn with correct chirality.
     **/
    public void setFixedHand(Chain chain, SSE p) {
        if (allowedTypes.contains(p.getFixedType())) {
            System.out.println(String.format("Checking fixed structure chirality for fixed start %d", p.getSymbolNumber()));
            SSE q = find(chain, p);
            if (q != null) {
                System.out.println("Found suitable motif for fixed chirality check");
                SSE r = null;   // XXX FIXME XXX
                Hand chir = chiral2d(chain, q, r);
                if (chir != Hand.UNKNOWN) {
                    System.out.println(String.format("Changing chirality of fixed structure starting at %d", p.getSymbolNumber()));
                    chain.reflectFixedXY(p);
                }
            } else {
                System.out.println("No suitable motif found for fixed chirality check");
            }

        }
    }
    
    private SSE find(Chain chain, SSE p) {
        // TODO : FIXME - see SetFixedHand!! XXX
        for (SSE q : chain.getSSEs()) {
            if (chain.findFixedStart(q) == p) {
                boolean found = false;
                int n = 0;
                for (SSE r : chain.rangeFrom(q.To)) {
                    if (chain.findFixedStart(r) != p) break;
                    n += 1;
                    if (r.getDirection() == q.getDirection()) {
                        Hand chir = chiral2d(chain, q, r);
                        System.out.println("chir " + chir);
                        if (n > 1 && chir != Hand.UNKNOWN) {
                            found = true;
                        } else {
                            found = false;
                        }
                        break;
                    }
                    if (found) return q;
                }
            }   
        }
        return null;
    }
    
    /**
     * This could be a Cartoon method if it was recast as finding the sign of
     * the determinant of the matrix of course, this would mean not re-using the
     * Torsion method so cunningly, but hey.
     */
    public Hand chiral2d(Chain chain, SSE p, SSE q) {
        Hand hand = Hand.UNKNOWN;
        Hand lasthand = Hand.UNKNOWN;

        Vector3d a, b, c, d;
        if (p.getDirection() == 'U') a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 1.0);
        else a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        b = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        c = new Vector3d(q.getCartoonX(), q.getCartoonY(), 0.0);

        int i = 0;
        for (SSE r : chain.range(p.To, q)) {
            d = new Vector3d(r.getCartoonX(), r.getCartoonY(), 0.0);
            lasthand = hand;
            double theta = this.AngleBetweenLines(b, c, d);
            if (theta < 0.5 || theta > 179.5) {
                hand = Hand.UNKNOWN;
            } else {
                double torsion = Axis.Torsion(a, b, c, d);
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

    // might not be as accurate as the original
    public double AngleBetweenLines(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d ba = diff(b, a);
        Vector3d bc = diff(b, c);
        if (ba.length() == 0.0 || bc.length() == 0.0) return 0.0;
        return Math.toDegrees(ba.angle(bc));
    }

    private Vector3d diff(Vector3d a, Vector3d b) {
        Vector3d ab = a;
        ab.sub(b);
        return ab;
    }

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
        
    }

}
