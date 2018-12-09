package tops.engine.inserts;

import tops.engine.Vertex;

public class NumberInsert extends Insert {

    private Integer value;

    public NumberInsert(String value) {
        this(Integer.parseInt(value));
    }

    public NumberInsert(int value) {
        super(Insert.NUMBER);
        this.value = value;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public int getMinSize() {
        return this.value.intValue();
    }

    @Override
    public int getMaxSize() {
        return this.value.intValue();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public boolean match(Vertex v) {
        return (this.value.intValue() > 0);
    }

    @Override
    public boolean compareString(Insert other) {
        String otherString = (String) other.getValue();
        return otherString.length() < this.value.intValue();
    }

    @Override
    public boolean compareNumber(Insert other) {
        Integer otherInteger = (Integer) other.getValue();
        return otherInteger.intValue() < this.value.intValue();
    }

    @Override
    public boolean compareRange(Insert other) {
        int[] otherArray = (int[]) other.getValue();
        // array is (min, max) this number has to be greater than min and less
        // than max
        return (otherArray[0] < this.value.intValue())
                && (otherArray[1] > this.value.intValue());
    }

    @Override
    public boolean compareRegex(Insert other) {
        // TODO java.regex.Matcher/java.regex.Pattern stuff!
        return false;
    }

}
