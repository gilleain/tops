package tops.drawing.layers;

import java.awt.Graphics2D;
import java.awt.Point;


/**
 * @author maclean
 *
 */
public interface LayerElement {
    
    public boolean isUp();
    
    public int getCenterX();
    
    public Point getNTerminalPoint();
    
    public Point getCTerminalPoint();
    
    public void layout(int centerX, int centerY, GeometricParameters p);
    
    public void draw(Graphics2D g);
    
    public boolean equals(LayerElement other);

}
