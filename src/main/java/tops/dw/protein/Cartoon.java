package tops.dw.protein;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import tops.dw.editor.Annotation;
import tops.dw.editor.PostscriptFactory;

public class Cartoon {
    
    private SecStrucElement root;   // XXX TODO : refactor!
    
    private List<Annotation> annotations;  
    
    private boolean start_new_connection = true;
    
    private Font[] FontsArr = new Font[10];
    
    private Font currentFont;

    private Font Font12;
    
    public Cartoon(SecStrucElement root) {
        this.root = root;
        
        this.annotations = new ArrayList<Annotation>();
        int fs, i;
        for (fs = 48, i = 0; (fs > 11) && i < this.FontsArr.length; fs -= 4, i++) {
            this.FontsArr[i] = new Font("TimesRoman", Font.PLAIN, fs);
        }
        this.Font12 = new Font("TimesRoman", Font.PLAIN, 12);
        this.currentFont = null; 
    }
    
    public Cartoon() {
        // TODO Auto-generated constructor stub
        this(null); // XXX ugh
    }

    public void flipMultiple(List<SecStrucElement> list) {
        for (SecStrucElement s : list) {
            this.flip(s);
        }
    }

    public void flip(SecStrucElement s) {
        if (s.getDirection().equals("U")) {
            s.setDirection("D");
        } else if (s.getDirection().equals("D")) {
            s.setDirection("U");
        }
    }
    
    public List<SecStrucElement> selectContained(Rectangle r) {
        List<SecStrucElement> list = new ArrayList<SecStrucElement>();
        for (SecStrucElement s = this.root; s != null; s = s.GetTo()) {
            Point pos = s.GetPosition();
            if (r.contains(pos)) {
                list.add(s);
            }
        }
        return list;
    }
    
    public SecStrucElement SelectByPosition(Point p) {
        SecStrucElement selected = null;
    
        if (p != null) {
    
            SecStrucElement s;
            double minsep = Double.POSITIVE_INFINITY;
            double sep;
            Point ps;
    
            for (s = this.root; s != null; s = s.GetTo()) {
                ps = s.GetPosition();
                sep = separation(p, ps);
                if ((sep < s.GetSymbolRadius()) && (sep < minsep)) {
                    minsep = sep;
                    selected = s;
                }
            }
        }
        return selected;
    }
    
    public String convertStructureToString() {
        if (this.root == null)
            return new String();

        StringBuffer topsString = new StringBuffer();
        for (SecStrucElement s = this.root; s != null; s = s.GetTo()) {
            char type = s.getType().charAt(0);
            type = (s.getDirection().equals("D")) ? Character.toLowerCase(type)
                    : type;
            topsString.append(type);
        }
        return topsString.toString();
    }
    
    public SecStrucElement getRoot() {
        return root;
    }

    public void getEPS(int w, int h, Vector<String> EPS) {

        // draw secondary structures
        this.SecStrucsEPS(EPS, w, h);

        // draw connections
        this.ConnectionsEPS(EPS, w, h);
    }
    
    public void highlightByResidueNumber(int[] residueNumbers) {
        int index = 0;
        for (SecStrucElement s = root; s != null; s = s.GetTo()) {
            System.out.println("sse " + s + " trying residue " + residueNumbers[index]);
            while (tryHighlightingByResidueNumber(root, residueNumbers[index])) {
                System.out.println("highlighting " + residueNumbers[index]);
                index++;
                if (index >= residueNumbers.length) {
                    return;
                }
            }
        }
    }
    
    public void highlightByResidueNumber(int residueNumber) {
        for (SecStrucElement s = root; s != null; s = s.GetTo()) {
            if (tryHighlightingByResidueNumber(root, residueNumber)) {
                return;
            }
        }
    }
    
    private boolean tryHighlightingByResidueNumber(SecStrucElement sse, int residueNumber) {
        if (sse.containsResidue(residueNumber)) {
            sse.setColour(Color.YELLOW);
            return true;
        } else {
            int loopStart;
            SecStrucElement last = sse.GetFrom();
            if (last == null) {
                loopStart = 0;
            } else {
                loopStart = last.getPDBFinishResidue();
            }
            
            if (loopStart <= residueNumber && residueNumber <= sse.getPDBStartResidue()) {
                annotateConnection(last, sse);    // FIXME!
                return true;
            }
        }
        return false;
    }
    
    private void SecStrucsEPS(Vector<String> EPS, int w, int h) {

        if (EPS == null)
            return;

        // draw secondary structures
        SecStrucElement s;
        Point pos;
        int rad;
        Color c;
        for (s = root; s != null; s = s.GetTo()) {
            pos = s.GetPosition();
            rad = s.GetSymbolRadius();

            c = s.getColour();

            if (s.getType().equals("H")) {
                EPS = PostscriptFactory.makeCircle(pos.x, h - pos.y, rad, c, EPS);
            } else if (s.getType().equals("E")) {
                if (s.getDirection().equals("U")) {
                    EPS = PostscriptFactory.makeUpTriangle(pos.x, h - pos.y, rad, c, EPS);
                } else {
                    EPS = PostscriptFactory.makeDownTriangle(pos.x, h - pos.y, rad, c, EPS);
                }
            } else if (s.getType().equals("C") || s.getType().equals("N")) {
                EPS = PostscriptFactory.makeText(
                        "Times-Roman", (3 * rad) / 4, pos.x, h - pos.y, s.getLabel(), EPS);
            }
        }

    }
    
    public void annotateConnection(SecStrucElement a, SecStrucElement b) {
        Point pA = a.GetPosition();
        Point pB = b.GetPosition();
        int x = (int)((pA.x / 2.0) + (pB.x / 2.0));
        int y = (int)((pA.y / 2.0) + (pB.y / 2.0));
        Point midPoint = new Point(x, y);
        Annotation annotation = new Annotation(midPoint);
        this.annotations.add(annotation);
        System.out.println("adding annotation " + annotation);
    }
    
    private void ConnectionsEPS(Vector<String> EPS, int w, int h) {
        SecStrucElement s;
        for (s = root; (s != null) && (s.GetTo() != null); s = s.GetTo()) {
            this.start_new_connection = true;
            this.DrawConnection(s, EPS);
            EPS.addElement(PostscriptFactory.stroke());
        }
    }

    public void paint(Graphics g) {
//      System.out.println("painting...");
        
        g.setColor(Color.BLACK);
        for (SecStrucElement s = root; s != null; s = s.GetTo()) {
            this.DrawSecStruc(s, g);
        }

        for (SecStrucElement s = root; (s != null) && (s.GetTo() != null); s = s.GetTo()) {
            this.DrawConnection(s, g);
        }
        
        g.setColor(Color.RED);
        for (int i = 0; i < this.annotations.size(); i++) {
            Annotation annotation = (Annotation) this.annotations.get(i);
            annotation.draw(g);
        }
        g.setColor(Color.BLACK);
    }

    public Rectangle EPSBoundingBox(int h) {

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
            y = h - pos.y;
            rad = s.GetSymbolRadius();
            if (x > xmax)
                xmax = x;
            if (y > ymax)
                ymax = y;
            if (x < xmin)
                xmin = x;
            if (y < ymin)
                ymin = y;
            if (rad > rmax)
                rmax = rad;
        }
        xmax += rmax;
        ymax += rmax;
        xmin -= rmax;
        ymin -= rmax;

        for (s = root; s != null; s = s.GetTo()) {
            if (!(s.GetConnectionTo().isEmpty())) {
                Enumeration<Point> ConnectionEnum = s.GetConnectionTo().elements();
                Point PointTo;
                while (ConnectionEnum.hasMoreElements()) {
                    PointTo = (Point) ConnectionEnum.nextElement();
                    x = PointTo.x;
                    y = h - PointTo.y;
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

        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }
    

    /* private method to draw a secondary structure */
    private void DrawSecStruc(SecStrucElement ss, Graphics gc) {

        int ScreenR;
        Point ScreenPos;
        Color c = ss.getColour();

        ScreenR = ss.GetSymbolRadius();
        ScreenPos = ss.GetPosition();

        if (ss.getType().equals("H")) {
            this.DrawHelix(ScreenPos.x, ScreenPos.y, ScreenR, c, gc);
        }

        if (ss.getType().equals("E")) {
            this.DrawStrand(ScreenPos.x, ScreenPos.y, ScreenR, ss.getDirection(), c, gc);
        }

        if ((ss.getType().equals("C")) || (ss.getType().equals("N")))
            this.DrawTerminus(ScreenPos.x, ScreenPos.y, ScreenR, ss.getLabel(), c, gc);

    }
    
    /* private method to draw the symbol of an N or C terminus */
    private void DrawTerminus(int x, int y, int r, String lab, Color c, Graphics g) {
        
        if (lab == null) { return; }
        
        FontMetrics fm = this.setFontSize(lab, r, g);
        
        int fontHeight = fm.getHeight();
        int stringWidth = fm.stringWidth(lab);
        
        x -= stringWidth / 2;
        y += fontHeight / 2;

        g.setColor(Color.BLACK);
        g.drawString(lab, x, y);
    }
    
    private FontMetrics setFontSize(String lab, int r, Graphics g) {
        try {
            if (this.currentFont == null) {
                g.setFont(this.FontsArr[0]);
            } else {
                g.setFont(this.currentFont);
            }
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(lab);
            int h = fm.getHeight();

            int i = 1;
            while (i < this.FontsArr.length && (Math.max(w, h) > r)) {
                if (this.FontsArr[i] == null) {
                    break;
                }
                this.currentFont = this.FontsArr[i];
                g.setFont(this.FontsArr[i]);
                fm = g.getFontMetrics();
                w = fm.stringWidth(lab);
                i++;
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            g.setFont(this.Font12);
        }
        return g.getFontMetrics();
    }
    
    
    /*
     * private method to draw the connection between two secondary structure
     * elements
     */
    private void DrawConnection(SecStrucElement s, Object GraphicsOutput) {

        SecStrucElement To;
        int FromScreenR, ToScreenR;

        if ((GraphicsOutput == null) || (s == null))
            return;

        To = s.GetTo();
        if (To == null)
            return;

        /* Don't connect from a C terminus or to an N terminus */
        if (s.getType().equals("C") || To.getType().equals("N"))
            return;

        FromScreenR = s.GetSymbolRadius();
        ToScreenR = To.GetSymbolRadius();

        /*
         * in the case of no intervening connection points just join between the
         * two symbols
         */
        if (s.GetConnectionTo().isEmpty()) {
            this.JoinPoints(s.GetPosition(), s.getDirection(), s.getType(), FromScreenR, To
                    .GetPosition(), To.getDirection(), To.getType(), ToScreenR,
                    GraphicsOutput);
        }
        /* the case where there are some intervening connection points */
        else {

            Enumeration<Point> ConnectionEnum = s.GetConnectionTo().elements();
            Point PointTo = (Point) ConnectionEnum.nextElement();

            this.JoinPoints(s.GetPosition(), s.getDirection(), s.getType(), FromScreenR,
                    PointTo, "*", "*", 0, GraphicsOutput);

            Point PointFrom;
            while (ConnectionEnum.hasMoreElements()) {
                PointFrom = PointTo;
                PointTo = (Point) ConnectionEnum.nextElement();
                this.JoinPoints(PointFrom, "*", "*", 0, PointTo, "*", "*", 0,
                        GraphicsOutput);
            }

            PointFrom = PointTo;
            this.JoinPoints(PointFrom, "*", "*", 0, To.GetPosition(), To.getDirection(),
                    To.getType(), ToScreenR, GraphicsOutput);

        }

    }
    

    
    /*
     * lines go from and the centre of the symbols except in certain cases when
     * they are drawn to/from the boundary
     */
    private void JoinPoints(Point p1, String Dir1, String Type1, int Radius1,
            Point p2, String Dir2, String Type2, int Radius2,
            Object GraphicsOutput) {

        Point To, From;

        /*
         * draw from border rather than centre if direction is down (D) or if
         * Type is N or C
         */
        if (Dir1.equals("D") || Type1.equals("C") || Type1.equals("N")) {
            if (Type1.equals("E")) {
                From = this.DownTriangleBorder(p1, p2, Radius1);
            } else {
                From = this.CircleBorder(p1, p2, Radius1);
            }
        } else {
            From = p1;
        }

        /*
         * draw to border rather than centre if direction is up (U) or if Type
         * is N or C
         */
        if (Dir2.equals("U") || Type2.equals("C") || Type2.equals("N")) {
            if (Type2.equals("E")) {
                To = this.UpTriangleBorder(p2, p1, Radius2);
            } else {
                To = this.CircleBorder(p2, p1, Radius2);
            }
        } else {
            To = p2;
        }

        // at this point the GraphicsOutput is either a Graphics
        // or a Vector to which postscript should be written
        if (GraphicsOutput instanceof Graphics) {
            Graphics gc = (Graphics) GraphicsOutput;
            gc.drawLine(From.x, From.y, To.x, To.y);
        } else if (GraphicsOutput instanceof Vector) {
            @SuppressWarnings("unchecked")
            Vector<String> ps = (Vector<String>) GraphicsOutput;
            if (this.start_new_connection) {
                ps.addElement(PostscriptFactory.makeMove(From.x, 0 - From.y));  //FIXME
                this.start_new_connection = false;
            }
            ps.addElement(PostscriptFactory.makeLine(To.x, 0 - To.y)); //FIXME
        }

    }
    
    /*
     * private method calculates the point on the border of an up equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point UpTriangleBorder(Point p1, Point p2, int r) {

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

        double s = separation(p1, p2);

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
    
    public double separation(SecStrucElement sse, Point p) {
        return separation(p, sse.GetPosition());
    }
    
    /* private method calculates the separation of two points */
    /* one day I'll put it in a more sensible place for re-use */
    /* ...and one day, it has! gmt 13/05/08 */
    private double separation(Point p1, Point p2) {
        int sep = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
        return Math.sqrt(sep);
    }
    
    /*
     * private method calculates the point on a circle of centre p1 and radius r
     * which lies on the line p1->p2
     */
    private Point CircleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = this.separation(p1, p2);

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
    private Point DownTriangleBorder(Point p1, Point p2, int r) {

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

        double s = this.separation(p1, p2);

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
    

    /* private method to apply a scaling value to the diagram */
    public void ApplyScale(float scale) {

        SecStrucElement s;
        Point p;
        int x, y, r;
        Vector<Point> conns;
        Enumeration<Point> en;

        for (s = this.root; s != null; s = s.GetTo()) {

            p = s.GetPosition();
            x = p.x;
            y = p.y;
            x = Math.round(scale * x);
            y = Math.round(scale * y);
            p.x = x;
            p.y = y;

            r = s.GetSymbolRadius();
            s.SetSymbolRadius(Math.round(r * scale));

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = en.nextElement();
                    x = p.x;
                    y = p.y;
                    x = Math.round(scale * x);
                    y = Math.round(scale * y);
                    p.x = x;
                    p.y = y;
                }
            }

        }

    }
    
    /**
     * this methods applies the inverse of the scale to the diagram
     */
    public void InvertScale(float scale) {
        if (scale > 0.0)
            scale = 1.0F / scale;
        else
            scale = 1.0F;
        this.ApplyScale(scale);
    }
    
    public void InvertY() {
        Vector<Point> conns;
        Enumeration<Point> en;
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

    /* private method to draw a helix symbol (circle) */
    private void DrawHelix(int x, int y, int r, Color c, Graphics g) {

        int d = 2 * r;
        int ex = x - r;
        int ey = y - r;

        if (c == null) {
            c = Color.BLACK;
        }

        g.setColor(c);
        g.fillOval(ex, ey, d, d);
        g.setColor(Color.black);
        g.drawOval(ex, ey, d, d);

    }

    /*
     * private method to draw a strand symbol (triangle, pointing up or down
     * according to strand direction )
     */
    private void DrawStrand(int x, int y, int r, String dir, Color c, Graphics g) {

        Polygon triangle = new Polygon();

        double pi6 = Math.PI / 6.0;
        double cospi6 = Math.cos(pi6);
        double sinpi6 = Math.sin(pi6);

        int rsinpi6 = (int) (r * sinpi6);
        int rcospi6 = (int) (r * cospi6);

        if (dir.equals("D")) {
            triangle.addPoint(x, y + r);
            triangle.addPoint(x - rcospi6, y - rsinpi6);
            triangle.addPoint(x + rcospi6, y - rsinpi6);
        } else {
            triangle.addPoint(x, y - r);
            triangle.addPoint(x - rcospi6, y + rsinpi6);
            triangle.addPoint(x + rcospi6, y + rsinpi6);
        }

        if (c == null) {
            c = Color.BLACK;
        }

        g.setColor(c);
        g.fillPolygon(triangle);
        g.setColor(Color.black);
        g.drawPolygon(triangle);

    }
    

    public void ReflectXY() {

        SecStrucElement s;

        for (s = root; s != null; s = s.GetTo()) {
            if (s.getDirection().equals("D")) {
                s.setDirection("U");
            } else if (s.getDirection().equals("U")) {
                s.setDirection("D");
            }
        }
    }

    public void ReflectZX() {

        SecStrucElement s;
        Point p;
        int y;
        Enumeration<Point> en;

        for (s = root; s != null; s = s.GetTo()) {
            p = s.GetPosition();
            y = p.y * (-1);
            p.y = y;
            if (s.GetConnectionTo() != null) {
                en = s.GetConnectionTo().elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    y = p.y * (-1);
                    p.y = y;
                }
            }
        }
    }

    public void ReflectYZ() {

        SecStrucElement s;
        Point p;
        int x;
        Enumeration<Point> en;

        for (s = root; s != null; s = s.GetTo()) {
            p = s.GetPosition();
            x = p.x * (-1);
            p.x = x;
            if (s.GetConnectionTo() != null) {
                en = s.GetConnectionTo().elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    x = p.x * (-1);
                    p.x = x;
                }
            }
        }
    }

    public void RotateX() {
        this.ReflectZX();
        this.ReflectXY();
    }

    public void RotateY() {
        this.ReflectYZ();
        this.ReflectXY();
    }

    public void RotateZ() {
        this.ReflectYZ();
        this.ReflectZX();
    }
    
    /**
     * Translate and scale the diagram to fit within a rectangle.
     * 
     * @param x the upper left x
     * @param y the upper left y
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @param b the border
     */
    public void fitToRectangle(int x, int y, int w, int h, int b) {
        try {
            Rectangle bb = this.TopsBoundingBox();
            float s1 = (float)(w - (2 * b)) / (float)(bb.width);
            float s2 = (float)(h - (2 * b)) / (float)(bb.height);
            float s = Math.min(s1, s2);
            if (s > 1.0F) { s = 1.0F; }
            System.out.print(s);
            this.ApplyScale(s);
            Point centroid = this.TopsCentroid();
            int dy = -centroid.y + (h / 2);
            int dx = -centroid.x + (w / 2);
            this.TranslateDiagram(dx, dy);
        } catch (TopsLinkedListException tlle) {
            
        }
    }
    
    public Rectangle TopsBoundingBox() throws TopsLinkedListException {

        int x, y, minx, maxx, miny, maxy, r, maxr;

        SecStrucElement ss;

        if (root.GetPosition() == null)
            root.SetPosition(new Point(0, 0));

        x = root.GetPosition().x;
        y = root.GetPosition().y;

        minx = x;
        maxx = x;
        miny = y;
        maxy = y;
        maxr = root.GetSymbolRadius();

        for (ss = root.To; ss != null; ss = ss.To) {

            x = ss.GetPosition().x;
            y = ss.GetPosition().y;
            r = ss.GetSymbolRadius();

            if (x < minx)
                minx = x;
            if (x > maxx)
                maxx = x;
            if (y < miny)
                miny = y;
            if (y > maxy)
                maxy = y;

            if (r > maxr)
                maxr = r;

        }

        return new Rectangle(minx - maxr, miny - maxr, maxx - minx + 2 * maxr,
                maxy - miny + 2 * maxr);
    }
    
    public Point TopsCentroid() {
        
        int centx, centy, n;
        SecStrucElement ss;

        n = 1;
        centx = root.GetPosition().x;
        centy = root.GetPosition().y;

        for (ss = root.To; ss != null; ss = ss.To) {
            n++;
            centx += ss.GetPosition().x;
            centy += ss.GetPosition().y;
        }

        centx = (int) (((float) centx) / ((float) n));
        centy = (int) (((float) centy) / ((float) n));

        return new Point(centx, centy);
    }
    
    
    /**
     * this method translates the diagram, it does not do a repaint
     * 
     * @param x -
     *            the x translation to apply
     * @param y -
     *            the y translation to apply
     */
    public synchronized void TranslateDiagram(int x, int y) {
        SecStrucElement s;
        Vector<Point> conns;
        Enumeration<Point> en;
        Point p;

        for (s = root; s != null; s = s.GetTo()) {
            s.GetPosition().x += x;
            s.GetPosition().y += y;

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    p.x += x;
                    p.y += y;
                }
            }
        }
    }
    

}
