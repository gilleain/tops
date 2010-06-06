package tops.drawing.actions;

import tops.drawing.Diagram;
import tops.drawing.symbols.Arc;


/**
 * @author maclean
 *
 */
public class AddArc implements Action {
    private Diagram diagram;
    private Arc arc;
    
    public AddArc(Arc arc, Diagram diagram) {
        this.arc = arc;
        this.diagram = diagram;
    }
    
    public void doIt() {
        this.diagram.addArc(this.arc);
    }
    
    public void undoIt() {
        this.diagram.removeArc(this.arc);
    }
        
    public String description() {
        return "Adding Arc";
    }

}
