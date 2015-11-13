package tops.model;

public class Chain extends SSEGraph {
    
    private final String name;
    
    public Chain(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
