package tops.view.cartoon.builder;

//Make PS Documents

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.util.Vector;

import tops.view.cartoon.PostscriptFactory;
import tops.view.cartoon.TextCartoonBuilder;

public class PSBuilder implements TextCartoonBuilder {
    
    private String font = "Times-Roman";

    private Vector<String> eps; // the product that this builder will return

//    private Rectangle bBox; // the bounds of the cartoon

    private int h; // convenience variable

    public PSBuilder(Rectangle bBox) {
        this.eps = new Vector<String>();
        this.eps = PostscriptFactory.makeEPSHeader(this.eps, 0, 0, bBox.width, bBox.height);
//        this.bBox = bBox;
        this.h = bBox.height;
    }

    public void printProduct(PrintWriter out) {
        if (this.eps != null) {
            // eps = PostscriptFactory.addBoundingBox( eps, bBox.x, bBox.y,
            // bBox.x + bBox.width, bBox.y + bBox.height );

            this.eps.addElement(PostscriptFactory.showpage());
            this.eps.addElement(PostscriptFactory.EndDocument());
            this.eps.addElement(PostscriptFactory.EOF());
            for (int idx = 0; idx < this.eps.size(); idx++) {
                String s = (String) this.eps.get(idx);
                out.println(s);
                //System.err.println(s);
            }
        }
    }

    public void connect(Point p1, Point p2) {
        this.eps.addElement(PostscriptFactory.makeMove(this.h - p1.x, p1.y));
        this.eps.addElement(PostscriptFactory.makeLine(this.h - p1.x, p1.y));
        this.eps.addElement(PostscriptFactory.stroke());
    }

    public void drawHelix(Point center, int rad, Color c) {
        this.eps = PostscriptFactory.makeCircle(
                this.h - center.x, center.y, rad, c, this.eps);
    }

    public void drawStrand(Point center, Point left, Point right, Color c) {
        this.eps.addElement(PostscriptFactory.newPath());
        this.eps.addElement(PostscriptFactory.makeMove(this.h - center.x, center.x));
        this.eps.addElement(PostscriptFactory.makeLine(this.h - left.x, left.y));
        this.eps.addElement(PostscriptFactory.makeLine(this.h - right.x, right.y));
        this.eps.addElement(PostscriptFactory.closePath());
        // do colour stuff
        this.eps.addElement(PostscriptFactory.gsave());
        this.eps.addElement(PostscriptFactory.setColour(c));
        this.eps.addElement(PostscriptFactory.fill());
        this.eps.addElement(PostscriptFactory.grestore());
        this.eps.addElement(PostscriptFactory.setColour(Color.black));
        // end colour stuff
        this.eps.addElement(PostscriptFactory.stroke());
        this.eps.addElement(PostscriptFactory.makeMove(this.h - center.x, center.y));
    }

    public void drawTerminus(Point center, int r, String label) {
        this.eps = PostscriptFactory.makeText(font, 
                (3 * r) / 4, this.h - center.x, center.y, label, this.eps);
    }

    @Override
    public void drawLabel(Point center, String text) {
        this.eps = PostscriptFactory.makeText(font, 12, center.x, center.y, text, eps);
    }
}
