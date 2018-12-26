package tops.port.calculate;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UP;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateDirection implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateDirection.class.getName());
    
    /**
     * Directions of cartoon symbols are binary, whereas SSEs have full blown
     * vectors.
     **/
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Assigning directions to secondary structures");
        SSE root = chain.getSSEs().get(0);
        SSE q = root;
        for (SSE p : chain.iterNext(root)) {
            if (p != root) {
                if (q.isParallel(p)) {
                    if (p.getDirection() != q.getDirection()) chain.flipSymbols(p);
                } else {
                    if (p.getDirection() == q.getDirection()) chain.flipSymbols(p);
                }
                q = p;
            }
        }

        /*
          This loop makes some local direction changes to the directions of ss elements which
          have no fixed list attached. Their direction is determined from the previous element in
          the sequence rather than the previous element in the Next list which is what is used
          above.
          This does a better job for beta-alpha-beta units.
          DW 5/9/96
        */
        SSE prev = root;
        SSE nextToRoot = chain.getNext(root);
        for (SSE p : chain.iterNext(root)) {
            if (p != root && p != nextToRoot && p.hasFixed()) {
                q = prev;
                if (q.isParallel(p)) {
                    if (q.getDirection() == UP) {
                        p.setDirection(UP);
                    } else {
                        p.setDirection(DOWN);
                    }
                } else {
                    if (q.getDirection() == UP) { 
                        p.setDirection(DOWN);
                    } else { 
                        p.setDirection(UP);
                    }
                }
            }
            prev = p;
        }
     }

    @Override
    public void setParameter(String key, double value) {
        // no-op
    }

}
