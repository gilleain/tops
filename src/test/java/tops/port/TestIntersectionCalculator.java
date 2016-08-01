package tops.port;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tops.port.IntersectionCalculator.Intersection;
import tops.port.IntersectionCalculator.IntersectionType;

public class TestIntersectionCalculator {
    
    @Test
    public void testCrossing() {
        IntersectionCalculator calc = new IntersectionCalculator();
        double px = 10;
        double py = 10;
        double qx = 20;
        double qy = 20;
        double rx = 20;
        double ry = 10;
        double sx = 10;
        double sy = 20;
        
        Intersection result = calc.lineCross(px, py, qx, qy, rx, ry, sx, sy);
        assertEquals(IntersectionType.CROSSING, result.type);
    }

}
