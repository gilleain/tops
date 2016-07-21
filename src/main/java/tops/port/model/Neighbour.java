package tops.port.model;

public class Neighbour {
    
    public SSE sse;
    
    public int distance;
    
    public Neighbour(SSE sse, int distance) {
        this.sse = sse;
        this.distance = distance;
    }
    
    public String toString() {
        return String.format("%s (%s)", sse.getSymbolNumber(), distance);
    }

}
