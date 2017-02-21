package tops.view.diagram;

public abstract class OrientedVertex extends Vertex {
    
    private boolean isDown;
    
    public OrientedVertex(int position, boolean isDown) {
        super(position);
        this.isDown = isDown;
    }
    
    public boolean isDown() {
        return this.isDown;
    }

}
