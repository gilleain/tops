package tops.drawing.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;


/**
 * @author maclean
 *
 */
public class Arrow implements LayerElement {
    
    private boolean isUp;
    private Polygon shape;
    private Point topPoint;
    private Point botPoint;
    
    public Arrow(boolean isUp) {
        this.isUp = isUp;
        
    }
    
    public boolean equals(LayerElement other) {
        return other instanceof Arrow && this.getCenterX() == other.getCenterX();
    }
    
    public boolean isUp() {
        return this.isUp;
    }
    
    public int getCenterX() {
        return (int) this.shape.getBounds().getCenterX();
    }
    
    public Point getNTerminalPoint() {
        if (this.isUp) {
            return this.botPoint;
        } else {
            return this.topPoint;
        }
    }
    
    public Point getCTerminalPoint() {
        if (this.isUp) {
            return this.topPoint;
        } else {
            return this.botPoint;
        }
    }
    
    public void layout(int centerX, int centerY, GeometricParameters p) {
        int w2 = p.strandWidth / 2;
        int h2 = p.strandHeight / 2;
   
        this.topPoint = new Point(centerX, centerY - h2);
        this.botPoint = new Point(centerX, centerY + h2);
        
        int[] xs = new int[8];
        int[] ys = new int[8];
        if (this.isUp) { 
            int arrowHeadY = centerY - (h2 / 2);
            
            xs[0] = topPoint.x;
            ys[0] = topPoint.y;
            
            xs[1] = centerX + w2;
            ys[1] = arrowHeadY;
            
            xs[2] = centerX + (w2 / 2);
            ys[2] = arrowHeadY;
            
            xs[3] = centerX + (w2 / 2);
            ys[3] = botPoint.y;
            
            xs[4] = botPoint.x;
            ys[4] = botPoint.y;
            
            xs[5] = centerX - (w2 / 2);
            ys[5] = botPoint.y;
            
            xs[6] = xs[5];
            ys[6] = arrowHeadY;
            
            xs[7] = centerX - w2;
            ys[7] = arrowHeadY;
            
        } else {
               int arrowHeadY = centerY + (h2 / 2);
                
                xs[0] = botPoint.x;
                ys[0] = botPoint.y;
                
                xs[1] = centerX + w2;
                ys[1] = arrowHeadY;
                
                xs[2] = centerX + (w2 / 2);
                ys[2] = arrowHeadY;
                
                xs[3] = xs[2];
                ys[3] = topPoint.y;
                
                xs[4] = topPoint.x;
                ys[4] = topPoint.y;
                
                xs[5] = centerX - (w2 / 2);
                ys[5] = topPoint.y;
                
                xs[6] = xs[5];
                ys[6] = arrowHeadY;
                
                xs[7] = centerX - w2;
                ys[7] = arrowHeadY;
        }
        
        this.shape = new Polygon(xs, ys, 8);
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.yellow);
        g.fill(this.shape);
        g.setColor(Color.black);
        g.draw(this.shape);
    }

}
