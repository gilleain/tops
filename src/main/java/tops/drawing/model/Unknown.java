package tops.drawing.model;

public class Unknown extends SSE {
    
    private int sseNumber;


    public Unknown(int sseNumber) {
        this.sseNumber = sseNumber;
    }
    
    @Override
    public boolean isUp() {
        // TODO Auto-generated method stub
        return false;
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

    @Override
    public boolean equals(SSE other) {
        return (other instanceof Unknown);  // TODO...
    }
    
    public String toString() {
        return "Unknown " + this.sseNumber + "("  + getStringRange() + ")";
    }

}
