package tops.view.cartoon.builder;

//Make Images

import java.awt.Color;
import java.awt.Image;
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
    public void connect(int x1, int y1, int x2, int y2) {
        graphicsBuilder.connect(x1, y1, x2, y2);
    }

    @Override
    public void drawHelix(int x, int y, int r, Color c) {
        graphicsBuilder.drawHelix(x, y, r, c);
    }

    @Override
    public void drawStrand(int pointX, int pointY, int leftX, int leftY,
            int rightX, int rightY, Color c) {
        graphicsBuilder.drawStrand(pointX, pointY, leftX, leftY, rightX, rightY, c);
    }

    @Override
    public void drawTerminus(int x, int y, int r, String label) {
        graphicsBuilder.drawTerminus(x, y, r, label);
    }

}
