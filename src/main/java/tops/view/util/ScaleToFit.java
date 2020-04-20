package tops.view.util;

import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

/**
 * Scales a model bounds to fit a canvas.
 * 
 * @author gilleain
 *
 */
public class ScaleToFit {
    
    private final Rectangle2D canvas;
    
    private final Point2d center;   // the center of the canvas
    
    private final double scale;     // the amount to scale points, up or down
    
    // if true, makes the calculations simpler
    private final boolean isConcentric = false; // XXX TODO
    
    private final Point2d dd;
    
    public ScaleToFit(Rectangle2D canvas, Rectangle2D model) {
        this.center = new Point2d(canvas.getCenterX(), canvas.getCenterY());
        this.dd = new Point2d(canvas.getCenterX() - model.getCenterX(),
                              canvas.getCenterY() - model.getCenterY());
        this.canvas = canvas;
        
        double cw = canvas.getWidth();
        double ch = canvas.getHeight();
        double mw = model.getWidth();
        double mh = model.getHeight();
        
//        this.scale = 0.5 * Math.min(cw / mw, ch / mh);
        this.scale = Math.min(cw / mw, ch / mh);
        System.out.println("Scale = " + scale + " dd = " + dd);
    }
    
    public double getScale() {
        return this.scale;
    }
    
    
    public Point2d transform(Point2d original) {
        // shift to canvas center
        double ox = original.x + dd.x;
        double oy = original.y + dd.y;
        
        // calculate difference from that center for scaling
        double dx = ox - center.x;
        double dy = oy - center.y;
        
        // center-scale from that point
        double nx = ox + (scale * dx);
        double ny = oy + (scale * dy);
        
        return new Point2d(nx, ny);
    }

}
