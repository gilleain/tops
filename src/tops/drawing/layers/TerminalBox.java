package tops.drawing.layers;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class TerminalBox {

    private String label;
    private Rectangle rectangle;
//    private int labelX;
//    private int labelY;
    
    public TerminalBox(String label, int size, int centerX, int centerY) {
        this.label = label;
        int s2 = size / 2;
        this.rectangle = new Rectangle(centerX - s2, centerY - s2, size, size);
     
//        this.labelX = this.rectangle.x;
//        this.labelY = this.rectangle.y + size;
    }
    
    public boolean equals(LayerElement other) {
        return other instanceof TerminalBox && this.getCenterX() == other.getCenterX();
    }
    
    public int getCenterX() {
        return -1;
    }
    
    public Point getBasePoint() {
        int x = this.rectangle.x + (this.rectangle.width / 2);
        int y = this.rectangle.y + this.rectangle.height;
        return new Point(x, y); 
    }
    
    public Point getTopPoint() {
        int x = this.rectangle.x + (this.rectangle.width / 2);
        int y = this.rectangle.y;
        return new Point(x, y); 
    }
    
    public void draw(Graphics2D g) {
        g.drawRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
        
        Font f = Font.decode("Arial-BOLD-25");
        g.setFont(f);
        Rectangle2D stringBounds = f.getStringBounds(label, g.getFontRenderContext());
        int gap = (this.rectangle.width / 2) - ((int)stringBounds.getWidth() / 2);
        int labelX = this.rectangle.x + gap;
        int labelY = this.rectangle.y + this.rectangle.height - gap;
        g.drawString(this.label, labelX, labelY);
    }
}
