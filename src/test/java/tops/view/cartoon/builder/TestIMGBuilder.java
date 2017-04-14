package tops.view.cartoon.builder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestIMGBuilder {
    
    @Mock
    private OutputStream out;
    
    @Mock
    private Image image;
    
    @Mock
    private Graphics graphics;
    
    @Before
    public void setup() {
        when(image.getGraphics()).thenReturn(graphics);
    }
    
    @Test
    public void testCreate() {
        String name = "Name";
        Rectangle bb = new Rectangle(10, 10, 10, 10);
        int width = 200;
        int height = 200;
        
        new IMGBuilder(image, name, bb, width, height);

        verify(graphics).setColor(Color.WHITE);
        verify(graphics).fillRect(0, 0, width, height);
        
        verify(graphics).setColor(Color.BLACK);
        verify(graphics).drawString(name, IMGBuilder.NAME_X, IMGBuilder.NAME_Y);
        verify(graphics).drawRect(0, 0, width - 2, height - 2);
    }
    
    private IMGBuilder get() {
        return new IMGBuilder(image, "name", new Rectangle(1, 1, 1, 1), 1, 1);
    }
    
    @Test
    public void testConnect() {
        IMGBuilder builder = get();
        int x1 = 10;
        int y1 = 20;
        int x2 = 30;
        int y2 = 40;
        builder.connect(x1, y1, x2, y2);
        
        verify(graphics).drawLine(x1, y1, x2, y2);
    }
    
    @Test
    public void testDrawHelix() {
        IMGBuilder builder = get();
        
        int x = 10;
        int y = 20;
        int r = 5;
        Color color = Color.RED;
        builder.drawHelix(x, y, r, color);
        
        verify(graphics).setColor(color);
        verify(graphics).fillOval(x - r, y - r, 2 * r, 2 * r);
        // XXX - seems like a mockito bug here : why two times?
        verify(graphics, times(2)).setColor(Color.BLACK);
        
        verify(graphics).drawOval(x - r, y - r, 2 * r, 2 * r);
    }
    
    public void testDrawStrand() {
        IMGBuilder builder = get();
        int pointX = 10;
        int pointY = 20;
        int leftX  = 30;
        int leftY  = 40;
        int rightX  = 30;
        int rightY  = 40;
        Color color = Color.BLUE;
        
        builder.drawStrand(pointX, pointY, leftX, leftY, rightX, rightY, color);
        verify(graphics).setColor(color);
        verify(graphics).fillPolygon(new int[] { pointX,  leftX,  rightX },
                                     new int[] { pointY,  leftY,  rightY }, 3);
        
        verify(graphics).setColor(Color.BLACK);
        verify(graphics).drawPolygon(new int[] { pointX,  leftX,  rightX },
                                     new int[] { pointY,  leftY,  rightY }, 3);
    }

}
