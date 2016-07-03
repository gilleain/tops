package python.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CartoonSymbol {

    private int symbolNumber;
    public double CartoonX;
    public double CartoonY;
    private String sseType;
    private char direction;
    private int radius;
    private Object colour;
    private boolean fill;
    private char label;
    private List<Integer> connections;
    public double SymbolRadius;

    public CartoonSymbol(int symbolNumber, String sseType, char direction) {
        this.symbolNumber = symbolNumber;
        this.CartoonX = 0;
        this.CartoonY = 0;
        this.sseType = sseType;
        this.direction = direction;
        this.radius = 20;
        this.colour = Color.BLACK;
        this.fill = false;
        this.label = ' ';
        this.connections = new ArrayList<Integer>();
    }

    public void flip() {
        if (this.direction == 'U') this.direction = 'D';
        else this.direction = 'U';
    }

    public boolean SamePosition(CartoonSymbol q) {
        return (this.CartoonX == q.CartoonX) && (this.CartoonY == q.CartoonY);
    }

    public boolean IsInCircle(double centerX, double centerY, double radius) {
        double separation = distance2D(CartoonX, CartoonY, centerX, centerY);
        if (separation <= radius) return true;
        else return false;
    }
    
    public double distance2D(CartoonSymbol other) {
        return distance2D(CartoonX, CartoonY, other.CartoonX, other.CartoonY);
    }
    
    private double distance2D(double x1, double y1, double x2, double y2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        return Math.sqrt((dX * dX) + (dY * dY)); 
    }
}