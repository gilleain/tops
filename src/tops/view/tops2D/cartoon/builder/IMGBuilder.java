package tops.view.tops2D.cartoon.builder;

//Make Images

import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics; //scaling!
import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;

import Acme.JPM.Encoders.GifEncoder;

import tops.view.tops2D.cartoon.CartoonBuilder;

public class IMGBuilder implements CartoonBuilder {

    private Graphics g; // for drawing

    // private Graphics2D g; //for drawing
    private Image image; // the product that this builder will return

    private OutputStream out; // the stream to write to

    private int w, h;
//    private int scalex, scaley;

    // to compensate for the bad centering of the bounding rectangle!
//    private static final int BORDER = 10; 

    private static final int NAME_X = 10;

    private static final int NAME_Y = 20;

    static {
        try {
            Class<IMGBuilder> c = IMGBuilder.class;
            com.eteks.awt.PJAGraphicsManager.getDefaultGraphicsManager()
                    .loadFont(
                            c.getClassLoader().getResourceAsStream("pja.pjaf"));
        } catch (Exception ie) {
            System.out.print("Can not load font " + ie);
        }
    }

    public IMGBuilder(String name, Rectangle bb, OutputStream out, int width,
            int height) {
        this.out = out;
        this.w = width;
        this.h = height;
        //int currentWidth = bb.width;
        //int currentHeight = bb.height;
        //int type = BufferedImage.TYPE_INT_RGB;
        this.image = new com.eteks.awt.PJAImage(this.w, this.h);
        // BufferedImage image = new PJABufferedImage(w, h, type)
        this.g = this.image.getGraphics();

        this.g.setColor(Color.white);
        this.g.fillRect(0, 0, this.w, this.h);
        this.g.setColor(Color.black);
        if ((this.w > 100) && (this.h > 100)) {
            // only draw the name if there is room!
            this.g.drawString(name, IMGBuilder.NAME_X, IMGBuilder.NAME_Y); 
        }
        this.g.drawRect(0, 0, this.w - 2, this.h - 2); // bounds
    }

    public void printProduct() {
        try {
            GifEncoder gif = new GifEncoder(this.image, this.out);
            gif.encode();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
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
            this.g.setColor(Color.black);
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
            this.g.setColor(Color.black);
        }
        this.g.drawPolygon(xPoints, yPoints, 3); // draw outline
    }

    public void drawTerminus(int x, int y, int r, String label) {
        if ((this.w > 100) && (this.h > 100) && (label != null)) {
            this.g.drawString(label, x, y);
        } else {
            int d = 2 * r;
            int cornerX = x - r;
            int cornerY = y - r;
            this.g.drawRect(cornerX, cornerY, d, d);
        }
    }
}
