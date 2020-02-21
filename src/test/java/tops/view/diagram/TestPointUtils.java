package tops.view.diagram;

import java.util.List;

import javax.vecmath.Point2d;

import org.junit.Test;

public class TestPointUtils {
    
    @Test
    public void testTriangle() {
        testPoly(3);
    }
    
    @Test
    public void testSquare() {
        testPoly(4);
    }
    
    @Test
    public void testPentagon() {
        testPoly(5);
    }
    
    private void testPoly(int n) {
        Point2d c = new Point2d(10, 10);
        List<Point2d> points = PointUtils.makePoints(c, 0, 10, n);
        int i = 0;
        for (Point2d p : points) {
            System.out.println(i + " " + PointUtils.toString(p));
            i++;
        }
    }

}
