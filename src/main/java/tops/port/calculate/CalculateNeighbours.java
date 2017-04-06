package tops.port.calculate;

import java.util.List;
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

         List<SSE> sses = chain.getSSEs();
         for (int index = 0; index < sses.size() - 1; index++) {
             SSE p = sses.get(index);
             if (neitherStrandNorHelix(p)) continue;
             for (int secondIndex = index + 1; secondIndex < sses.size(); secondIndex++) {
                 SSE q = sses.get(secondIndex);
                 if (neitherStrandNorHelix(q)) continue;
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
     
     private boolean neitherStrandNorHelix(SSE sse) {
         return !sse.isStrand() && !sse.isHelix();
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
