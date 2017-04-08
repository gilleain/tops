package tops.port.model;

public enum Direction {
    UP('U'),
    DOWN('D'),
    UNKNOWN('*');
    
    private char c;
    
    Direction(char c) {
        this.c = c;
    }
    
    public String toString() {
        return String.valueOf(c);
    }
    
    public static Direction fromString(String d) {
        for (Direction direction : Direction.values()) {
            if (direction.c == d.charAt(0)) {
                return direction;
            }
        }
        return null;
    }
}
