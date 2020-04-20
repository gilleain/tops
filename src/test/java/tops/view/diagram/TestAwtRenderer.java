package tops.view.diagram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.junit.Test;

import tops.view.model.Arc;
import tops.view.model.Shape;
import tops.view.util.ImageFactory;

public class TestAwtRenderer {
    
    @Test
    public void testUpArc() throws IOException {
        double arcHeight = 10;
        Vector2d orientation = new Vector2d(0, -1);
        Point2d start = new Point2d(10, 10);
        Point2d end = new Point2d(30, 10);
        Shape shape = new Arc(orientation, start, end, arcHeight, true, Color.BLACK);
        
        int width = 100;
        int height = 100;
        ImageFactory imageFactory = new ImageFactory(width, height);
        
        AwtRenderer render = new AwtRenderer();
        render.render(shape, imageFactory.getGraphics(), new Rectangle2D.Double(0, 0, width, height));
        imageFactory.save("arc_up.png");
    }
    
    @Test
    public void testDownArc() throws IOException {
        double arcHeight = 10;
        Vector2d orientation = new Vector2d(1, 0);
        Point2d start = new Point2d(10, 10);
        Point2d end = new Point2d(50, 10);
        Shape shape = new Arc(orientation, start, end, arcHeight, true, Color.BLACK);
        
        int width = 100;
        int height = 100;
        double border = 50;
        
        ImageFactory imageFactory = new ImageFactory(width + (int)(2 * border), height + (int)(2 * border));
        
        AwtRenderer render = new AwtRenderer();
        render.render(shape, imageFactory.getGraphics(), new Rectangle2D.Double(border, border, width - border, height - border));
        imageFactory.save("arc_down.png");
    }

}
