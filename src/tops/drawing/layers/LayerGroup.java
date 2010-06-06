package tops.drawing.layers;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;


/**
 * @author maclean
 *
 */
public class LayerGroup implements LayerElement {
    
    private ArrayList elements;
    
    public LayerGroup() {
        this.elements = new ArrayList();
    }
    
    public boolean equals(LayerElement other) {
        return other instanceof LayerGroup && this.getCenterX() == other.getCenterX();
    }
    
    public int getCenterX() {
        return -1;  //TODO
    }
    
    public Point getNTerminalPoint() {
        return null;    // TODO
    }

    public Point getCTerminalPoint() {
        return null;    // TODO
    }
    
    public boolean isUp() {
        return false;
    }
    
    public void layout(int centerX, int centerY, GeometricParameters p) {
        int xPos = centerX;
        for (int i = 0; i < this.elements.size(); i++) {
            LayerElement element = (LayerElement) this.elements.get(i);
            element.layout(xPos, centerY, p);
            //System.out.println("Laying out " + strand + " at " + xPos);
            xPos += p.separation;
        }
    }
    
    public void draw(Graphics2D g) {
        for (int i = 0; i < this.elements.size(); i++) {
            LayerElement element = (LayerElement) this.elements.get(i);
            element.draw(g);
        }
    }

}
