package tops.view.diagram;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

/**
 * A dreaded utility function class.
 * 
 * @author gilleain
 *
 */
public class PointUtils {

    public static List<Point2d> makePoints(Point2d center, double startAngle, double r, int n) {
        List<Point2d> points = new ArrayList<>();
        double dAng = 360.0 / n;
        double ang = startAngle;
        System.out.println("dAng " + dAng);
        for (int index = 0; index < n; index++) {
            double x = center.x + (r * cos(toRadians(ang)));
            double y = center.y + (r * sin(toRadians(ang)));
            points.add(new Point2d(x, y));
            System.out.println("ang " + ang);
            ang += dAng;
        }
        return points;
    }
    
    public static String toString(Point2d p) {
        return String.format("(%2.2f, %2.2f)", p.x, p.y);
    }
}
