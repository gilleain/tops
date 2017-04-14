package tops.view.cartoon.builder;

//Make PS Documents

import java.awt.Color;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.util.Vector;

import tops.view.cartoon.PostscriptFactory;
import tops.view.cartoon.TextCartoonBuilder;

public class PSBuilder implements TextCartoonBuilder {

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

    public void connect(int x1, int y1, int x2, int y2) {
        this.eps.addElement(PostscriptFactory.makeMove(this.h - x1, y1));
        this.eps.addElement(PostscriptFactory.makeLine(this.h - x2, y2));
        this.eps.addElement(PostscriptFactory.stroke());
    }

    public void drawHelix(int x, int y, int rad, Color c) {
        this.eps = PostscriptFactory.makeCircle(this.h - x, y, rad, c, this.eps);
    }

    public void drawStrand(int pointX, int pointY, int leftX, int leftY,
            int rightX, int rightY, Color c) {
        this.eps.addElement(PostscriptFactory.newPath());
        this.eps.addElement(PostscriptFactory.makeMove(this.h - pointX, pointY));
        this.eps.addElement(PostscriptFactory.makeLine(this.h - leftX, leftY));
        this.eps.addElement(PostscriptFactory.makeLine(this.h - rightX, rightY));
        this.eps.addElement(PostscriptFactory.closePath());
        // do colour stuff
        this.eps.addElement(PostscriptFactory.gsave());
        this.eps.addElement(PostscriptFactory.setColour(c));
        this.eps.addElement(PostscriptFactory.fill());
        this.eps.addElement(PostscriptFactory.grestore());
        this.eps.addElement(PostscriptFactory.setColour(Color.black));
        // end colour stuff
        this.eps.addElement(PostscriptFactory.stroke());
        this.eps.addElement(PostscriptFactory.makeMove(this.h - pointX, pointY));
    }

    public void drawTerminus(int x, int y, int r, String label) {
        this.eps = PostscriptFactory.makeText("Times-Roman", (3 * r) / 4, this.h - x, y,
                label, this.eps);
    }
}
