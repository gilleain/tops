package tops.drawing.actions;

public interface Action {
    public void doIt();
    public void undoIt();
    public String description();
}
 
