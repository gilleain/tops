package tops.drawing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tops.drawing.symbols.Bond;
import tops.drawing.symbols.CartoonConnector;
import tops.drawing.symbols.SSESymbol;

public interface Cartoon {

	public void addCartoonConnector(CartoonConnector c);

	public void addCartoonConnector(int i, CartoonConnector c);

	public void addSSESymbol(SSESymbol sseSymbol);

	/*
	 * Use the selected connector to add sseSymbol between the endpoints. 
	 */
	public void addSSESymbolAtSelectedConnector(SSESymbol sseSymbol);

	/*
	 * If just clicking somewhere on the canvas, adds a sseSymbol just before the CTerm. 
	 */
	public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol);

	public void addSSESymbolBeforeCTerminus(SSESymbol sseSymbol, boolean selectSymbol);

	public boolean bondBetween(SSESymbol start, SSESymbol end, List<Bond> bonds);

	public boolean canCreateABond(SSESymbol source, SSESymbol dest);

	// returns true if a Hydrogen bond can be creates between the source and the destination
	public boolean canCreateHydrogenBond(SSESymbol source, SSESymbol dest);

	//returns true if it is valid to create an R arc between the source and destination
	public boolean canCreateLArc(SSESymbol source, SSESymbol dest);

	public boolean canCreatePBond(SSESymbol source, SSESymbol dest);

	public boolean canCreateRArc(SSESymbol source, SSESymbol dest);

	public void centerOn(int x, int y);

	public void centerOn(Point p);

	public void checkSize(int sseSize);

	public void clear();

	public Object clone();

	// returns true if the first sseSymbol is a strand (gmt:?)
	public boolean connectionStartsOnTop();

	public void createABond(SSESymbol source, SSESymbol destination);

	public void createDownHelix(int symbolNumber, int x, int y, int canvasSize);

	public void createDownStrand(int symbolNumber, int x, int y, int canvasSize);

	public void createLeftArc(SSESymbol source, SSESymbol destination);

	public void createPBond(SSESymbol source, SSESymbol destination);

	public void createRightArc(SSESymbol source, SSESymbol destination);

	// make the N/C termini for an empty Cartoon
	public void createTermini();

	public void createUpHelix(int symbolNumber, int x, int y, int canvasSize);

	public void createUpStrand(int symbolNumber, int x, int y, int canvasSize);

	public void deleteSSESymbol(SSESymbol sseSymbol);

	public void deselectAllSSESymbols();

	public void draw(Graphics g);

	public void fixTerminalPositions();

	public void flip();

	public void flipSymbol(SSESymbol symbol);

	public void flipXAxis(Point p);

	public void flipYAxis(Point p);

	// returns the R_Arc from that is created between a symbol and another symbol
	// if one of those symbols is sseSymbols<symbolNum>
	public String getBondInfo(int symbolNum, List<Bond> ar, String sse_rep);

	public Rectangle2D getBoundingBox();

	public CartoonConnector getCartoonConnector(int i);

	public Point getCenterPoint();

	public String getEdgeString();

	public CartoonConnector getSelectedConnector();

	public SSESymbol getSelectedSSESymbol(Point p);

	public ArrayList<SSESymbol> getSelectedSSESymbols();

	public Dimension getSize();

	/**
	 * @param dimension the width or height of the canvas 
	 * @return the size of the sses in this cartoon
	 */
	public int getSSESize(int dimension);

	public SSESymbol getSSESymbol(int i);

	public SSESymbol getSSESymbolAt(Point p);

	public SSESymbol getSSESymbolByNumber(int symbolnum);

	public String getVertexString();

	public boolean hasSelectedConnector();

	public void insertSymbol(SSESymbol source, SSESymbol dest, SSESymbol newSSESymbol);

	public void moveSelectedSSESymbols(int xDif, int yDif);

	public int numberOfConnectors();

	public int numberOfSelectedSSESymbols();

	public int numberOfSSESymbols();

	public void relayout();

	public void removeArc(int symbolNumber, int arcType);

	public void removeCartoonConnector(CartoonConnector connection);

	public void removeSSESymbol(int symbolNumber);

	public void renumberSymbolsFrom(int fromIndex);

	public void resizeSymbols();

	public void selectAllSSESymbols();

	public SSESymbol selectSSESymbol(Point p);

	public void setSelectedConnector(CartoonConnector connection);

	public boolean shouldCreateAntiParallelBond(SSESymbol source, SSESymbol dest);

	public boolean shouldCreateParallelBond(SSESymbol source, SSESymbol dest);

	public Iterator<SSESymbol> sseSymbolIterator();

	public SSESymbol toggleHighlightSSESymbol(Point p);

	public SSESymbol toggleSelectSSESymbol(Point p);

	// outputs this SSE String
	public String toString();

	public void updateBonds(SSESymbol symbol, List<Bond> bonds);

	public void updateConnectors(int symbolIndex);

	public void updateConnectors(SSESymbol sseSymbol);

	public void validateHBonds();

	public void writeToStream(PrintWriter pw);

}