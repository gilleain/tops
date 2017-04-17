package tops.web.display.applet;

import java.awt.Rectangle;
import java.util.List;

import tops.dw.protein.SecStrucElement;

public class LinearLayout {
    
    private int minimumSeparation;
    
    public LinearLayout(int minimumSeparation) {
        this.minimumSeparation = minimumSeparation;
    }
    
    public void layout(List<SecStrucElement> list, Rectangle bounds) {
        int minX = bounds.x;
        int maxX = bounds.x + bounds.width;
        
        int numberOfSymbols = list.size();
        if (numberOfSymbols < 2)
            return;
        // first, sort the list by x-coordinate to ensure that we lay out in the
        // same order we started
        // SORT
        // now, find the average X-coordinate of the list
        int sumY = 0;
        for (SecStrucElement s : list) {
            sumY += s.getPosition().y;
        }

        int averageY = (sumY / numberOfSymbols);
        int lineLength = maxX - minX;
        int separation = lineLength / numberOfSymbols;

        if (separation < minimumSeparation) {
            separation = minimumSeparation;
        }

        int xpos = minX;
        for (SecStrucElement s : list) {
            s.placeElement(xpos, averageY);
            xpos += separation;
        }
    }

}
