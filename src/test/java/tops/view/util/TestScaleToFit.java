package tops.view.util;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

import org.junit.Ignore;
import org.junit.Test;

public class TestScaleToFit {
    
    @Test
    public void testScaleWidthFromOrigin() {
        Rectangle2D canvas = originAnchoredRectangle(300, 200);
        Rectangle2D model = originAnchoredRectangle(200, 100);
        ScaleToFit scale = new ScaleToFit(canvas, model);
        assertEquals("Width 1.5x scale", 1.5, scale.getScale(), 0.001);
    }
    
    @Test
    public void testScaleHeightFromOrigin() {
        Rectangle2D canvas = originAnchoredRectangle(300, 200);
        Rectangle2D model = originAnchoredRectangle(100, 100);
        ScaleToFit scale = new ScaleToFit(canvas, model);
        assertEquals("Height 2x scale", 2.0, scale.getScale(), 0.001);
    }
    
    @Test
    public void testScaleWidthFromCentered() {
        Point2d c = new Point2d(100, 100);
        Rectangle2D canvas = pointCenteredRectangle(c, 300, 200);
        Rectangle2D model = pointCenteredRectangle(c, 100, 100);
        ScaleToFit scale = new ScaleToFit(canvas, model);
        assertEquals("Height 2x scale", 2.0, scale.getScale(), 0.001);
    }
    
    @Test
    @Ignore	// for now ...
    public void testTransform() {
        Point2d p1 = new Point2d(10, 20);
        Point2d p2 = new Point2d(35, 52);
        
        Rectangle2D canvas = originAnchoredRectangle(200, 200);
        Rectangle2D model = originAnchoredRectangle(100, 100);
        
        ScaleToFit scale = new ScaleToFit(canvas, model);
        
        Point2d p1t = scale.transform(p1);
        assertPoint(p1t, 20, 40);
        
        Point2d p2t = scale.transform(p2);
        assertPoint(p2t, 70, 104);
    }
    
    @Test
    @Ignore	// for now ...
    public void testConcentricTransform() {
        Point2d center = new Point2d(75, 75);
        Rectangle2D canvas = pointCenteredRectangle(center, 100, 100);
        Rectangle2D model =  pointCenteredRectangle(center, 50, 50);
        
        ScaleToFit scale = new ScaleToFit(canvas, model);
        
        Point2d p1 = new Point2d(75, 50);
        Point2d p1t = scale.transform(p1);
        assertPoint(p1t, 75, 25);
        
        Point2d p2 = new Point2d(50, 100);
        Point2d p2t = scale.transform(p2);
        assertPoint(p2t, 25, 125);
        
        Point2d p3 = new Point2d(100, 100);
        Point2d p3t = scale.transform(p3);
        assertPoint(p3t, 125, 125);
    }
    
    private void assertPoint(Point2d p, double x, double y) {
        assertEquals("Xcoord", x, p.x, 0.001);
        assertEquals("Ycoord", y, p.y, 0.001);
    }
    
    private Rectangle2D originAnchoredRectangle(int width, int height) {
        return new Rectangle2D.Double(0, 0, width, height);
    }
    
    private Rectangle2D pointCenteredRectangle(Point2d center, int width, int height) {
        double w2 = width / 2.0;
        double h2 = height / 2.0;
        return new Rectangle2D.Double(center.x - w2, center.y - h2, width, height);
    }
}
