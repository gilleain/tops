package tops.view.cartoon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public final class PostscriptFactory {

    private static final String EOF = "%%EOF";

    private static final String END_DOCUMENT = "%%EndDocument";

    private static final String SHOWPAGE = "showpage";

    private static final String GRESTORE = "grestore";

    private static final String GSAVE = "gsave";

    private static final String FILL = "fill";

    private static final String STROKE = "stroke";

    private static final String BOUNDING_BOX = "%%BoundingBox: ";

    private static double root3 = Math.sqrt(3.0);

    public static final int A4_WIDTH = 595;

    public static final int A4_HEIGHT = 841;
    
    private PostscriptFactory() {
        // prevent construction
    }

    public static List<String> makeEPSHeader(List<String> ps, int bbx1, int bby1, int bbx2,
            int bby2) {
        if (ps == null) {
            ps = new ArrayList<>();
        }

        ps.add("%!PS-Adobe-3.0 EPSF-3.0");
        ps.add("%%Creator: DWPostscriptFactoryClass, Copyright 1998, European Bioinformatics Institute");
        ps.add(BOUNDING_BOX + bbx1 + " " + bby1 + " " + bbx2 + " " + bby2);
        ps.add("%%EndComments");
        ps.add("%%EndProlog");

        return ps;

    }

    public static List<String> makePSHeader(List<String> ps, int npages, int pageOrder) {
        if (ps == null) {
            ps = new ArrayList<>();
        }

        ps.add("%!PS-Adobe-3.0");
        ps.add("%%Creator: DWPostscriptFactoryClass, Copyright 1998, European Bioinformatics Institute");
        ps.add(PostscriptFactory.pages(npages));
        ps.add(PostscriptFactory.pageOrder(pageOrder));
        ps.add("%%EndComments");
        ps.add("%%EndProlog");

        return ps;

    }

    public static List<String> addBoundingBox(List<String> ps, int bbx1, int bby1, int bbx2, int bby2) {
        if (ps == null)
            return new ArrayList<>();

        int i = 0;
        boolean found = false;
        for (String s : ps) {
            if (s.startsWith("%%BoundingBox")) {
                found = true;
                break;
            }
            i++;
        }

        if (found) {
            ps.set(i, BOUNDING_BOX + bbx1 + " " + bby1 + " " + bbx2 + " " + bby2);
        } else {
            ps.add(i, BOUNDING_BOX + bbx1 + " " + bby1 + " " + bbx2 + " " + bby2);
        }

        return ps;

    }

    public static String translate(int x, int y) {
        StringBuilder sb = new StringBuilder();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("translate");

        return sb.toString();
    }

    public static String scale(float sx, float sy) {
        StringBuilder sb = new StringBuilder();

        sb.append(sx);
        sb.append(" ");
        sb.append(sy);
        sb.append(" ");
        sb.append("scale");

        return sb.toString();
    }

    public static List<String> makeUpTriangle(int x, int y, int r, Color c, List<String> ps) {
        int dx;
        int dy;
        List<String> postscript;

        if (ps == null) {
            postscript = new ArrayList<>();
        } else {
            postscript = ps;
        }

        postscript.add(PostscriptFactory.newPath());
        postscript.add(PostscriptFactory.makeMove(x, y));
        postscript.add(PostscriptFactory.makeRelativeMove(0, r));

        double dbr = r;
        dx = (int) (dbr * (PostscriptFactory.root3 / 2.0));
        dy = (int) (-1.5 * dbr);
        postscript.add(PostscriptFactory.makeRelativeLine(dx, dy));

        dx = (int) (-dbr * PostscriptFactory.root3);
        dy = 0;
        postscript.add(PostscriptFactory.makeRelativeLine(dx, dy));

        postscript.add(PostscriptFactory.closePath());
        if ((c != null)) {
            postscript.add(PostscriptFactory.gsave());
            postscript.add(PostscriptFactory.setColour(c));
            postscript.add(PostscriptFactory.fill());
            postscript.add(PostscriptFactory.grestore());
            postscript.add(PostscriptFactory.setColour(Color.black));
        }
        postscript.add(PostscriptFactory.stroke());

        postscript.add(PostscriptFactory.makeMove(x, y));

        return postscript;
    }

    public static List<String> makeDownTriangle(int x, int y, int r, Color c, List<String> ps) {
        int dx;
        int dy;

        List<String> postscript;

        if (ps == null) {
            postscript = new ArrayList<>();
        } else {
            postscript = ps;
        }

        postscript.add(PostscriptFactory.newPath());
        postscript.add(PostscriptFactory.makeMove(x, y));
        postscript.add(PostscriptFactory.makeRelativeMove(0, -r));

        double dbr = r;
        dx = (int) (dbr * (PostscriptFactory.root3 / 2.0));
        dy = (int) (1.5 * dbr);
        postscript.add(PostscriptFactory.makeRelativeLine(dx, dy));

        dx = (int) (-dbr * PostscriptFactory.root3);
        dy = 0;
        postscript.add(PostscriptFactory.makeRelativeLine(dx, dy));

        postscript.add(PostscriptFactory.closePath());
        if ((c != null)) {
            postscript.add(PostscriptFactory.gsave());
            postscript.add(PostscriptFactory.setColour(c));
            postscript.add(PostscriptFactory.fill());
            postscript.add(PostscriptFactory.grestore());
            postscript.add(PostscriptFactory.setColour(Color.black));
        }
        postscript.add(PostscriptFactory.stroke());

        postscript.add(PostscriptFactory.makeMove(x, y));

        return postscript;
    }

    public static List<String> makeCircle(int x, int y, int r, Color c, List<String> ps) {
        List<String> postscript;

        if (ps == null) {
            postscript = new ArrayList<>();
        } else {
            postscript = ps;
        }

        StringBuilder sb = new StringBuilder();

        postscript.add(PostscriptFactory.newPath());

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append(r);
        sb.append(" 0 360 arc");

        postscript.add(sb.toString());
        postscript.add(PostscriptFactory.closePath());

        if ((c != null)) {
            postscript.add(PostscriptFactory.gsave());
            postscript.add(PostscriptFactory.setColour(c));
            postscript.add(PostscriptFactory.fill());
            postscript.add(PostscriptFactory.grestore());
            postscript.add(PostscriptFactory.setColour(Color.black));
        }
        postscript.add(PostscriptFactory.stroke());

        postscript.add(PostscriptFactory.makeMove(x, y));

        return postscript;

    }

    public static List<String> makeText(String font, int fontSize, int x, int y,
            String text, List<String> ps) {
        List<String> postscript;

        if (ps == null) {
            postscript = new ArrayList<>();
        } else {
            postscript = ps;
        }

        postscript.add("/" + font + " findfont");
        StringBuilder sb = new StringBuilder();
        sb.append(fontSize);
        sb.append(" scalefont");
        postscript.add(sb.toString());
        postscript.add("setfont");
        sb = new StringBuilder();
        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" moveto");
        postscript.add(sb.toString());
        postscript.add("(" + text + ")" + " show");

        return postscript;

    }

    public static String newPath() {
        return "newpath";
    }

    public static String closePath() {
        return "closepath";
    }

    public static String makeMove(int x, int y) {
        StringBuilder sb = new StringBuilder();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("moveto");

        return sb.toString();
    }

    public static String makeRelativeMove(int x, int y) {
        StringBuilder sb = new StringBuilder();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("rmoveto");

        return sb.toString();
    }

    public static String makeRelativeLine(int x, int y) {
        StringBuilder sb = new StringBuilder();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("rlineto");

        return sb.toString();
    }

    public static String makeLine(int x, int y) {
        StringBuilder sb = new StringBuilder();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("lineto");

        return sb.toString();
    }

    public static String setColour(Color c) {
        return PostscriptFactory.setColour(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static String setColour(int r, int g, int b) {
        return PostscriptFactory.setColour(r / 255.0f, g / 255.0f,
                b / 255.0f);
    }

    public static String setColour(float r, float g, float b) {
        StringBuilder sb = new StringBuilder();
        sb.append(r);
        sb.append(" ");
        sb.append(g);
        sb.append(" ");
        sb.append(b);
        sb.append(" setrgbcolor");
        return sb.toString();
    }

    public static String stroke() {
        return STROKE;
    }

    public static String fill() {
        return FILL;
    }

    public static String gsave() {
        return GSAVE;
    }

    public static String grestore() {
        return GRESTORE;
    }

    public static String showpage() {
        return SHOWPAGE;
    }

    public static String endDocument() {
        return END_DOCUMENT;
    }

    public static String eof() {
        return EOF;
    }

    public static String page(int x, int y) {
        StringBuilder stb = new StringBuilder("%%Page: ");
        stb.append(x);
        stb.append(" ");
        stb.append(y);
        return stb.toString();
    }

    public static String pages(int x) {
        StringBuilder stb = new StringBuilder("%%Pages: ");
        stb.append(x);
        return stb.toString();
    }

    public static String pageOrder(int x) {
        StringBuilder stb = new StringBuilder("%%PageOrder: ");
        if (x >= 0)
            stb.append("Ascend");
        else
            stb.append("Descend");
        return stb.toString();
    }

    // a method to make a PS array from a set of EPS files
    private static int border = 36;

    public static List<String> psArrayA4(
    		List<String> titles, List<List<String>> epsFiles, int margin, float scale) throws PSException {
        if (epsFiles == null || epsFiles.isEmpty())
            throw new PSException("Nothing to draw!");
        if (scale <= 0.01f)
            throw new PSException("Scale value too small");

        float invscle = 1.0f / scale;

        List<String> postscript = new ArrayList<>();

        int nfiles = epsFiles.size();

        // determine largest X and Y sizes
        int xs;
        int ys;
        int maxx = 0;
        int maxy = 0;
        int[] bb;
        for (List<String> eps : epsFiles) {
            bb = PostscriptFactory.getBoundingBox(eps);
            xs = bb[2] - bb[0];
            ys = bb[3] - bb[1];
            if (xs > maxx)
                maxx = xs;
            if (ys > maxy)
                maxy = ys;
        }
        maxx = Math.round(scale * maxx);
        maxy = Math.round(scale * maxy);

        // determine array parameters and number of pages required.
        int xsize = maxx + PostscriptFactory.border;
        int ysize = maxy + PostscriptFactory.border;
        int nx = (PostscriptFactory.A4_WIDTH - 2 * margin) / xsize;
        int ny = (PostscriptFactory.A4_HEIGHT - 2 * margin) / ysize;

        if (nx <= 0 || ny <= 0)
            throw new PSException("Diagrams too big for page!");

        int npages = 1 + (nfiles - 1) / (nx * ny);

        postscript = PostscriptFactory.makePSHeader(postscript, npages, 1);

        // construct the array
        Iterator<String> ent = titles.iterator();
        int nf = 0;
        int ix;
        int iy = -1;
        int np = 0;
        int[] lowleft = new int[2];
        int xt;
        int yt;
        int tx;
        int ty;
        String ttl;
        for (List<String> eps : epsFiles) {

            // new page
            if (PostscriptFactory.mod(nf, nx * ny) == 0) {
                np++;
                iy = -1;
                if (nf > 0)
                    postscript.add(PostscriptFactory.showpage());
                postscript.add(PostscriptFactory.page(np, np));
            }
            ix = PostscriptFactory.mod(nf, nx);
            if (ix == 0)
                iy++;

            // translate to the lower left corner
            lowleft[0] = margin + PostscriptFactory.border / 2 + ix * xsize;
            lowleft[1] = margin + (ny - iy - 1) * ysize;
            postscript.add(PostscriptFactory.translate(lowleft[0], lowleft[1]));

            // get bounding box
            bb = PostscriptFactory.getBoundingBox(eps);

            // add title
            if (ent.hasNext())
                ttl = ent.next();
            else
                ttl = "NoTitle";
            int ttls = 12;
            ty = ysize - (2 * PostscriptFactory.border) / 3;
            // tx = xsize/2 - border/2 - 12*ttl.length()/2;
            tx = xsize / 2 - PostscriptFactory.border / 2;
            postscript = PostscriptFactory.makeText("TimesRoman", ttls, tx, ty, ttl, postscript);

            // translate and scale so that bounding box lies in correct position
            postscript.add(PostscriptFactory.scale(scale, scale));
            xt = -Math.round(scale * bb[0]);
            yt = -Math.round(scale * bb[1]);
            postscript.add(PostscriptFactory.translate(xt, yt));

            // add the EPS file (may need a new path)
            postscript.add(PostscriptFactory.newPath());

            PostscriptFactory.appendEPS(postscript);

            // reposition the origin at the lower left of the page
            postscript.add(PostscriptFactory.translate(-xt, -yt));
            postscript.add(PostscriptFactory.scale(invscle, invscle));
            postscript.add(PostscriptFactory.translate(-lowleft[0], -lowleft[1]));

            // increment file number
            nf++;

        }

        postscript.add(PostscriptFactory.showpage());
        postscript.add(PostscriptFactory.endDocument());
        postscript.add(PostscriptFactory.eof());

        return postscript;

    }

    private static void appendEPS(List<String> postscript) {
        for (String line : postscript) {
            if (!line.startsWith("%") && !line.startsWith(SHOWPAGE)) {
                postscript.add(line);
            }
        }
    }

    // returns n mod m
    private static int mod(int n, int m) {
        int md;
        md = n - (n / m) * m;
        return md;
    }

    private static int[] getBoundingBox(List<String> eps) throws PSException {
        if (eps != null) {
            for (String line : eps) {
                if (line.startsWith("%%BoundingBox:")) {
                    return getBoundingBox(line);
                }
            }
        }
        return new int[] {0, 0, 0, 0};
    }
    
    private static int[] getBoundingBox(String line) throws PSException {
        int[] bb = new int[4];
        StringTokenizer st = new StringTokenizer(line);
        if (st.countTokens() == 5) {
            st.nextToken();
            for (int i = 0; i < 4; i++) {
                String tok = st.nextToken();
                try {
                    int b = Integer.parseInt(tok);
                    bb[i] = b;
                } catch (NumberFormatException nfe) {
                    throw new PSException(
                            "Incorrect specification of bounding box");
                }
            }
        }
        return bb;
    }

}
