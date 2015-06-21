package tops.dw.protein;

import java.util.*;
import java.io.*;
import java.awt.*;

import tops.dw.editor.Annotation;
import tops.dw.editor.PostscriptFactory;


public class SecStrucElement {

    private int SymbolNumber;

    private String Type;

    private String Direction;

    private int PDBStartResidue;

    private int PDBFinishResidue;

    private String Chain;

    private String Label;

    private Color Colour;

    private SecStrucElement From = null, To = null;

    private SecStrucElement Fixed = null;

    private int FixedIndex;

    private SecStrucElement Next = null;

    private int NextIndex;

    private Point Position;

    private int SymbolRadius;

    private Vector<Point> ConnectionTo;

	private String FixedType;

    private Vector<Integer> BridgePartner;

    private Vector<String> BridgePartnerSide;

    private Vector<String> BridgePartnerType;

    private Vector<Integer> Neighbour;

    private int SeqStartResidue, SeqFinishResidue;

    private int Chirality;

    private float AxesStartPoint[] = new float[3];

    private float AxesFinishPoint[] = new float[3];

    private float AxisLength;

    private int Fill;

    private Font[] FontsArr = new Font[10];
    
    private Font currentFont;

    private Font Font12;
    
    private boolean start_new_connection = true;
    
    private ArrayList<Annotation> annotations;	// TODO : put this in the SSE container class

    public SecStrucElement() {
        this.Position = new Point(0, 0);
        this.ConnectionTo = new Vector<Point>();
        this.BridgePartner = new Vector<Integer>();
        this.BridgePartnerSide = new Vector<String>();
        this.BridgePartnerType = new Vector<String>();
        this.Neighbour = new Vector<Integer>();
        this.Colour = null;
        
        int fs, i;
        for (fs = 48, i = 0; (fs > 11) && i < this.FontsArr.length; fs -= 4, i++) {
            this.FontsArr[i] = new Font("TimesRoman", Font.PLAIN, fs);
        }
        this.Font12 = new Font("TimesRoman", Font.PLAIN, 12);
        this.currentFont = null; 

        this.annotations = new ArrayList<Annotation>();
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
    
    public void highlightByResidueNumber(int[] residueNumbers) {
    	int index = 0;
    	for (SecStrucElement s = this; s != null; s = s.GetTo()) {
    		System.out.println("sse " + s + " trying residue " + residueNumbers[index]);
    		while (s.tryHighlightingByResidueNumber(this, residueNumbers[index])) {
    			System.out.println("highlighting " + residueNumbers[index]);
    			index++;
    			if (index >= residueNumbers.length) {
        			return;
        		}
    		}
    	}
    }
    
    public void highlightByResidueNumber(int residueNumber) {
    	for (SecStrucElement s = this; s != null; s = s.GetTo()) {
    		if (s.tryHighlightingByResidueNumber(this, residueNumber)) {
    			return;
    		}
    	}
    }
    
    private boolean tryHighlightingByResidueNumber(SecStrucElement root, int residueNumber) {
    	if (this.containsResidue(residueNumber)) {
			this.setColour(Color.YELLOW);
			return true;
		} else {
			int loopStart;
			SecStrucElement last = this.GetFrom();
			if (last == null) {
				loopStart = 0;
			} else {
				loopStart = last.PDBFinishResidue;
			}
			
			if (loopStart <= residueNumber && residueNumber <= this.PDBStartResidue) {
				root.annotateConnection(last, this);	// FIXME!
				return true;
			}
		}
    	return false;
    }
    
    public boolean containsResidue(int residueNumber) {
    	return this.PDBStartResidue <= residueNumber && this.PDBFinishResidue >= residueNumber;
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
    
    public void getEPS(int w, int h, Vector<String> EPS) {

        // draw secondary structures
        this.SecStrucsEPS(EPS, w, h);

        // draw connections
        this.ConnectionsEPS(EPS, w, h);
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
        for (s = this; s != null; s = s.GetTo()) {
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

        for (s = this; s != null; s = s.GetTo()) {
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

    

    private void ConnectionsEPS(Vector<String> EPS, int w, int h) {
        SecStrucElement s;
        for (s = this; (s != null) && (s.GetTo() != null); s = s.GetTo()) {
            this.start_new_connection = true;
            this.DrawConnection(s, EPS);
            EPS.addElement(PostscriptFactory.stroke());
        }
    }

    private void SecStrucsEPS(Vector<String> EPS, int w, int h) {

        if (EPS == null)
            return;

        // draw secondary structures
        SecStrucElement s;
        Point pos;
        int rad;
        Color c;
        for (s = this; s != null; s = s.GetTo()) {
            pos = s.GetPosition();
            rad = s.GetSymbolRadius();

            c = s.getColour();

            if (s.Type.equals("H")) {
                EPS = PostscriptFactory.makeCircle(pos.x, h - pos.y, rad, c, EPS);
            } else if (s.Type.equals("E")) {
                if (s.Direction.equals("U")) {
                    EPS = PostscriptFactory.makeUpTriangle(pos.x, h - pos.y, rad, c, EPS);
                } else {
                    EPS = PostscriptFactory.makeDownTriangle(pos.x, h - pos.y, rad, c, EPS);
                }
            } else if (s.Type.equals("C") || s.Type.equals("N")) {
                EPS = PostscriptFactory.makeText(
                		"Times-Roman", (3 * rad) / 4, pos.x, h - pos.y, s.Label, EPS);
            }
        }

    }
    
    public void paint(Graphics g) {
//    	System.out.println("painting...");
    	
    	g.setColor(Color.BLACK);
        for (SecStrucElement s = this; s != null; s = s.GetTo()) {
            this.DrawSecStruc(s, g);
        }

        for (SecStrucElement s = this; (s != null) && (s.GetTo() != null); s = s.GetTo()) {
            this.DrawConnection(s, g);
        }
        
        g.setColor(Color.RED);
        for (int i = 0; i < this.annotations.size(); i++) {
        	Annotation annotation = (Annotation) this.annotations.get(i);
        	annotation.draw(g);
        }
        g.setColor(Color.BLACK);
        
    }
    
    /* private method to draw a secondary structure */
    private void DrawSecStruc(SecStrucElement ss, Graphics gc) {

        int ScreenR;
        Point ScreenPos;
        Color c = ss.getColour();

        ScreenR = ss.GetSymbolRadius();
        ScreenPos = ss.GetPosition();

        if (ss.Type.equals("H")) {
            this.DrawHelix(ScreenPos.x, ScreenPos.y, ScreenR, c, gc);
        }

        if (ss.Type.equals("E")) {
            this.DrawStrand(ScreenPos.x, ScreenPos.y, ScreenR, ss.Direction, c, gc);
        }

        if ((ss.Type.equals("C")) || (ss.Type.equals("N")))
            this.DrawTerminus(ScreenPos.x, ScreenPos.y, ScreenR, ss.Label, c, gc);

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

    /* private method to draw the symbol of an N or C terminus */
    private void DrawTerminus(int x, int y, int r, String lab, Color c,
            Graphics g) {
    	
    	if (lab == null) { return; }
    	
    	FontMetrics fm = this.setFontSize(lab, r, g);
        
        int fontHeight = fm.getHeight();
        int stringWidth = fm.stringWidth(lab);
        
        x -= stringWidth / 2;
        y += fontHeight / 2;

        g.setColor(Color.BLACK);
        g.drawString(lab, x, y);
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
        if (s.Type.equals("C") || To.Type.equals("N"))
            return;

        FromScreenR = s.GetSymbolRadius();
        ToScreenR = To.GetSymbolRadius();

        /*
         * in the case of no intervening connection points just join between the
         * two symbols
         */
        if (s.GetConnectionTo().isEmpty()) {
            this.JoinPoints(s.GetPosition(), s.Direction, s.Type, FromScreenR, To
                    .GetPosition(), To.Direction, To.Type, ToScreenR,
                    GraphicsOutput);
        }
        /* the case where there are some intervening connection points */
        else {

            Enumeration<Point> ConnectionEnum = s.GetConnectionTo().elements();
            Point PointTo = (Point) ConnectionEnum.nextElement();

            this.JoinPoints(s.GetPosition(), s.Direction, s.Type, FromScreenR,
                    PointTo, "*", "*", 0, GraphicsOutput);

            Point PointFrom;
            while (ConnectionEnum.hasMoreElements()) {
                PointFrom = PointTo;
                PointTo = (Point) ConnectionEnum.nextElement();
                this.JoinPoints(PointFrom, "*", "*", 0, PointTo, "*", "*", 0,
                        GraphicsOutput);
            }

            PointFrom = PointTo;
            this.JoinPoints(PointFrom, "*", "*", 0, To.GetPosition(), To.Direction,
                    To.Type, ToScreenR, GraphicsOutput);

        }

    }

    /*
     * private method to draw the line connecting two secondary structure
     * symbols
     */
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
                ps.addElement(PostscriptFactory.makeMove(From.x, 0 - From.y));	//FIXME
                this.start_new_connection = false;
            }
            ps.addElement(PostscriptFactory.makeLine(To.x, 0 - To.y)); //FIXME
        }

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

        double s = this.separation(p1, p2);

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
    
    public double separation(Point p) {
    	return this.separation(p, this.GetPosition());
    }

    /* private method calculates the separation of two points */
    /* one day I'll put it in a more sensible place for re-use */
    /* ...and one day, it has! gmt 13/05/08 */
    private double separation(Point p1, Point p2) {

        int sep;
        double fsep;

        sep = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
        fsep = sep;

        fsep = Math.sqrt(fsep);

        return fsep;

    }
    
    public void InvertY() {
        Vector<Point> conns;
        Enumeration<Point> en;
        Point p;
        SecStrucElement s;

        for (s = this; s != null; s = s.GetTo()) {

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
    
    public void ApplyScale(float scale) {

        SecStrucElement s;
        Point p;
        int x, y, r;
        Vector<Point> conns;
        Enumeration<Point> en;

        for (s = this; s != null; s = s.GetTo()) {

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
                    p = (Point) en.nextElement();
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

        for (s = this; s != null; s = s.GetTo()) {
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



    public void SetFixedType(String ft) {
        this.FixedType = ft;
    }

    public String GetFixedType() {
        return this.FixedType;
    }

    public void AddBridgePartner(int bp) {
        if (this.BridgePartner == null)
            this.BridgePartner = new Vector<Integer>();
        this.BridgePartner.addElement(new Integer(bp));
    }

    public void AddBridgePartnerSide(String side) {
        if (this.BridgePartnerSide == null)
            this.BridgePartnerSide = new Vector<String>();
        this.BridgePartnerSide.addElement(side);
    }

    public void AddBridgePartnerType(String type) {
        if (this.BridgePartnerType == null)
            this.BridgePartnerType = new Vector<String>();
        this.BridgePartnerType.addElement(type);
    }

    public Vector<Integer> GetBridgePartner() {
        return this.BridgePartner;
    }

    public Vector<String> GetBridgePartnerSide() {
        return this.BridgePartnerSide;
    }

    public Vector<String> GetBridgePartnerType() {
        return this.BridgePartnerType;
    }

    public void AddNeighbour(int nb) {
        if (this.Neighbour == null)
            this.Neighbour = new Vector<Integer>();
        this.Neighbour.addElement(new Integer(nb));
    }

    public Vector<Integer> GetNeighbour() {
        return this.Neighbour;
    }

    public void SetSeqStartResidue(int ssr) {
        this.SeqStartResidue = ssr;
    }

    public int GetSeqStartResidue() {
        return this.SeqStartResidue;
    }

    public void SetSeqFinishResidue(int sfr) {
        this.SeqFinishResidue = sfr;
    }

    public int GetSeqFinishResidue() {
        return this.SeqFinishResidue;
    }

    public void SetChirality(int c) {
        this.Chirality = c;
    }

    public int GetChirality() {
        return this.Chirality;
    }

    public void SetAxesStartPoint(float x, float y, float z) {
        this.AxesStartPoint[0] = x;
        this.AxesStartPoint[1] = y;
        this.AxesStartPoint[2] = z;
    }

    public float[] GetAxesStartPoint() {
        return this.AxesStartPoint;
    }

    public void SetAxesFinishPoint(float x, float y, float z) {
        this.AxesFinishPoint[0] = x;
        this.AxesFinishPoint[1] = y;
        this.AxesFinishPoint[2] = z;
    }

    public float[] GetAxesFinishPoint() {
        return this.AxesFinishPoint;
    }

    public void SetAxisLength(float len) {
        this.AxisLength = len;
    }

    public float GetAxisLength() {
        return this.AxisLength;
    }

    public void SetFill(int f) {
        this.Fill = f;
    }

    public int GetFill() {
        return this.Fill;
    }

    public void PlaceElement(int x, int y) {
        if (this.Position == null)
            this.Position = new Point(0, 0);
        this.Position.setLocation(x, y);
    }

    public void PlaceElementX(int x) {
        if (this.Position == null)
            this.Position = new Point(0, 0);
        this.Position.x = x;
    }

    public void PlaceElementY(int y) {
        if (this.Position == null)
            this.Position = new Point(0, 0);
        this.Position.y = y;
    }

    public Point GetPosition() {
        return this.Position;
    }

    public void SetPosition(Point p) {
        if (this.Position == null) {
            this.Position = new Point(p.x, p.y);
        } else {
            this.Position.x = p.x;
            this.Position.y = p.y;
        }
    }

    public void Translate(int tx, int ty) {
        if (this.Position == null) {
            this.Position = new Point();
        }
        this.Position.x += tx;
        this.Position.y += ty;
    }

    public void TranslateFixed(int tx, int ty) {
        SecStrucElement s;
        for (s = this.GetFixedStart(); s != null; s = s.GetFixed()) {
            s.Translate(tx, ty);
        }

    }

    public void SetSymbolRadius(int r) {
        this.SymbolRadius = r;
    }

    public int GetSymbolRadius() {
        return this.SymbolRadius;
    }

    public SecStrucElement Delete() {

        SecStrucElement root = this.GetRoot();
        if (root == this) {
            if (this.To != null)
                this.To.SetFrom(null);
            return this.To;
        }

        if (this.From != null)
            this.From.SetTo(this.To);

        if (this.To != null)
            this.To.SetFrom(this.From);

        return root;

    }

    public SecStrucElement GetRoot() {
        SecStrucElement s;
        for (s = this; s.GetFrom() != null; s = s.GetFrom())
            ;
        return s;
    }

    public SecStrucElement GetFixedStart() {

        SecStrucElement s = null, t = null;

        for (s = this.GetRoot(); s != null; s = s.GetNext()) {
            for (t = s; t != null; t = t.GetFixed()) {
                if (t == this)
                    break;
            }
        }

        if (t == this)
            return s;
        else
            return null;

    }

    public void AddConnectionTo(int x, int y) {

        Point p = new Point(x, y);

        if (this.ConnectionTo == null)
            this.ConnectionTo = new Vector<Point>();

        this.ConnectionTo.addElement(p);

    }

    public void AddConnectionTo(Point p) {
        if (p != null) {
            if (this.ConnectionTo == null)
                this.ConnectionTo = new Vector<Point>();
            this.ConnectionTo.addElement(p);
        }
    }

    public Vector<Point> GetConnectionTo() {
        if (this.ConnectionTo == null)
            this.ConnectionTo = new Vector<Point>();
        return this.ConnectionTo;
    }

    public void ClearConnectionTo() {
        this.ConnectionTo = new Vector<Point>();
    }

    public void SetFixed(SecStrucElement s) {
        this.Fixed = s;
    }

    public SecStrucElement GetFixed() {
        return this.Fixed;
    }

    public void SetFixedIndex(int i) {
        this.FixedIndex = i;
    }

    public int GetFixedIndex() {
        return this.FixedIndex;
    }

    public void SetNextIndex(int i) {
        this.NextIndex = i;
    }

    public int GetNextIndex() {
        return this.NextIndex;
    }

    public void SetNext(SecStrucElement s) {
        this.Next = s;
    }

    public SecStrucElement GetNext() {
        return this.Next;
    }

    public void SetTo(SecStrucElement s) {
        this.To = s;
    }

    public void SetFrom(SecStrucElement s) {
        this.From = s;
    }

    public SecStrucElement GetTo() {
        return this.To;
    }

    public SecStrucElement GetFrom() {
        return this.From;
    }

    public boolean IsRoot() {
        if (this.From == null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean IsTerminus() {

        if (this.Type.equals("N") || this.Type.equals("C")) {
            return true;
        } else {
            return false;
        }

    }

    public int Length() {
        return (this.PDBFinishResidue - this.PDBStartResidue + 1);
    }

    public int GetFixNumRes() {

        SecStrucElement t;
        int size = 0;

        if (this.Type.equals("H") || this.Type.equals("E")) {
            for (t = this.GetFixedStart(), size = 0; t != null; t = t.GetFixed())
                size += t.Length();
        }

        return size;

    }

    public SecStrucElement GetSSEByNumber(int num) {

        SecStrucElement ss;
        int i = 0;
        for (ss = this; ss != null && i < num; ss = ss.To, i++)
            ;

        return ss;

    }

    public String getRelDirection(SecStrucElement s) {
        if (this.Direction.equals(s.Direction))
            return "P";
        else
            return "A";
    }

    public Point TopsCentroid() throws TopsLinkedListException {

        if (!this.IsRoot())
            throw new TopsLinkedListException();

        int centx, centy, n;
        SecStrucElement ss;

        n = 1;
        centx = this.GetPosition().x;
        centy = this.GetPosition().y;

        for (ss = this.To; ss != null; ss = ss.To) {
            n++;
            centx += ss.GetPosition().x;
            centy += ss.GetPosition().y;
        }

        centx = (int) (((float) centx) / ((float) n));
        centy = (int) (((float) centy) / ((float) n));

        return new Point(centx, centy);

    }

    public Rectangle TopsBoundingBox() throws TopsLinkedListException {

        int x, y, minx, maxx, miny, maxy, r, maxr;

        SecStrucElement ss;

        if (!this.IsRoot())
            throw new TopsLinkedListException();

        if (this.Position == null)
            this.Position = new Point(0, 0);

        x = this.Position.x;
        y = this.Position.y;

        minx = x;
        maxx = x;
        miny = y;
        maxy = y;
        maxr = this.SymbolRadius;

        for (ss = this.To; ss != null; ss = ss.To) {

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

    public void ReflectXY() {

        SecStrucElement s;

        for (s = this.GetRoot(); s != null; s = s.GetTo()) {
            if (s.Direction.equals("D"))
                s.Direction = "U";
            else if (s.Direction.equals("U"))
                s.Direction = "D";
        }
    }

    public void ReflectZX() {

        SecStrucElement s;
        Point p;
        int y;
        Enumeration<Point> en;

        for (s = this.GetRoot(); s != null; s = s.GetTo()) {
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

        for (s = this.GetRoot(); s != null; s = s.GetTo()) {
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
    

    public int getSymbolNumber() {
		return SymbolNumber;
	}

	public void setSymbolNumber(int symbolNumber) {
		SymbolNumber = symbolNumber;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getDirection() {
		return Direction;
	}

	public void setDirection(String direction) {
		Direction = direction;
	}

	public int getPDBStartResidue() {
		return PDBStartResidue;
	}

	public void setPDBStartResidue(int pDBStartResidue) {
		PDBStartResidue = pDBStartResidue;
	}

	public int getPDBFinishResidue() {
		return PDBFinishResidue;
	}

	public void setPDBFinishResidue(int pDBFinishResidue) {
		PDBFinishResidue = pDBFinishResidue;
	}

	public String getChain() {
		return Chain;
	}

	public void setChain(String chain) {
		Chain = chain;
	}

    public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}

	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (this.Type.equals("H"))
            sb.append("Helix");
        else if (this.Type.equals("E"))
            sb.append("Strand");
        else if (this.Type.equals("N"))
            sb.append("N terminus");
        else if (this.Type.equals("C"))
            sb.append("C terminus");

        String ch;
        if ((this.Chain == null) || (this.Chain.equals("0")))
            ch = " ";
        else
            ch = this.Chain;

        if (this.Type.equals("H") || this.Type.equals("E")) {
            sb.append(" " + ch + this.PDBStartResidue + " to " + ch
                    + this.PDBFinishResidue);
        }

        return sb.toString();

    }

    public void setColour(Color c) {
        this.Colour = c;
    }

    public Color getColour() {
        return this.Colour;
    }

    /* START I/O methods */

    public void PrintAsText(PrintWriter ps) {

        ps.println("SecondaryStructureType " + this.Type);
        ps.println("Direction " + this.Direction);
        if (this.Label != null)
            ps.println("Label " + this.Label);
        else
            ps.println("Label");

        Color c = this.getColour();
        if (c == null)
            c = Color.white;
        ps.println("Colour " + c.getRed() + " " + c.getGreen() + " "
                + c.getBlue());

        int n = -1;
        if (this.Next != null)
            n = this.Next.SymbolNumber;
        ps.println("Next " + n);

        int f = -1;
        if (this.Fixed != null)
            f = this.Fixed.SymbolNumber;
        ps.println("Fixed " + f);

        if (this.FixedType != null)
            ps.println("FixedType " + this.FixedType);
        else
            ps.println("FixedType UNKNOWN");

        ps.print("BridgePartner");
        if (this.BridgePartner != null) {
            Enumeration<Integer> en = this.BridgePartner.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print(((Integer) en.nextElement()).intValue());
            }
        }
        ps.print("\n");

        ps.print("BridgePartnerSide");
        if (this.BridgePartnerSide != null) {
            Enumeration<String> en = this.BridgePartnerSide.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print((String) en.nextElement());
            }
        }
        ps.print("\n");

        ps.print("BridgePartnerType");
        if (this.BridgePartnerType != null) {
            Enumeration<String> en = this.BridgePartnerType.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print((String) en.nextElement());
            }
        }
        ps.print("\n");

        ps.print("Neighbour");
        if (this.Neighbour != null) {
            Enumeration<Integer> en = this.Neighbour.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print(((Integer) en.nextElement()).intValue());
            }
        }
        ps.print("\n");

        ps.println("SeqStartResidue " + this.SeqStartResidue);
        ps.println("SeqFinishResidue " + this.SeqFinishResidue);

        ps.println("PDBStartResidue " + this.PDBStartResidue);
        ps.println("PDBFinishResidue " + this.PDBFinishResidue);

        ps.println("SymbolNumber " + this.SymbolNumber);
        ps.println("Chain " + this.Chain);

        ps.println("Chirality " + this.Chirality);

        Point p = this.Position;
        if (p == null)
            p = new Point();
        ps.println("CartoonX " + p.x);
        ps.println("CartoonY " + p.y);

        ps.println("AxesStartPoint " + this.AxesStartPoint[0] + " "
                + this.AxesStartPoint[1] + " " + this.AxesStartPoint[2]);
        ps.println("AxesFinishPoint " + this.AxesFinishPoint[0] + " "
                + this.AxesFinishPoint[1] + " " + this.AxesFinishPoint[2]);

        ps.println("SymbolRadius " + this.SymbolRadius);

        ps.println("AxisLength " + this.AxisLength);

        Vector<Point> ct = this.GetConnectionTo();
        ps.println("NConnectionPoints " + ct.size());
        ps.print("ConnectionTo");
        Enumeration<Point> en = ct.elements();
        Point cp;
        while (en.hasMoreElements()) {
            cp = (Point) en.nextElement();
            ps.print(" " + cp.x + " " + cp.y);
        }
        ps.print("\n");

        ps.println("Fill " + this.Fill);

    }

    /* END I/O methods */

    /* START debugging methods */

    public void PrintLists() {

        SecStrucElement s, root;

        root = this.GetRoot();

        System.out.println("To list");
        System.out.println(" ");

        for (s = root; s != null; s = s.GetTo()) {
            s.PrintElement();
        }

        System.out.println(" ");
        System.out.println(" ");

    }

    public void PrintElement() {
        System.out.println("SymbolNumber " + this.SymbolNumber);
        if (this.To != null)
            System.out.println("To " + this.To.SymbolNumber);
        if (this.From != null)
            System.out.println("From " + this.From.SymbolNumber);
        System.out.println("NextIndex " + this.NextIndex);
        if (this.Next != null)
            System.out.println("Next " + this.Next.SymbolNumber);
        System.out.println("FixedIndex " + this.FixedIndex);
        if (this.Fixed != null)
            System.out.println("Fixed " + this.Fixed.SymbolNumber);
        System.out.println(" ");
    }

    /* END debugging methods */

}
