package python.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CartoonSymbol {

    private int symbolNumber;
    private double cartoonX;
    private double cartoonY;
    private String sseType;
    private char direction;
    private int radius;
    private int[] colour;
    private boolean fill;
    private String label;
    private List<Integer> connections;
    private double SymbolRadius;
    private boolean symbolPlaced;

    public CartoonSymbol() {
        // TODO
    }
    
    public CartoonSymbol(int symbolNumber, String sseType, char direction) {
        this.symbolNumber = symbolNumber;
        this.cartoonX = 0;
        this.cartoonY = 0;
        this.sseType = sseType;
        this.direction = direction;
        this.radius = 20;
        this.colour = new int[] { Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue() };
        this.fill = false;
        this.label = " ";
        this.connections = new ArrayList<Integer>();
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public void setLabel(String label) { 
        this.label = label;
    }
    
    public int getSymbolNumber() {
        return this.symbolNumber;
    }
    
    public void setSymbolNumber(int symbolNumber) {
        this.symbolNumber  = symbolNumber;
    }
    
    
    public double getSymbolRadius() {
        return SymbolRadius;
    }
    
    public int[] getColor() {
        return this.colour;
    }
    
    public double getCartoonX() {
        return cartoonX;
    }
    
    public void setCartoonX(double cartoonX) {
        this.cartoonX = cartoonX;
    }
    
    public double getCartoonY() {
        return cartoonY;
    }
    
    public void setCartoonY(double cartoonY) {
        this.cartoonY = cartoonY;
    }

    public void flip() {
        if (this.direction == 'U') this.direction = 'D';
        else this.direction = 'U';
    }

    public boolean SamePosition(CartoonSymbol q) {
        return (this.cartoonX == q.cartoonX) && (this.cartoonY == q.cartoonY);
    }

    public boolean IsInCircle(double centerX, double centerY, double radius) {
        double separation = distance2D(cartoonX, cartoonY, centerX, centerY);
        if (separation <= radius) return true;
        else return false;
    }
    
    public double distance2D(CartoonSymbol other) {
        return distance2D(cartoonX, cartoonY, other.cartoonX, other.cartoonY);
    }
    
    private double distance2D(double x1, double y1, double x2, double y2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        return Math.sqrt((dX * dX) + (dY * dY)); 
    }

    public void setSymbolPlaced(boolean symbolPlaced) {
        this.symbolPlaced = symbolPlaced;
    }

    public boolean isSymbolPlaced() {
        return this.symbolPlaced;
    }
}