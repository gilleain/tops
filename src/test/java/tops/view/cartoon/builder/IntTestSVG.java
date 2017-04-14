package tops.view.cartoon.builder;

import static org.junit.Assert.assertTrue;
import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UP;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;

import java.awt.Point;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import tops.dw.protein.Cartoon;
import tops.dw.protein.SecStrucElement;
import tops.port.model.Direction;
import tops.port.model.SSEType;
import tops.view.cartoon.CartoonDrawer;

public class IntTestSVG {
    
    @Test
    public void testDraw() throws IOException {
        StringWriter writer = new StringWriter();
        
        Cartoon cartoon = new Cartoon();
        cartoon.addSSE(sse(EXTENDED, UP, new Point(50, 50)));
        cartoon.addSSE(sse(HELIX, DOWN, new Point(70, 70)));
        cartoon.addSSE(sse(EXTENDED, UP, new Point(50, 70)));
        CartoonDrawer cartoonDrawer = new CartoonDrawer();
        
        cartoonDrawer.draw("hello", "SVG", cartoon, new PrintWriter(writer));
        
        String result = writer.toString();
//        System.out.println(result);   // TODO : better asserts!
        assertTrue(result.contains("polygon"));
        assertTrue(result.contains("circle"));
        assertTrue(result.contains("line"));
    }
    
    private SecStrucElement sse(SSEType type, Direction direction, Point p) {
        SecStrucElement s = new SecStrucElement();
        s.setType(type);
        s.setDirection(direction);
        s.setPosition(p);
        return s;
    }

}
