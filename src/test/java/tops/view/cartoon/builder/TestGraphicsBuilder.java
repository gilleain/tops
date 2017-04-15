package tops.view.cartoon.builder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.OutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestGraphicsBuilder {
    
    @Mock
    private OutputStream out;
    
    @Mock
    private Graphics graphics;
    
    @Test
    public void testCreate() {
        String name = "Name";
        Rectangle bb = new Rectangle(10, 10, 10, 10);
        int width = 200;
        int height = 200;
        
        new GraphicsBuilder(graphics, name, bb, width, height);

        verify(graphics).setColor(Color.WHITE);
        verify(graphics).fillRect(0, 0, width, height);
        
        verify(graphics).setColor(Color.BLACK);
        verify(graphics).drawRect(0, 0, width - 2, height - 2);
    }
    
    private GraphicsBuilder get() {
        return new GraphicsBuilder(graphics, "name", new Rectangle(1, 1, 1, 1), 1, 1);
    }
    
    @Test
    public void testConnect() {
        GraphicsBuilder builder = get();
        Point p1 = new Point(10, 20);
        Point p2 = new Point(30, 40);
        builder.connect(p1, p2);
        
        verify(graphics).drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    
    @Test
    public void testDrawHelix() {
        GraphicsBuilder builder = get();
        
        Point center = new Point(10, 20);
        int r = 5;
        Color color = Color.RED;
        builder.drawHelix(center, r, color);
        
        verify(graphics).setColor(color);
        verify(graphics).fillOval(center.x - r, center.y - r, 2 * r, 2 * r);
        verify(graphics, times(2)).setColor(Color.BLACK);
        
        verify(graphics).drawOval(center.x - r, center.y - r, 2 * r, 2 * r);
    }
    
    @Test
    public void testDrawStrand() {
        GraphicsBuilder builder = get();
        Point center = new Point(10, 20);
        Point left   = new Point(30, 40);
        Point right  = new Point(40, 50);
        Color color = Color.BLUE;
        
        builder.drawStrand(center, left, right, color);
        verify(graphics).setColor(color);
        verify(graphics).fillPolygon(new int[] { center.x,  left.x,  right.x },
                                     new int[] { center.y,  left.y,  right.y }, 3);
        
        verify(graphics, times(2)).setColor(Color.BLACK);
        verify(graphics).drawPolygon(new int[] { center.x,  left.x,  right.x },
                                     new int[] { center.y,  left.y,  right.y }, 3);
    }
    
    @Test
    public void testDrawLabel() {
        GraphicsBuilder builder = get();
        Point center = new Point(10, 20);
        String text = "hello";
        builder.drawLabel(center, text);
        verify(graphics).drawString(text, center.x, center.y);
    }

}
