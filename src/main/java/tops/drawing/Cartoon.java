package tops.drawing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tops.drawing.symbols.Bond;
import tops.drawing.symbols.Box;
import tops.drawing.symbols.CartoonConnector;
import tops.drawing.symbols.Circle;
import tops.drawing.symbols.ConnectionSymbol;
import tops.drawing.symbols.DashedLine;
import tops.drawing.symbols.EquilateralTriangle;
import tops.drawing.symbols.SSESymbol;

/**
 * A 2.5 dimensional view of a protein topology.
 * 
 * @author maclean
 *
 */
public class Cartoon implements Cloneable {
    private List<SSESymbol> sseSymbols;
    private List<CartoonConnector> connections;
    private List<Bond> rArcs;
    private List<Bond> lArcs;
    private List<Bond> aBonds;
    private List<Bond> pBonds;
    
    private CartoonConnector selectedConnector;
    
    private static final int MAX_SSE_SIZE = 20;
    private int currentSSESize;

    public Cartoon() {
        this.currentSSESize = Cartoon.MAX_SSE_SIZE;
        
        this.sseSymbols = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.rArcs = new ArrayList<>();
        this.lArcs = new ArrayList<>();
        this.aBonds = new ArrayList<>();
        this.pBonds = new ArrayList<>();
        
        this.createTermini();
        this.selectedConnector = null;
        
    }
    
    /**
     * @param dimension the width or height of the canvas 
     * @return the size of the sses in this cartoon
     */
    public int getSSESize(int dimension) {
        int calculatedSize = dimension / (this.sseSymbols.size() + 1);
        if (calculatedSize > this.MAX_SSE_SIZE) {
            return this.MAX_SSE_SIZE;
        }
        return calculatedSize;
    }

    public Object clone() {
        try {
            Cartoon c = (Cartoon) super.clone();
            
            c.sseSymbols = new ArrayList<>();
            for (int i = 0; i < this.sseSymbols.size(); i++) {
                c.sseSymbols.add((SSESymbol)(this.sseSymbols.get(i)).clone());
            }
            
            c.connections = new ArrayList<>();
            for (int i = 0; i < this.connections.size(); i++) {
                CartoonConnector connector = this.connections.get(i);
                CartoonConnector shallowClone = (CartoonConnector) connector.clone(); 
                this.cloneBond(connector, shallowClone, c.sseSymbols);
                c.connections.add(shallowClone);
            }
            
            c.lArcs = this.cloneBonds(c, this.lArcs);
            c.rArcs = this.cloneBonds(c, this.rArcs);
            c.aBonds = this.cloneBonds(c, this.aBonds);
            c.pBonds = this.cloneBonds(c, this.pBonds);
            
            c.selectedConnector = null;
            
            return c;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    
    private ArrayList<Bond> cloneBonds(Cartoon clone, List<Bond> bonds) {
        ArrayList<Bond> bondClones = new ArrayList<>();
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = bonds.get(i);
            Bond shallowClone = (Bond) bond.clone();
            this.cloneBond(bond, shallowClone, clone.sseSymbols);
            bondClones.add(shallowClone);
        }
        return bondClones;
    }
    
    private void cloneBond(ConnectionSymbol originalConnection, 
    					   ConnectionSymbol clonedConnection, 
    					   List<SSESymbol> clonedSSESymbols) {
        SSESymbol start = originalConnection.getStartSSESymbol();
        SSESymbol end = originalConnection.getEndSSESymbol();
        int startIndex = this.sseSymbols.indexOf(start);
        int endIndex = this.sseSymbols.indexOf(end);
        clonedConnection.setStartSSESymbol((SSESymbol) clonedSSESymbols.get(startIndex));
        clonedConnection.setEndSSESymbol((SSESymbol) clonedSSESymbols.get(endIndex));
    }
    
    public void relayout() {
        for (int i = 0; i < this.sseSymbols.size(); i++ ) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.recreateShape();
        }
        
        for (int i = 0; i < this.connections.size(); i++) {
            CartoonConnector connection = this.connections.get(i);
            connection.recreateShape();
        }
        
        Bond bond;
        for (int i = 0; i < this.lArcs.size(); i++) {
            bond = lArcs.get(i);
            bond.recreateShape();
        }

        for (int i = 0; i < rArcs.size(); i++) {
            bond = rArcs.get(i);
            bond.recreateShape();
        }

        for (int i = 0; i < aBonds.size(); i++) {
            bond = aBonds.get(i);
            bond.recreateShape();
        }

        for (int i = 0; i < pBonds.size(); i++) {
            bond = pBonds.get(i);
            bond.recreateShape();
        }
    }
    
    public void writeToStream(PrintWriter pw) {

        // figures
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentFigure = this.sseSymbols.get(i);
            pw.write(currentFigure.toString());
            pw.println();
        }
        
        
        Bond bond;
        // l arcs
        for (int i = 0; i < this.lArcs.size(); i++) {
            bond = lArcs.get(i);
            pw.write(bond.toString());
            pw.println();
        }

        // r arc
        for (int i = 0; i < rArcs.size(); i++) {
            bond = rArcs.get(i);
            pw.write(bond.toString());
            pw.println();
        }

        // a_bonds
        for (int i = 0; i < aBonds.size(); i++) {
            bond = aBonds.get(i);
            pw.write(bond.toString());
            pw.println();
        }

        // p_bonds
        for (int i = 0; i < pBonds.size(); i++) {
            bond = pBonds.get(i);
            pw.write(bond.toString());
            pw.println();
        }
    }
    
    public Dimension getSize() {
        
        int largestX = 0;
        int largestY = 0;

        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol currentFigure = sseSymbols.get(i);
            Rectangle figureRectangle = currentFigure.getShape().getBounds();

            if (i == 0) {
                // the largest_x and y become the first entry
                largestX = figureRectangle.x;
                largestY = figureRectangle.y;
            } else {
                if (figureRectangle.x > largestX) {
                    largestX = figureRectangle.x;
                }

                if (figureRectangle.y > largestY) {
                    largestY = figureRectangle.y;
                }
            }
        } 
        return new Dimension(largestX, largestY);
    }
    
    public Point getCenterPoint() {
        int centerX = 0;
        int centerY = 0;
        
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            Point c = sseSymbol.getCenter();
            centerX += c.x;
            centerY += c.y;
        }
        int n = this.sseSymbols.size();
        centerX = (int) (((float) centerX) / ((float) n));
        centerY = (int) (((float) centerY) / ((float) n));
        
        return new Point(centerX, centerY);
    }
    
    public Rectangle2D getBoundingBox() {
        double minX = 0;
        double minY = 0;
        double maxX = 0;
        double maxY = 0;
        
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol sseSymbol = sseSymbols.get(i);
            Rectangle symbolBounds = sseSymbol.getShape().getBounds();
            
            if (i == 0) {
                minX = symbolBounds.x;
                minY = symbolBounds.y;
                maxX = symbolBounds.getMaxX();
                maxY = symbolBounds.getMaxY();
            } else {
                if (symbolBounds.x < minX) minX = symbolBounds.x;
                if (symbolBounds.y < minY) minY = symbolBounds.y;
                if (symbolBounds.getMaxX() > maxX) maxX = symbolBounds.getMaxX();
                if (symbolBounds.getMaxY() > maxY) maxY = symbolBounds.getMaxY();
            }
        }
        
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
    
    public boolean hasSelectedConnector() {
        return this.selectedConnector != null;
    }
    
    public CartoonConnector getSelectedConnector() {
        return this.selectedConnector;
    }
    
    public void setSelectedConnector(CartoonConnector connection) {
        this.selectedConnector = connection;
    }
    
    public void removeSSESymbol(int symbolNumber) {
        // add the new connection
        int n = this.numberOfSSESymbols();
        if (n > 1) {
            // SPECIAL CASE! Selected the first symbol
            if ((n != 1 && symbolNumber != 1 && (symbolNumber != n))) {
                SSESymbol s = this.getSSESymbol(symbolNumber);
                SSESymbol d = this.getSSESymbol(symbolNumber - 2);
                
                CartoonConnector con = new CartoonConnector(symbolNumber - 1, s, d);
                this.addCartoonConnector(con);
            }
        }

        //remove the connection
        int size = this.numberOfConnectors();
        int loop = 0;

        boolean incomingOnly = symbolNumber == n;
        boolean outgoingOnly = symbolNumber == 1;

        while (loop < size) {
            CartoonConnector bond = this.getCartoonConnector(loop);
            int sourceNumber = bond.getStartSSESymbol().getSymbolNumber();
            int destNumber = bond.getEndSSESymbol().getSymbolNumber();

            if (incomingOnly) {
                if (destNumber == symbolNumber)
                    this.removeCartoonConnector(bond);
                break;
            } else if (outgoingOnly) {
                if (sourceNumber == symbolNumber)
                    this.removeCartoonConnector(bond);
                break;
            } else {
                if (sourceNumber == symbolNumber || destNumber == symbolNumber) {

                    this.removeCartoonConnector(bond);
                    size = size - 1;
                    loop = loop - 1;
                }
            }
            loop++;
        }
        
        // inefficient, but equivalent to previous code
        this.removeArc(symbolNumber, 0);
        this.removeArc(symbolNumber, 1);
        this.removeArc(symbolNumber, 2);
        this.removeArc(symbolNumber, 3);

        this.removeSSESymbol(symbolNumber);
    }
    
    // make the N/C termini for an empty Cartoon
    public void createTermini() {
        int terminusSize = Cartoon.MAX_SSE_SIZE / 2;
        Box nTerminus = new Box(1, 250, 250, terminusSize, "N");
        Box cTerminus = new Box(3, 350, 250, terminusSize, "C"); 

        this.sseSymbols.add(nTerminus);
        this.sseSymbols.add(cTerminus);

        CartoonConnector con = new CartoonConnector(2, nTerminus, cTerminus);
        this.connections.add(con);
    }
    
    public void fixTerminalPositions() {
    	int numberOfSymbols = this.sseSymbols.size();
    	SSESymbol nTerminus = this.getSSESymbol(0);
    	SSESymbol cTerminus = this.getSSESymbol(numberOfSymbols - 1);
    	
    	// guard against calling this method on an empty cartoon
    	if (numberOfSymbols > 2) {
    		SSESymbol firstSSE = this.getSSESymbol(1);
    		Point firstCenter = firstSSE.getBoundingBoxCenter();
    		
    		// FIXME : what if there are only 3 symbols?
    		if (numberOfSymbols > 3) {
    			SSESymbol secondSSE = this.getSSESymbol(2);
    			Point secondCenter = secondSSE.getBoundingBoxCenter();
    			int dx = secondCenter.x - firstCenter.x;
    			int dy = secondCenter.y - firstCenter.y;
    			nTerminus.setPosition(firstCenter.x - dx, firstCenter.y - dy);
    			this.getCartoonConnector(0).recreateShape();
    		}	
    		
    		SSESymbol lastSSE = this.getSSESymbol(numberOfSymbols - 2);
    		Point lastCenter = lastSSE.getBoundingBoxCenter(); 
    		
    		if (numberOfSymbols > 4) {
    			SSESymbol penultimateSSE = this.getSSESymbol(numberOfSymbols - 3);
    			Point penultimateCenter = penultimateSSE.getBoundingBoxCenter();
    			int dx = penultimateCenter.x - lastCenter.x;
    			int dy = penultimateCenter.y - lastCenter.y;
    			cTerminus.setPosition(lastCenter.x - dx, lastCenter.y - dy);
    			this.getCartoonConnector(this.connections.size() - 1).recreateShape();
    		}
    	}
    	
    }
    
    public Iterator<SSESymbol> sseSymbolIterator() {
        return this.sseSymbols.iterator();
    }

    public void clear() {
        this.sseSymbols.clear();
        this.connections.clear();
        this.rArcs.clear();
        this.lArcs.clear();
        this.aBonds.clear();
        this.pBonds.clear();
        
        // after clearing, we still want to display N/C boxes
        this.createTermini();
    }
    
    public void addSSESymbol(SSESymbol sseSymbol) {
        if (this.selectedConnector != null) {
            this.addSSESymbolAtSelectedConnector(sseSymbol);
        } else {
            this.addSSESymbolBeforeCTerminus(sseSymbol);
        }
    }

    /*
     * Use the selected connector to add sseSymbol between the endpoints. 
     */
    public void addSSESymbolAtSelectedConnector(SSESymbol sseSymbol) {
        SSESymbol start = this.selectedConnector.getStartSSESymbol();
        SSESymbol end =  this.selectedConnector.getEndSSESymbol();
        this.insertSymbol(start, end, sseSymbol);
    }
    
    /*
     * If just clicking somewhere on the canvas, adds a sseSymbol just before the CTerm. 
     */
    public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol) {
    	this.addSSESymbolBeforeCTerminus(sseSymbol, true);
    }
    
    public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol, boolean selectSymbol) {
        
        int numberOfSSESymbols = this.numberOfSSESymbols();
        SSESymbol source;
        if (numberOfSSESymbols == 2) {  // only N/C
            source = this.sseSymbols.get(0);
        } else {
            source = this.getSSESymbol(numberOfSSESymbols - 2);
        }
        SSESymbol dest = this.sseSymbols.get(numberOfSSESymbols - 1);
        this.insertSymbol(source, dest, sseSymbol);
        
        if (selectSymbol) {
        	sseSymbol.setSelectionState(true);
        }
    }
    
    public void insertSymbol(SSESymbol source, SSESymbol dest, SSESymbol newSSESymbol) {
        System.out.println("inserting between " + source + " and " + dest);
        int sourceIndex = this.sseSymbols.indexOf(source);
        if (sourceIndex == -1) {
            System.out.println(source + " not found in :");
            for (int i = 0; i < this.sseSymbols.size(); i++) {
                SSESymbol sseSymbol  = this.sseSymbols.get(i);
                System.out.println(sseSymbol);
            }
        }
        sseSymbols.add(sourceIndex + 1, newSSESymbol);
        
        for (int i = sourceIndex + 1; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.setSymbolNumber(i);
        }
        newSSESymbol.setSymbolNumber(source.getSymbolNumber() + 1);
        
        CartoonConnector connector = this.connections.get(sourceIndex);
        connector.setEndSSESymbol(newSSESymbol);
        connector.recreateShape();
        
        connections.add(sourceIndex + 1, new CartoonConnector(sourceIndex + 1, newSSESymbol, dest));
    }
    
    public void updateConnectors(SSESymbol sseSymbol) {
        this.updateConnectors(this.sseSymbols.indexOf(sseSymbol));
    }

    public void updateConnectors(int symbolIndex) {
        if (symbolIndex > 0) {
            CartoonConnector previous = this.connections.get(symbolIndex - 1);
            previous.recreateShape();
        }
        
        if (symbolIndex < this.sseSymbols.size() - 1) {
            CartoonConnector next = this.connections.get(symbolIndex);
            next.recreateShape();
        }
    }

    public SSESymbol selectSSESymbol(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentFigure = this.sseSymbols.get(i);
            if (currentFigure.containsPoint(p.x, p.y)) {
                currentFigure.setSelectionState(true);
                return currentFigure;
            }
        }
        return null;
    }
    
    public SSESymbol toggleSelectSSESymbol(Point p) {
        SSESymbol selected = null;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(p.x, p.y)) {
                if (sseSymbol.isSelected()) {
                    sseSymbol.setSelectionState(false);
                } else {
                    sseSymbol.setSelectionState(true);
                }
                selected = sseSymbol;
            } else {
                sseSymbol.setSelectionState(false);
            }
        }
        return selected;
    }
    
    public SSESymbol toggleHighlightSSESymbol(Point p) {
        SSESymbol highlighted = null;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(p.x, p.y)) {
                sseSymbol.setHighlightState(true);
                highlighted = sseSymbol;
            } else {
                sseSymbol.setHighlightState(false);
            }
        }
        return highlighted;
    }
    
    public void moveSelectedSSESymbols(int xDif, int yDif) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                currentSymbol.move(xDif, yDif);
                this.updateConnectors(i);
                this.updateBonds(currentSymbol, aBonds);
                this.updateBonds(currentSymbol, pBonds);
                this.updateBonds(currentSymbol, lArcs);
                this.updateBonds(currentSymbol, rArcs);
            }
        }
    }
    
    public void updateBonds(SSESymbol symbol, List<Bond> bonds) {
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = bonds.get(i);
            if (bond.contains(symbol)) {
                bond.recreateShape();
            }
        }
    }
     
    public SSESymbol getSSESymbolAt(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.containsPoint(p.x, p.y)) {
                return currentSymbol;
            }
        }
        return null;
    }
    
    public List<SSESymbol> getSelectedSSESymbols() {
        ArrayList<SSESymbol> selected = new ArrayList<>();
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                selected.add(currentSymbol);
            }
        }
        return selected;
    }
    
    public SSESymbol getSelectedSSESymbol(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.isSelected() && currentSymbol.containsPoint(p.x, p.y)) {
                return currentSymbol;
            }
        }
        return null;
    }
    
    public int numberOfSelectedSSESymbols() {
        int numberOfSelectedSSESymbols = 0;
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol currentSymbol = sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                numberOfSelectedSSESymbols++;
            }
        }
        return numberOfSelectedSSESymbols;
    }
    
    public void selectAllSSESymbols() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.setSelectionState(true);
        } 
    }
    
    public void deselectAllSSESymbols() {
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol currentSymbol = sseSymbols.get(i);
            currentSymbol.setSelectionState(false);
        }
    }
    
    // returns true if the first sseSymbol is a strand (gmt:?)
    public boolean connectionStartsOnTop() {
        if (this.numberOfSSESymbols() > 0) {
            SSESymbol firstFigure = this.getSSESymbol(0);
            return firstFigure instanceof EquilateralTriangle;
        } else
            return false;
    }
    
    public void centerOn(int x, int y) {
    	this.centerOn(new Point(x, y));
    }
    
    public void centerOn(Point p) {
        Point center = this.getCenterPoint();
        System.out.println("centering " + center + " on " + p);
        int dX = p.x - center.x;
        int dY = p.y - center.y;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.move(dX, dY);
        }
        this.relayout();
    }
    
    public void flip() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (this.sseSymbols.get(i));
            currentSymbol.flip();
        }
    }
    
    public void flipSymbol(SSESymbol symbol) {
        symbol.flip();
        this.updateConnectors(symbol);
    }
    
    public void flipXAxis(Point p) {
        for (int i = 0; i < this.numberOfSSESymbols(); i++) {

            SSESymbol currentSSESymbol = this.sseSymbols.get(i);

            int pointY = p.y;
            int yDiff;

            Point currentPoint = currentSSESymbol.getCenter();

            if (currentPoint.y > p.y) {
                yDiff = currentPoint.y - pointY;
                currentSSESymbol.move(0, -yDiff);
            } else {
                yDiff = pointY - currentPoint.y;
                currentSSESymbol.move(0, yDiff);
            }
        }
        this.relayout();
    }
    
    public void flipYAxis(Point p) {
        for (int i = 0; i < this.numberOfSSESymbols(); i++) {

            SSESymbol currentSSESymbol = this.sseSymbols.get(i);
            int pointX = p.x;
            int xDif;

            Point currentPoint = currentSSESymbol.getCenter();

            if (currentPoint.y > p.y) {
                xDif = currentPoint.y - pointX;
                currentSSESymbol.move(-xDif, 0);
            } else {
                xDif = pointX - currentPoint.y;
                currentSSESymbol.move(xDif, 0);
            }
        }
        this.relayout();
    }

 
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.draw(g2);
        }
        
        for (int i = 0; i < this.connections.size(); i++) {
            CartoonConnector connection = this.connections.get(i);
            connection.draw(g2);
        }
        
        for (Bond e : lArcs) {
            ((DashedLine) e).draw(g2);
        }

        for (Bond e : rArcs) {
            ((DashedLine) e).draw(g2);
        }

        for (Bond e : pBonds) {
            ((DashedLine) e).draw(g2);
        }

        for (Bond e : aBonds) {
            ((DashedLine) e).draw(g2);
        }
    }
    
    public SSESymbol getSSESymbol(int i) {
        return this.sseSymbols.get(i);
    }
    
    public SSESymbol getSSESymbolByNumber(int symbolnum) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol fig = this.sseSymbols.get(i);
            if (fig.hasSymbolNumber(symbolnum)) {
                return fig;
            }
        }
        return null;
    }

    public int numberOfSSESymbols() {
        return this.sseSymbols.size();
    }
    
    public void addCartoonConnector(CartoonConnector c) {
        this.connections.add(c);
    }
    
    public void addCartoonConnector(int i, CartoonConnector c) {
        this.connections.add(i, c);
    }
    
    public CartoonConnector getCartoonConnector(int i) {
        return this.connections.get(i);
    }
    
    public void removeCartoonConnector(CartoonConnector connection) {
        this.connections.remove(connection);
    }
    
    public int numberOfConnectors() {
        return this.connections.size();
    }
    
    public void checkSize(int sseSize) {
        if (sseSize != this.currentSSESize) {
            this.currentSSESize = sseSize;
            this.resizeSymbols();
        }
    }
    
    public void resizeSymbols() {
        // TODO
    }
    
    public void createUpStrand(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        EquilateralTriangle equilateralTriangle = new EquilateralTriangle(symbolNumber, x, y, sseSize, false);
        this.checkSize(sseSize);
        this.addSSESymbol(equilateralTriangle);
    }
    
    public void createDownStrand(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        EquilateralTriangle equilateralTriangle = new EquilateralTriangle(symbolNumber, x, y, sseSize, true);
        this.checkSize(sseSize);
        this.addSSESymbol(equilateralTriangle);
    }
    
    public void createUpHelix(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        Circle circle = new Circle(symbolNumber, x, y, sseSize, false);
        this.checkSize(sseSize);
        this.addSSESymbol(circle);
    }
    
    public void createDownHelix(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        Circle circle = new Circle(symbolNumber, x, y, sseSize, true);
        this.checkSize(sseSize);
        this.addSSESymbol(circle);
    }
    
    public void createRightArc(SSESymbol source, SSESymbol destination) {
        this.rArcs.add(new DashedLine(source, destination, Bond.RIGHT_CHIRAL));
    }
    
    public void createLeftArc(SSESymbol source, SSESymbol destination) {
        this.lArcs.add(new DashedLine(source, destination, Bond.LEFT_CHIRAL));
    }
    
    public void createABond(SSESymbol source, SSESymbol destination) {
        this.aBonds.add(new DashedLine(source, destination, Bond.ANTIPARALLEL_HBOND));
    }
    
    public void createPBond(SSESymbol source, SSESymbol destination) {
        this.pBonds.add(new DashedLine(source, destination, Bond.PARALLEL_HBOND));
    }

    public void removeArc(int symbolNumber, int arcType) {
        Iterator<Bond> itr;
        switch (arcType) {
            case Bond.RIGHT_CHIRAL: itr = this.rArcs.iterator(); break;
            case Bond.LEFT_CHIRAL: itr = this.lArcs.iterator(); break;
            case Bond.ANTIPARALLEL_HBOND: itr = this.aBonds.iterator(); break;
            case Bond.PARALLEL_HBOND: itr = this.pBonds.iterator(); break;
            default: return;
        }
        
        while (itr.hasNext()) {
            ConnectionSymbol bond = itr.next();
            int sourceNumber = bond.getStartSSESymbol().getSymbolNumber();
            int destNumber = bond.getEndSSESymbol().getSymbolNumber();

            if (sourceNumber == symbolNumber || destNumber == symbolNumber) {
                itr.remove();
                return;
            }
        }
    }

    public void deleteSSESymbol(SSESymbol sseSymbol) {
        deleteBonds(sseSymbol, this.lArcs);
        deleteBonds(sseSymbol, this.rArcs);
        deleteBonds(sseSymbol, this.aBonds);
        deleteBonds(sseSymbol, this.pBonds);
        deleteConnections(sseSymbol);
        this.sseSymbols.remove(sseSymbol);
    }
    
    private void deleteBonds(SSESymbol sseSymbol, List<Bond> bonds) {
        Iterator<Bond> itr = bonds.iterator();
        while (itr.hasNext()) {
            Bond bond = itr.next();
            if (bond.contains(sseSymbol)) {
                itr.remove();
            }
        }
    }

    private void deleteConnections(SSESymbol sseSymbol) {
        int symbolIndex = this.sseSymbols.indexOf(sseSymbol);
        SSESymbol nextSymbol = this.sseSymbols.get(symbolIndex + 1);
        
        CartoonConnector previousConnection = this.connections.get(symbolIndex - 1);
        this.connections.remove(symbolIndex);
        previousConnection.setEndSSESymbol(nextSymbol);
        previousConnection.recreateShape();
    }
    
    public String getVertexString() {
        // only concerned with the items in the sseSymbols araylist

        //THIS IS HORIBLE : basically, we don't know if there are inserts until we encounter one
        //...so in order to get a proper vertex string like N[]E[]E[1:2]E[]C we have to make BOTH!
        String vertexSt = "";
        String insertedVertexSt = "";

        //this is a hack to ensure there are [] after N and before C, but ONLY if there are any other inserts!
        boolean hasInserts = false;

        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol fig = sseSymbols.get(i);


            if (i < sseSymbols.size()) {
                // now add the range info
                for (int j = 0; j < this.connections.size(); j++) {
                    // get the connection


                    CartoonConnector currentConnection = connections.get(j);

                    int connectionSource = currentConnection.getStartSSESymbol().getSymbolNumber();
                    int connectionDest = currentConnection.getEndSSESymbol().getSymbolNumber();


                    if ( (connectionSource == i && connectionDest == i + 1 ) ||
                            (connectionSource == i + 1 && connectionDest == i) ) {
                        // we add the description of the connection to the
                        // vertex string
                        
                        // THIS IS MADNESS
                        //String range_info = cur_con.label();
                        String range_info = ""; // FIXME

                        //range_info.replaceAll(":", "-");

                        if (range_info.equals("")) {
                            insertedVertexSt += "[]";
                        } else {
                            hasInserts = true;
                            if (range_info.equals("*"))
                                range_info = "";

                            insertedVertexSt += "[" + range_info + "]";
                        }
                    }
                }
            }

            if (fig instanceof EquilateralTriangle && !fig.isDown()) {
                vertexSt += "E";
                insertedVertexSt += "E";
            } else if (fig instanceof EquilateralTriangle && fig.isDown()) {
                vertexSt += "e";
                insertedVertexSt += "e";
            } else if (fig instanceof Circle && fig.isDown()) {
                vertexSt += "h";
                insertedVertexSt += "h";
            } else if (fig instanceof Circle && !fig.isDown()) {
                vertexSt += "H";
                insertedVertexSt += "H";
            }
        }
        // now add on the last vertex
        // we are looking one less than normal

        if (hasInserts) {
            return "N[]" + insertedVertexSt + "[]C";
        } else {
            return "N" + vertexSt + "C";
        }
    }

    public String getEdgeString() {
        StringBuilder edgeVertex = new StringBuilder();
        validateHBonds();

        for (int i = 0; i < sseSymbols.size(); i++) {
            edgeVertex.append(getBondInfo(i + 1, rArcs, "R"));
            edgeVertex.append(getBondInfo(i + 1, lArcs, "L"));
            edgeVertex.append(getBondInfo(i + 1, aBonds, "A"));
            edgeVertex.append(getBondInfo(i + 1, pBonds, "P"));
        }
        return edgeVertex.toString();
    }

    public void validateHBonds() {
        // loop through the aBonds and pBonds looking for bonds that
        // should be switched

        ArrayList<Bond> tempBonds = new ArrayList<>();
        tempBonds.clear();

        for (int i = 0; i < pBonds.size(); i++) {
            // look for bonds in aBonds that should be in pBonds
            Bond currentBond = pBonds.get(i);

            SSESymbol source = currentBond.getStartSSESymbol();
            SSESymbol dest = currentBond.getEndSSESymbol();

            // now look for A bonds
            if ((source instanceof EquilateralTriangle && source.isDown()) 
                    || (source instanceof EquilateralTriangle && dest instanceof EquilateralTriangle)) {
                // remove this bond from a_Bonds, it belongs in P bonds
                // strore them into temp bonds temporarily.
                tempBonds.add(currentBond);
            }
        }
        pBonds.removeAll(tempBonds);
        aBonds.addAll(tempBonds);

        tempBonds.clear();
        for (int i = 0; i < aBonds.size(); i++) {
            // look for bonds in aBonds that should be in pBonds
            Bond currentBond = aBonds.get(i);

            SSESymbol source = currentBond.getStartSSESymbol();
            SSESymbol dest = currentBond.getEndSSESymbol();

            String sourceClass = source.getClass().toString();
            String destClass = dest.getClass().toString();

            // now look for P bonds
            if (sourceClass.equals(destClass) ) {
                // remove this bond from a_Bonds, it belongs in P bonds
                // strore them into temp bonds temporarily.
                tempBonds.add(currentBond);
            }
        }
        aBonds.removeAll(tempBonds);
        pBonds.addAll(tempBonds);

    }

    // returns the R_Arc from that is created between a symbol and another symbol
    // if one of those symbols is sseSymbols<symbolNum>
    public String getBondInfo(int symbolNum, List<Bond> ar, String sse_rep) {
        StringBuilder bondString = new StringBuilder();
        for (int j = 0; j < ar.size(); j++) {
            Bond bond = ar.get(j);

            // the source number for this arc
            int sourceNum = bond.getStartSSESymbol().getSymbolNumber() - 1;
            int destNum = bond.getEndSSESymbol().getSymbolNumber() - 1;

            if (sourceNum == symbolNum) {
                boolean souceLessThanDest = sourceNum < destNum;
                if (souceLessThanDest) {
                    bondString.append(sourceNum + ":" + destNum + sse_rep + " ");
                } else {
                    bondString.append(destNum + ":" + sourceNum + sse_rep + " ");
                }
            }
        }
        return bondString.toString();
    }

    
    public boolean bondBetween(SSESymbol start, SSESymbol end, List<Bond> bonds) {
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = bonds.get(i);
            if (bond.contains(start) && bond.contains(end)) {
                return true;
            }
        }
        return false;
    }

    // returns true if a Hydrogen bond can be creates between the source and the destination
    public boolean canCreateHydrogenBond(SSESymbol source, SSESymbol dest) {
        //can only create a Hydrogen bond between Strands
        if (source instanceof Circle || dest instanceof Circle) {
            return false;
        }

        return !bondBetween(source, dest, this.aBonds) && !bondBetween(source, dest, this.pBonds);
    }

    public boolean shouldCreateParallelBond(SSESymbol source, SSESymbol dest) {
        return (source.isDown() && dest.isDown()) || (!source.isDown() && !dest.isDown());
    }

    public boolean shouldCreateAntiParallelBond(SSESymbol source, SSESymbol dest) {
        return (!source.isDown() && dest.isDown()) || (source.isDown() && !dest.isDown());
    }

    public boolean canCreateRArc(SSESymbol source, SSESymbol dest) {
        boolean isValid;

        if (source instanceof EquilateralTriangle) {
            isValid = dest instanceof EquilateralTriangle;
        } else if (source instanceof Circle){
            isValid = dest instanceof Circle;
        }

        if (bondBetween(source, dest, this.aBonds)) {
            return false;
        }

        isValid = !(bondBetween(source, dest, this.lArcs));
        isValid = !(bondBetween(source, dest, this.rArcs));

        return isValid;
    }

    //returns true if it is valid to create an R arc between the source and destination
    public boolean canCreateLArc(SSESymbol source, SSESymbol dest) {
        boolean isValid = true;

        //check if there exists an L Arc between the source and destination
        if (source instanceof EquilateralTriangle)
            isValid = (dest instanceof EquilateralTriangle);
        else if (source instanceof Circle )
            isValid = dest instanceof Circle;

        if (bondBetween(source, dest, this.aBonds))   // can't create a L_A bond
            isValid = isValid && false;

        isValid = !(bondBetween(source, dest, this.rArcs));
        isValid = !(bondBetween(source, dest, this.lArcs));
        return isValid;
    }

    public boolean canCreatePBond(SSESymbol source, SSESymbol dest) {
       return source != dest;
    }

    public boolean canCreateABond(SSESymbol source, SSESymbol dest) {
        return !this.bondBetween(source, dest, aBonds) && !this.bondBetween(source, dest, pBonds);
    }

    // outputs this SSE String
    public String toString() {
        String vertex = this.getVertexString();
        String edge = this.getEdgeString();
        return vertex + " " + edge;
    }

}
