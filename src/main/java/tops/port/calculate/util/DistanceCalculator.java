package tops.port.calculate.util;

import javax.vecmath.Vector3d;

import tops.port.model.SSE;
import tops.port.model.TorsionResult;

public class DistanceCalculator {
    
    public static double secStrucSeparation(SSE p, SSE q) {
//        pk, pj, sk, sj, torsion
        TorsionResult result = p.closestApproach(q);
        double sk = result.getSk();
        double sj = result.getSj();
        double torsion = result.getTorsion();

        //check error
        if (torsion < -990.0 ) return 0.0;

        Vector3d pk = null;
        if (sk < 0.0) pk = p.axis.getAxisStartPoint();
        else if (sk > 1.0) pk = p.axis.getAxisFinishPoint();

        Vector3d pj = null;
        if (sj < 0.0) pj = q.axis.getAxisStartPoint();
        else if (sj > 1.0) pj = q.axis.getAxisFinishPoint();

        return distance3D(pk, pj);
    }
    
    public static double distance3D(Vector3d a, Vector3d b) {
        return diff(b, a).length();
    }
    
    private static Vector3d diff(Vector3d a, Vector3d b) {
        Vector3d ab = a;
        ab.sub(b);
        return ab;
    }


}
