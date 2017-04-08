package tops.port.io;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UNKNOWN;
import static tops.port.model.Direction.UP;

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
    
    private PrintStream OUT; /* Output file */
    private double WIDTH = A4_W; /* A4 width */
    private double HEIGHT = A4_H; /* A4 height */
    private double PWIDTH = A4_W; /* Picture width */
    private double PHEIGHT = A4_H; /* Picture height */
    private double XMIN = 0.0; /* X minimum */
    private double YMIN = 0.0; /* Y minimum */
    private double XMAX = A4_W; /* X default scale = centimeters */
    private double YMAX = A4_H; /* Y default scale = centimeters */
    private double XP = PPI / ITOC; /* X default points per centimeter */
    private double YP = PPI / ITOC; /* Y default points per centimeter */
    private String FONT = null; /* Default font */
    private int POINT = 15; /* Default point size */
    private double TEXTPOS = 0.0; /* Default text position */
    private double TEXTHGT = 0.0; /* Default text position */

    private static final double PI2 = 0;
    /* Hard coded but will change */
    int Radius;
    double Scale = 0.60;
    StringBuffer buffer;

    boolean Small;

    SSE Root;

    int GridUnitSize;

    private double PSxy(double v) {
        return ((double) (v) / (double) GridUnitSize);
    }

    private double iPSxy(double v) {
        return ((double) (v) * (double) GridUnitSize);
    }

    private final double VERT_SEP = 2.0;
    private final double BORDER = 5.0;
    private final String TITLE_PREF = "TOPS Cartoon: ";
    
    private IntersectionCalculator intersectionCalculator;
    
    public TopsPrint() {
        this.intersectionCalculator = new IntersectionCalculator();
    }

    public boolean printCartoons(List<Cartoon> cartoons,
            String FileName, String ProtName, PlotFragInformation pfi) {

        int i;
        double Xmax= 0;
        double Xmin = 0;
        double Ymax = 0;
        double Ymin = 0;
        double[] CartoonTransX, CartoonTransY;
        double RubricSpace;
        String Title;
        int lt;

        if (cartoons.isEmpty()) { return false; }

        /* Open file */
        try {
            OpenPostscript(FileName);
        } catch (FileNotFoundException fnf) { 
            return false;
        }

        /* Find size of space required for cartoons */
        CartoonTransX = new double[cartoons.size()];
        CartoonTransY = new double[cartoons.size()];

        FindTotSize(cartoons, Xmin, Xmax, Ymin, Ymax, CartoonTransX, CartoonTransY);

        /* Find space needed for rubric */
        RubricSpace = 1.0 + (pfi.getNumberOfFragments()) * 0.5;

        /* Print bounding box command */
        if (Small) {
            PageSize(Xmax - Xmin + BORDER, Ymax - Ymin + BORDER + RubricSpace
                    + VERT_SEP * (cartoons.size() - 1));
        } else {
            PageSize(21.9, 29.0);
        }

        /* Define cartoon symbols */
        DefCartoonSymbols();

        /* Prepare plotting area */
        PictureSize(Xmax - Xmin + BORDER, Ymax - Ymin + BORDER + RubricSpace
                + VERT_SEP * (cartoons.size() - 1));
        if (!Small)
            CentrePage();
        ScalePage(Xmin - (BORDER / 2.0), Xmax + (BORDER / 2.0),
                Ymin - (BORDER / 2.0) - VERT_SEP * (cartoons.size() - 1),
                Ymax + RubricSpace + (BORDER / 2.0));
        if (!Small)
            Perimeter();
        ChooseFont("Courier", ConvertXPoint(PSxy(Radius)));
        TextCentre(0.5, 0.38);
        LineWidth(0.01);

        /* Print title */
        lt = TITLE_PREF.length() + ProtName.length() + 1;
        if (lt > 0) {
            Title = TITLE_PREF + ProtName;
            PrintText(Title, (Xmax + Xmin) / 2.0,
                    Ymax + RubricSpace + (BORDER / 4.0));
        }

        /* Print plot rubric */
        PrintPlotRubric(pfi, (Xmax + Xmin) / 2.0, Ymax + RubricSpace);

        /* Print the Cartoons */
        for (i = 0; i < cartoons.size(); i++) {
            Translate(PSxy(CartoonTransX[i]), PSxy(CartoonTransY[i]));
            PrintCartoon(cartoons.get(i));
        }

        /* Done */
        EndPostscript();

        return true;
    }

    /*
     * a function to find the total size of the cartoon diagrams with the
     * cartoons stacked in the y direction centred in the x direction on the
     * centre of the first
     */
    private void FindTotSize(List<Cartoon> cartoons, double Xmin,
            double Xmax, double Ymin, double Ymax, double[] CartoonTransX,
            double[] CartoonTransY) {

        double xmax = 0;
        double xmin = 0;
        double ymax = 0;
        double ymin = 0;
        double cxmax, cxmin, cymax, cymin = 0;
        double cyminp, tx;
        double exwid, wid;

        for (int i = 0; i < cartoons.size(); i++) {
            CartoonTransX[i] = 0.0;
            CartoonTransY[i] = 0.0;
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
                xmin = PSxy(cxmin);
                xmax = PSxy(cxmax);
                ymin = PSxy(cymin);
                ymax = PSxy(cymax);
                CartoonTransY[0] = 0.0;
            } else {
                wid = PSxy(cxmax - cxmin);
                exwid = wid - xmax + xmin;
                if (exwid > 0.0) {
                    xmin -= exwid / 2.0;
                    xmax += exwid / 2.0;
                }
                ymin -= PSxy(cymax - cymin);

                CartoonTransY[i] -= (cymax + iPSxy(VERT_SEP) - cyminp);
            }
        }

        Xmin = xmin;
        Xmax = xmax;
        Ymin = ymin;
        Ymax = ymax;

        tx = 0.0;
        for (int i = 0; i < cartoons.size(); i++) {
            Cartoon cartoon = cartoons.get(i);
            cxmin = cartoon.getSSEs().get(0).getCartoonX();
            for (SSE p : cartoon.getSSEs()) {
                if (p.isSymbolPlaced()) {
                    if ((double) p.getCartoonX() < cxmin) {
                        cxmin = (double) p.getCartoonX();
                    }
                }
            }

            CartoonTransX[i] = iPSxy(xmin) - cxmin - tx;

            tx += CartoonTransX[i];

        }

        return;

    }

    /*
     * Function to Print a rubric for the plot
     */
    private void PrintPlotRubric(PlotFragInformation pfi, double xpos, double ypos) {
        StringBuffer buffer = new StringBuffer();
        double y = ypos;

        for (int i = 0; i < pfi.getNumberOfFragments(); i++) {
            buffer.append('N');
            buffer.append(i + 1);
            buffer.append(pfi.getStartFragmentChainLimit(i));
            buffer.append(pfi.getStartFragmentResidueLimit(i));
            buffer.append('C');
            buffer.append(i + 2);
            buffer.append(pfi.getEndFragmentChainLimit(i));
            buffer.append(pfi.getEndFragmentResidueLimit(i));

            PrintText(buffer.toString(), xpos, y);
            y = y - 0.5;
        }
    }

    /*
     * a function which prints a single Cartoon
     */
    private void PrintCartoon(Cartoon cartoon) {

        int i, ncp;
        SSEType FromSSType, ToSSType;

        // Symbols
        for (SSE p : cartoon.getSSEs()) {
            if (p.isSymbolPlaced()) {
                switch (p.getSSEType()) {
                    case EXTENDED:
                        MakeObject(
                                p.getDirection() == UP ? "UpTriangle" : "DownTriangle",
                                3, Verbatim(1.0 - (p.getFill()? 0 : 1)), /// XXX Fill?
                                PSxy(p.getCartoonX()), PSxy(p.getCartoonY()));
                        break;
                    case HELIX:
                        MakeObject("Circle", 3, Verbatim(1.0 - (p.getFill()? 0 : 1)), /// XXX Fill?
                                PSxy(p.getCartoonX()), PSxy(p.getCartoonY()));
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

                ToSSType = p.getSSEType();

                if (prev != null) {

                    FromSSType = prev.getSSEType();
                    if (!(ToSSType == SSEType.CTERMINUS || ToSSType == SSEType.NTERMINUS)
                      && (FromSSType == SSEType.CTERMINUS) || (FromSSType == SSEType.NTERMINUS)) {

                        if (prev.getNConnectionPoints() > 0) {
                            ncp = prev.getNConnectionPoints();

                            JoinPoints(PSxy(prev.getCartoonX()),
                                    PSxy(prev.getCartoonY()),
                                    PSxy(prev.getConnectionTo(0).x),
                                    PSxy(prev.getConnectionTo(0).y),
                                    prev.getDirection(), UNKNOWN,
                                    prev.getSSEType(),
                                    p.getSSEType());

                            for (i = 0; i < (ncp - 1); i++) {
                                JoinPoints(PSxy(prev.getConnectionTo(i).x),
                                        PSxy(prev.getConnectionTo(i).y),
                                        PSxy(prev.getConnectionTo(i + 1).x),
                                        PSxy(prev.getConnectionTo(i + 1).y),
                                        UNKNOWN, UNKNOWN, prev.getSSEType(),
                                        p.getSSEType());
                            }

                            JoinPoints(PSxy(prev.getConnectionTo(ncp - 1).x),
                                    PSxy(prev.getConnectionTo(ncp - 1).y),
                                    PSxy(p.getCartoonX()),
                                    PSxy(p.getCartoonY()), UNKNOWN, p.getDirection(),
                                    prev.getSSEType(),
                                    p.getSSEType());

                        } else {
                            JoinPoints(PSxy(prev.getCartoonX()),
                                    PSxy(prev.getCartoonY()),
                                    PSxy(p.getCartoonX()),
                                    PSxy(p.getCartoonY()), prev.getDirection(),
                                    p.getDirection(), prev.getSSEType(),
                                    p.getSSEType());
                        }

                    }

                    if (FromSSType == SSEType.NTERMINUS) {
                        MakeObject("Square", 2, PSxy(prev.getCartoonX()),
                                PSxy(prev.getCartoonY()));
                        PrintText(prev.getLabel(), PSxy(prev.getCartoonX()),
                                PSxy(prev.getCartoonY()));
                    }

                }

                if (ToSSType == SSEType.CTERMINUS) {
                    MakeObject("Square", 2, PSxy(p.getCartoonX()), PSxy(p.getCartoonY()));
                    PrintText(p.getLabel(), PSxy(p.getCartoonX()), PSxy(p.getCartoonY()));
                }
                
                prev = p;
            }
        }
    }

    /*
     * a function which writes out postscript code to define the symbols used in
     * cartoons
     */
    private void DefCartoonSymbols() {

        DefineObject("Square");
        Literal("moveto");
        MoveRelative(PSxy(-Radius) / 2.0, PSxy(Radius) / 2.0);
        LineRelative(PSxy(Radius), 0.0);
        LineRelative(0.0, PSxy(-Radius));
        LineRelative(PSxy(-Radius), 0.0);
        ClosePath();
        Fill(1.0);
        EndObject();

        DefineObject("Circle");
        buffer.append(String.format(" %d %d %d arc", ConvertXPoint(PSxy(Radius) * Scale), 0,360));
        Literal(buffer.toString());
        Fill(-1.0);
        OutLine();
        EndObject();

        DefineObject("UpTriangle");
        Literal("moveto");
        MoveRelative(0.0, PSxy(Radius));
        LineRelative(PSxy(Radius) * Math.sin(PI2 / 3.0),
                PSxy(Radius) * (Math.cos(PI2 / 3.0) - 1.0));
        LineRelative(-2.0 * PSxy(Radius) * Math.sin(PI2 / 3.0), 0.0);
        ClosePath();
        Fill(-1.0);
        OutLine();
        EndObject();

        DefineObject("DownTriangle");
        Literal("moveto");
        MoveRelative(0.0, PSxy(-Radius));
        LineRelative(PSxy(Radius) * Math.sin(PI2 / 3.0),
                PSxy(-Radius) * (Math.cos(PI2 / 3.0) - 1.0));
        LineRelative(-2.0 * PSxy(Radius) * Math.sin(Math.PI / 3.0), 0.0);
        ClosePath();
        Fill(-1.0);
        OutLine();
        EndObject();

        DefineObject("Line");
        Literal("moveto");
        Literal("lineto");
        OutLine();
        EndObject();

    }

    /*
     * function cross_circle
     * 
     * Tom F. August 1992
     * 
     * Function to return crossing point at which a line from A, B crosses the
     * circle X and Y are replaced by the crossing point
     */
    private Vector2d CrossCircle(double X, double Y, double A, double B) {
        int i;
        double itv = 20.0; 

        Point2d lp = new Point2d(0, 0);
        Point2d AB = new Point2d(A, B);
        Point2d XY = new Point2d(X, Y);
        for (i = 0; i <= itv; i++) {
            double rx = X + PSxy(Radius) * Math.sin((double) i * PI2 / itv) * Scale;
            double ry = Y + PSxy(Radius) * Math.cos((double) i * PI2 / itv) * Scale;
            if (i > 0) {
                Intersection intersection = 
                        intersectionCalculator.lineCross(lp, new Point2d(rx, ry), AB, XY);
                if (intersection.type != IntersectionCalculator.IntersectionType.NOT_CROSSING) {
                    return intersection.point;
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
            double rx = X + PSxy(Radius) * Math.sin((double) i * PI2 / itv);
            double ry = Y + PSxy(Radius) * Math.cos((double) i * PI2 / itv);
            if (i > 0) {
                Intersection intersection = intersectionCalculator.lineCross(lp, new Point2d(rx, ry), AB, XY);
                if (intersection.type != IntersectionCalculator.IntersectionType.NOT_CROSSING) {
                    return intersection.point;
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
            double rx = X + PSxy(Radius) * Math.sin((double) i * PI2 / itv);
            double ry = Y - PSxy(Radius) * Math.cos((double) i * PI2 / itv);
            if (i > 0) {
                Intersection intersection =
                        intersectionCalculator.lineCross(lp, new Point2d(rx, ry), AB, XY);
                if (intersection.type != IntersectionType.NOT_CROSSING) {
                    return intersection.point;
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
                CrossCircle(px, py, qx, qy);
        }
        if (direction2 != DOWN && direction2 != UNKNOWN) {
            if (pt == SSEType.EXTENDED)
                CrossUpTriangle(qx, qy, px, py);
            if (pt == SSEType.HELIX)
                CrossCircle(qx, qy, px, py);
        }
        MakeObject("Line", 4, px, py, qx, qy);
    }

    /*
     * function point_to_cm
     * 
     * Tom F. November 1992
     * 
     * Function to convert pointsize to centremetres (useful with text)
     */
    private double PointToCm(double point) {
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
    private boolean OpenPostscript(String output_file) throws FileNotFoundException {
        if ("".equals(output_file)) {
            OUT = System.out;
        } else {
            OUT = new PrintStream(new File(output_file));
        }
        fprintf(OUT, "%%!PS-Adobe-1.0\n");
        fprintf(OUT, "%%%%Creator: postgen written by Tom Flores\n");
        fprintf(OUT, "%%%%Title: postscript procedure\n");
        fprintf(OUT, "%%%%CreationDate: Unknown\n");
        fprintf(OUT, "%%%%Pages: 1\n");

        return true;    // XXX TODO
    }

    /*
     * function page_size
     * 
     * Tom F. October 1992
     * 
     * This function sets the page size
     */
    private void PageSize(double width, double height) {

        if (width > 0.0)
            WIDTH = width;
        if (height > 0.0)
            HEIGHT = height;
        if (WIDTH < PWIDTH)
            PWIDTH = WIDTH;
        if (HEIGHT < PHEIGHT)
            PHEIGHT = HEIGHT;
        fprintf(OUT, "%%%%BoundingBox: %.1f %.1f %.1f %.1f\n", 0.0, 0.0,
                WIDTH * PPI * CTOI, HEIGHT * PPI * CTOI);
    }

    /*
     * function picture_size
     * 
     * Tom F. October 1992
     * 
     * This function sets up the picture size
     */
    private void PictureSize(double width, double height) {
        if (width > 0.0)
            PWIDTH = width;
        if (height > 0.0)
            PHEIGHT = height;
    }

    /*
     * function scale_page
     * 
     * Tom F. October 1992
     * 
     * This function sets the page scale
     */
    private void ScalePage(double xmin, double xmax, double ymin, double ymax) {

        if (xmin < xmax) {
            XMIN = xmin;
            XMAX = xmax;
        }
        if (ymin < ymax) {
            YMIN = ymin;
            YMAX = ymax;
        }
        XP = (PWIDTH * PPI) / ((XMAX - XMIN) * ITOC);
        YP = (PHEIGHT * PPI) / ((YMAX - YMIN) * ITOC);
        if (XMIN != 0.0 && YMIN != 0.0)
            fprintf(OUT, "%.1f %.1f  translate\n",
                    -XMIN * PWIDTH / (XMAX - XMIN) * PPI * CTOI,
                    -YMIN * PHEIGHT / (YMAX - YMIN) * PPI * CTOI);
    }

    /*
     * function centre_page
     * 
     * Tom F. October 1992
     * 
     * This function centres a picture on a page
     */
    private void CentrePage() {
        fprintf(OUT, "%.1f %.1f translate\n",
                ((WIDTH - PWIDTH) / 2.0 * PPI * CTOI),
                ((HEIGHT - PHEIGHT) / 2.0 * PPI * CTOI));
    }

    /*
     * function perimeter
     * 
     * Tom F. October 1992
     * 
     * This function draws a box round the picture
     */
    private void Perimeter() {
        fprintf(OUT, "newpath\n");
        fprintf(OUT, "  %.1f %.1f moveto\n", (XMIN * XP), (YMIN * YP));
        fprintf(OUT, "  %.1f %.1f lineto\n", (XMIN * XP), (YMAX * YP));
        fprintf(OUT, "  %.1f %.1f lineto\n", (XMAX * XP), (YMAX * YP));
        fprintf(OUT, "  %.1f %.1f lineto\n", (XMAX * XP), (YMIN * YP));
        fprintf(OUT, "  %.1f %.1f lineto\n", (XMIN * XP), (YMIN * YP));
        fprintf(OUT, "  closepath\n");
        fprintf(OUT, "stroke\n");
    }

    /*
     * function define_object
     * 
     * Tom F. October 1992
     * 
     * Function to begin an object definition
     */
    private void DefineObject(String object_name) {
        fprintf(OUT, "/%s\n{ newpath\n", object_name);
    }

    /*
     * function end_object
     * 
     * Tom F. OCtober 1992
     * 
     * Function to finish object definition
     */
    private void EndObject() {
        fprintf(OUT, " } def\n");
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
    private void MakeObject(String object_name, int i, double... args) {
        fprintf(OUT, "  ");
        for (double arg : args) {
            fprintf(OUT, "%.1f ", (double) (arg * XP));
        }
        fprintf(OUT, "%s\n", object_name);
    }

    /*
     * function translate D. Westhead 11/09/96
     */
    private void Translate(double x, double y) {
        fprintf(OUT, "  %.1f %.1f translate\n", (x * XP), (y * YP));
    }

    /*
     * function move_to
     * 
     * Tom F. October 1992
     */
    private void MoveTo(double x, double y) {
        fprintf(OUT, "  %.1f %.1f moveto\n", (x * XP), (y * YP));
    }

    /*
     * Function move_relative
     * 
     * Tom F. October 1992
     */
    private void MoveRelative(double x, double y) {
        fprintf(OUT, "  %.1f %.1f rmoveto\n", (x * XP), (y * YP));
    }

    /*
     * function line_to
     * 
     * Tom F. October 1992
     */
    private void LineTo(double x, double y) {
        fprintf(OUT, "  %.1f %.1f lineto\n", (x * XP), (y * YP));
    }

    /*
     * function line_relative
     * 
     * Tom F. October 1992
     */
    private void LineRelative(double x, double y) {
        fprintf(OUT, "  %.1f %.1f rlineto\n", (x * XP), (y * YP));
    }

    /*
     * function close_path
     * 
     * Tom F. October 1992
     */
    private void ClosePath() {
        fprintf(OUT, "  closepath\n");
    }

    /*
     * function newpath
     * 
     * Tom F. October 1992
     */
    private void NewPath() {
        fprintf(OUT, "newpath\n");
    }

    /*
     * function end_postscript
     * 
     * Tom F. October 1992
     * 
     * Function to complete postscript picture
     */
    private void EndPostscript() {
        fprintf(OUT, "showpage\n");
        if (OUT != System.out)
            OUT.close();
    }

    /*
     * function outline
     * 
     * TOm F. October 1992
     */
    private void OutLine() {
        fprintf(OUT, "  stroke\n");
    }

    /*
     * function fill
     * 
     * Tom F. October 1992
     */
    private void Fill(double level) {
        if (level >= 0.0)
            fprintf(OUT, "  gsave\n  %.2f setgray fill\n  grestore\n", level);
        else
            fprintf(OUT, "  gsave\n  setgray fill\n  grestore\n");
    }

    /*
     * function line_width
     * 
     * Tom F. October 1992
     */
    private void LineWidth(double width) {
        fprintf(OUT, "  %.1f setlinewidth\n", (width * XP));
    }

    /*
     * function literal
     * 
     * Tom F. October 1992
     * 
     * Function to allow literal translation of postscript ie. written to file
     * directly - this allows for any additional bits to be added freely
     */
    private void Literal(String postlit) {
        fprintf(OUT, "  %s\n", postlit);
    }

    /*
     * function choose_font
     * 
     * Tom F. October 1992
     * 
     * function to select font
     */
    private void ChooseFont(String font, int point_size) {
        FONT = font;
        if (point_size > 0)
            POINT = point_size;
        fprintf(OUT, "/%s findfont %d scalefont setfont\n", FONT, POINT);
    }

    /*
     * function character_height
     * 
     * Tom F. October 1992
     * 
     * This function set the character height
     */
    private void CharacterHeight(double height) {
        if (POINT != height * YP && height * YP > 0.0) {
            POINT = (int) (height * YP);
            ChooseFont(FONT, POINT);
        }
    }

    /*
     * function print_text
     * 
     * Tom F. October 1992
     * 
     * Function to output text
     */
    private void PrintText(String text, double x, double y) {
        fprintf(OUT, "  %.1f %.1f moveto\n", (x * XP),
                (y * YP - TEXTHGT * POINT));
        fprintf(OUT, "  (%s) dup stringwidth pop\n", text);
        fprintf(OUT, "  %.2f mul 0 rmoveto\n", -TEXTPOS);
        fprintf(OUT, "   show\n");
    }

    /*
     * function text_centre
     * 
     * Tom F. October 1992
     * 
     * Function to determine text centre value as fraction
     */
    private void TextCentre(double length, double height) {
        TEXTPOS = length;
        TEXTHGT = height;
    }

    /*
     * function convert_x_point
     * 
     * Tom F. October 1992
     * 
     * This function converts a given x value to its point value
     */
    private int ConvertXPoint(double value) {
        return (int) (value * XP);
    }

    private double Verbatim(double value) {
        return value / XP;
    }

    /*
     * function convert_y_point
     * 
     * Tom F. October 1992
     * 
     * This function converts a given y value to its point value
     */
    private int ConvertYPoint(double value) {
        return (int) (value * YP);
    }

    /*
     * function debug_ps
     * 
     * Tom F. October 1992
     * 
     * This function outputs the values and states of internal variables
     */
    void debug_ps() {
        fprintf(System.err, "PS - variables:\n");
        fprintf(System.err, "WIDTH     = %f\n", WIDTH);
        fprintf(System.err, "HEIGHT    = %f\n", HEIGHT);
        fprintf(System.err, "PWIDTH    = %f\n", PWIDTH);
        fprintf(System.err, "PHEIGHT   = %f\n", PHEIGHT);
        fprintf(System.err, "XMIN      = %f\n", XMIN);
        fprintf(System.err, "XMAX      = %f\n", XMAX);
        fprintf(System.err, "YMIN      = %f\n", YMIN);
        fprintf(System.err, "YMAX      = %f\n", YMAX);
        fprintf(System.err, "XP        = %f\n", XP);
        fprintf(System.err, "YP        = %f\n", YP);
    }

    private void fprintf(PrintStream stream, String message,
            Object... inserts) {

    }

}