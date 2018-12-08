package tops.port.calculate.chirality;

import javax.vecmath.Point3d;

import tops.port.calculate.chirality.StereoTool.TetrahedralSign;
import tops.port.model.Chain;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class SimpleChirality implements ChiralityInterface {

    @Override
    public Hand chiral3d(Chain chain, SSE p, SSE q) {
        // make a plane from the four points : start/end of p and q
        Point3d pointA = new Point3d(p.axis.getAxisStartPoint());
        Point3d pointB = new Point3d(p.axis.getAxisFinishPoint());
        Point3d pointC = new Point3d(q.axis.getAxisStartPoint());
                
        // get the point to test
        Point3d pointD = getTestPoint(chain, p, q);
        
        // find signed point-plane distance and compare the sign
        
        TetrahedralSign sign = StereoTool.getHandedness(pointA, pointB, pointC, pointD);
        return sign == TetrahedralSign.PLUS? Hand.LEFT : Hand.RIGHT;
    }
    
    private Point3d getTestPoint(Chain chain, SSE p, SSE q) {
        Point3d point = new Point3d();
        int count = 0;
        for (SSE sse : chain.rangeFrom(p)) {
            if (sse == q) {
                break;
            } else if (sse == p) {
                continue;
            } else {
                point.add(sse.axis.getCentroid());
                count++;
            }
        }
        point.scale(count);
        return point;
    }

}
