package tops.port.model;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UP;
import static tops.port.model.SSEType.HELIX;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

public class CartoonSymbol {

    private int symbolNumber;
    private SSEType sseType;
    private double cartoonX;
    private double cartoonY;
    private Direction direction;
    private int radius;
    private Color colour;
    private boolean fill;
    private String label;
    private List<Point2d> connections;
    private boolean symbolPlaced;

    public CartoonSymbol() {
        this(0, HELIX, UP);
    }
    
    public CartoonSymbol(int symbolNumber, SSEType sseType, Direction direction) {
        this.symbolNumber = symbolNumber;
        this.cartoonX = 0;
        this.cartoonY = 0;
        this.sseType = sseType;
        this.direction = direction;
        this.radius = 20;
        this.colour = Color.BLACK;
        this.fill = false;
        this.label = " ";
        this.connections = new ArrayList<>();
    }
    
    public void addConnectionTo(Point2d p) {
        this.connections.add(p);
    }
    
    public List<Point2d> getConnectionsTo() {
        return this.connections;
    }
    
    
    public boolean getFill() {
        return this.fill;
    }
    
    public void setFill(boolean fill) {
        this.fill = fill;
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
    
    
    public int getRadius() {
        return radius;
    }
    
    public Color getColor() {
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
        if (this.direction == UP) {
            this.direction = DOWN;
        } else {
            this.direction = UP;
        }
    }

    public boolean samePosition(CartoonSymbol q) {
        return (this.cartoonX == q.cartoonX) && (this.cartoonY == q.cartoonY);
    }

    public boolean isInCircle(double centerX, double centerY, double radius) {
        double separation = distance2D(cartoonX, cartoonY, centerX, centerY);
        return separation <= radius;
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

    public void setColor(Color c) {
        this.colour = c;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}