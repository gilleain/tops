package tops.port.io;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UNKNOWN;
import static tops.port.model.Direction.UP;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import tops.port.IntersectionCalculator;
import tops.port.IntersectionCalculator.Intersection;
import tops.port.IntersectionCalculator.IntersectionType;
import tops.port.model.Cartoon;
import tops.port.model.Direction;
import tops.port.model.PlotFragInformation;
import tops.port.model.SSE;
import tops.port.model.SSEType;

public class TopsPrint {
    
    /*
     * These variables are set for usage through out these functions and
     * are only accessible from this file
     */
    static final double ITOC = 2.540; /* Inches to centimetres */
    public static final double CTOI = 0.3937; /* Centimetres to inches */
    public static final double PPI = 72; /* Points per inch */
    public static final double A4_W = 21.0; /* A4 width */
    public static final double A4_H = 29.7; /* A4 height */
    
    private PrintStream out; /* Output file */
    private double width = A4_W; /* A4 width */
    private double height = A4_H; /* A4 height */
    private double pWidth = A4_W; /* Picture width */
    private double pHeight = A4_H; /* Picture height */
    private double xMin = 0.0; /* X minimum */
    private double yMin = 0.0; /* Y minimum */
    private double xMax = A4_W; /* X default scale = centimeters */
    private double yMax = A4_H; /* Y default scale = centimeters */
    private double xp = PPI / ITOC; /* X default points per centimeter */
    private double yp = PPI / ITOC; /* Y default points per centimeter */
    private String font = null; /* Default font */
    private int point = 15; /* Default point size */
    private double textpos = 0.0; /* Default text position */
    private double texthgt = 0.0; /* Default text position */

    private static final double PI2 = 0;
    /* Hard coded but will change */
    private int radius;
    private double scale = 0.60;
    private StringBuilder buffer;

    private boolean small;

    private int gridUnitSize;

    private double psXY(double v) {
        return (v / (double) gridUnitSize);
    }

    private double iPSxy(double v) {
        return (v * (double) gridUnitSize);
    }

    private static final double VERT_SEP = 2.0;
    private static final double BORDER = 5.0;
    private static final String TITLE_PREF = "TOPS Cartoon: ";
    
    private IntersectionCalculator intersectionCalculator;
    
    public TopsPrint() {
        this.intersectionCalculator = new IntersectionCalculator();
    }

    public boolean printCartoons(List<Cartoon> cartoons, String fileName, String proteinName, PlotFragInformation pfi) {

        int i;
        
        double[] cartoonTransX;
        double[] cartoonTransY;
        double rubricSpace;
        String title;
        int lt;

        if (cartoons.isEmpty()) { return false; }

        /* Open file */
        try {
            openPostscript(fileName);
        } catch (FileNotFoundException fnf) { 
            return false;
        }

        /* Find size of space required for cartoons */
        cartoonTransX = new double[cartoons.size()];
        cartoonTransY = new double[cartoons.size()];

        Rectangle2D b = findTotSize(cartoons, cartoonTransX, cartoonTransY);

        /* Find space needed for rubric */
        rubricSpace = 1.0 + (pfi.getNumberOfFragments()) * 0.5;

        /* Print bounding box command */
        if (small) {
            setPageSize(b.getWidth() + BORDER, b.getHeight() + BORDER + rubricSpace
                    + VERT_SEP * (cartoons.size() - 1));
        } else {
            setPageSize(21.9, 29.0);
        }

        /* Define cartoon symbols */
        defCartoonSymbols();

        /* Prepare plotting area */
        setPictureSize(b.getWidth() + BORDER, b.getHeight() + BORDER + rubricSpace
                + VERT_SEP * (cartoons.size() - 1));
        if (!small)
            centrePage();
        scalePage(b.getMinX() - (BORDER / 2.0), b.getMaxX() + (BORDER / 2.0),
                b.getMinY() - (BORDER / 2.0) - VERT_SEP * (cartoons.size() - 1),
                b.getMaxY() + rubricSpace + (BORDER / 2.0));
        if (!small)
            perimeter();
        chooseFont("Courier", convertXPoint(psXY(radius)));
        textCentre(0.5, 0.38);
        lineWidth(0.01);

        /* Print title */
        lt = TITLE_PREF.length() + proteinName.length() + 1;
        if (lt > 0) {
            title = TITLE_PREF + proteinName;
            printText(title, (b.getMinX() + b.getMaxX()) / 2.0,
                    b.getMaxY() + rubricSpace + (BORDER / 4.0));
        }

        /* Print plot rubric */
        printPlotRubric(pfi, (b.getMinX() + b.getMaxX()) / 2.0, b.getMaxY() + rubricSpace);

        /* Print the Cartoons */
        for (i = 0; i < cartoons.size(); i++) {
            translate(psXY(cartoonTransX[i]), psXY(cartoonTransY[i]));
            printCartoon(cartoons.get(i));
        }

        /* Done */
        endPostscript();

        return true;
    }

    /*
     * a function to find the total size of the cartoon diagrams with the
     * cartoons stacked in the y direction centred in the x direction on the
     * centre of the first
     */
    private Rectangle2D findTotSize(List<Cartoon> cartoons,double[] cartoonTransX, double[] cartoonTransY) {

        double xmax = 0;
        double xmin = 0;
        double ymax = 0;
        double ymin = 0;
        double cxmax;
        double cxmin;
        double cymax;
        double cymin = 0;
        double cyminp;
        double tx;
        double exwid;
        double wid;

        for (int i = 0; i < cartoons.size(); i++) {
            cartoonTransX[i] = 0.0;
            cartoonTransY[i] = 0.0;
        }

        for (int i = 0; i < cartoons.size(); i++) {
            Cartoon cartoon = cartoons.get(i); 
            cyminp = cymin;
            SSE p = cartoon.getSSEs().get(0);
            cxmax = cxmin = (double) p.getCartoonX();
            cymax = cymin = (double) p.getCartoonY();
            for (SSE q : cartoon.getSSEs().subList(1, cartoon.getSSEs().size())) {
                if (q.isSymbolPlaced()) {
                    if ((double) q.getCartoonX() > cxmax)
                        cxmax = (double) q.getCartoonX();
                    if ((double) q.getCartoonX() < cxmin)
                        cxmin = (double) q.getCartoonX();
                    if ((double) q.getCartoonY() > cymax)
                        cymax = (double) q.getCartoonY();
                    if ((double) q.getCartoonY() < cymin)
                        cymin = (double) q.getCartoonY();
                }
            }

            if (i == 0) {
                xmin = psXY(cxmin);
                xmax = psXY(cxmax);
                ymin = psXY(cymin);
                ymax = psXY(cymax);
                cartoonTransY[0] = 0.0;
            } else {
                wid = psXY(cxmax - cxmin);
                exwid = wid - xmax + xmin;
                if (exwid > 0.0) {
                    xmin -= exwid / 2.0;
                    xmax += exwid / 2.0;
                }
                ymin -= psXY(cymax - cymin);

                cartoonTransY[i] -= (cymax + iPSxy(VERT_SEP) - cyminp);
            }
        }

    

        tx = 0.0;
        for (int i = 0; i < cartoons.size(); i++) {
            Cartoon cartoon = cartoons.get(i);
            cxmin = cartoon.getSSEs().get(0).getCartoonX();
            for (SSE p : cartoon.getSSEs()) {
                if (p.isSymbolPlaced() && (double) p.getCartoonX() < cxmin) {
                    cxmin = (double) p.getCartoonX();
                }
            }

            cartoonTransX[i] = iPSxy(xmin) - cxmin - tx;

            tx += cartoonTransX[i];

        }

        return new Rectangle2D.Double(xmin, xmax, ymin, ymax);

    }

    /*
     * Function to Print a rubric for the plot
     */
    private void printPlotRubric(PlotFragInformation pfi, double xpos, double ypos) {
        StringBuilder rubric = new StringBuilder();
        double y = ypos;

        for (int i = 0; i < pfi.getNumberOfFragments(); i++) {
            rubric.append('N');
            rubric.append(i + 1);
            rubric.append(pfi.getStartFragmentChainLimit(i));
            rubric.append(pfi.getStartFragmentResidueLimit(i));
            rubric.append('C');
            rubric.append(i + 2);
            rubric.append(pfi.getEndFragmentChainLimit(i));
            rubric.append(pfi.getEndFragmentResidueLimit(i));

            printText(rubric.toString(), xpos, y);
            y = y - 0.5;
        }
    }

    /*
     * a function which prints a single Cartoon
     */
    private void printCartoon(Cartoon cartoon) {

        int i;
        int ncp;
        SSEType fromSSType;
        SSEType toSSType;

        // Symbols
        for (SSE p : cartoon.getSSEs()) {
            if (p.isSymbolPlaced()) {
                switch (p.getSSEType()) {
                    case EXTENDED:
                        makeObject(
                                p.getDirection() == UP ? "UpTriangle" : "DownTriangle",
                                3, verbatim(1.0 - (p.getFill()? 0 : 1)), /// XXX Fill?
                                psXY(p.getCartoonX()), psXY(p.getCartoonY()));
                        break;
                    case HELIX:
                        makeObject("Circle", 3, verbatim(1.0 - (p.getFill()? 0 : 1)), /// XXX Fill?
                                psXY(p.getCartoonX()), psXY(p.getCartoonY()));
                        break;
                    default:
                        break;
                }
            }
        }

        // Lines
        SSE prev = null;
        for (SSE p : cartoon.getSSEs()) {
            if (p.isSymbolPlaced()) {

                toSSType = p.getSSEType();

                if (prev != null) {

                    fromSSType = prev.getSSEType();
                    if (!(toSSType == SSEType.CTERMINUS || toSSType == SSEType.NTERMINUS)
                      && (fromSSType == SSEType.CTERMINUS) || (fromSSType == SSEType.NTERMINUS)) {

                        if (prev.getNConnectionPoints() > 0) {
                            ncp = prev.getNConnectionPoints();

                            JoinPoints(psXY(prev.getCartoonX()),
                                    psXY(prev.getCartoonY()),
                                    psXY(prev.getConnectionTo(0).x),
                                    psXY(prev.getConnectionTo(0).y),
                                    prev.getDirection(), UNKNOWN,
                                    prev.getSSEType(),
                                    p.getSSEType());

                            for (i = 0; i < (ncp - 1); i++) {
                                JoinPoints(psXY(prev.getConnectionTo(i).x),
                                        psXY(prev.getConnectionTo(i).y),
                                        psXY(prev.getConnectionTo(i + 1).x),
                                        psXY(prev.getConnectionTo(i + 1).y),
                                        UNKNOWN, UNKNOWN, prev.getSSEType(),
                                        p.getSSEType());
                            }

                            JoinPoints(psXY(prev.getConnectionTo(ncp - 1).x),
                                    psXY(prev.getConnectionTo(ncp - 1).y),
                                    psXY(p.getCartoonX()),
                                    psXY(p.getCartoonY()), UNKNOWN, p.getDirection(),
                                    prev.getSSEType(),
                                    p.getSSEType());

                        } else {
                            JoinPoints(psXY(prev.getCartoonX()),
                                    psXY(prev.getCartoonY()),
                                    psXY(p.getCartoonX()),
                                    psXY(p.getCartoonY()), prev.getDirection(),
                                    p.getDirection(), prev.getSSEType(),
                                    p.getSSEType());
                        }

                    }

                    if (fromSSType == SSEType.NTERMINUS) {
                        makeObject("Square", 2, psXY(prev.getCartoonX()),
                                psXY(prev.getCartoonY()));
                        printText(prev.getLabel(), psXY(prev.getCartoonX()),
                                psXY(prev.getCartoonY()));
                    }

                }

                if (toSSType == SSEType.CTERMINUS) {
                    makeObject("Square", 2, psXY(p.getCartoonX()), psXY(p.getCartoonY()));
                    printText(p.getLabel(), psXY(p.getCartoonX()), psXY(p.getCartoonY()));
                }
                
                prev = p;
            }
        }
    }

    /*
     * a function which writes out postscript code to define the symbols used in
     * cartoons
     */
    private void defCartoonSymbols() {

        defineObject("Square");
        literal("moveto");
        moveRelative(psXY(-radius) / 2.0, psXY(radius) / 2.0);
        lineRelative(psXY(radius), 0.0);
        lineRelative(0.0, psXY(-radius));
        lineRelative(psXY(-radius), 0.0);
        closePath();
        fill(1.0);
        endObject();

        defineObject("Circle");
        buffer.append(String.format(" %d %d %d arc", convertXPoint(psXY(radius) * scale), 0,360));
        literal(buffer.toString());
        fill(-1.0);
        outLine();
        endObject();

        defineObject("UpTriangle");
        literal("moveto");
        moveRelative(0.0, psXY(radius));
        lineRelative(psXY(radius) * Math.sin(PI2 / 3.0),
                psXY(radius) * (Math.cos(PI2 / 3.0) - 1.0));
        lineRelative(-2.0 * psXY(radius) * Math.sin(PI2 / 3.0), 0.0);
        closePath();
        fill(-1.0);
        outLine();
        endObject();

        defineObject("DownTriangle");
        literal("moveto");
        moveRelative(0.0, psXY(-radius));
        lineRelative(psXY(radius) * Math.sin(PI2 / 3.0),
                psXY(-radius) * (Math.cos(PI2 / 3.0) - 1.0));
        lineRelative(-2.0 * psXY(radius) * Math.sin(Math.PI / 3.0), 0.0);
        closePath();
        fill(-1.0);
        outLine();
        endObject();

        defineObject("Line");
        literal("moveto");
        literal("lineto");
        outLine();
        endObject();

    }

    /*
     * function cross_circle
     * 
     * Tom F. August 1992
     * 
     * Function to return crossing point at which a line from A, B crosses the
     * circle X and Y are replaced by the crossing point
     */
    private Vector2d crossCircle(double x, double y, double a, double b) {
        int i;
        double itv = 20.0; 

        Point2d lp = new Point2d(0, 0);
        Point2d ab = new Point2d(a, b);
        Point2d xy = new Point2d(x, y);
        for (i = 0; i <= itv; i++) {
            double rx = x + psXY(radius) * Math.sin((double) i * PI2 / itv) * scale;
            double ry = y + psXY(radius) * Math.cos((double) i * PI2 / itv) * scale;
            if (i > 0) {
                Intersection intersection = 
                        intersectionCalculator.lineCross(lp, new Point2d(rx, ry), ab, xy);
                if (intersection.getType() != IntersectionCalculator.IntersectionType.NOT_CROSSING) {
                    return intersection.getPoint();
                }
            }
            lp = new Point2d(rx, ry);
        }
        return null;
    }

    /*
     * function cross_up_triangle
     * 
     * Tom F. August 1992
     * 
     * Function to return crossing point at which a line from A, B crosses the
     * triangle X and Y are replaced by the crossing point
     */

    private Vector2d CrossUpTriangle(double X, double Y, double A, double B) {
        int i;
        double itv = 3.0;
        Point2d lp = new Point2d(0, 0);
        Point2d AB = new Point2d(A, B);
        Point2d XY = new Point2d(X, Y);
        
        for (i = 0; i <= itv; i++) {
            double rx = X + psXY(radius) * Math.sin((double) i * PI2 / itv);
            double ry = Y + psXY(radius) * Math.cos((double) i * PI2 / itv);
            if (i > 0) {
                Intersection intersection = intersectionCalculator.lineCross(lp, new Point2d(rx, ry), AB, XY);
                if (intersection.getType() != IntersectionCalculator.IntersectionType.NOT_CROSSING) {
                    return intersection.getPoint();
                }
            }
            lp = new Point2d(rx, ry);
        }
        return null;
    }

    /*
     * function cross_down_triangle
     * 
     * Tom F. August 1992
     * 
     * Function to return crossing point at which a line from A, B crosses the
     * triangle X and Y are replaced by the crossing point
     */
    private Vector2d CrossDownTriangle(double X, double Y, double A, double B) {
        double itv = 3.0;

        Point2d lp = new Point2d(0, 0);
        Point2d AB = new Point2d(A, B);
        Point2d XY = new Point2d(X, Y);
        for (int i = 0; i <= itv; i++) {
            double rx = X + psXY(radius) * Math.sin((double) i * PI2 / itv);
            double ry = Y - psXY(radius) * Math.cos((double) i * PI2 / itv);
            if (i > 0) {
                Intersection intersection =
                        intersectionCalculator.lineCross(lp, new Point2d(rx, ry), AB, XY);
                if (intersection.getType() != IntersectionType.NOT_CROSSING) {
                    return intersection.getPoint();
                }
            }
            lp = new Point2d(rx, ry);
        }
        return null;
    }

    /*
     * function join_points
     * 
     * Tom F. September 1992
     * 
     * Function to join two points
     */
    private void JoinPoints(double px, double py, double qx, double qy, Direction direction,
            Direction direction2, SSEType pt, SSEType qt) {
        if (direction != UP && direction != UNKNOWN) {
            if (pt == SSEType.EXTENDED)
                CrossDownTriangle(px, py, qx, qy);
            if (pt == SSEType.HELIX)
                crossCircle(px, py, qx, qy);
        }
        if (direction2 != DOWN && direction2 != UNKNOWN) {
            if (pt == SSEType.EXTENDED)
                CrossUpTriangle(qx, qy, px, py);
            if (pt == SSEType.HELIX)
                crossCircle(qx, qy, px, py);
        }
        makeObject("Line", 4, px, py, qx, qy);
    }

    /*
     * function point_to_cm
     * 
     * Tom F. November 1992
     * 
     * Function to convert pointsize to centremetres (useful with text)
     */
    private double pointToCm(double point) {
        return point * ITOC / PPI;
    }

    /*
     * function open_postscript
     * 
     * Tom F. October 1992
     * 
     * This function opens a postscript file for output. The file requires the
     * output device. If the file is "" ie. no name then output is sent to
     * stdout. Function returns false on failure. This function writes all the
     * header parts to the file
     */
    private boolean openPostscript(String outputFile) throws FileNotFoundException {
        if ("".equals(outputFile)) {
            out = System.out;
        } else {
            out = new PrintStream(new File(outputFile));
        }
        fprintf(out, "%%!PS-Adobe-1.0\n");
        fprintf(out, "%%%%Creator: postgen written by Tom Flores\n");
        fprintf(out, "%%%%Title: postscript procedure\n");
        fprintf(out, "%%%%CreationDate: Unknown\n");
        fprintf(out, "%%%%Pages: 1\n");

        return true;    // XXX TODO
    }

    /*
     * function page_size
     * 
     * Tom F. October 1992
     * 
     * This function sets the page size
     */
    private void setPageSize(double width, double height) {

        if (width > 0.0)
            this.width = width;
        if (height > 0.0)
            this.height = height;
        if (this.width < pWidth)
            pWidth = this.width;
        if (this.height < pHeight)
            pHeight = this.height;
        fprintf(out, "%%%%BoundingBox: %.1f %.1f %.1f %.1f\n", 0.0, 0.0,
                this.width * PPI * CTOI, this.height * PPI * CTOI);
    }

    /*
     * function picture_size
     * 
     * Tom F. October 1992
     * 
     * This function sets up the picture size
     */
    private void setPictureSize(double width, double height) {
        if (width > 0.0)
            pWidth = width;
        if (height > 0.0)
            pHeight = height;
    }

    /*
     * function scale_page
     * 
     * Tom F. October 1992
     * 
     * This function sets the page scale
     */
    private void scalePage(double xmin, double xmax, double ymin, double ymax) {

        if (xmin < xmax) {
            this.xMin = xmin;
            this.xMax = xmax;
        }
        if (ymin < ymax) {
            this.yMin = ymin;
            this.yMax = ymax;
        }
        this.xp = (pWidth * PPI) / ((this.xMax - this.xMin) * ITOC);
        this.yp = (pHeight * PPI) / ((this.yMax - this.yMin) * ITOC);
        if (this.xMin != 0.0 && this.yMin != 0.0)
            fprintf(out, "%.1f %.1f  translate\n",
                    -this.xMin * pWidth / (this.xMax - this.xMin) * PPI * CTOI,
                    -this.yMin * pHeight / (this.yMax - this.yMin) * PPI * CTOI);
    }

    /*
     * function centre_page
     * 
     * Tom F. October 1992
     * 
     * This function centres a picture on a page
     */
    private void centrePage() {
        fprintf(out, "%.1f %.1f translate\n",
                ((width - pWidth) / 2.0 * PPI * CTOI),
                ((height - pHeight) / 2.0 * PPI * CTOI));
    }

    /*
     * function perimeter
     * 
     * Tom F. October 1992
     * 
     * This function draws a box round the picture
     */
    private void perimeter() {
        fprintf(out, "newpath\n");
        fprintf(out, "  %.1f %.1f moveto\n", (xMin * xp), (yMin * yp));
        fprintf(out, "  %.1f %.1f lineto\n", (xMin * xp), (yMax * yp));
        fprintf(out, "  %.1f %.1f lineto\n", (xMax * xp), (yMax * yp));
        fprintf(out, "  %.1f %.1f lineto\n", (xMax * xp), (yMin * yp));
        fprintf(out, "  %.1f %.1f lineto\n", (xMin * xp), (yMin * yp));
        fprintf(out, "  closepath\n");
        fprintf(out, "stroke\n");
    }

    /*
     * function define_object
     * 
     * Tom F. October 1992
     * 
     * Function to begin an object definition
     */
    private void defineObject(String objectName) {
        fprintf(out, "/%s\n{ newpath\n", objectName);
    }

    /*
     * function end_object
     * 
     * Tom F. OCtober 1992
     * 
     * Function to finish object definition
     */
    private void endObject() {
        fprintf(out, " } def\n");
    }

    /*
     * function make_object
     * 
     * Tom F. October 1992
     * 
     * This function uses a variable argument list, the first member represents
     * the object to be placed, the next is an integer that represents the
     * number of of parameters to be stacked before the object is drawn these
     * must all be doubles!
     */
    private void makeObject(String objectName, int i, double... args) {
        fprintf(out, "  ");
        for (double arg : args) {
            fprintf(out, "%.1f ", (double) (arg * xp));
        }
        fprintf(out, "%s\n", objectName);
    }

    /*
     * function translate D. Westhead 11/09/96
     */
    private void translate(double x, double y) {
        fprintf(out, "  %.1f %.1f translate\n", (x * xp), (y * yp));
    }

    /*
     * function move_to
     * 
     * Tom F. October 1992
     */
    private void MoveTo(double x, double y) {
        fprintf(out, "  %.1f %.1f moveto\n", (x * xp), (y * yp));
    }

    /*
     * Function move_relative
     * 
     * Tom F. October 1992
     */
    private void moveRelative(double x, double y) {
        fprintf(out, "  %.1f %.1f rmoveto\n", (x * xp), (y * yp));
    }

    /*
     * function line_to
     * 
     * Tom F. October 1992
     */
    private void LineTo(double x, double y) {
        fprintf(out, "  %.1f %.1f lineto\n", (x * xp), (y * yp));
    }

    /*
     * function line_relative
     * 
     * Tom F. October 1992
     */
    private void lineRelative(double x, double y) {
        fprintf(out, "  %.1f %.1f rlineto\n", (x * xp), (y * yp));
    }

    /*
     * function close_path
     * 
     * Tom F. October 1992
     */
    private void closePath() {
        fprintf(out, "  closepath\n");
    }

    /*
     * function newpath
     * 
     * Tom F. October 1992
     */
    private void newPath() {
        fprintf(out, "newpath\n");
    }

    /*
     * function end_postscript
     * 
     * Tom F. October 1992
     * 
     * Function to complete postscript picture
     */
    private void endPostscript() {
        fprintf(out, "showpage\n");
        if (out != System.out)
            out.close();
    }

    /*
     * function outline
     * 
     * TOm F. October 1992
     */
    private void outLine() {
        fprintf(out, "  stroke\n");
    }

    /*
     * function fill
     * 
     * Tom F. October 1992
     */
    private void fill(double level) {
        if (level >= 0.0)
            fprintf(out, "  gsave\n  %.2f setgray fill\n  grestore\n", level);
        else
            fprintf(out, "  gsave\n  setgray fill\n  grestore\n");
    }

    /*
     * function line_width
     * 
     * Tom F. October 1992
     */
    private void lineWidth(double width) {
        fprintf(out, "  %.1f setlinewidth\n", (width * xp));
    }

    /*
     * function literal
     * 
     * Tom F. October 1992
     * 
     * Function to allow literal translation of postscript ie. written to file
     * directly - this allows for any additional bits to be added freely
     */
    private void literal(String postlit) {
        fprintf(out, "  %s\n", postlit);
    }

    /*
     * function choose_font
     * 
     * Tom F. October 1992
     * 
     * function to select font
     */
    private void chooseFont(String font, int pointSize) {
        this.font = font;
        if (pointSize > 0)
            point = pointSize;
        fprintf(out, "/%s findfont %d scalefont setfont\n", this.font, point);
    }

    /*
     * function character_height
     * 
     * Tom F. October 1992
     * 
     * This function set the character height
     */
    private void CharacterHeight(double height) {
        if (point != height * yp && height * yp > 0.0) {
            point = (int) (height * yp);
            chooseFont(font, point);
        }
    }

    /*
     * function print_text
     * 
     * Tom F. October 1992
     * 
     * Function to output text
     */
    private void printText(String text, double x, double y) {
        fprintf(out, "  %.1f %.1f moveto\n", (x * xp),
                (y * yp - texthgt * point));
        fprintf(out, "  (%s) dup stringwidth pop\n", text);
        fprintf(out, "  %.2f mul 0 rmoveto\n", -textpos);
        fprintf(out, "   show\n");
    }

    /*
     * function text_centre
     * 
     * Tom F. October 1992
     * 
     * Function to determine text centre value as fraction
     */
    private void textCentre(double length, double height) {
        textpos = length;
        texthgt = height;
    }

    /*
     * function convert_x_point
     * 
     * Tom F. October 1992
     * 
     * This function converts a given x value to its point value
     */
    private int convertXPoint(double value) {
        return (int) (value * xp);
    }

    private double verbatim(double value) {
        return value / xp;
    }

    /*
     * function convert_y_point
     * 
     * Tom F. October 1992
     * 
     * This function converts a given y value to its point value
     */
    private int ConvertYPoint(double value) {
        return (int) (value * yp);
    }

    /*
     * function debug_ps
     * 
     * Tom F. October 1992
     * 
     * This function outputs the values and states of internal variables
     */
    void debugPs() {
        fprintf(System.err, "PS - variables:\n");
        fprintf(System.err, "WIDTH     = %f\n", width);
        fprintf(System.err, "HEIGHT    = %f\n", height);
        fprintf(System.err, "PWIDTH    = %f\n", pWidth);
        fprintf(System.err, "PHEIGHT   = %f\n", pHeight);
        fprintf(System.err, "XMIN      = %f\n", xMin);
        fprintf(System.err, "XMAX      = %f\n", xMax);
        fprintf(System.err, "YMIN      = %f\n", yMin);
        fprintf(System.err, "YMAX      = %f\n", yMax);
        fprintf(System.err, "XP        = %f\n", xp);
        fprintf(System.err, "YP        = %f\n", yp);
    }

    private void fprintf(PrintStream stream, String message, Object... inserts) {
        stream.print(String.format(message, inserts));
    }

}