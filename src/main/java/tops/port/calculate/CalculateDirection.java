package tops.port.calculate;

import static tops.port.model.Direction.UP;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Chain;
import tops.port.model.Direction;
import tops.port.model.SSE;
import tops.port.model.tse.BaseTSE;

public class CalculateDirection implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateDirection.class.getName());
    
    /**
     * Directions of cartoon symbols are binary, whereas SSEs have full blown
     * vectors.
     **/
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Assigning directions to secondary structures");
        
        for (BaseTSE tse : chain.getTSEs()) {
            assignDirections(tse);
        }
        
     }
    

    private void assignDirections(BaseTSE tse) {
        // set the first SSE to be up
        SSE prev = null;
        for (SSE sse : tse.getElements()) {
            if (prev == null) {
                sse.setDirection(UP);
            } else {
                Direction prevDirection = prev.getDirection();
                if (sse.isParallel(prev)) {
                    sse.setDirection(prevDirection);
                } else {
                    sse.setDirection(prevDirection.opposite());
                }
            }
            prev = sse;
        }
    }


    @Override
    public void setParameter(String key, double value) {
        // no-op
    }

}
