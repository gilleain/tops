package tops.view.tops2D.cartoon;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Vector;

import tops.dw.protein.SecStrucElement;
import tops.view.tops2D.cartoon.builder.IMGBuilder;
import tops.view.tops2D.cartoon.builder.PDFBuilder;
import tops.view.tops2D.cartoon.builder.PSBuilder;
import tops.view.tops2D.cartoon.builder.SVGBuilder;

//the 'director' class for CartoonBuilders
//usage :
//	c = new CartoonDrawer();
//  c.draw("name", "PDF" | "IMG", root);
//  Obj o = c.getProduct();

public class CartoonDrawer {

    private CartoonBuilder builder;

    private static final int BORDER_WIDTH = 10;

    public CartoonDrawer() {
    	this.builder = null;
    }

    // this method is for byte representations
    public void draw(String name, String type, int w, int h, SecStrucElement root, OutputStream os) throws IOException {
        Rectangle bb = this.init(root, w - (2 * CartoonDrawer.BORDER_WIDTH), w, h);
        if (type.equals("IMG")) {
            this.builder = new IMGBuilder(name, bb, os, w, h);
        } else if (type.equals("PDF")) {
            this.builder = new PDFBuilder(bb, os);
        } else {
            throw new IOException("Unsupported output type : " + type);
        }
        this.draw(root);
        this.builder.printProduct();
    }

    // this method is for text representations
    public void draw(String name, String type, SecStrucElement root, PrintWriter pw) throws IOException {
        Rectangle bb = this.init(root);
        if (type.equals("SVG")) {
            this.builder = new SVGBuilder(bb, pw);
        } else if (type.equals("PS")) {
            this.builder = new PSBuilder(bb, pw);
        } else {
            throw new IOException("Unsupported output type : " + type);
        }
        //System.err.println("drawing");
        this.draw(root);
        //System.err.println("printing");
        this.builder.printProduct();
    }

    private Rectangle init(SecStrucElement root) {
        // neded to convert c tops coordinates to Java coordinates
        this.invertY(root); 
        Rectangle bb = this.getBoundingBox(root);
        System.err.println("bb before = " + bb);

        this.centerDiagram(root, CartoonDrawer.BORDER_WIDTH, CartoonDrawer.BORDER_WIDTH, bb.width, bb.height, bb);
        Point currentCorner = bb.getLocation();
        bb.translate(CartoonDrawer.BORDER_WIDTH - currentCorner.x, CartoonDrawer.BORDER_WIDTH - currentCorner.y);
        return bb;
    }

    private Rectangle init(SecStrucElement root, int length, int w, int h) {
        Rectangle bb = this.getBoundingBox(root);
        //System.err.println("bb before = " + bb);

        // get the largest dimension
        int largestDimension = Math.max(bb.width, bb.height); 

        //System.err.println("largest dimension = " + largestDimension + " length = " + length);

        // rounding can give you 0.0!
        float scale = ((float) length / (float) largestDimension); 

        //System.err.println("scale = " + scale);
        this.applyScale(root, scale);
        this.invertY(root); // neded to convert c tops coordinates to Java  coordinates
        if (scale != 1.0) bb = this.getBoundingBox(root); // get the new bounds

        //System.err.println("bb after = " + bb);
        Point currentCorner = bb.getLocation();

        //System.err.println("location of current corner = " + currentCorner);

        this.centerDiagram(root, CartoonDrawer.BORDER_WIDTH, CartoonDrawer.BORDER_WIDTH, w, h, bb);

        // translate bb /after/ moving the SSEs! (BE  BETTER!)
        bb.translate(0 - currentCorner.x, 0 - currentCorner.y); 
        return bb;
    }

    private void draw(SecStrucElement root) {
        SecStrucElement s;
        for (s = root; s != null; s = s.GetTo()) {
            this.drawSecStruc(s);
            if (s.GetTo() != null)
                this.drawConnection(s);
        }
    }

    private void invertY(SecStrucElement root) {
        Vector conns;
        Enumeration en;
        Point p;
        SecStrucElement s;

        for (s = root; s != null; s = s.GetTo()) {
            s.GetPosition().y *= -1;

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    p.y *= -1;
                }
            }
        }
    }

    public void applyScale(SecStrucElement root, float scale) {

        SecStrucElement s;
        Point p;
        //int x, y;
        int r;
        Vector conns;
        Enumeration en;

        for (s = root; s != null; s = s.GetTo()) {

            p = s.GetPosition();
            p.x = Math.round(scale * p.x);
            p.y = Math.round(scale * p.y);

            r = s.GetSymbolRadius();
            s.SetSymbolRadius(Math.round(r * scale));

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    p.x = Math.round(scale * p.x);
                    p.y = Math.round(scale * p.y);
                }
            }
        }
    }

    private void centerDiagram(SecStrucElement root, int x, int y, int width, int height, Rectangle bounds) {
        Point currentCenter = new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
        Point trueCenter = new Point((x + width) / 2, (y + height) / 2);

        System.err.println("Current center = " + currentCenter);
        System.err.println("True center = " + trueCenter);

        int shiftX = trueCenter.x - currentCenter.x;
        int shiftY = trueCenter.y - currentCenter.y;

        this.translateDiagram(root, shiftX, shiftY);
    }

    private void translateDiagram(SecStrucElement root, int x, int y) {
        System.err.println("translating by " + x + " " + y);
        SecStrucElement s;
        Vector conns;
        Enumeration en;
        Point p;

        for (s = root; s != null; s = s.GetTo()) {
            s.Translate(x, y);

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    p.translate(x, y);
                }
            }

        }
    }

    private Rectangle getBoundingBox(SecStrucElement root) {
        // without the 'int h' parameter, this is the same as
        // root.TopsBoundingBox()?
        // not sure why EPSBoundingBox has to have 'y = h - pos.y'??

        int xmin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE;
        int ymax = Integer.MIN_VALUE;
        int rmax = Integer.MIN_VALUE;

        SecStrucElement s;
        Point pos;
        int rad, x, y;
        for (s = root; s != null; s = s.GetTo()) {
            pos = s.GetPosition();
            x = pos.x;
            y = pos.y;
            rad = s.GetSymbolRadius();
            if (x > xmax) xmax = x;
            if (y > ymax) ymax = y;
            if (x < xmin) xmin = x;
            if (y < ymin) ymin = y;
            if (rad > rmax) rmax = rad;

            if (!(s.GetConnectionTo().isEmpty())) {
                Enumeration ConnectionEnum = s.GetConnectionTo().elements();
                Point PointTo;
                while (ConnectionEnum.hasMoreElements()) {
                    PointTo = (Point) ConnectionEnum.nextElement();
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

    private void drawSecStruc(SecStrucElement ss) {

        int radius = ss.GetSymbolRadius();
        Point pos = ss.GetPosition();
        Color col = ss.getColour();

        if (ss.Type.equals("H")) {
            this.drawHelix(pos.x, pos.y, radius, col);
        } else if (ss.Type.equals("E")) {
            this.drawStrand(pos.x, pos.y, radius, ss.Direction, col);
        } else if ((ss.Type.equals("C")) || (ss.Type.equals("N"))) {
            this.drawTerminus(pos.x, pos.y, radius, ss.Label);
        }

    }

    private void drawTerminus(int x, int y, int r, String label) {
        this.builder.drawTerminus(x, y, r, label); // facade?
    }

    private void drawHelix(int x, int y, int r, Color c) {
        this.builder.drawHelix(x, y, r, c); // facade?
    }

    private void drawStrand(int x, int y, int r, String dir, Color c) {

        double pi6 = Math.PI / 6.0;
        double cospi6 = Math.cos(pi6);
        double sinpi6 = Math.sin(pi6);

        int rsinpi6 = (int) (r * sinpi6);
        int rcospi6 = (int) (r * cospi6);

        int pointX, pointY;
        int leftX, leftY;
        int rightX, rightY;

        if (dir.equals("D")) {
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

        this.builder.drawStrand(pointX, pointY, leftX, leftY, rightX, rightY, c);
    }

    private void drawConnection(SecStrucElement from) {

        if ((from == null))
            return;

        SecStrucElement to = from.GetTo();
        /* Don't connect from a C terminus or to an N terminus */
        if (from.Type.equals("C") || to.Type.equals("N"))
            return;

        int radiusFrom = from.GetSymbolRadius();
        int radiusTo = to.GetSymbolRadius();

        Point pointTo = to.GetPosition();
        Point pointFrom = from.GetPosition();

        /*
         * draw from border rather than centre if direction is down (D) or if
         * Type is N or C
         */
        if (from.Direction.equals("D") || from.Type.equals("C")
                || from.Type.equals("N")) {
            if (from.Type.equals("E")) {
                pointFrom = this.downTriangleBorder(from.GetPosition(), to
                        .GetPosition(), radiusFrom);
            } else {
                pointFrom = this.circleBorder(from.GetPosition(), to.GetPosition(),
                        radiusFrom);
            }
        }

        /*
         * draw to border rather than centre if direction is up (U) or if Type
         * is N or C
         */
        if (to.Direction.equals("U") || to.Type.equals("C")
                || to.Type.equals("N")) {
            if (to.Type.equals("E")) {
                pointTo = this.upTriangleBorder(to.GetPosition(),
                        from.GetPosition(), radiusTo);
            } else {
                pointTo = this.circleBorder(to.GetPosition(), from.GetPosition(),
                        radiusTo);
            }
        }

        if (from.GetConnectionTo().isEmpty()) {
            this.joinPoints(pointTo, pointFrom);
        } else {

            Enumeration connectionEnum = from.GetConnectionTo().elements();
            Point connectionPointTo = (Point) connectionEnum.nextElement();

            this.joinPoints(pointFrom, connectionPointTo);

            Point connectionPointFrom;
            while (connectionEnum.hasMoreElements()) {
                connectionPointFrom = connectionPointTo; // join next to
                                                            // previous
                connectionPointTo = (Point) connectionEnum.nextElement(); // get
                                                                            // next
                this.joinPoints(connectionPointFrom, connectionPointTo); // join!
            }
            connectionPointFrom = connectionPointTo; // get the last in the
                                                        // chain
            this.joinPoints(connectionPointFrom, pointTo); // finally, connect to
                                                        // the next SSE
        }
    }

    private void joinPoints(Point from, Point to) {
        this.builder.connect(from.x, from.y, to.x, to.y);
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
