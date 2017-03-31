package tops.port.model;

public enum SSEType {
    HELIX("H"), 
    EXTENDED("E"), 
    COIL("U"), 
    RIGHT_ALPHA_HELIX("H"), 
    HELIX_310("H"), 
    PI_HELIX("H"), 
    TURN("T"), 
    ISO_BRIDGE("I"), 
    LEFT_ALPHA_HELIX("H"), 
    NTERMINUS("N"), 
    CTERMINUS("C");
    
    private String oneLetterName;
    
    SSEType(String oneLetterName) {
        this.oneLetterName = oneLetterName;
    }
    
    public String getOneLetterName() {
        return oneLetterName;
    }
}