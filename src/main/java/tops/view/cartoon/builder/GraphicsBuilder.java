package tops.view.cartoon.builder;

import java.awt.Color;
import java.awt.Graphics;
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
        if (w > 100 && h > 100) {
            // only draw the name if there is room!
            this.g.drawString(name, NAME_X, NAME_Y); 
        }
        this.g.drawRect(0, 0, this.w - 2, this.h - 2); // bounds
    }

    public void connect(int x1, int y1, int x2, int y2) {
        this.g.drawLine(x1, y1, x2, y2);
    }

    public void drawHelix(int x, int y, int r, Color c) {
        // x *= scalex;
        // y *= scaley;

        int d = 2 * r;
        int ex = x - r;
        int ey = y - r;

        if (c != null) {
            this.g.setColor(c);
            this.g.fillOval(ex, ey, d, d);
            this.g.setColor(Color.BLACK);
        }

        this.g.drawOval(ex, ey, d, d);
    }

    public void drawStrand(int pointX, int pointY, int leftX, int leftY,
            int rightX, int rightY, Color c) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = pointX;
        xPoints[1] = leftX;
        xPoints[2] = rightX;

        yPoints[0] = pointY;
        yPoints[1] = leftY;
        yPoints[2] = rightY;

        if (c != null) {
            this.g.setColor(c);
            this.g.fillPolygon(xPoints, yPoints, 3);
            this.g.setColor(Color.BLACK);
        }
        this.g.drawPolygon(xPoints, yPoints, 3); // draw outline
    }

    public void drawTerminus(int x, int y, int r, String label) {
        if (this.w > 100 && this.h > 100 && label != null) {
            this.g.drawString(label, x, y);
        } else {
            int d = 2 * r;
            int cornerX = x - r;
            int cornerY = y - r;
            this.g.drawRect(cornerX, cornerY, d, d);
        }
    }
}
