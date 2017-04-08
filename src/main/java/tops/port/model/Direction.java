package tops.port.model;

public enum Direction {
    UP('U'),
    DOWN('D'),
    UNKNOWN('*');
    
    private char c;
    
    private Direction(char c) {
        this.c = c;
    }
    
    public String toString() {
        return String.valueOf(c);
    }
}
