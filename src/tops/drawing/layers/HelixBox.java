package tops.drawing.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class HelixBox implements LayerElement {
    
    private Shape shape;
    private boolean isUp;
    
    public HelixBox(boolean isUp) {
        this.isUp = isUp;
    }
    
    public boolean equals(LayerElement other) {
        return other instanceof HelixBox && this.getCenterX() == other.getCenterX();
    }
    
    public Point getNTerminalPoint() {
        if (this.isUp) {
            return null;    // TODO
        } else {
            return null;    // TODO
        }
    }
    
    public Point getCTerminalPoint() {
        if (this.isUp) {
            return null;    // TODO
        } else {
            return null;    // TODO
        }
    }
    
    public boolean isUp() {
        return this.isUp;
    }
    
    public int getCenterX() {
        return (int) this.shape.getBounds2D().getCenterX();
    }
    
    public void layout(int centerX, int centerY, GeometricParameters p) {
        int w2 = p.helixWidth / 2;
        int h2 = p.helixHeight / 2;
        int x = centerX - w2;
        int y = centerY - h2;
        int w = p.helixWidth;
        int h = p.helixHeight;
        
        this.shape = new Rectangle2D.Float(x, y, w, h);
    }
    
    public void draw(Graphics2D g) {
        System.out.println("drawing " + this.shape);
        
        g.setColor(Color.RED);
        g.fill(this.shape);
        g.setColor(Color.BLACK);
        g.draw(this.shape);
    }

}
