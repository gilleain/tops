package python.model;

public class BridgeRange {
    
    public int start;
    
    public int end;
    
    public int length() {
        return Math.abs(end - start);  
    }

}
