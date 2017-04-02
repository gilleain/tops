package tops.dw.protein;

import static tops.port.model.SSEType.COIL;
import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import tops.dw.editor.Annotation;
import tops.dw.editor.PostscriptFactory;
import tops.port.model.SSEType;

public class Cartoon {
    
    private List<SecStrucElement> sses;
    
    private List<SecStrucElement> fixed;
    
    private List<Annotation> annotations;  
    
    private boolean startNewConnection = true;
    
    private Font[] fontsArr = new Font[10];
    
    private Font currentFont;

    private Font font12;
    
    public Cartoon(SecStrucElement... elements) {
        this.sses = new ArrayList<SecStrucElement>();
        for (SecStrucElement element : elements) {
            sses.add(element);
        }
        
        this.fixed = new ArrayList<SecStrucElement>();
        
        this.annotations = new ArrayList<Annotation>();
        int fs, i;
        for (fs = 48, i = 0; (fs > 11) && i < this.fontsArr.length; fs -= 4, i++) {
            this.fontsArr[i] = new Font("TimesRoman", Font.PLAIN, fs);
        }
        this.font12 = new Font("TimesRoman", Font.PLAIN, 12);
        this.currentFont = null; 
    }
    
    public Cartoon() {
        // TODO Auto-generated constructor stub
        this(new SecStrucElement[]{}); // XXX ugh
    }

    public void translateFixed(int tx, int ty) {
        for (SecStrucElement s : fixed) {
            s.translate(tx, ty);
        }
    }
    
    public int getFixNumRes() {
        int size = 0;

        for (SecStrucElement t : fixed) {
            size += t.length();
        }

        return size;
    }
    
    public SecStrucElement addSymbol(SSEType type, String direction, int x, int y, SecStrucElement selectedSymbol) {
        int defaultSeparation = 30; // ARBITRARY!
        int defaultRadius = 10; // ARBITRARY!
        
        SecStrucElement newSSE = new SecStrucElement();
        newSSE.setType(type);
        newSSE.setDirection(direction);
        newSSE.placeElement(x, y);
        newSSE.setSymbolRadius(defaultRadius);
        
        if (this.sses.isEmpty()) {
            // make N and C terminii
            SecStrucElement nTerminus = new SecStrucElement();
            nTerminus.setType(NTERMINUS);
            nTerminus.setDirection("U");
            nTerminus.setLabel("N");
            nTerminus.placeElement(x - defaultSeparation, y); // ARBITRARY!
            nTerminus.setSymbolRadius(defaultRadius);

            SecStrucElement cTerminus = new SecStrucElement();
            cTerminus.setType(CTERMINUS);
            cTerminus.setDirection("U");
            cTerminus.setLabel("C");
            cTerminus.placeElement(x + defaultSeparation, y); // ARBITRARY!
            cTerminus.setSymbolRadius(defaultRadius);

        } else {
            // deal with the C-terminus in a special way - add the new SSE
            // _before_ it
            if (selectedSymbol.getType() == CTERMINUS) {
                sses.add(sses.indexOf(selectedSymbol), newSSE);
            } else {
                sses.add(sses.indexOf(selectedSymbol), newSSE);
            }
        }
        
        return newSSE;
    }
    
    public void delete(SecStrucElement SelectedSymbol) {
        // can't delete terminii!
        if (SelectedSymbol.getType() == NTERMINUS || SelectedSymbol.getType() == CTERMINUS) {
            System.err.println("can't delete terminii!");
            return;
        }

        // delete terminii if we delete the final symbol
        if (sses.size() == 3) { // selected symbol plus two terminii
            sses.remove(SelectedSymbol);
            return;
        }
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
        for (SecStrucElement s : sses) {
            Point pos = s.getPosition();
            if (r.contains(pos)) {
                list.add(s);
            }
        }
        return list;
    }
    
    public SecStrucElement selectByPosition(Point p) {
        SecStrucElement selected = null;
    
        if (p != null) {
            double minsep = Double.POSITIVE_INFINITY;
            for (SecStrucElement s : sses) {
                Point ps = s.getPosition();
                double sep = separation(p, ps);
                if (sep < s.getSymbolRadius() && sep < minsep) {
                    minsep = sep;
                    selected = s;
                }
            }
        }
        return selected;
    }
    
    public String convertStructureToString() {
        StringBuffer topsString = new StringBuffer();
        for (SecStrucElement s : sses) {
            char type = s.getType().getOneLetterName().charAt(0);
            type = (s.getDirection().equals("D")) ? 
                    Character.toLowerCase(type) : type;
            topsString.append(type);
        }
        return topsString.toString();
    }

    public void getEPS(int w, int h, Vector<String> EPS) {

        // draw secondary structures
        this.secStrucsEPS(EPS, w, h);

        // draw connections
        this.connectionsEPS(EPS, w, h);
    }
    
    public void highlightByResidueNumber(int[] residueNumbers) {
        int index = 0;
        SecStrucElement last = null;
        for (SecStrucElement s : sses) {
            System.out.println("sse " + s + " trying residue " + residueNumbers[index]);
            while (tryHighlightingByResidueNumber(s, last, residueNumbers[index])) {
                System.out.println("highlighting " + residueNumbers[index]);
                index++;
                if (index >= residueNumbers.length) {
                    return;
                }
            }
            last = s;
        }
    }
    
    public void highlightByResidueNumber(int residueNumber) {
        SecStrucElement last = null;
        for (int index = 0; index < sses.size(); index++) {
            SecStrucElement ss = sses.get(index);
            if (tryHighlightingByResidueNumber(ss, last, residueNumber)) {
                return;
            }
            last = ss;
        }
    }
    
    private boolean tryHighlightingByResidueNumber(SecStrucElement sse, SecStrucElement last, int residueNumber) {
        if (sse.containsResidue(residueNumber)) {
            sse.setColour(Color.YELLOW);
            return true;
        } else {
            int loopStart;
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
    
    private void secStrucsEPS(Vector<String> EPS, int w, int h) {

        if (EPS == null)
            return;

        // draw secondary structures
        Point pos;
        int rad;
        Color c;
        for (SecStrucElement s : sses) {
            pos = s.getPosition();
            rad = s.getSymbolRadius();

            c = s.getColour();

            if (s.getType() == HELIX) {
                EPS = PostscriptFactory.makeCircle(pos.x, h - pos.y, rad, c, EPS);
            } else if (s.getType() == EXTENDED) {
                if (s.getDirection().equals("U")) {
                    EPS = PostscriptFactory.makeUpTriangle(pos.x, h - pos.y, rad, c, EPS);
                } else {
                    EPS = PostscriptFactory.makeDownTriangle(pos.x, h - pos.y, rad, c, EPS);
                }
            } else if (s.getType() == CTERMINUS || s.getType() == NTERMINUS) {
                EPS = PostscriptFactory.makeText(
                        "Times-Roman", (3 * rad) / 4, pos.x, h - pos.y, s.getLabel(), EPS);
            }
        }

    }
    
    public void annotateConnection(SecStrucElement a, SecStrucElement b) {
        Point pA = a.getPosition();
        Point pB = b.getPosition();
        int x = (int)((pA.x / 2.0) + (pB.x / 2.0));
        int y = (int)((pA.y / 2.0) + (pB.y / 2.0));
        Point midPoint = new Point(x, y);
        Annotation annotation = new Annotation(midPoint);
        this.annotations.add(annotation);
        System.out.println("adding annotation " + annotation);
    }
    
    private void connectionsEPS(Vector<String> EPS, int w, int h) {
        for (int index = 0; index < sses.size() - 1; index++) {
            SecStrucElement s = sses.get(index);
            SecStrucElement t = sses.get(index + 1);
            this.drawConnection(s, t, EPS);
            EPS.addElement(PostscriptFactory.stroke());
        }
    }

    public void paint(Graphics g) {
//      System.out.println("painting...");
        
        g.setColor(Color.BLACK);
        for (SecStrucElement ss : sses) {
            this.drawSecStruc(ss, g);
        }

        for (int index = 0; index < sses.size() - 1; index++) {
            SecStrucElement s = sses.get(index);
            SecStrucElement t = sses.get(index + 1);
            this.drawConnection(s, t, g);
        }
        
        g.setColor(Color.RED);
        for (int i = 0; i < this.annotations.size(); i++) {
            Annotation annotation = (Annotation) this.annotations.get(i);
            annotation.draw(g);
        }
        g.setColor(Color.BLACK);
    }

    public Rectangle epsBoundingBox(int h) {

        int xmin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE;
        int ymax = Integer.MIN_VALUE;
        int rmax = Integer.MIN_VALUE;

        Point pos;
        int rad, x, y;
        for (SecStrucElement s : sses) {
            pos = s.getPosition();
            x = pos.x;
            y = h - pos.y;
            rad = s.getSymbolRadius();
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

        for (SecStrucElement s : sses) {
            if (!(s.getConnectionTo().isEmpty())) {
                for (Point pointTo : s.getConnectionTo()) {
                    x = pointTo.x;
                    y = h - pointTo.y;
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
    private void drawSecStruc(SecStrucElement ss, Graphics gc) {

        int ScreenR;
        Point ScreenPos;
        Color c = ss.getColour();

        ScreenR = ss.getSymbolRadius();
        ScreenPos = ss.getPosition();

        if (ss.getType() == HELIX) {
            this.drawHelix(ScreenPos.x, ScreenPos.y, ScreenR, c, gc);
        }

        if (ss.getType() == EXTENDED) {
            this.drawStrand(ScreenPos.x, ScreenPos.y, ScreenR, ss.getDirection(), c, gc);
        }

        if (ss.getType() == CTERMINUS || ss.getType() == NTERMINUS)
            this.drawTerminus(ScreenPos.x, ScreenPos.y, ScreenR, ss.getLabel(), c, gc);

    }
    
    /* private method to draw the symbol of an N or C terminus */
    private void drawTerminus(int x, int y, int r, String lab, Color c, Graphics g) {
        
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
                g.setFont(this.fontsArr[0]);
            } else {
                g.setFont(this.currentFont);
            }
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(lab);
            int h = fm.getHeight();

            int i = 1;
            while (i < this.fontsArr.length && (Math.max(w, h) > r)) {
                if (this.fontsArr[i] == null) {
                    break;
                }
                this.currentFont = this.fontsArr[i];
                g.setFont(this.fontsArr[i]);
                fm = g.getFontMetrics();
                w = fm.stringWidth(lab);
                i++;
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            g.setFont(this.font12);
        }
        return g.getFontMetrics();
    }
    
    
    /*
     * private method to draw the connection between two secondary structure
     * elements
     */
    private void drawConnection(SecStrucElement s, SecStrucElement t, Object GraphicsOutput) {

        int FromScreenR, ToScreenR;

        if ((GraphicsOutput == null) || (s == null))
            return;

        /* Don't connect from a C terminus or to an N terminus */
        if (s.getType() == CTERMINUS || t.getType() == NTERMINUS)
            return;

        FromScreenR = s.getSymbolRadius();
        ToScreenR = t.getSymbolRadius();

        /*
         * in the case of no intervening connection points just join between the
         * two symbols
         */
        if (s.getConnectionTo().isEmpty()) {
            this.joinPoints(s.getPosition(), s.getDirection(), s.getType(), FromScreenR, 
                    t.getPosition(), t.getDirection(), t.getType(), ToScreenR,
                    GraphicsOutput);
        }
        /* the case where there are some intervening connection points */
        else {

            Iterator<Point> connections = s.getConnectionTo().iterator();
            Point pointTo = connections.next();

            this.joinPoints(s.getPosition(), s.getDirection(), s.getType(), FromScreenR,
                    pointTo, "*", COIL, 0, GraphicsOutput);

            Point pointFrom;
            while (connections.hasNext()) {
                pointFrom = pointTo;
                pointTo = connections.next();
                this.joinPoints(pointFrom, "*", COIL, 0, pointTo, "*", COIL, 0,
                        GraphicsOutput);
            }

            pointFrom = pointTo;
            this.joinPoints(pointFrom, "*", COIL, 0, t.getPosition(), t.getDirection(),
                    t.getType(), ToScreenR, GraphicsOutput);

        }

    }
    

    
    /*
     * lines go from and the centre of the symbols except in certain cases when
     * they are drawn to/from the boundary
     */
    private void joinPoints(Point p1, String Dir1, SSEType sseType, int Radius1,
            Point p2, String Dir2, SSEType sseType2, int Radius2,
            Object GraphicsOutput) {

        Point To, From;

        /*
         * draw from border rather than centre if direction is down (D) or if
         * Type is N or C
         */
        if (Dir1.equals("D") || sseType.equals("C") || sseType.equals("N")) {
            if (sseType.equals("E")) {
                From = this.downTriangleBorder(p1, p2, Radius1);
            } else {
                From = this.circleBorder(p1, p2, Radius1);
            }
        } else {
            From = p1;
        }

        /*
         * draw to border rather than centre if direction is up (U) or if Type
         * is N or C
         */
        if (Dir2.equals("U") || sseType2.equals("C") || sseType2.equals("N")) {
            if (sseType2.equals("E")) {
                To = this.upTriangleBorder(p2, p1, Radius2);
            } else {
                To = this.circleBorder(p2, p1, Radius2);
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
            if (this.startNewConnection) {
                ps.addElement(PostscriptFactory.makeMove(From.x, 0 - From.y));  //FIXME
                this.startNewConnection = false;
            }
            ps.addElement(PostscriptFactory.makeLine(To.x, 0 - To.y)); //FIXME
        }

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
        return separation(p, sse.getPosition());
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
    private Point circleBorder(Point p1, Point p2, int r) {

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
    
    public SecStrucElement getSSEByNumber(int num) {
        return sses.get(num);
    }
    

    /* private method to apply a scaling value to the diagram */
    public void applyScale(float scale) {
        Point p;
        int x, y, r;
        
        for (SecStrucElement s : sses) {

            p = s.getPosition();
            x = p.x;
            y = p.y;
            x = Math.round(scale * x);
            y = Math.round(scale * y);
            p.x = x;
            p.y = y;

            r = s.getSymbolRadius();
            s.setSymbolRadius(Math.round(r * scale));
            for (Point pc : s.getConnectionTo()) {
                x = pc.x;
                y = pc.y;
                x = Math.round(scale * x);
                y = Math.round(scale * y);
                pc.x = x;
                pc.y = y;
            }

        }

    }
    
    /**
     * this methods applies the inverse of the scale to the diagram
     */
    public void invertScale(float scale) {
        if (scale > 0.0)
            scale = 1.0F / scale;
        else
            scale = 1.0F;
        this.applyScale(scale);
    }
    
    public void invertY() {
        for (SecStrucElement s : sses) {
            s.getPosition().y *= -1;
            for (Point p : s.getConnectionTo()) {
                p.y *= -1;
            }
        }
    }

    /* private method to draw a helix symbol (circle) */
    private void drawHelix(int x, int y, int r, Color c, Graphics g) {

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
    private void drawStrand(int x, int y, int r, String dir, Color c, Graphics g) {

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
    

    public void reflectXY() {
        for (SecStrucElement s : sses) {
            if (s.getDirection().equals("D")) {
                s.setDirection("U");
            } else if (s.getDirection().equals("U")) {
                s.setDirection("D");
            }
        }
    }

    public void reflectZX() {
        int y;
        for (SecStrucElement s : sses) {
            Point p = s.getPosition();
            y = p.y * (-1);
            p.y = y;
            for (Point pc : s.getConnectionTo()) {
                y = pc.y * (-1);
                pc.y = y;
            }
        }
    }

    public void reflectYZ() {
        int x;
        for (SecStrucElement ss : sses) {
            Point p = ss.getPosition();
            x = p.x * (-1);
            p.x = x;
            for (Point pc : ss.getConnectionTo()) {
                x = pc.x * (-1);
                pc.x = x;
            }
        }
    }

    public void rotateX() {
        this.reflectZX();
        this.reflectXY();
    }

    public void rotateY() {
        this.reflectYZ();
        this.reflectXY();
    }

    public void rotateZ() {
        this.reflectYZ();
        this.reflectZX();
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
        Rectangle bb = this.topsBoundingBox();
        float s1 = (float)(w - (2 * b)) / (float)(bb.width);
        float s2 = (float)(h - (2 * b)) / (float)(bb.height);
        float s = Math.min(s1, s2);
        if (s > 1.0F) { s = 1.0F; }
        System.out.print(s);
        this.applyScale(s);
        Point centroid = this.topsCentroid();
        int dy = -centroid.y + (h / 2);
        int dx = -centroid.x + (w / 2);
        this.translateDiagram(dx, dy);
    }
    
    public Rectangle topsBoundingBox() {
        int minx = 0;
        int maxx = 0;
        int miny = 0;
        int maxy = 0;
        int maxr = 0;

        for (SecStrucElement ss : sses) {

            int x = ss.getPosition().x;
            int y = ss.getPosition().y;
            int r = ss.getSymbolRadius();

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
    
    public Point topsCentroid() {
        int n = 1;
        int centx = 0;
        int centy = 0;
        for (SecStrucElement ss : sses) {
            n++;
            centx += ss.getPosition().x;
            centy += ss.getPosition().y;
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
    public void translateDiagram(int x, int y) {
        for (SecStrucElement s : sses) {
            s.getPosition().x += x;
            s.getPosition().y += y;

            for (Point p : s.getConnectionTo()) {
                p.x += x;
                p.y += y;
            }
        }
    }

    public List<SecStrucElement> getSSEs() {
        return sses;
    }

    public void addSSE(SecStrucElement s) {
        sses.add(s);
    }
}
