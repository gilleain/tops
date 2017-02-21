package tops.view.cartoon.builder;

//Make SVG Documents

import java.awt.Color;
import java.awt.Rectangle;
import java.io.PrintWriter;

import tops.view.cartoon.CartoonBuilder;

public class SVGBuilder implements CartoonBuilder {

    private StringBuffer document; // the product that this builder will return

    private static final String cssfile = "file:///Users/maclean/development/tops/style.css"; // path to the css file

    private PrintWriter out; // the stream to print to

    public SVGBuilder(Rectangle bb, PrintWriter out) {
        int x = bb.x;
        int y = bb.y;
        int height = bb.height;
        int width = bb.width;
        this.out = out;

        this.document = new StringBuffer();
        this.document.append("<?xml version=\"1.0\" standalone=\"no\"?>\n");
        this.document.append("<?xml-stylesheet href=\"" + SVGBuilder.cssfile + "\" type=\"text/css\"?>"); // stylesheet
        this.document.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" ");
        this.document
                .append("\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n");
        this.document.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ");
        this.document.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
        this.document.append("id=\"body\" width=\"").append(width).append("\" height=\"");
        this.document.append(height).append("\" viewBox=\"0 0 ");
        this.document.append(width).append(" ").append(height).append("\">");

        this.document.append("<g id=\"content\">\n");
        // draw a bounding box, just to see what it looks like
        this.document.append("<rect ");
        this.document.append("x=\"").append(x).append("\" "); // X coordinate of starting vertex
        this.document.append("y=\"").append(y).append("\" "); // Y coordinate of starting vertex
        this.document.append("width=\"").append(width).append("\" "); // Rectangle width
        this.document.append("height=\"").append(height).append("\" "); // Rectangle height
        // style (to override the rect style in css)
        this.document.append("style=\"fill:none; stroke:blue; stroke-width:3\""); 
        this.document.append("/>\n");
    }

    public void printProduct() {
        if (this.document != null) {
            this.document.append("</g>\n</svg>\n");
            this.out.println(this.document.toString());
        }
    }

    public String makeColourString(Color c) {
        //return "rgb(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ")";
        return "none";
    }

    public void connect(int x1, int y1, int x2, int y2) {
        this.document.append("<line ");
        this.document.append("x1=\"").append(x1).append("\" ");// x1 coordinate
        this.document.append("y1=\"").append(y1).append("\" ");// y1 coordinate
        this.document.append("x2=\"").append(x2).append("\" ");// x2 coordinate
        this.document.append("y2=\"").append(y2).append("\" ");// y2 coordinate
        this.document.append(" style=\"stroke:black; stroke-width:2;\"/>\n");
    }

    public void drawHelix(int x, int y, int rad, Color c) {
        this.document.append("<circle ");
        this.document.append("r=\"").append(rad).append("\" ");// radius
        this.document.append("cx=\"").append(x).append("\" ");// x coordinate
        this.document.append("cy=\"").append(y).append("\" ");// y coordinate
        this.document.append("style=\"fill:" + this.makeColourString(c) + "; stroke:red; stroke-width:2\"");
        this.document.append(" />\n");
    }

    public void drawStrand(int pointX, int pointY, int leftX, int leftY, int rightX, int rightY, Color c) {
        this.document.append("<polygon ");
        this.document.append("points=\"");
        this.document.append(pointX).append(" ").append(pointY);                // X1, Y1
        this.document.append(" ").append(leftX).append(" ").append(leftY);      // X2, Y2
        this.document.append(" ").append(rightX).append(" ").append(rightY);    // X3, Y3
        this.document.append("\" style=\"fill:" + this.makeColourString(c) + "; stroke:yellow; stroke-width:2;");
        this.document.append(" \"/>\n");
    }

    public void drawTerminus(int x, int y, int r, String label) {
        int width = r;
        int height = r;
        this.document.append("<rect ");
        this.document.append("x=\"").append(x).append("\" ");// X coordinate of starting vertex
        this.document.append("y=\"").append(y).append("\" ");// Y coordinate of starting vertex
        this.document.append("width=\"").append(width).append("\" ");// Rectangle width
        this.document.append("height=\"").append(height).append("\" ");// Rectangle height
        this.document.append(" style=\"fill:none; stroke:black; stroke-width:2;\"/>\n");
    }
}
