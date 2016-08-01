package tops.port;

import static org.junit.Assert.assertEquals;

import javax.vecmath.Point2d;

import org.junit.Test;

import tops.port.IntersectionCalculator.Intersection;
import tops.port.IntersectionCalculator.IntersectionType;

public class TestIntersectionCalculator {
    
    @Test
    public void testCrossing() {
        IntersectionCalculator calc = new IntersectionCalculator();
        Point2d p = new Point2d(10, 10);
        Point2d q = new Point2d(20, 20);
        Point2d r = new Point2d(20, 10);
        Point2d s = new Point2d(10, 20);
        
        Intersection result = calc.lineCross(p, q, r, s);
        assertEquals(IntersectionType.CROSSING, result.type);
    }

}
