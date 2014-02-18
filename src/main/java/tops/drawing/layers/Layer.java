package tops.drawing.layers;

import java.awt.Graphics2D;
import java.util.ArrayList;


/**
 * @author maclean
 *
 */
public class Layer {
    
    private int zOrder; // 0 (front) to N (back) for N+1 layers
    private ArrayList<LayerElement> layerElements;
    
    public Layer(int zOrder) {
        this.layerElements = new ArrayList<LayerElement>();
        this.zOrder = zOrder;
    }
    
    public void addLayerElement(LayerElement element) {
        this.layerElements.add(element);
    }
    
    public int getZOrder() {
        return this.zOrder;
    }
    
    public void layout(int startX, int startY, GeometricParameters p) {
        int centerX = startX;
        int centerY = startY;
        for (int i = 0; i < this.layerElements.size(); i++) {
            LayerElement layerElement = (LayerElement) this.layerElements.get(i);
            layerElement.layout(centerX, centerY, p);
            centerX += p.separation;
        }
    }
    
    public void draw(Graphics2D g) {
        for (int i = 0; i < this.layerElements.size(); i++) {
            LayerElement layerElement = (LayerElement) this.layerElements.get(i);
            layerElement.draw(g);
        }
    }

}
