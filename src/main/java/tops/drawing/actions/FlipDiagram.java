package tops.drawing.actions;

import tops.drawing.Diagram;


public class FlipDiagram implements Action {
    private Diagram diagram;
    
    public FlipDiagram(Diagram diagram) {
        this.diagram = diagram;
    }
    
    public void doIt() {
        this.diagram.flip();
    }
    
    public void undoIt() {
        this.diagram.flip();
    }
    
        
    public String description() {
        return "Flip Diagram";
    }
}
