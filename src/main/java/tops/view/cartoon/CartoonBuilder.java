package tops.view.cartoon;

//interface for ImgBuilder, SVGBuilder, and PSBuilder

import java.awt.Color;
import java.awt.Point;

public interface CartoonBuilder {

    public void connect(Point p1, Point p2);

    public void drawHelix(Point center, int rad, Color c);

    public void drawStrand(Point top, Point left, Point right, Color c);

    public void drawTerminus(Point center, int r, String label);
    
    public void drawLabel(Point center, String text);
}
