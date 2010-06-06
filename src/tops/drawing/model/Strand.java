package tops.drawing.model;


public class Strand extends SSE {
    private int sseNumber;
	private boolean isUp;
    
    public Strand(int sseNumber, boolean isUp) {
        this.sseNumber = sseNumber;
        this.isUp = isUp;
    }
    
    public void setSSENumber(int sseNumber) {
        this.sseNumber = sseNumber;
    }
    
    public boolean isNumber(int sseNumber) {
        return this.sseNumber == sseNumber;
    }
    
    public int getSSENumber() {
        return this.sseNumber;
    }
    
    public boolean isUp() {
        return this.isUp;
    }
    
    public boolean equals(SSE other) {
        return other instanceof Strand && other.getSSENumber() == this.sseNumber;
    }
    
    public String toString() {
        return "Strand " + this.sseNumber;
    }

}
