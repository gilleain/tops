package tops.view.cartoon.builder;

//Make Images

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import tops.view.cartoon.ByteCartoonBuilder;

public class IMGBuilder implements ByteCartoonBuilder {

    private GraphicsBuilder graphicsBuilder;

    private Image image; // the product that this builder will return

    // to compensate for the bad centering of the bounding rectangle!
//    private static final int BORDER = 10; 

    public IMGBuilder(Image image, String name, Rectangle bb, int width, int height) {
        this.image = image;
        graphicsBuilder = new GraphicsBuilder(image.getGraphics(), name, bb, height, height);
    }

    public void printProduct(OutputStream out) {
        try {
            ImageIO.write((RenderedImage) image, "GIF", out);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    @Override
    public void connect(Point p1, Point p2) {
        graphicsBuilder.connect(p1, p2);
    }

    @Override
    public void drawHelix(Point center, int r, Color c) {
        graphicsBuilder.drawHelix(center, r, c);
    }

    @Override
    public void drawStrand(Point center, Point left, Point right, Color c) {
        graphicsBuilder.drawStrand(center, left, right, c);
    }

    @Override
    public void drawTerminus(Point p, int r, String label) {
        graphicsBuilder.drawTerminus(p, r, label);
    }

    @Override
    public void drawLabel(Point center, String text) {
        graphicsBuilder.drawLabel(center, text);
    }

}
