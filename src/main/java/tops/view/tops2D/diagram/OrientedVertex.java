package tops.view.tops2D.diagram;

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
