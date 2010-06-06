package tops.view.tops2D.diagram;

import java.awt.*;
import java.awt.geom.*;

public abstract class SSE {

    protected boolean isDown;
    protected Rectangle2D bb;
    private Color color = null;
    private Shape s;
    private int pos;

    public SSE(boolean b, int p) {
        // XXX potentially a problem if we try to draw without layout!
        this.bb = null; 
        
        this.isDown = b;
        this.pos = p;
       
    }
    
    public void setBounds(Rectangle2D bb) {
        this.bb = bb;
        if (this.isDown) {
            this.shift();
        }
    }
    
    public int getCenterX() {
        return (int) this.bb.getCenterX();
    }

    public void shift() {
        this.bb.setFrame(this.bb.getX(), this.bb.getY() - this.bb.getWidth(), 
                this.bb.getWidth(), this.bb.getHeight());
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Shape getShape() {
        if (this.s == null) {
            this.s = this.createShape();
        }
        return this.s;
    }

    public double getCenter() {
        return (this.bb.getX() + (this.bb.getWidth() / 2));
    }

    public int getPos() {
        return this.pos;
    }

    public abstract Shape createShape();

    public Color getColor() {
        return (this.color != null) ? this.color : Color.black;
    }

    public double getSize() {
        return this.bb.getWidth();
    }
}
