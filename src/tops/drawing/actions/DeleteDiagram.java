package tops.drawing.actions;

import tops.drawing.Diagram;
import tops.drawing.app.DiagramEditorCanvas;


public class DeleteDiagram implements Action {
    
    private Diagram diagramToDelete;
    private DiagramEditorCanvas canvas;
    
    public DeleteDiagram(Diagram diagramToDelete, DiagramEditorCanvas canvas) {
        this.diagramToDelete = diagramToDelete;
        this.canvas = canvas;
    }
    
    public void doIt() {
        this.canvas.deleteDiagram();
    }
    
    public void undoIt() {
        this.canvas.setDiagram(this.diagramToDelete);
    }
    
    public String description() {
        return "Delete Diagram";
    }
    
}
