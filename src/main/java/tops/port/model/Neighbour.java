package tops.port.model;

public class Neighbour {
    
    private SSE sse;
    
    private int distance;
    
    public Neighbour(SSE sse, int distance) {
        this.setSse(sse);
        this.setDistance(distance);
    }
    
    public String toString() {
        return String.format("%s (%s)", getSse().getSymbolNumber(), getDistance());
    }

    public SSE getSse() {
        return sse;
    }

    public void setSse(SSE sse) {
        this.sse = sse;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

}
