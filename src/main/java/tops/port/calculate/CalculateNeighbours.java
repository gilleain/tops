package tops.port.calculate;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Vector3d;

import tops.port.calculate.util.DistanceCalculator;
import tops.port.model.Chain;
import tops.port.model.SSE;

public class CalculateNeighbours implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateNeighbours.class.getName());
    
    private double CutoffDistance = 20;
    
    /**
     * Function to assign spatial neighbours
     **/
     public void calculate(Chain chain) {
         log.log(Level.INFO, "STEP : Calculating secondary structure neighbour lists");

         for (SSE p : chain.getSSEs()) {
             if (!p.isStrand() && !p.isHelix()) continue;
             for (SSE q : chain.rangeFrom(p.To)) {
                 if (!q.isStrand() && !q.isHelix()) continue;
                 if (p.hasBridgePartner(q)) continue;
                 double shdis = simpleSSESeparation(p, q);
                 if (shdis > CutoffDistance) continue;

                 // Update first secondary structure //
                 p.addNeighbour(q, (int)shdis);

                 // Update second secondary structure //
                 q.addNeighbour(p, (int)shdis);
             }
         }
     }
     
     public double simpleSSESeparation(SSE p, SSE q) {
         Vector3d pk = plus(p.axis.AxisStartPoint, p.axis.AxisFinishPoint);
         Vector3d pj = plus(q.axis.AxisStartPoint, q.axis.AxisFinishPoint);
         pk.scale(1/2.0);
         pj.scale(1/2.0);
         return DistanceCalculator.distance3D(pk, pj);
     }
     
     private Vector3d plus(Vector3d a, Vector3d b) {
         Vector3d c = new Vector3d(a);
         c.add(b);
         return c; 
     }

    @Override
    public void setParameter(String key, double value) {
        if (key.equals("cutoffDistance")) {
            CutoffDistance = value;
        }
    }

}
