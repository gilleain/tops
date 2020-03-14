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
//        this.center = new Point2d(canvas.getCenterX(), canvas.getCenterY());
        this.center = new Point2d(model.getCenterX(), model.getCenterY());
        this.dd = new Point2d(canvas.getCenterX() - model.getCenterX(),
                              canvas.getCenterY() - model.getCenterY());
        this.canvas = canvas;
        
        double cw = canvas.getWidth();
        double ch = canvas.getHeight();
        double mw = model.getWidth();
        double mh = model.getHeight();
        
//        this.scale = 0.5 * Math.min(cw / mw, ch / mh);
        this.scale = Math.min(cw / mw, ch / mh);
        System.out.println("Scale = " + scale);
    }
    
    public double getScale() {
        return this.scale;
    }
    
    
    public Point2d transform(Point2d original) {
        double ox = original.x;
        double oy = original.y;
        
        double dx = ox - center.x;
        double dy = oy - center.y;
        
        double nx = ox + (scale * dx) + dd.x;
        double ny = oy + (scale * dy) + dd.y;
        
        // TODO incorporate the difference between the canvas center and the model center
        
        return new Point2d(nx, ny);
    }

}
