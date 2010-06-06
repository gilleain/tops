package tops.drawing.model;



/**
 * @author maclean
 *
 */
public class Terminus extends SSE {
    
    private String label;
    
    public Terminus(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public int getSSENumber() {
        return -1;
    }
    
    public boolean isNumber(int n) {
        return false;       // XXX : not sure which to return
    }
    
    public boolean equals(SSE other) {
        return other instanceof Terminus;
    }
    
    public boolean isUp() {
        return false;       // XXX : not sure which to return
    }

}
