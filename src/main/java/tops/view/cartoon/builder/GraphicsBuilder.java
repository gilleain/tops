package tops.view.cartoon.builder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import tops.view.cartoon.CartoonBuilder;

public class GraphicsBuilder implements CartoonBuilder {

    public static final int NAME_X = 10;

    public static final int NAME_Y = 20;
    
    private Graphics g; // for drawing
    
    private int w;
    
    private int h;
    
    public GraphicsBuilder(Graphics g, String name, Rectangle bb, int width, int height) {
        this.w = width;
        this.h = height;
        //int currentWidth = bb.width;  // TODO - why pass in bb?
        //int currentHeight = bb.height;
        this.g = g;

        this.g.setColor(Color.WHITE);
        this.g.fillRect(0, 0, this.w, this.h);
        this.g.setColor(Color.BLACK);
        this.g.drawRect(0, 0, this.w - 2, this.h - 2); // bounds
    }

    public void connect(Point p1, Point p2) {
        this.g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    public void drawHelix(Point center, int r, Color c) {
        // x *= scalex;
        // y *= scaley;

        int d = 2 * r;
        int ex = center.x - r;
        int ey = center.y - r;

        if (c != null) {
            this.g.setColor(c);
            this.g.fillOval(ex, ey, d, d);
            this.g.setColor(Color.BLACK);
        }

        this.g.drawOval(ex, ey, d, d);
    }

    public void drawStrand(Point top, Point left, Point right, Color c) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = top.x;
        xPoints[1] = left.x;
        xPoints[2] = right.x;

        yPoints[0] = top.y;
        yPoints[1] = left.y;
        yPoints[2] = right.y;

        if (c != null) {
            this.g.setColor(c);
            this.g.fillPolygon(xPoints, yPoints, 3);
            this.g.setColor(Color.BLACK);
        }
        this.g.drawPolygon(xPoints, yPoints, 3); // draw outline
    }

    public void drawTerminus(Point center, int r, String label) {
        if (this.w > 100 && this.h > 100 && label != null) {
            this.g.drawString(label, center.x, center.y);
        } else {
            int d = 2 * r;
            int cornerX = center.x - r;
            int cornerY = center.y - r;
            this.g.drawRect(cornerX, cornerY, d, d);
        }
    }

    @Override
    public void drawLabel(Point center, String text) {
        this.g.drawString(text, center.x, center.y); 
    }
}
