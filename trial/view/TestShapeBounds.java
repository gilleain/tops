package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.junit.Test;

import tops.view.model.Arc;
import tops.view.model.Shape;

public class TestShapeBounds {
    
    @Test
    public void test() throws IOException {
        int width = 500;
        int height = 300;
        
        Image image = new BufferedImage(
                width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        Arc arc = makeArc();
        drawArc(g, arc);
        drawBounds(g, arc);
        ImageIO.write((RenderedImage)image, "PNG", new File("shapes.png"));
    }
    
    private void drawBounds(Graphics2D g, Shape shape) {
        Color c = g.getColor();
        g.setColor(Color.GRAY);
        Rectangle2D bounds = shape.getBounds();
        System.out.println("Drawing bounds " + bounds);
        g.draw(bounds);
        g.setColor(c);
    }
    
    private void drawArc(Graphics2D g, Arc arc) {
        Point2d s = arc.getStart();
        Point2d e = arc.getEnd();
        int sx = (int)s.x;
        int sy = (int)s.y;
        int w = (int)(e.x - s.x);
        int h = (int)arc.getHeight();
        Color c = g.getColor();
        g.setColor(arc.getColor());
        System.out.println("Drawing arc " + s + " " + e + " " + w + " " + h);
        g.drawArc(sx, sy - h, w, h, 0, 180);
        g.setColor(c);
    }
    
    private Arc makeArc() {
        double axis = 150;
        Vector2d o = new Vector2d(1, 0);
        Point2d s = new Point2d(100, axis);
        Point2d e = new Point2d(200, axis);
        double h = (e.x - s.x) / 2.0;
        return new Arc(o, s, e, h, Color.BLACK);
    }

}
