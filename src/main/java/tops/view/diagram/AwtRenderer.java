package tops.view.diagram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import tops.view.model.Arc;
import tops.view.model.BoundsVisitor;
import tops.view.model.Box;
import tops.view.model.Shape;
import tops.view.model.Triangle;
import tops.view.util.ScaleToFit;

public class AwtRenderer {
    
    private class AwtVisitor implements Shape.Visitor {
        
        private final Graphics2D graphics;
        private final ScaleToFit scaleToFit;
        
        public AwtVisitor(Graphics2D graphics, ScaleToFit scaleToFit) {
            this.graphics = graphics;
            this.scaleToFit = scaleToFit;
        }

        @Override
        public void visit(Shape shape) {
            if (shape instanceof Arc) {
                visitArc((Arc)shape);
            } else if (shape instanceof Box) {
                visitBox((Box) shape);
            } else if (shape instanceof Triangle) {
                visitTriangle((Triangle) shape);
            }
        }
        
        private void visitTriangle(Triangle shape) {
            double r = 10.0;    // TODO ...
            
            double ox = shape.getCenter().x;
            double oy = shape.getCenter().y;
            boolean isUp = isUp(shape.getOrientation());    // XXX TODO
            
            Point2d pCenter = scaleToFit.transform(new Point2d(ox, oy));
            List<Point2d> corners = PointUtils.makePoints(pCenter, isUp? 0 : 180, r, 3);
            System.out.println("Drawing poly "  + corners);
            
            int[] xPoints = new int[] {(int)corners.get(0).x, (int)corners.get(1).x, (int)corners.get(2).x };
            int[] yPoints = new int[] {(int)corners.get(0).y, (int)corners.get(1).y, (int)corners.get(2).y };
            
            Color c = graphics.getColor();
            graphics.setColor(Color.RED); // TODO
            graphics.draw(new Polygon(xPoints, yPoints, 3));
            graphics.setColor(c);
        }
        
        private final Vector2d UP = new Vector2d(1, 0);
        private boolean isUp(Vector2d orientation) {
            return UP.dot(orientation) == 0;
        }
        
        private void visitBox(Box shape) {
            double ox = shape.getCenter().x;
            double oy = shape.getCenter().y;
            double ow = shape.getWidth();
            double oh = shape.getHeight();
            
            Point2d p = scaleToFit.transform(new Point2d(ox, oy));
            double nw = ow * scaleToFit.getScale();
            double nh = oh * scaleToFit.getScale();
            Color old = graphics.getColor();
            System.out.println("Drawing box at " + p + " width " + nw + " height " + nh);
            graphics.setColor(Color.BLACK); // TODO
            graphics.draw(new Rectangle2D.Double(p.x - (nw / 2), p.y - (nh / 2), nw, nh));
            graphics.setColor(old);
        }

        private void visitArc(Arc shape) {
            double oX = shape.getStart().x;
            double oY = shape.getStart().y;
            double oW = shape.getEnd().x - oX;
            double oH = shape.getHeight();
            
            System.out.println("Visiting arc " + shape);
            
//            Point2d p = transform(new Point2d(oX, oY));
            Point2d p = scaleToFit.transform(new Point2d(oX, oY));
            double scale = scaleToFit.getScale();
            double x = p.x;
            double y = p.y;
            double w = oW * scale;
            double h = oH * scale;
            
            Color old = graphics.getColor();
            graphics.setColor(shape.getColor());
            System.out.println("Drawing at " + x + " " + y + " " + w + " " + h);
            graphics.draw(new Arc2D.Double(x, y , w, h, 180, 180, Arc2D.OPEN));
            graphics.setColor(old);
        }
        
    }
    
    public void render(Shape shape, Graphics2D graphics, Rectangle2D canvas) {
        // fit the model to the canvas
        Rectangle2D modelBounds = getModelBounds(shape);
        System.out.println("Model bounds " + modelBounds);
        
//        double scale = calculateScale(canvas, modelBounds);
        
        // make a visitor
        AwtVisitor visitor = new AwtVisitor(graphics, getScale(canvas, modelBounds));
        
        // pass the visitor to the shape tree
        shape.accept(visitor);
    }
    
    private ScaleToFit getScale(Rectangle2D canvas, Rectangle2D modelBounds) {
        return new ScaleToFit(canvas, modelBounds);
    }
    
    private double calculateScale(Rectangle2D canvas, Rectangle2D modelBounds) {
        double widthScale = canvas.getWidth() / modelBounds.getWidth();
        double heightScale = canvas.getHeight() / modelBounds.getHeight();
        return Math.min(widthScale, heightScale);   /// XXX needs all cases of scaling up/down
    }

    private Rectangle2D getModelBounds(Shape shape) {
//        double minX = 0;
//        double minY = 0;
//        double maxX = 0;
//        double maxY = 0;
//        
//        // TODO
//        
//        return new Rectangle2D.Double(minX, minY, minX + maxX, minY + maxY);
        BoundsVisitor boundsVisitor = new BoundsVisitor();
        shape.accept(boundsVisitor);
        return boundsVisitor.getBounds();
    }

}
