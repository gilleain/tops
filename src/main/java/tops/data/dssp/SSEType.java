package tops.data.dssp;

public enum SSEType {
    ALPHA_HELIX ("H"),
    HELIX_310 ("G"),
    PI_HELIX ("I"),
    EXTENDED ("E"),
    TURN ("T"),
    ISO_BRIDGE ("B"),
    S_BEND("S"),
    UNKNOWN ("U");
    
    private String code;
    
    private SSEType(String code) {
        this.code = code;
    }
    
    public static SSEType fromCode(String code) {
        for (SSEType type : SSEType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return SSEType.UNKNOWN;
    }
}
