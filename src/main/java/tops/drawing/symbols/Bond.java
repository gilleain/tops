package tops.drawing.symbols;

import java.awt.Color;
import java.awt.Shape;


/**
 * @author maclean
 *
 */
public abstract class Bond extends ConnectionSymbol implements Cloneable {
    
    public static final int LEFT_CHIRAL = 0;
    public static final int RIGHT_CHIRAL = 1;
    public static final int PARALLEL_HBOND = 2;
    public static final int ANTIPARALLEL_HBOND = 3;
    
    private int type;
    
    public Bond(SSESymbol start, SSESymbol end, int type) {
        super(start, end);
        this.type = type;
    }
    
    public Object clone() {
        return super.clone();
    }
    
    public int getType() {
        return this.type;
    }
    
    public Color getColor() {
        switch (this.type) {
            case Bond.LEFT_CHIRAL:
                return Color.ORANGE;
            case Bond.RIGHT_CHIRAL:
                return Color.BLUE;
            case Bond.ANTIPARALLEL_HBOND:
                return Color.GREEN;
            case Bond.PARALLEL_HBOND:
                return Color.RED;
            default:
                return Color.BLACK; // could raise an exception? 
        }
    }

    public abstract Shape createSelectionBoundary();
    public abstract Shape createShape();

}
