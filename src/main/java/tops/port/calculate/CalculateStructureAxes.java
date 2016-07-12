package tops.port.calculate;

import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateStructureAxes implements Calculation {
 
    public void calculate(Chain chain) {
        // Calculate axes //
        System.out.println("Calculating secondary structure vectors");
        for (SSE sse : chain.getSSEs()) {
            if (!sse.isStrand() && !sse.isHelix()) continue;
            secondaryStructureAxis(chain, sse);
        }

      
    }
    
    public void secondaryStructureAxis(Chain chain, SSE sse) {
        sse.setAxis(
                chain.secondaryStructureAxis(
                        sse.sseData.SeqStartResidue, sse.sseData.SeqFinishResidue));
    }

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
    }

}
