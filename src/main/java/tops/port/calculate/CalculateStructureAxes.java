package tops.port.calculate;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Axis;
import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateStructureAxes implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateStructureAxes.class.getName());
 
    public void calculate(Chain chain) {
        // Calculate axes //
        log.log(Level.INFO, "STEP : Calculating secondary structure vectors");
        for (SSE sse : chain.getSSEs()) {
            if (!sse.isStrand() && !sse.isHelix()) continue;
            secondaryStructureAxis(chain, sse);
        }
      
    }
    
    public void secondaryStructureAxis(Chain chain, SSE sse) {
        Axis axis = new Axis(
                chain.secondaryStructureAxis(
                        sse.sseData.seqStartResidue, sse.sseData.seqFinishResidue));
        sse.setAxis(axis);
    }

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
    }

}
