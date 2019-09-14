package tops.port.model;

public enum Direction {
    UP('U'),
    DOWN('D'),
    UNKNOWN('*');
    
    private char c;
    
    Direction(char c) {
        this.c = c;
    }
    
    public Direction opposite() {
        if (this == UP) {
            return DOWN;
        } else if (this == DOWN){
            return UP;
        } else {
            return UNKNOWN;
        }
    }
    
    @Override
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
