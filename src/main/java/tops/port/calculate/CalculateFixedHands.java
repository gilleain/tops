package tops.port.calculate;

import static tops.port.model.Direction.UP;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Vector3d;

import tops.port.model.Axis;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class CalculateFixedHands implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateFixedHands.class.getName());
    
    private List<FixedType> allowedTypes = new ArrayList<FixedType>() {{
        add(FixedType.BARREL); 
        add(FixedType.SANDWICH);
        add(FixedType.SHEET);    // XXX not in original code, not sure why?
        add(FixedType.CURVED_SHEET); 
        add(FixedType.V_CURVED_SHEET); 
    }};

    
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : calculating fixed hands");
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
            log.log(Level.INFO, "Checking fixed structure chirality for fixed start {0}", p.getSymbolNumber());
            SSE q = find(chain, p);
            if (q != null) {
                log.info("Found suitable motif for fixed chirality check");
                SSE r = null;   // XXX FIXME XXX
                Hand chir = chiral2d(chain, q, r);
                if (chir != Hand.UNKNOWN) {
                    log.log(Level.INFO, "Changing chirality of fixed structure starting at {0}", p.getSymbolNumber());
                    chain.reflectFixedXY(p);
                }
            } else {
                log.info("No suitable motif found for fixed chirality check");
            }

        }
    }
    
    private SSE find(Chain chain, SSE sse) {
        // TODO : FIXME - see SetFixedHand!! XXX
        List<SSE> sses = chain.getSSEs();
        for (int index = 0; index < sses.size(); index++) {
            SSE sseA = sses.get(index);
            if (chain.findFixedStart(sseA) == sse) {
                boolean found = false;
                int n = 0;
                for (int secondIndex = index + 1; secondIndex < sses.size(); secondIndex++) {
                    SSE sseB = sses.get(secondIndex);
                    if (chain.findFixedStart(sseB) != sse) break;
                    n += 1;
                    if (sseB.getDirection() == sseA.getDirection()) {
                        Hand chir = chiral2d(chain, sseA, sseB);
                        if (n > 1 && chir != Hand.UNKNOWN) {
                            found = true;
                        } else {
                            found = false;
                        }
                    }
                    if (found) {
                        return sseA;
                    }
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
            double theta = this.angleBetweenLines(b, c, d);
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
            i++;
        }
        return hand;
    }

    // might not be as accurate as the original
    public double angleBetweenLines(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d ba = diff(b, a);
        Vector3d bc = diff(b, c);
        if (ba.length() == 0.0 || bc.length() == 0.0) return 0.0;
        return Math.toDegrees(ba.angle(bc));
    }

    private Vector3d diff(Vector3d va, Vector3d vb) {
        Vector3d ab = va;
        ab.sub(vb);
        return ab;
    }

    @Override
    public void setParameter(String key, double value) {
        // no-op
        
    }

}
