package tops.port.calculate;

import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateNeighbours implements Calculation {
    
    private double CutoffDistance = 20;
    
    /**
     * Function to assign spatial neighbours
     **/
     public void calculate(Chain chain) {
         System.out.println("STEP : Calculating secondary structure neighbour lists");

         for (SSE p : chain.getSSEs()) {
             if (!p.isStrand() && !p.isHelix()) continue;
             for (SSE q : chain.rangeFrom(p.To)) {
                 if (!q.isStrand() && !q.isHelix()) continue;
                 if (p.hasBridgePartner(q)) continue;
                 double shdis = chain.simpleSSESeparation(p, q);
                 if (shdis > CutoffDistance) continue;

                 // Update first secondary structure //
                 p.addNeighbour(q, (int)shdis);

                 // Update second secondary structure //
                 q.addNeighbour(p, (int)shdis);
             }
         }
     }

    @Override
    public void setParameter(String key, double value) {
        if (key.equals("cutoffDistance")) {
            CutoffDistance = value;
        }
    }

}
