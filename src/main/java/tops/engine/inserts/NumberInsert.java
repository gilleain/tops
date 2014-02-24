package tops.engine.inserts;

import tops.engine.Vertex;

public class NumberInsert extends Insert {

    private Integer number;

    public NumberInsert(String number) {
        this(Integer.parseInt(number));
    }

    public NumberInsert(int number) {
        super(Insert.NUMBER);
        this.number = new Integer(number);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Object getValue() {
        return this.number;
    }

    @Override
    public int getMinSize() {
        return this.number.intValue();
    }

    @Override
    public int getMaxSize() {
        return this.number.intValue();
    }

    @Override
    public String toString() {
        return this.number.toString();
    }

    @Override
    public boolean match(Vertex v) {
        return (this.number.intValue() > 0);
    }

    @Override
    public boolean compareString(Insert other) {
        String otherString = (String) other.getValue();
        return otherString.length() < this.number.intValue();
    }

    @Override
    public boolean compareNumber(Insert other) {
        Integer otherInteger = (Integer) other.getValue();
        return otherInteger.intValue() < this.number.intValue();
    }

    @Override
    public boolean compareRange(Insert other) {
        int[] otherArray = (int[]) other.getValue();
        // array is (min, max) this number has to be greater than min and less
        // than max
        return (otherArray[0] < this.number.intValue())
                && (otherArray[1] > this.number.intValue());
    }

    @Override
    public boolean compareRegex(Insert other) {
//        java.util.regex.Pattern regex = (java.util.regex.Pattern) other.getValue();
        // DO java.regex.Matcher/java.regex.Pattern stuff!
        return false;
    }

}
