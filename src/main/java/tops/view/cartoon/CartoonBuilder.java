package tops.view.cartoon;

//interface for ImgBuilder, SVGBuilder, and PSBuilder

import java.awt.Color;

public interface CartoonBuilder {

    public void connect(int x1, int y1, int x2, int y2);

    public void drawHelix(int x, int y, int rad, Color c);

    public void drawStrand(int pointX, int pointY, int leftX, int leftY,
            int rightX, int rightY, Color c);

    public void drawTerminus(int x, int y, int r, String label);
}
