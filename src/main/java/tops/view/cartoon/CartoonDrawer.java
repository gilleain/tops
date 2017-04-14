package tops.view.cartoon;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import tops.dw.protein.Cartoon;
import tops.dw.protein.SecStrucElement;
import tops.port.model.Direction;
import tops.view.cartoon.builder.BuilderFactory;
import tops.view.cartoon.builder.GraphicsBuilder;

//the 'director' class for CartoonBuilders
//usage :
//	c = new CartoonDrawer();
//  c.draw("name", "PDF" | "IMG", cartoon);
//  Obj o = c.getProduct();

public class CartoonDrawer {

    private static final int BORDER_WIDTH = 10;
    
    private BuilderFactory factory;
    
    public void draw(Graphics g, int w, int h, Cartoon cartoon) {
        Rectangle bb = this.init(cartoon, w - (2 * CartoonDrawer.BORDER_WIDTH), w, h);
        this.draw(cartoon, new GraphicsBuilder(g, "", bb, w, h));
    }

    // this method is for byte representations
    public void draw(String name, String type, int w, int h, Cartoon cartoon, OutputStream os) throws IOException {
        Rectangle bb = this.init(cartoon, w - (2 * CartoonDrawer.BORDER_WIDTH), w, h);
        Image image = new BufferedImage(bb.width, bb.height, BufferedImage.TYPE_3BYTE_BGR);
        
        ByteCartoonBuilder builder = factory.makeForImage(type, image, bb);
        this.draw(cartoon, builder);
        builder.printProduct(os);
    }

    // this method is for text representations
    public void draw(String name, String type, Cartoon cartoon, PrintWriter pw) throws IOException {
        Rectangle bb = this.init(cartoon);
        TextCartoonBuilder builder = factory.make(type, bb);
        this.draw(cartoon, builder);
        builder.printProduct(pw);
    }

    private Rectangle init(Cartoon cartoon) {
        // neded to convert c tops coordinates to Java coordinates
        cartoon.invertY(); 
        Rectangle bb = cartoon.boundingBox();
        System.err.println("bb before = " + bb);

        this.centerDiagram(cartoon, CartoonDrawer.BORDER_WIDTH, CartoonDrawer.BORDER_WIDTH, bb.width, bb.height, bb);
        Point currentCorner = bb.getLocation();
        bb.translate(CartoonDrawer.BORDER_WIDTH - currentCorner.x, CartoonDrawer.BORDER_WIDTH - currentCorner.y);
        return bb;
    }

    private Rectangle init(Cartoon cartoon, int length, int w, int h) {
        Rectangle bb = cartoon.boundingBox();
        //System.err.println("bb before = " + bb);

        // get the largest dimension
        int largestDimension = Math.max(bb.width, bb.height); 

        //System.err.println("largest dimension = " + largestDimension + " length = " + length);

        // rounding can give you 0.0!
        float scale = ((float) length / (float) largestDimension); 

        //System.err.println("scale = " + scale);
        cartoon.applyScale(scale);
        cartoon.invertY(); // needed to convert c tops coordinates to Java  coordinates
        if (scale != 1.0) bb = cartoon.boundingBox(); // get the new bounds

        //System.err.println("bb after = " + bb);
        Point currentCorner = bb.getLocation();

        //System.err.println("location of current corner = " + currentCorner);

        this.centerDiagram(cartoon, CartoonDrawer.BORDER_WIDTH, CartoonDrawer.BORDER_WIDTH, w, h, bb);

        // translate bb /after/ moving the SSEs! (BE  BETTER!)
        bb.translate(0 - currentCorner.x, 0 - currentCorner.y); 
        return bb;
    }

    private void draw(Cartoon cartoon, CartoonBuilder builder) {
        SecStrucElement last = null;
        for (SecStrucElement s : cartoon.getSSEs()) {
            this.drawSecStruc(s, builder);
            if (last != null) {
                this.drawConnection(builder, last, s);
            }
            last = s;
        }
    }

    private void invertY(SecStrucElement root) {
        for (SecStrucElement s : new ArrayList<SecStrucElement>()) { // TODO
            s.getPosition().y *= -1;

            for (Point p : s.getConnectionTo()) {
                p.y *= -1;
            }
        }
    }

    public void applyScale(Cartoon cartoon, float scale) {
        for (SecStrucElement s : cartoon.getSSEs()) {

            Point p = s.getPosition();
            p.x = Math.round(scale * p.x);
            p.y = Math.round(scale * p.y);

            int r = s.getSymbolRadius();
            s.setSymbolRadius(Math.round(r * scale));

            for (Point pc : s.getConnectionTo()) {
                pc.x = Math.round(scale * pc.x);
                pc.y = Math.round(scale * pc.y);
            }
        }
    }

    private void centerDiagram(Cartoon cartoon, int x, int y, int width, int height, Rectangle bounds) {
        Point currentCenter = new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
        Point trueCenter = new Point((x + width) / 2, (y + height) / 2);

        System.err.println("Current center = " + currentCenter);
        System.err.println("True center = " + trueCenter);

        int shiftX = trueCenter.x - currentCenter.x;
        int shiftY = trueCenter.y - currentCenter.y;

        cartoon.translateDiagram(shiftX, shiftY);
    }

    private Rectangle getBoundingBox(Cartoon cartoon) {
        // without the 'int h' parameter, this is the same as
        // root.TopsBoundingBox()?
        // not sure why EPSBoundingBox has to have 'y = h - pos.y'??

        int xmin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE;
        int ymax = Integer.MIN_VALUE;
        int rmax = Integer.MIN_VALUE;

        Point pos;
        int rad, x, y;
        for (SecStrucElement s : cartoon.getSSEs()) {
            pos = s.getPosition();
            x = pos.x;
            y = pos.y;
            rad = s.getSymbolRadius();
            if (x > xmax) xmax = x;
            if (y > ymax) ymax = y;
            if (x < xmin) xmin = x;
            if (y < ymin) ymin = y;
            if (rad > rmax) rmax = rad;

            if (!(s.getConnectionTo().isEmpty())) {
                for (Point PointTo : s.getConnectionTo()) {
                    x = PointTo.x;
                    y = PointTo.y;
                    if (x > xmax)
                        xmax = x;
                    if (y > ymax)
                        ymax = y;
                    if (x < xmin)
                        xmin = x;
                    if (y < ymin)
                        ymin = y;
                }
            }
        }
        xmax += rmax;
        ymax += rmax;
        xmin -= rmax;
        ymin -= rmax;

        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    private void drawSecStruc(SecStrucElement ss, CartoonBuilder builder) {

        int radius = ss.getSymbolRadius();
        Point pos = ss.getPosition();
        Color col = ss.getColour();

        if (ss.getType() == HELIX) {
            this.drawHelix(builder, pos.x, pos.y, radius, col);
        } else if (ss.getType() == EXTENDED) {
            this.drawStrand(builder, pos.x, pos.y, radius, ss.getDirection(), col);
        } else if (ss.getType() == CTERMINUS || ss.getType() == NTERMINUS) {
            this.drawTerminus(builder, pos.x, pos.y, radius, ss.getLabel());
        }

    }

    private void drawTerminus(CartoonBuilder builder, int x, int y, int r, String label) {
        builder.drawTerminus(x, y, r, label); // facade?
    }

    private void drawHelix(CartoonBuilder builder, int x, int y, int r, Color c) {
        builder.drawHelix(x, y, r, c); // facade?
    }

    private void drawStrand(CartoonBuilder builder, int x, int y, int r, Direction direction, Color c) {

        double pi6 = Math.PI / 6.0;
        double cospi6 = Math.cos(pi6);
        double sinpi6 = Math.sin(pi6);

        int rsinpi6 = (int) (r * sinpi6);
        int rcospi6 = (int) (r * cospi6);

        int pointX, pointY;
        int leftX, leftY;
        int rightX, rightY;

        if (direction == DOWN) {
            pointX = x;
            pointY = y + r;
            leftX = x - rcospi6;
            leftY = y - rsinpi6;
            rightX = x + rcospi6;
            rightY = y - rsinpi6;
        } else {
            pointX = x;
            pointY = y - r;
            leftX = x - rcospi6;
            leftY = y + rsinpi6;
            rightX = x + rcospi6;
            rightY = y + rsinpi6;
        }

        builder.drawStrand(pointX, pointY, leftX, leftY, rightX, rightY, c);
    }

    private void drawConnection(CartoonBuilder builder, SecStrucElement from, SecStrucElement to) {

        if (from == null) {
            return;
        }

        /* Don't connect from a C terminus or to an N terminus */
        if (from.getType() == CTERMINUS || from.getType() == NTERMINUS) {
            return;
        }

        int radiusFrom = from.getSymbolRadius();
        int radiusTo = to.getSymbolRadius();

        Point pointTo = to.getPosition();
        Point pointFrom = from.getPosition();

        /*
         * draw from border rather than centre if direction is down (D) or if
         * Type is N or C
         */
        if (from.getDirection() == DOWN || from.getType() == CTERMINUS || from.getType() == NTERMINUS) {
            if (from.getType() == EXTENDED) {
                pointFrom = this.downTriangleBorder(
                        from.getPosition(), to.getPosition(), radiusFrom);
            } else {
                pointFrom = this.circleBorder(
                        from.getPosition(), to.getPosition(), radiusFrom);
            }
        }

        /*
         * draw to border rather than centre if direction is up (U) or if Type
         * is N or C
         */
        if (to.getDirection().equals("U") || to.getType() == CTERMINUS || to.getType() == NTERMINUS) {
            if (to.getType() == EXTENDED) {
                pointTo = this.upTriangleBorder(to.getPosition(),
                        from.getPosition(), radiusTo);
            } else {
                pointTo = this.circleBorder(to.getPosition(), from.getPosition(),
                        radiusTo);
            }
        }

        if (from.getConnectionTo().isEmpty()) {
            this.joinPoints(builder, pointTo, pointFrom);
        } else {

            Iterator<Point> connectionEnum = from.getConnectionTo().iterator();
            Point connectionPointTo = connectionEnum.next();

            this.joinPoints(builder, pointFrom, connectionPointTo);

            Point connectionPointFrom;
            while (connectionEnum.hasNext()) {
                connectionPointFrom = connectionPointTo; // join next to previous
                connectionPointTo = connectionEnum.next(); // get next
                this.joinPoints(builder, connectionPointFrom, connectionPointTo); // join!
            }
            connectionPointFrom = connectionPointTo; // get the last in the chain
            
            // finally, connect to the next SSE
            this.joinPoints(builder, connectionPointFrom, pointTo); 
        }
    }

    private void joinPoints(CartoonBuilder builder, Point from, Point to) {
        builder.connect(from.x, from.y, to.x, to.y);
    }

    /*
     * private method calculates the point on a circle of centre p1 and radius r
     * which lies on the line p1->p2
     */
    private Point circleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = p1.distance(p2);

        if ((s < r) || (r <= 0.0))
            return p1;

        xb = x1 + (r / s) * (x2 - x1);
        yb = y1 + (r / s) * (y2 - y1);

        return new Point((int) xb, (int) yb);

    }

    /*
     * private method calculates the point on the border of a down equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point downTriangleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        double theta = 0.0, gamma = 0.0;

        double l;

        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double pi6 = Math.PI / 6.0;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = p1.distance(p2);

        /* theta in range -PI < theta <= PI */
        theta = Math.atan2(y2 - y1, x2 - x1);

        if ((-pi6 < theta) && (theta <= pi2)) {
            gamma = theta - pi6;
        } else if ((pi2 < theta) && (theta <= pi)) {
            gamma = theta - 5.0 * pi6;
        } else if ((-pi < theta) && (theta <= -5.0 * pi6)) {
            gamma = 7.0 * pi6 + theta;
        } else if ((-5.0 * pi6 < theta) && (theta <= -pi6)) {
            gamma = theta + pi2;
        }

        l = (r) / (2.0 * Math.cos(gamma));

        xb = x1 + (x2 - x1) * l / s;
        yb = y1 + (y2 - y1) * l / s;

        return new Point((int) xb, (int) yb);

    }

    /*
     * private method calculates the point on the border of an up equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point upTriangleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        double theta = 0.0, gamma = 0.0;

        double l;

        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double pi6 = Math.PI / 6.0;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = p1.distance(p2);

        /* theta in range -PI < theta <= PI */
        theta = Math.atan2(y2 - y1, x2 - x1);

        if ((-pi2 < theta) && (theta <= pi6)) {
            gamma = theta + pi6;
        } else if ((pi6 < theta) && (theta <= 5.0 * pi6)) {
            gamma = theta - pi2;
        } else if ((5.0 * pi6 < theta) && (theta <= pi)) {
            gamma = 7.0 * pi6 - theta;
        } else if ((-pi < theta) && (theta <= -pi2)) {
            gamma = theta + 5.0 * pi6;
        }

        l = (r) / (2.0 * Math.cos(gamma));

        xb = x1 + (x2 - x1) * l / s;
        yb = y1 + (y2 - y1) * l / s;

        return new Point((int) xb, (int) yb);

    }
}
