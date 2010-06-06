package tops.drawing.actions;

import java.util.ArrayList;

import tops.drawing.Diagram;
import tops.drawing.symbols.Line;
import tops.drawing.symbols.SSESymbol;

public class DeleteSymbols implements Action {
    
    private ArrayList symbolsToDelete;
    private Diagram diagram;
    private ArrayList deletionPoints;
    private ArrayList edgeLists;
    
    public DeleteSymbols(ArrayList symbolsToDelete, Diagram diagram) {
        this.symbolsToDelete = symbolsToDelete;
        this.diagram = diagram;
        this.edgeLists = new ArrayList();
        this.deletionPoints = new ArrayList();
    }
    
    public void doIt() {
        for (int i = 0; i < this.symbolsToDelete.size(); i++) {
            SSESymbol symbol = (SSESymbol)this.symbolsToDelete.get(i);
            this.edgeLists.add(this.diagram.getArcsFrom(symbol));
            this.deletionPoints.add(this.diagram.removeSSESymbol(symbol));
        }
    }
    
    public void undoIt() {
        for (int i = 0; i < this.symbolsToDelete.size(); i++) {
            SSESymbol symbol = (SSESymbol)this.symbolsToDelete.get(i);
            Line deletionPoint = (Line) this.deletionPoints.get(i);
            ArrayList edges = (ArrayList) this.edgeLists.get(i);
            this.diagram.insertSSESymbol(symbol, deletionPoint);
            this.diagram.addEdges(edges);
        }
    }
        
    public String description() {
        return "Delete SSE";
    }
}
