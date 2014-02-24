package tops.drawing.model;


/**
 * @author maclean
 *
 */
public class Helix extends SSE {
    
    private int sseNumber;
    private boolean isUp;
    
    public Helix(int sseNumber, boolean isUp) {
        this.sseNumber = sseNumber;
        this.isUp = isUp;
    }
    
    public int getSSENumber() {
        return this.sseNumber;
    }
    
   public boolean isNumber(int n) {
       return this.sseNumber == n;
   }
   
   public boolean isUp() {
       return this.isUp;
   }
   
   public boolean equals(SSE other) {
       return other instanceof Helix && other.getSSENumber() == this.sseNumber;
   }
    
    public String toString() {
        return "Helix";
    }
}
