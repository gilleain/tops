package tops.dssp;


public enum SSEType {
    RIGHT_ALPHA_HELIX ('H'), 
    HELIX_310 ('G'), 
    PI_HELIX ('I'), 
    EXTENDED ('E'), 
    TURN ('T'), 
    ISO_BRIDGE ('B');
    
    private final char symbol;
    
    private SSEType(char symbol) {
        this.symbol = symbol;
    }
    
    public String toString() {
        return String.valueOf(symbol);
    }
}
