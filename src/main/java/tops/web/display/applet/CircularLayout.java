package tops.web.display.applet;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import tops.dw.protein.SecStrucElement;

public class CircularLayout {
    
    private int minimumSeparation;
    
    public CircularLayout(int minimumSeparation) {
        this.minimumSeparation = minimumSeparation;
    }
    
    public void layout(List<SecStrucElement> list, Rectangle bounds) {
        int numberOfSymbols = list.size();
        if (numberOfSymbols < 3) {
            return; // be ruth-less
        }

        int minimumPerimiter = this.minimumSeparation * numberOfSymbols;
        int minimumRadius = (int) (minimumPerimiter / (2 * Math.PI));
        int boundsRadius = (Math.min(bounds.width, bounds.height) / 2);
        if (boundsRadius < minimumRadius)
            boundsRadius = minimumRadius;

        int startAngle = 0;
        int finishAngle = startAngle + 360;
        int angleIncrement = (360 / numberOfSymbols);

        int index = 0;
        for (int angle = startAngle; angle < finishAngle; angle += angleIncrement) {
            if (index >= list.size()) {
                break;
            }
            SecStrucElement s = list.get(index);
            Point currentPoint = this.nextCirclePoint(boundsRadius, angle);
            s.placeElement(currentPoint.x, currentPoint.y);
            index++;
        }
    }
    
    private Point nextCirclePoint(int radius, int angle) {
        int x = (int) (radius * Math.sin(angle));
        int y = (int) (radius * Math.cos(angle));
        return new Point(x, y);
    }

}
