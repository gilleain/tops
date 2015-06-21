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
public class TopsCartoon implements Cloneable, Cartoon {
    private List<SSESymbol> sseSymbols;
    private List<CartoonConnector> connections;
    private List<Bond> rArcs;
    private List<Bond> lArcs;
    private List<Bond> aBonds;
    private List<Bond> pBonds;
    
    private CartoonConnector selected_connector;
    
    private int MAX_SSE_SIZE;
    private int CURRENT_SSE_SIZE;

    public TopsCartoon() {
        this.MAX_SSE_SIZE = 20;
        this.CURRENT_SSE_SIZE = this.MAX_SSE_SIZE;
        
        this.sseSymbols = new ArrayList<SSESymbol>();
        this.connections = new ArrayList<CartoonConnector>();
        this.rArcs = new ArrayList<Bond>();
        this.lArcs = new ArrayList<Bond>();
        this.aBonds = new ArrayList<Bond>();
        this.pBonds = new ArrayList<Bond>();
        
        this.createTermini();
        this.selected_connector = null;
        
    }
    
    public TopsCartoon(ArrayList<SSESymbol> sseSymbols, ArrayList<CartoonConnector> connections, ArrayList<Bond> rArcs, ArrayList<Bond> lArcs, ArrayList<Bond> aBonds, ArrayList<Bond> pBonds) {
        this();
        this.sseSymbols = sseSymbols;
        this.connections = connections;
        this.rArcs = rArcs;
        this.lArcs = lArcs;
        this.aBonds = aBonds;
        this.pBonds = pBonds;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSSESize(int)
	 */
    @Override
	public int getSSESize(int dimension) {
        int calculatedSize = (int) dimension / (this.sseSymbols.size() + 1);
        if (calculatedSize > this.MAX_SSE_SIZE) {
            return this.MAX_SSE_SIZE;
        }
        return calculatedSize;
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#clone()
	 */
    @Override
	public Object clone() {
        try {
            TopsCartoon c = (TopsCartoon) super.clone();
            
            c.sseSymbols = new ArrayList<SSESymbol>();
            for (int i = 0; i < this.sseSymbols.size(); i++) {
                c.sseSymbols.add((SSESymbol)(this.sseSymbols.get(i)).clone());
            }
            
            c.connections = new ArrayList<CartoonConnector>();
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
            
            c.selected_connector = null;
            
            return c;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    
    private ArrayList<Bond> cloneBonds(TopsCartoon clone, List<Bond> bonds) {
        ArrayList<Bond> bondClones = new ArrayList<Bond>();
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#relayout()
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#writeToStream(java.io.PrintWriter)
	 */
    @Override
	public void writeToStream(PrintWriter pw) {

        // figures
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol cur_fig = this.sseSymbols.get(i);
            pw.write(cur_fig.toString());
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSize()
	 */
    @Override
	public Dimension getSize() {
        
        int largest_x = 0;
        int largest_y = 0;

        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol cur_fig = sseSymbols.get(i);
            Rectangle fig_rect = cur_fig.getShape().getBounds();

            if (i == 0) {
                // the largest_x and y become the first entry
                largest_x = fig_rect.x;
                largest_y = fig_rect.y;
            } else {
                if (fig_rect.x > largest_x) {
                    largest_x = fig_rect.x;
                }

                if (fig_rect.y > largest_y) {
                    largest_y = fig_rect.y;
                }
            }
        } 
        return new Dimension(largest_x, largest_y);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getCenterPoint()
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getBoundingBox()
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#hasSelectedConnector()
	 */
    @Override
	public boolean hasSelectedConnector() {
        return this.selected_connector != null;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSelectedConnector()
	 */
    @Override
	public CartoonConnector getSelectedConnector() {
        return this.selected_connector;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#setSelectedConnector(tops.drawing.symbols.CartoonConnector)
	 */
    @Override
	public void setSelectedConnector(CartoonConnector connection) {
        this.selected_connector = connection;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#removeSSESymbol(int)
	 */
    @Override
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

        boolean in_coming_only = symbolNumber == n;
        boolean out_going_only = symbolNumber == 1;

        while (loop < size) {
            CartoonConnector bond = this.getCartoonConnector(loop);
            int source_num = bond.getStartSSESymbol().getSymbolNumber();
            int dest_num = bond.getEndSSESymbol().getSymbolNumber();

            if (in_coming_only) {
                if (dest_num == symbolNumber)
                    this.removeCartoonConnector(bond);
                break;
            } else if (out_going_only) {
                if (source_num == symbolNumber)
                    this.removeCartoonConnector(bond);
                break;
            } else {
                if (source_num == symbolNumber || dest_num == symbolNumber) {

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
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createTermini()
	 */
    @Override
	public void createTermini() {
        int terminusSize = this.MAX_SSE_SIZE / 2;
        Box nTerminus = new Box(1, 250, 250, terminusSize, "N");
        Box cTerminus = new Box(3, 350, 250, terminusSize, "C"); 

        this.sseSymbols.add(nTerminus);
        this.sseSymbols.add(cTerminus);

        CartoonConnector con = new CartoonConnector(2, nTerminus, cTerminus);
        this.connections.add(con);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#fixTerminalPositions()
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#sseSymbolIterator()
	 */
    @Override
	public Iterator<SSESymbol> sseSymbolIterator() {
        return this.sseSymbols.iterator();
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#clear()
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#addSSESymbol(tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void addSSESymbol(SSESymbol sseSymbol) {
        if (this.selected_connector != null) {
            this.addSSESymbolAtSelectedConnector(sseSymbol);
        } else {
            this.addSSESymbolBeforeCTerminus(sseSymbol);
        }
    }

    /*
     * Use the selected connector to add sseSymbol between the endpoints. 
     */
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#addSSESymbolAtSelectedConnector(tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void addSSESymbolAtSelectedConnector(SSESymbol sseSymbol) {
        SSESymbol start = this.selected_connector.getStartSSESymbol();
        SSESymbol end =  this.selected_connector.getEndSSESymbol();
        this.insertSymbol(start, end, sseSymbol);
    }
    
    /*
     * If just clicking somewhere on the canvas, adds a sseSymbol just before the CTerm. 
     */
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#addSSESymbolBeforeCTerminus(tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol) {
    	this.addSSESymbolBeforeCTerminus(sseSymbol, true);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#addSSESymbolBeforeCTerminus(tops.drawing.symbols.SSESymbol, boolean)
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#insertSymbol(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
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
        
        this.renumberSymbolsFrom(sourceIndex + 1);
        newSSESymbol.setSymbolNumber(source.getSymbolNumber() + 1);
        
        CartoonConnector connector = this.connections.get(sourceIndex);
        connector.setEndSSESymbol(newSSESymbol);
        connector.recreateShape();
        
        connections.add(sourceIndex + 1, new CartoonConnector(sourceIndex + 1, newSSESymbol, dest));
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#renumberSymbolsFrom(int)
	 */
    @Override
	public void renumberSymbolsFrom(int fromIndex) {
        for (int i = fromIndex; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.setSymbolNumber(i);
        }
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#updateConnectors(tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void updateConnectors(SSESymbol sseSymbol) {
        this.updateConnectors(this.sseSymbols.indexOf(sseSymbol));
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#updateConnectors(int)
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#selectSSESymbol(java.awt.Point)
	 */
    @Override
	public SSESymbol selectSSESymbol(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol cur_fig = this.sseSymbols.get(i);
            if (cur_fig.containsPoint(p.x, p.y)) {
                cur_fig.setSelectionState(true);
                return cur_fig;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#toggleSelectSSESymbol(java.awt.Point)
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#toggleHighlightSSESymbol(java.awt.Point)
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#moveSelectedSSESymbols(int, int)
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#updateBonds(tops.drawing.symbols.SSESymbol, java.util.List)
	 */
    @Override
	public void updateBonds(SSESymbol symbol, List<Bond> bonds) {
        for (int i = 0; i < bonds.size(); i++) {
            Bond bond = bonds.get(i);
            if (bond.contains(symbol)) {
                bond.recreateShape();
            }
        }
    }
     
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSSESymbolAt(java.awt.Point)
	 */
    @Override
	public SSESymbol getSSESymbolAt(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.containsPoint(p.x, p.y)) {
                return currentSymbol;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSelectedSSESymbols()
	 */
    @Override
	public ArrayList<SSESymbol> getSelectedSSESymbols() {
        ArrayList<SSESymbol> selected = new ArrayList<SSESymbol>();
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.isSelected()) {
                selected.add(currentSymbol);
            }
        }
        return selected;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSelectedSSESymbol(java.awt.Point)
	 */
    @Override
	public SSESymbol getSelectedSSESymbol(Point p) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = this.sseSymbols.get(i);
            if (currentSymbol.isSelected() && currentSymbol.containsPoint(p.x, p.y)) {
                return currentSymbol;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#numberOfSelectedSSESymbols()
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#selectAllSSESymbols()
	 */
    @Override
	public void selectAllSSESymbols() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.setSelectionState(true);
        } 
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#deselectAllSSESymbols()
	 */
    @Override
	public void deselectAllSSESymbols() {
        for (int i = 0; i < sseSymbols.size(); i++) {
            SSESymbol currentSymbol = sseSymbols.get(i);
            currentSymbol.setSelectionState(false);
        }
    }
    
    // returns true if the first sseSymbol is a strand (gmt:?)
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#connectionStartsOnTop()
	 */
    @Override
	public boolean connectionStartsOnTop() {
        if (this.numberOfSSESymbols() > 0) {
            SSESymbol first_fig = this.getSSESymbol(0);
            return first_fig instanceof EquilateralTriangle;
        } else
            return false;
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#centerOn(int, int)
	 */
    @Override
	public void centerOn(int x, int y) {
    	this.centerOn(new Point(x, y));
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#centerOn(java.awt.Point)
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#flip()
	 */
    @Override
	public void flip() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol currentSymbol = (this.sseSymbols.get(i));
            currentSymbol.flip();
        }
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#flipSymbol(tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void flipSymbol(SSESymbol symbol) {
        symbol.flip();
        this.updateConnectors(symbol);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#flipXAxis(java.awt.Point)
	 */
    @Override
	public void flipXAxis(Point p) {
        for (int i = 0; i < this.numberOfSSESymbols(); i++) {

            SSESymbol currentSSESymbol = this.sseSymbols.get(i);

            int point_y = p.y;
            int y_dif;

            Point currentPoint = currentSSESymbol.getCenter();

            if (currentPoint.y > p.y) {
                y_dif = currentPoint.y - point_y;
                currentSSESymbol.move(0, -y_dif);
            } else {
                y_dif = point_y - currentPoint.y;
                currentSSESymbol.move(0, y_dif);
            }
        }
        this.relayout();
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#flipYAxis(java.awt.Point)
	 */
    @Override
	public void flipYAxis(Point p) {
        for (int i = 0; i < this.numberOfSSESymbols(); i++) {

            SSESymbol currentSSESymbol = this.sseSymbols.get(i);
            int point_x = p.x;
            int x_dif;

            Point currentPoint = currentSSESymbol.getCenter();

            if (currentPoint.y > p.y) {
                x_dif = currentPoint.y - point_x;
                currentSSESymbol.move(-x_dif, 0);
            } else {
                x_dif = point_x - currentPoint.y;
                currentSSESymbol.move(x_dif, 0);
            }
        }
        this.relayout();
    }

 
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#draw(java.awt.Graphics)
	 */
    @Override
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
        
        Iterator<Bond> e = lArcs.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }

        e = rArcs.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }

        e = pBonds.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }

        e = aBonds.iterator();
        while (e.hasNext()) {
            ((DashedLine) e.next()).draw(g2);
        }
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSSESymbol(int)
	 */
    @Override
	public SSESymbol getSSESymbol(int i) {
        return this.sseSymbols.get(i);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getSSESymbolByNumber(int)
	 */
    @Override
	public SSESymbol getSSESymbolByNumber(int symbolnum) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol fig = this.sseSymbols.get(i);
            if (fig.hasSymbolNumber(symbolnum)) {
                return fig;
            }
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#numberOfSSESymbols()
	 */
    @Override
	public int numberOfSSESymbols() {
        return this.sseSymbols.size();
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#addCartoonConnector(tops.drawing.symbols.CartoonConnector)
	 */
    @Override
	public void addCartoonConnector(CartoonConnector c) {
        this.connections.add(c);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#addCartoonConnector(int, tops.drawing.symbols.CartoonConnector)
	 */
    @Override
	public void addCartoonConnector(int i, CartoonConnector c) {
        this.connections.add(i, c);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getCartoonConnector(int)
	 */
    @Override
	public CartoonConnector getCartoonConnector(int i) {
        return this.connections.get(i);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#removeCartoonConnector(tops.drawing.symbols.CartoonConnector)
	 */
    @Override
	public void removeCartoonConnector(CartoonConnector connection) {
        this.connections.remove(connection);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#numberOfConnectors()
	 */
    @Override
	public int numberOfConnectors() {
        return this.connections.size();
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#checkSize(int)
	 */
    @Override
	public void checkSize(int sseSize) {
        if (sseSize != this.CURRENT_SSE_SIZE) {
            this.CURRENT_SSE_SIZE = sseSize;
            this.resizeSymbols();
        }
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#resizeSymbols()
	 */
    @Override
	public void resizeSymbols() {
        // TODO
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createUpStrand(int, int, int, int)
	 */
    @Override
	public void createUpStrand(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        EquilateralTriangle equilateralTriangle = new EquilateralTriangle(symbolNumber, x, y, sseSize, false);
        this.checkSize(sseSize);
        this.addSSESymbol(equilateralTriangle);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createDownStrand(int, int, int, int)
	 */
    @Override
	public void createDownStrand(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        EquilateralTriangle equilateralTriangle = new EquilateralTriangle(symbolNumber, x, y, sseSize, true);
        this.checkSize(sseSize);
        this.addSSESymbol(equilateralTriangle);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createUpHelix(int, int, int, int)
	 */
    @Override
	public void createUpHelix(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        Circle circle = new Circle(symbolNumber, x, y, sseSize, false);
        this.checkSize(sseSize);
        this.addSSESymbol(circle);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createDownHelix(int, int, int, int)
	 */
    @Override
	public void createDownHelix(int symbolNumber, int x, int y, int canvasSize) {
        int sseSize = this.getSSESize(canvasSize);
        Circle circle = new Circle(symbolNumber, x, y, sseSize, true);
        this.checkSize(sseSize);
        this.addSSESymbol(circle);
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createRightArc(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void createRightArc(SSESymbol source, SSESymbol destination) {
        this.rArcs.add(new DashedLine(source, destination, Bond.RIGHT_CHIRAL));
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createLeftArc(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void createLeftArc(SSESymbol source, SSESymbol destination) {
        this.lArcs.add(new DashedLine(source, destination, Bond.LEFT_CHIRAL));
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createABond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void createABond(SSESymbol source, SSESymbol destination) {
        this.aBonds.add(new DashedLine(source, destination, Bond.ANTIPARALLEL_HBOND));
    }
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#createPBond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public void createPBond(SSESymbol source, SSESymbol destination) {
        this.pBonds.add(new DashedLine(source, destination, Bond.PARALLEL_HBOND));
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#removeArc(int, int)
	 */
    @Override
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
            int source_num = bond.getStartSSESymbol().getSymbolNumber();
            int dest_num = bond.getEndSSESymbol().getSymbolNumber();

            if (source_num == symbolNumber || dest_num == symbolNumber) {
                itr.remove();
                return;
            }
        }
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#deleteSSESymbol(tops.drawing.symbols.SSESymbol)
	 */
    @Override
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
    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getVertexString()
	 */
    @Override
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


                    CartoonConnector cur_con = connections.get(j);

                    int con_source = cur_con.getStartSSESymbol().getSymbolNumber();
                    int con_dest = cur_con.getEndSSESymbol().getSymbolNumber();


                    if ( (con_source == i && con_dest == i + 1 ) ||
                            (con_source == i + 1 && con_dest == i) ) {
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
                } // for
            } //  if

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

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getEdgeString()
	 */
    @Override
	public String getEdgeString() {
        String edgeVertex = "";
        validateHBonds();

        for (int i = 0; i < sseSymbols.size(); i++) {
            edgeVertex += getBondInfo(i + 1, rArcs, "R");
            edgeVertex += getBondInfo(i + 1, lArcs, "L");
            edgeVertex += getBondInfo(i + 1, aBonds, "A");
            edgeVertex += getBondInfo(i + 1, pBonds, "P");
        }
        return edgeVertex;
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#validateHBonds()
	 */
    @Override
	public void validateHBonds() {
        // loop through the aBonds and pBonds looking for bonds that
        // should be switched

        ArrayList<Bond> temp_bonds = new ArrayList<Bond>();
        temp_bonds.clear();

        for (int i = 0; i < pBonds.size(); i++) {
            // look for bonds in aBonds that should be in pBonds
            Bond cur_bond = pBonds.get(i);

            SSESymbol source = cur_bond.getStartSSESymbol();
            SSESymbol dest = cur_bond.getEndSSESymbol();

            // now look for A bonds
            if ((source instanceof EquilateralTriangle && source.isDown()) 
                    || (source instanceof EquilateralTriangle && dest instanceof EquilateralTriangle)) {
                // remove this bond from a_Bonds, it belongs in P bonds
                // strore them into temp bonds temporarily.
                temp_bonds.add(cur_bond);
            }
        }
        pBonds.removeAll(temp_bonds);
        aBonds.addAll(temp_bonds);

        temp_bonds.clear();
        for (int i = 0; i < aBonds.size(); i++) {
            // look for bonds in aBonds that should be in pBonds
            Bond cur_bond = aBonds.get(i);

            SSESymbol source = cur_bond.getStartSSESymbol();
            SSESymbol dest = cur_bond.getEndSSESymbol();

            String source_class = source.getClass().toString();
            String dest_class = dest.getClass().toString();

            // now look for P bonds
            if (source_class.equals(dest_class) ) {
                // remove this bond from a_Bonds, it belongs in P bonds
                // strore them into temp bonds temporarily.
                temp_bonds.add(cur_bond);
            }
        }
        aBonds.removeAll(temp_bonds);
        pBonds.addAll(temp_bonds);

    }

    // returns the R_Arc from that is created between a symbol and another symbol
    // if one of those symbols is sseSymbols<symbolNum>
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#getBondInfo(int, java.util.List, java.lang.String)
	 */
    @Override
	public String getBondInfo(int symbolNum, List<Bond> ar, String sse_rep) {
        String bond_string = "";
        for (int j = 0; j < ar.size(); j++) {
            Bond bond = ar.get(j);

            // the source number for this arc
            int source_num = bond.getStartSSESymbol().getSymbolNumber() - 1;
            int dest_num = bond.getEndSSESymbol().getSymbolNumber() - 1;

            if (source_num == symbolNum) {
                boolean souceLessThanDest = source_num < dest_num;
                if (souceLessThanDest == true)
                    bond_string += source_num + ":" + dest_num + sse_rep + " ";

                if (souceLessThanDest == false)
                    bond_string += dest_num + ":" + source_num + sse_rep + " ";
            }
        }
        return bond_string;
    }

    
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#bondBetween(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol, java.util.List)
	 */
    @Override
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
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#canCreateHydrogenBond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public boolean canCreateHydrogenBond(SSESymbol source, SSESymbol dest) {
        //can only create a Hydrogen bond between Strands
        if (source instanceof Circle || dest instanceof Circle) {
            return false;
        }

        if ((bondBetween(source, dest, this.aBonds)) || (bondBetween(source, dest, this.pBonds))) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#shouldCreateParallelBond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public boolean shouldCreateParallelBond(SSESymbol source, SSESymbol dest) {
        return (source.isDown() && dest.isDown()) || (!source.isDown() && !dest.isDown());
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#shouldCreateAntiParallelBond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public boolean shouldCreateAntiParallelBond(SSESymbol source, SSESymbol dest) {
        return (!source.isDown() && dest.isDown()) || (!source.isDown() && dest.isDown());
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#canCreateRArc(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
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
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#canCreateLArc(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#canCreatePBond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public boolean canCreatePBond(SSESymbol source, SSESymbol dest) {
       return source != dest;
    }

    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#canCreateABond(tops.drawing.symbols.SSESymbol, tops.drawing.symbols.SSESymbol)
	 */
    @Override
	public boolean canCreateABond(SSESymbol source, SSESymbol dest) {
        return !this.bondBetween(source, dest, aBonds) && !this.bondBetween(source, dest, pBonds);
    }

    // outputs this SSE String
    /* (non-Javadoc)
	 * @see tops.drawing.Cartoon#toString()
	 */
    @Override
	public String toString() {
        String vertex = this.getVertexString();
        String edge = this.getEdgeString();
        return vertex + " " + edge;
    }

}
