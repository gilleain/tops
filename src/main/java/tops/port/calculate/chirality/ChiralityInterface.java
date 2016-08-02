package tops.port.calculate.chirality;

import tops.port.model.Hand;
import tops.port.model.SSE;

/**
 * Ugh, can't think of a better name. Interface to allow for various calculations.
 * 
 * @author maclean
 *
 */
public interface ChiralityInterface {

    public Hand chiral3d(SSE p, SSE q);

}
