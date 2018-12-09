package tops.dw.editor;

import java.awt.*;
import java.util.*;
import java.util.List;


public final class PostscriptFactory {

    private static double root3 = Math.sqrt(3.0);

    public static int A4Width = 595;

    public static int A4Height = 841;

    public static Vector<String> makeEPSHeader(Vector<String> ps, int bbx1, int bby1, int bbx2,
            int bby2) {
        if (ps == null) {
            ps = new Vector<String>();
        }

        ps.addElement("%!PS-Adobe-3.0 EPSF-3.0");
        ps
                .addElement("%%Creator: DWPostscriptFactoryClass, Copyright 1998, European Bioinformatics Institute");
        ps.addElement("%%BoundingBox: " + bbx1 + " " + bby1 + " " + bbx2 + " "
                + bby2);
        ps.addElement("%%EndComments");
        ps.addElement("%%EndProlog");

        return ps;

    }

    public static Vector<String> makePSHeader(Vector<String> ps, int npages, int page_order) {
        if (ps == null) {
            ps = new Vector<String>();
        }

        ps.addElement("%!PS-Adobe-3.0");
        ps
                .addElement("%%Creator: DWPostscriptFactoryClass, Copyright 1998, European Bioinformatics Institute");
        ps.addElement(PostscriptFactory.Pages(npages));
        ps.addElement(PostscriptFactory.PageOrder(page_order));
        ps.addElement("%%EndComments");
        ps.addElement("%%EndProlog");

        return ps;

    }

    public static Vector<String> addBoundingBox(
    		Vector<String> ps, int bbx1, int bby1, int bbx2, int bby2) {
        if (ps == null)
            return null;

        Enumeration<String> en = ps.elements();

        String s;
        int i = 0;
        boolean found = false;
        while (en.hasMoreElements()) {
            s = (String) en.nextElement();
            if (s.startsWith("%%BoundingBox")) {
                found = true;
                break;
            }
            ++i;
        }

        if (found) {
            ps.setElementAt("%%BoundingBox: " + bbx1 + " " + bby1 + " " + bbx2 + " " + bby2, i);
        } else {
            ps.insertElementAt("%%BoundingBox: " + bbx1 + " " + bby1 + " " + bbx2 + " " + bby2, 1);
        }

        return ps;

    }

    public static String translate(int x, int y) {
        StringBuffer sb = new StringBuffer();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("translate");

        return sb.toString();
    }

    public static String scale(float sx, float sy) {
        StringBuffer sb = new StringBuffer();

        sb.append(sx);
        sb.append(" ");
        sb.append(sy);
        sb.append(" ");
        sb.append("scale");

        return sb.toString();
    }

    public static Vector<String> makeUpTriangle(int x, int y, int r, Color c, Vector<String> ps) {
        int dx, dy;
        Vector<String> postscript;

        if (ps == null) {
            postscript = new Vector<String>();
        } else {
            postscript = ps;
        }

        postscript.addElement(PostscriptFactory.newPath());
        postscript.addElement(PostscriptFactory.makeMove(x, y));
        postscript.addElement(PostscriptFactory.makeRelativeMove(0, r));

        double dbr = r;
        dx = (int) (dbr * (PostscriptFactory.root3 / 2.0));
        dy = (int) (-1.5 * dbr);
        postscript.addElement(PostscriptFactory.makeRelativeLine(dx, dy));

        dx = (int) (-dbr * PostscriptFactory.root3);
        dy = 0;
        postscript.addElement(PostscriptFactory.makeRelativeLine(dx, dy));

        postscript.addElement(PostscriptFactory.closePath());
        if ((c != null)) {
            postscript.addElement(PostscriptFactory.gsave());
            postscript.addElement(PostscriptFactory.setColour(c));
            postscript.addElement(PostscriptFactory.fill());
            postscript.addElement(PostscriptFactory.grestore());
            postscript.addElement(PostscriptFactory.setColour(Color.black));
        }
        postscript.addElement(PostscriptFactory.stroke());

        postscript.addElement(PostscriptFactory.makeMove(x, y));

        return postscript;
    }

    public static Vector<String> makeDownTriangle(int x, int y, int r, Color c,
            Vector<String> ps) {
        int dx, dy;

        Vector<String> postscript;

        if (ps == null) {
            postscript = new Vector<String>();
        } else {
            postscript = ps;
        }

        postscript.addElement(PostscriptFactory.newPath());
        postscript.addElement(PostscriptFactory.makeMove(x, y));
        postscript.addElement(PostscriptFactory.makeRelativeMove(0, -r));

        double dbr = r;
        dx = (int) (dbr * (PostscriptFactory.root3 / 2.0));
        dy = (int) (1.5 * dbr);
        postscript.addElement(PostscriptFactory.makeRelativeLine(dx, dy));

        dx = (int) (-dbr * PostscriptFactory.root3);
        dy = 0;
        postscript.addElement(PostscriptFactory.makeRelativeLine(dx, dy));

        postscript.addElement(PostscriptFactory.closePath());
        if ((c != null)) {
            postscript.addElement(PostscriptFactory.gsave());
            postscript.addElement(PostscriptFactory.setColour(c));
            postscript.addElement(PostscriptFactory.fill());
            postscript.addElement(PostscriptFactory.grestore());
            postscript.addElement(PostscriptFactory.setColour(Color.black));
        }
        postscript.addElement(PostscriptFactory.stroke());

        postscript.addElement(PostscriptFactory.makeMove(x, y));

        return postscript;
    }

    public static Vector<String> makeCircle(int x, int y, int r, Color c, Vector<String> ps) {
        Vector<String> postscript;

        if (ps == null) {
            postscript = new Vector<String>();
        } else {
            postscript = ps;
        }

        StringBuffer sb = new StringBuffer();

        postscript.addElement(PostscriptFactory.newPath());

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append(r);
        sb.append(" 0 360 arc");

        postscript.addElement(sb.toString());
        postscript.addElement(PostscriptFactory.closePath());

        if ((c != null)) {
            postscript.addElement(PostscriptFactory.gsave());
            postscript.addElement(PostscriptFactory.setColour(c));
            postscript.addElement(PostscriptFactory.fill());
            postscript.addElement(PostscriptFactory.grestore());
            postscript.addElement(PostscriptFactory.setColour(Color.black));
        }
        postscript.addElement(PostscriptFactory.stroke());

        postscript.addElement(PostscriptFactory.makeMove(x, y));

        return postscript;

    }

    public static Vector<String> makeText(String font, int font_size, int x, int y,
            String text, Vector<String> ps) {
        Vector<String> postscript;

        if (ps == null) {
            postscript = new Vector<String>();
        } else {
            postscript = ps;
        }

        postscript.addElement("/" + font + " findfont");
        StringBuffer sb = new StringBuffer();
        sb.append(font_size);
        sb.append(" scalefont");
        postscript.addElement(sb.toString());
        postscript.addElement("setfont");
        sb = new StringBuffer();
        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" moveto");
        postscript.addElement(sb.toString());
        postscript.addElement("(" + text + ")" + " show");

        return postscript;

    }

    public static String newPath() {
        return "newpath";
    }

    public static String closePath() {
        return "closepath";
    }

    public static String makeMove(int x, int y) {
        StringBuffer sb = new StringBuffer();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("moveto");

        return sb.toString();
    }

    public static String makeRelativeMove(int x, int y) {
        StringBuffer sb = new StringBuffer();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("rmoveto");

        return sb.toString();
    }

    public static String makeRelativeLine(int x, int y) {
        StringBuffer sb = new StringBuffer();

        sb.append(x);
        sb.append(" ");
        sb.append(y);
        sb.append(" ");
        sb.append("rlineto");

        return sb.toString();
    }

    public static String makeLine(int x, int y) {
        StringBuffer sb = new StringBuffer();

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
        StringBuffer sb = new StringBuffer();
        sb.append(r);
        sb.append(" ");
        sb.append(g);
        sb.append(" ");
        sb.append(b);
        sb.append(" setrgbcolor");
        return sb.toString();
    }

    public static String stroke() {
        return "stroke";
    }

    public static String fill() {
        return "fill";
    }

    public static String gsave() {
        return "gsave";
    }

    public static String grestore() {
        return "grestore";
    }

    public static String showpage() {
        return "showpage";
    }

    public static String EndDocument() {
        return "%%EndDocument";
    }

    public static String EOF() {
        return "%%EOF";
    }

    public static String Page(int x, int y) {
        StringBuffer stb = new StringBuffer("%%Page: ");
        stb.append(x);
        stb.append(" ");
        stb.append(y);
        return stb.toString();
    }

    public static String Pages(int x) {
        StringBuffer stb = new StringBuffer("%%Pages: ");
        stb.append(x);
        return stb.toString();
    }

    public static String PageOrder(int x) {
        StringBuffer stb = new StringBuffer("%%PageOrder: ");
        if (x >= 0)
            stb.append("Ascend");
        else
            stb.append("Descend");
        return stb.toString();
    }

    // a method to make a PS array from a set of EPS files
    private static int border = 36;

    public static Vector<String> PSArrayA4(
    		List<String> titles, List<String> ePSS, int margin, float scle) throws PSException {
        if (ePSS == null || ePSS.isEmpty())
            throw new PSException("Nothing to draw!");
        if (scle <= 0.01f)
            throw new PSException("Scale value too small");

        float invscle = 1.0f / scle;

        Vector<String> PS = new Vector<String>();

        int nfiles = ePSS.size();

        // determine largest X and Y sizes
        int xs, ys, maxx = 0, maxy = 0;
        int bb[];
        for (String eps : ePSS) {
            bb = PostscriptFactory.getBoundingBox(eps);
            xs = bb[2] - bb[0];
            ys = bb[3] - bb[1];
            if (xs > maxx)
                maxx = xs;
            if (ys > maxy)
                maxy = ys;
        }
        maxx = Math.round(scle * maxx);
        maxy = Math.round(scle * maxy);

        // determine array parameters and number of pages required.
        int xsize = maxx + PostscriptFactory.border;
        int ysize = maxy + PostscriptFactory.border;
        int nx = (PostscriptFactory.A4Width - 2 * margin) / xsize;
        int ny = (PostscriptFactory.A4Height - 2 * margin) / ysize;

        if (nx <= 0 || ny <= 0)
            throw new PSException("Diagrams too big for page!");

        int npages = 1 + (nfiles - 1) / (nx * ny);

        PS = PostscriptFactory.makePSHeader(PS, npages, 1);

        // construct the array
        Iterator<String> ent = titles.iterator();
        Iterator<String> enf = ePSS.iterator();
        int nf = 0, ix, iy = -1, np = 0;
        int lowleft[] = new int[2];
        int xt, yt, tx, ty;
        String ttl;
        while (enf.hasNext()) {

            // new page
            if (PostscriptFactory.mod(nf, nx * ny) == 0) {
                np++;
                iy = -1;
                if (nf > 0)
                    PS.addElement(PostscriptFactory.showpage());
                PS.addElement(PostscriptFactory.Page(np, np));
            }
            ix = PostscriptFactory.mod(nf, nx);
            if (ix == 0)
                iy++;

            // translate to the lower left corner
            lowleft[0] = margin + PostscriptFactory.border / 2 + ix * xsize;
            lowleft[1] = margin + (ny - iy - 1) * ysize;
            PS.addElement(PostscriptFactory.translate(lowleft[0], lowleft[1]));

            // get bounding box
            String eps = enf.next();
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
            PS = PostscriptFactory.makeText("TimesRoman", ttls, tx, ty, ttl, PS);

            // translate and scale so that bounding box lies in correct position
            PS.addElement(PostscriptFactory.scale(scle, scle));
            xt = -Math.round(scle * bb[0]);
            yt = -Math.round(scle * bb[1]);
            PS.addElement(PostscriptFactory.translate(xt, yt));

            // add the EPS file (may need a new path)
            PS.addElement(PostscriptFactory.newPath());

            PS.addElement(eps);

            // reposition the origin at the lower left of the page
            PS.addElement(PostscriptFactory.translate(-xt, -yt));
            PS.addElement(PostscriptFactory.scale(invscle, invscle));
            PS.addElement(PostscriptFactory.translate(-lowleft[0], -lowleft[1]));

            // increment file number
            nf++;

        }

        PS.addElement(PostscriptFactory.showpage());
        PS.addElement(PostscriptFactory.EndDocument());
        PS.addElement(PostscriptFactory.EOF());

        return PS;

    }

    private static void appendEPS(Vector<String> PS, Vector<String> EPS) {
        String line;
        Enumeration<String> en = EPS.elements();
        while (en.hasMoreElements()) {
            line = en.nextElement();
            if (!line.startsWith("%") && !line.startsWith("showpage")) {
                PS.addElement(line);
            }
        }
    }

    // returns n mod m
    private static int mod(int n, int m) {
        int md;
        md = n - (n / m) * m;
        return md;
    }

    private static int[] getBoundingBox(String eps) throws PSException {
        String tok;
        StringTokenizer st;
        int b, i;
        int bb[] = new int[4];
        for (i = 0; i < 4; i++)
            bb[i] = 0;

        if (eps != null) {
            String[] e = eps.split("\n");    // TODO
            for (String line : e) {
                if (line.startsWith("%%BoundingBox:")) {
                    st = new StringTokenizer(line);
                    if (st.countTokens() == 5) {
                        tok = st.nextToken();
                        for (i = 0; i < 4; i++) {
                            tok = st.nextToken();
                            try {
                                b = Integer.parseInt(tok);
                            } catch (NumberFormatException nfe) {
                                throw new PSException(
                                        "Incorrect specification of bounding box");
                            }
                            bb[i] = b;
                        }
                    }
                    break;
                }
            }
        }

        return bb;

    }

}
