package tops.drawing.actions;

import java.util.ArrayList;

import tops.drawing.Diagram;
import tops.drawing.symbols.Arc;
import tops.drawing.symbols.Line;
import tops.drawing.symbols.SSESymbol;

public class DeleteSymbols implements Action {
    
    private ArrayList<SSESymbol> symbolsToDelete;
    private Diagram diagram;
    private ArrayList<Line> deletionPoints;
    private ArrayList<ArrayList<Arc>> edgeLists;
    
    public DeleteSymbols(ArrayList<SSESymbol> symbolsToDelete, Diagram diagram) {
        this.symbolsToDelete = symbolsToDelete;
        this.diagram = diagram;
        this.edgeLists = new ArrayList<ArrayList<Arc>>();
        this.deletionPoints = new ArrayList<Line>();
    }
    
    public void doIt() {
        for (int i = 0; i < this.symbolsToDelete.size(); i++) {
            SSESymbol symbol = this.symbolsToDelete.get(i);
            this.edgeLists.add(this.diagram.getArcsFrom(symbol));
            this.deletionPoints.add(this.diagram.removeSSESymbol(symbol));
        }
    }
    
    public void undoIt() {
        for (int i = 0; i < this.symbolsToDelete.size(); i++) {
            SSESymbol symbol = this.symbolsToDelete.get(i);
            Line deletionPoint = this.deletionPoints.get(i);
            ArrayList<Arc> edges = this.edgeLists.get(i);
            this.diagram.insertSSESymbol(symbol, deletionPoint);
            this.diagram.addEdges(edges);
        }
    }
        
    public String description() {
        return "Delete SSE";
    }
}
