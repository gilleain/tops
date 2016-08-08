package tops.port.calculate;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateHands implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateHands.class.getName());

    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Calculating chiralities");
        for (SSE p : chain.getSSEs()) {
            p.Chirality = ChiralityCalculator.hand3D(chain, p);
        }
    }
    

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
    }

}
