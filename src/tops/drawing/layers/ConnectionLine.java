package tops.drawing.layers;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 * @author maclean
 *
 */
public class ConnectionLine {
    
    private ArrayList segments;
    private LayerElement first;
    private LayerElement second;
    
    public ConnectionLine(LayerElement first, LayerElement second) {
        this.segments = new ArrayList();
        this.first = first;
        this.second = second;
    }
    
    public void addSegment(Point a, Point b) {
        this.segments.add(new Line2D.Double(a, b));
    }
    
    public int getSSENumber() { return 0; }
    
    public boolean isUp() { return false; }
    
    public int getCenterX() { return 0; }
    
    public Point getNTerminalPoint() { return null; }
    
    public Point getCTerminalPoint() { return null; }
    
    public void layout(int centerX, GeometricParameters p) {
//        System.out.println("first " + first + " second " + second);

        if (first instanceof Arrow) {
            
            if (second instanceof Arrow) {
                Point a = first.getCTerminalPoint();
                Point b = new Point(a);
                if (first.isUp()) {
                    b.translate(0, -p.connectionLength);
                } else {
                    b.translate(0, p.connectionLength);
                }
                
                Point d = second.getNTerminalPoint();
                Point c = new Point(d);
                if (second.isUp()) {
                    c.translate(0, p.connectionLength);
                } else {
                    c.translate(0, -p.connectionLength);
                }
                
                this.addSegment(a, b);
                this.addSegment(b, c);
                this.addSegment(c, d);
                
            } else if (second instanceof HelixBox) {
                
            } else if (second instanceof TerminalBox) {
                int cTerminalBoxY = second.getCTerminalPoint().y;
                
                if (first.isUp()) {
                    cTerminalBoxY -= p.connectionLength;
                    this.addSegment(second.getCTerminalPoint(),
                            first.getCTerminalPoint());
                } else {
                    cTerminalBoxY += p.connectionLength;
                    this.addSegment(second.getNTerminalPoint(), 
                            first.getCTerminalPoint());
                }
            }
            
        } else if (first instanceof HelixBox) {
            
            if (second instanceof Arrow) {
                
            } else if (second instanceof HelixBox) {
                
            } else if (second instanceof TerminalBox) {
                
            }
            
        } else if (first instanceof TerminalBox) {
            
            if (second instanceof Arrow || second instanceof HelixBox) {
                int nTerminalBoxY = first.getNTerminalPoint().y;
                 
                if (second.isUp()) {
                    nTerminalBoxY += p.connectionLength;
                    this.addSegment(second.getNTerminalPoint(),
                            first.getNTerminalPoint());
                } else {
                    nTerminalBoxY -= p.connectionLength;
                    this.addSegment(second.getCTerminalPoint(),
                            first.getNTerminalPoint());
                }
            } else if (second instanceof TerminalBox) {
                
            }
            
        }
    }
    
    public void draw(Graphics2D g) {
        for (int i = 0; i < this.segments.size(); i++) {
            Line2D currentSegment = (Line2D) this.segments.get(i);
            g.draw(currentSegment);
        }
    }
}
