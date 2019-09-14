package tops.port.calculate;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Chain;

public class CalculateDirection implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateDirection.class.getName());
    
    /**
     * Directions of cartoon symbols are binary, whereas SSEs have full blown
     * vectors.
     **/
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Assigning directions to secondary structures");
   
     }
    

    @Override
    public void setParameter(String key, double value) {
        // no-op
    }

}
