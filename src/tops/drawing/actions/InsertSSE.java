package tops.drawing.actions;

import tops.drawing.Diagram;
import tops.drawing.symbols.Line;
import tops.drawing.symbols.SSESymbol;


public class InsertSSE implements Action {
    private SSESymbol sseToInsert;
    private Line insertionPoint;
    private Diagram diagram;
    
    public InsertSSE(SSESymbol sseToInsert, Line insertionPoint, Diagram diagram) {
        this.sseToInsert = sseToInsert;
        this.insertionPoint = insertionPoint;
        this.diagram = diagram;
    }
    
    public void doIt() {
        this.diagram.insertSSESymbol(this.sseToInsert, this.insertionPoint);
    }
    
    public void undoIt() {
        this.insertionPoint = this.diagram.removeSSESymbol(this.sseToInsert);
    }
        
    public String description() {
        return "Insert SSE";
    }
}
 
