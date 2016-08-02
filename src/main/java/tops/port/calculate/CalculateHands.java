package tops.port.calculate;

import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateHands implements Calculation {

    public void calculate(Chain chain) {
        System.out.println("STEP : Calculating chiralities");
        for (SSE p : chain.getSSEs()) {
            p.Chirality = ChiralityCalculator.hand3D(chain, p);
        }
    }
    

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
    }

}
