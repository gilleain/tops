package tops.engine.inserts;

import tops.engine.Vertex;

/**
 * Represents a regular expression or just a number
 * that constrains the gap between two SSEs.
 * 
 * @author gilleain
 *
 */
public abstract class Insert {

    public static final int STRING = 0; // a string of sses

    public static final int NUMBER = 1; // just a single number

    public static final int RANGE = 2; // a range (i,j)

    public static final int REGEX = 3; // a full blown regular expression

    private int type;

    public Insert(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public abstract boolean isNull();

    public abstract Object getValue();

    public abstract int getMinSize();

    public abstract int getMaxSize();

    public abstract boolean compareString(Insert other);

    public abstract boolean compareNumber(Insert other);

    public abstract boolean compareRange(Insert other);

    public abstract boolean compareRegex(Insert other);

    public abstract boolean match(Vertex v);

    public boolean match(Insert other) {
        int otherType = other.getType();

        switch (otherType) {
            case Insert.STRING:
                return this.compareString(other);
            case Insert.NUMBER:
                return this.compareNumber(other);
            case Insert.RANGE:
                return this.compareRange(other);
            case Insert.REGEX:
                return this.compareRegex(other);
            default:
                return false;
        }
    }
}
