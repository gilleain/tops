package tops.engine.inserts;

import tops.engine.Vertex;

public class StringInsert extends Insert {

    private String string;

    public StringInsert(String string) {
        super(Insert.STRING);
        this.string = string;
    }

    @Override
    public boolean isNull() {
        return this.string.equals("");
    }

    @Override
    public Object getValue() {
        return this.string;
    }

    @Override
    public int getMinSize() {
        return this.string.length();
    }

    @Override
    public int getMaxSize() {
        return this.string.length();
    }

    @Override
    public String toString() {
        return this.string;
    }

    @Override
    public boolean match(Vertex v) {
        return false; // !!!
    }

    @Override
    public boolean compareString(Insert other) {
        String otherString = (String) other.getValue();
        return this.string.equals(otherString);
    }

    @Override
    public boolean compareNumber(Insert other) {
        Integer number = (Integer) other.getValue();
        return this.string.length() < number.intValue();
    }

    @Override
    public boolean compareRange(Insert other) {
        int[] otherArray = (int[]) other.getValue();
        // array is (min, max) this number has to be greater than min and less
        // than max
        return (otherArray[0] < this.string.length())
                && (otherArray[1] > this.string.length());
    }

    @Override
    public boolean compareRegex(Insert other) {
//        java.util.regex.Pattern regex = (java.util.regex.Pattern) other.getValue();
        // DO java.regex.Matcher/java.regex.Pattern stuff!
        return false;
    }

}
