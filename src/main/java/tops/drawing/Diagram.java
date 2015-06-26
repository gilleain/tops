package tops.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import tops.drawing.symbols.Arc;
import tops.drawing.symbols.Box;
import tops.drawing.symbols.Bullet;
import tops.drawing.symbols.Line;
import tops.drawing.symbols.SSESymbol;
import tops.drawing.symbols.Triangle;

/**
 * @author maclean
 *
 */
public class Diagram {
    
    private List<SSESymbol> sseSymbols;
    private List<Arc> arcs;
    private List<Line> backbone;
    
    private boolean isLaidOut;
    
    private int width;
    private int height;
    
    public Diagram() {
        this.sseSymbols = new ArrayList<SSESymbol>();
        this.arcs = new ArrayList<Arc>();
        this.backbone = new ArrayList<Line>();
        
        this.isLaidOut = false;
        
        // Default values
        this.width = 400;
        this.height = 250;
    }
    
    public Diagram(String vertexString, String edgeString) {
        this(vertexString, edgeString, "");
    }
    
    public Diagram(String vertexString, String edgeString, String highlightString) {
        this();
        this.setVertices(vertexString);
        this.setEdges(edgeString);
        this.setHighlights(highlightString);
    }
    
    public void createTerminii() {
        Box nTerminus = new Box(0, "N");
        Box cTerminus = new Box(1, "C");
        
        this.sseSymbols.add(nTerminus);
        this.sseSymbols.add(cTerminus);
        
        Line backboneSegment = new Line(nTerminus, cTerminus);
        this.backbone.add(backboneSegment);
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public boolean isEmpty() {
        return this.sseSymbols.isEmpty();
    }
  
    public boolean needsLayout() {
        return !this.isLaidOut;
    }
    
    public void layout() {
        int axis = (2 * this.height) / 3;
        int numberOfVertices = this.sseSymbols.size();
        int centerSeparation = this.width / (numberOfVertices + 1);
        int symbolWidth = centerSeparation / 2;
        int symbolRadius = symbolWidth / 2;

        int xpos = centerSeparation;
        for (int i = 0; i < numberOfVertices; ++i) {
            SSESymbol sse = this.sseSymbols.get(i);
            sse.setDimensions(xpos, axis, symbolRadius);
            xpos += centerSeparation;
        }
        
        for (int j = 0; j < this.arcs.size(); j++) {
            Arc arc = this.arcs.get(j);
            arc.recreateShape();
        }
        
        for (int k = 0; k < this.backbone.size(); k++) {
            Line line = this.backbone.get(k);
            line.recreateShape();
        }
        
        this.isLaidOut = true;
    }

    public void setVertices(String vertexString) {
        int numberOfVertices = vertexString.length();

        for (int i = 0; i < numberOfVertices; i++) {
            char v = vertexString.charAt(i);
            SSESymbol symbol;
            switch (v) {
                case 'E':
                    symbol = new Triangle(i, false);
                    break;
                case 'e':
                    symbol = new Triangle(i, true);
                    break;
                case 'H':
                    symbol = new Bullet(i, false);
                    break;
                case 'h':
                    symbol = new Bullet(i, true);
                    break;
                case 'N':
                    symbol = new Box(i, "N");
                    break;
                case 'C':
                    symbol = new Box(i, "C");
                    break;
                default:
                    symbol = null;
                        
            }
            this.sseSymbols.add(symbol);
        }
    }
    
    public void setEdges(String estr) {
        int pos = 0;
        int last = 0;

        while (pos < estr.length()) { // for each char of the estr
            char ch = estr.charAt(pos);
            if (Character.isLetter(ch)) { // when you find a letter (A, P, R, L)
                String edgeStr = estr.substring(last, pos); // store the edge
                int Cpos = edgeStr.indexOf(':');
                
                int l = Integer.parseInt(edgeStr.substring(0, Cpos));
                int r = Integer.parseInt(edgeStr.substring(Cpos + 1, edgeStr.length()));
                
                SSESymbol left = this.sseSymbols.get(l);
                SSESymbol right = this.sseSymbols.get(r);
                
                switch (ch) {
                    case 'A':
                        this.arcs.add(new Arc(left, right, Arc.ANTIPARALLEL_HBOND));
                        break;

                    case 'P':
                        this.arcs.add(new Arc(left, right, Arc.PARALLEL_HBOND));
                        break;

                    case 'R':
                        this.arcs.add(new Arc(left, right, Arc.RIGHT_CHIRAL));
                        break;

                    case 'L':
                        this.arcs.add(new Arc(left, right, Arc.LEFT_CHIRAL));
                        break;

                    case 'Z':
                        this.arcs.add(new Arc(left, right, Arc.PARALLEL_HBOND));
                        this.arcs.add(new Arc(left, right, Arc.RIGHT_CHIRAL));
                        break;

                    case 'X':
                        this.arcs.add(new Arc(left, right, Arc.PARALLEL_HBOND));
                        this.arcs.add(new Arc(left, right, Arc.LEFT_CHIRAL));
                        break;
                }
                last = pos + 1;
            }
            ++pos;
        }
    }
    
    public void setHighlights(String highlights) {
        if (highlights == null || highlights.equals(""))
            return;
        // parse a string like '[1-3,2-4,3-5]'
        // remove enclosing []
        String bracketsOff = highlights.substring(1, highlights.length() - 1);
        StringTokenizer st = new StringTokenizer(bracketsOff, ",");
        
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int dash = pair.indexOf("-");
            Integer vertexToHighlight = Integer.valueOf(pair.substring(dash + 1));
            SSESymbol vertex = this.sseSymbols.get(vertexToHighlight.intValue());
            vertex.setSelectionState(true);
        }
    }
    
    /**
     * @param symbolNumbers an ArrayList of Integers
     */
    public void flipSSEList(ArrayList<Integer> symbolNumbers) {
        for (int i = 0; i < symbolNumbers.size(); i++) {
            int s = symbolNumbers.get(i);
            this.flipSSE(s);
        }
    }
    
    public void flipSSE(int symbolNumber) {
        SSESymbol sseSymbol = this.getSymbolWithNumber(symbolNumber);
        if (sseSymbol != null) {    /// XXX : silent fail!
            sseSymbol.flip();
        }
    }

    public void flip() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.flip();
        }
    }
    
    public ArrayList<Integer> getSelectedSymbolNumbers() {
        ArrayList<Integer> selectedNumbers = new ArrayList<Integer>();
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.isSelected()) {
                selectedNumbers.add(sseSymbol.getSymbolNumber());
            }
        }
        return selectedNumbers;
    }
    
    public ArrayList<SSESymbol> getSelectedSymbols() {
        ArrayList<SSESymbol> selectedSymbols = new ArrayList<SSESymbol>();
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.isSelected()) {
                selectedSymbols.add(sseSymbol);
            }
        }
        return selectedSymbols;
    }
    
    public SSESymbol getSymbolWithNumber(int symbolNumber) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.hasSymbolNumber(symbolNumber)) {
                return sseSymbol;
            }
        }
        return null;
    }
    
    public SSESymbol getSSESymbolAt(int x, int y) {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(x, y)) {
                return sseSymbol;
            }
        }
        return null;
    }
    
    /**
     * Toggle selection ensures that only one symbol is selected at a time.
     * 
     * @param x the x coordinate of the click.
     * @param y the y coordinate of the click.
     * @return the selected symbol or null if no selection.
     */
    public SSESymbol toggleSelectSymbolAt(int x, int y) {
        SSESymbol selected = null;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(x, y)) {
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
    
    /**
     * Simple selection allows any number of symbols to be selected at once.
     * 
     * @param x the x coordinate of the click.
     * @param y the y coordinate of the click.
     * @return the selected symbol or null if no selection.
     */
    public SSESymbol selectSymbolAt(int x, int y) {
        SSESymbol selected = this.getSSESymbolAt(x, y);
        if (selected != null) {
            selected.setSelectionState(true);
        }
        return selected;
    }
    
    /**
     * Toggle highlighting ensures that only one symbol is highlighted at a time.
     * 
     * @param x the x coordinate of the click.
     * @param y the y coordinate of the click.
     * @return the highlighted symbol or null if none found.
     */
    public SSESymbol toggleHighlightSymbolAt(int x, int y) {
        SSESymbol highlighted = null;
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.containsPoint(x, y)) {
                sseSymbol.setHighlightState(true);
                highlighted = sseSymbol;
            } else {
                sseSymbol.setHighlightState(false);
            }
        }
        return highlighted;
    }
    
    /**
     * Simple highlighting allows any number of symbols to be highlighted at once.
     * 
     * @param x the x coordinate of the click.
     * @param y the y coordinate of the click.
     * @return the highlighted symbol or null if none found.
     */
    public SSESymbol highlightSymbolAt(int x, int y) {
        SSESymbol highlighted = this.getSSESymbolAt(x, y);
        if (highlighted != null) {
            highlighted.setHighlightState(true);
        }
        return highlighted;
    }
    
    public Line toggleHighlightConnectionAt(int x, int y) {
        Line highlighted = null;
        for (int i = 0; i < this.backbone.size(); i++) {
            Line line = this.backbone.get(i);
            if (line.containsPoint(x, y)) {
                line.setHighlightState(true);
                highlighted = line;
            } else {
                line.setHighlightState(false);
            }
        }
        return highlighted;
    }
    
    public Line getHighlightedConnection() {
        for (int i = 0; i < this.backbone.size(); i++) {
            Line line = this.backbone.get(i);
            if (line.isHighlighted()) {
                return line;
            }
        }
        return null;
    }
    
    public boolean hasSelectedSymbols() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            if (sseSymbol.isSelected()) {
                return true;
            }
        }
        return false;
    }
    
    public void unselectAll() {
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol sseSymbol = this.sseSymbols.get(i);
            sseSymbol.setSelectionState(false);
        }
    }
    
    public void insertSSESymbol(SSESymbol sseToInsert, Line insertionPoint) {
        //set the sse number for the newly inserted sse
        System.out.println("inserting at : " + insertionPoint);
        
        SSESymbol leftSSE = insertionPoint.getStartSSESymbol();
        int sseToInsertNum = leftSSE.getSymbolNumber() + 1;
        int insertionIndex = sseToInsertNum;
        this.sseSymbols.add(insertionIndex, sseToInsert);
        
        //fix the numbering for all sses from insertionIndex inclusive
        this.renumberFrom(insertionIndex, 0);
        
        //rearrange the backbone
        insertionPoint.setStartSSESymbol(sseToInsert);
        Line leftHandBackboneSegment = new Line(leftSSE, sseToInsert);
        this.backbone.add(this.backbone.indexOf(insertionPoint), leftHandBackboneSegment);
        
        // relayout
        this.layout();
        insertionPoint.recreateShape();
    }
    
    public Line removeSSESymbol(int symbolNumber) {
        SSESymbol sseSymbol = this.getSymbolWithNumber(symbolNumber);
        return this.removeSSESymbol(sseSymbol);
    }
    
    public Line removeSSESymbol(SSESymbol sseToRemove) {
        int indexOfSSEToRemove = this.sseSymbols.indexOf(sseToRemove);
        
        // get the left incoming backbone segment and connect it to the next sse after the one to be deleted
        Line insertionPoint = this.backbone.get(indexOfSSEToRemove - 1);
        insertionPoint.setEndSSESymbol(this.sseSymbols.get(indexOfSSEToRemove + 1));
        
        this.renumberFrom(indexOfSSEToRemove + 1, -1);
        
        // now delete the sse and its right hand outgoing backbone segment
        this.sseSymbols.remove(indexOfSSEToRemove);
        this.backbone.remove(indexOfSSEToRemove);
        
        this.layout();
        System.out.println("insertion point = " + insertionPoint);
        
        return insertionPoint;
    }
    
    public void addEdges(ArrayList<Arc> edgesToAdd) {
        this.arcs.addAll(edgesToAdd);
//        Collections.sort(this.arcs);	 TODO : implement sorting of arcs!
    }
    
    public void addArc(Arc arc) {
        this.arcs.add(arc);
    }
    
    
    public ArrayList<Arc> getArcsFrom(SSESymbol sseSymbol) {
        ArrayList<Arc> arcsFromSSE = new ArrayList<Arc>();
        for (Arc arc : this.arcs) {
            if (arc.contains(sseSymbol)) {
                arcsFromSSE.add(arc);
            }
        }
        return arcsFromSSE;
    }
    
    public void removeArc(Arc arcToRemove) {
        this.arcs.remove(this.arcs.indexOf(arcToRemove));
    }
    
    /**
     * Fixes the numbering after e.g. an insert operation.
     * @param number the index to sseSymbols to start from (inclusive).
     * @param diff the amount to shift the numbering by.
     */
    public void renumberFrom(int number, int diff) {
        for (int i = number; i < this.sseSymbols.size(); i++) {
            SSESymbol nextSSE = this.sseSymbols.get(i);
            int newNumber = i + diff;
            System.out.println("renumbering " + nextSSE.toString() + " to " + newNumber);
            nextSSE.setSymbolNumber(newNumber);
        }
    }
    
    public void addHelix(int symbolNumber, int x, int y, int radius, boolean isDown) {
        this.sseSymbols.add(new Bullet(symbolNumber, x, y, radius, isDown));
    }
    
    public void addStrand(int symbolNumber, int x, int y, int radius, boolean isDown) {
        this.sseSymbols.add(new Triangle(symbolNumber, x, y, radius, isDown));
    }
    
    public void paint(Graphics2D g2) {
        if (this.needsLayout()) {
            this.layout();
        }
        
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, this.width, this.height);
        
        for (int i = 0; i < this.sseSymbols.size(); i++) {
            SSESymbol s = this.sseSymbols.get(i);
//            System.out.println("drawing " + s + " at " + s.getCenter());
            s.draw(g2);
        }
        
        for (int b = 0; b < this.backbone.size(); b++) {
            Line line =  this.backbone.get(b);
            line.draw(g2);
        }
        
        for (int j = 0; j < this.arcs.size(); j++) {
            Arc arc = this.arcs.get(j);
            arc.draw(g2);
        }
    }
    
    public String toPostscript() {
        StringBuilder ps = new StringBuilder();
        ps.append("%!PS-Adobe-3.0 EPSF-3.0\n");
        //ps.append("%!PS-Adobe-3.0\n");
        ps.append("%%Creator : tops.view.tops2D.diagram.DiagramDrawer\n");
        int x1 = 0;
        int y1 = 0;
        int x2 = this.width + x1;
        int y2 = this.height + y1;
        ps.append("%%BoundingBox: " + x1 + " " + y1 + " " + x2 + " " + y2 + "\n");
        ps.append("%%EndComments\n");
        ps.append("%%EndProlog\n");

        ps.append("1 -1 scale\n");    // flip the Y-axis
        ps.append("0 -" + this.height + " translate\n");    // translate back up

        int numberOfObjects = this.sseSymbols.size() + this.arcs.size();
        Shape[] shapes = new Shape[numberOfObjects];
        Color[] colors = new Color[numberOfObjects];
        int index = 0;
        Iterator<SSESymbol> sseItr = this.sseSymbols.iterator();

        while (sseItr.hasNext()) {
            SSESymbol sse = (sseItr.next());
            shapes[index] = sse.getShape();
            colors[index] = sse.getColor();
            index++;
        }

        Iterator<Arc> j = this.arcs.iterator();
        while (j.hasNext()) {
            Arc arc = (j.next());
            shapes[index] = arc.getShape();
            colors[index] = arc.getColor();
            index++;
        }
        
        for (int i = 0; i < shapes.length; i++) {
            PathIterator pathIterator = shapes[i].getPathIterator(new AffineTransform());
            ps.append("newpath\n");
            while (!pathIterator.isDone()) {
                float[] coords = new float[6]; 
                int segmentType = pathIterator.currentSegment(coords);
                switch (segmentType) {
                    case PathIterator.SEG_MOVETO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]);
                        ps.append(" moveto\n");
                        break;
                    case PathIterator.SEG_LINETO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]);
                        ps.append(" lineto\n");
                        break;
                    case PathIterator.SEG_QUADTO:
                        ps.append("quadto\n");
                        break;
                    case PathIterator.SEG_CUBICTO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]).append(" ");
                        ps.append(coords[2]).append(" ");
                        ps.append(coords[3]).append(" ");
                        ps.append(coords[4]).append(" ");
                        ps.append(coords[5]);
                        ps.append(" curveto\n");
                        break;
                    case PathIterator.SEG_CLOSE:
                        ps.append("closepath\n");
                        break;
                    default: break;
                }
                pathIterator.next();
            }
            //ps.append("1 setlinewidth\n");
            float[] rgb = colors[i].getRGBComponents(null);
            ps.append(rgb[0]).append(" ");
            ps.append(rgb[1]).append(" ");
            ps.append(rgb[2]).append(" setrgbcolor\n");
            ps.append("stroke\n");
        }
        ps.append("%EndDocument\n");
        ps.append("%EOF\n");

        return ps.toString();
    }

}
