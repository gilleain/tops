package tops.engine.inserts;

import tops.engine.Vertex;

public class RangeInsert extends Insert {

    private int[] rangeValues;

    public static final String SEPARATOR = ":";

    public RangeInsert(String range) {
        super(Insert.RANGE);
        if (range.equals("")) {
            this.rangeValues = null;
        } else {
            int separatorIndex = range.indexOf(RangeInsert.SEPARATOR);
            String first = range.substring(0, separatorIndex);
            String last = range.substring(separatorIndex + 1, range.length());
            int min = Integer.parseInt(first);
            int max = Integer.parseInt(last);

            this.rangeValues = new int[2];
            this.rangeValues[0] = min;
            this.rangeValues[1] = max;
        }
    }

    public RangeInsert(int[] range) {
        super(Insert.RANGE);
        this.rangeValues = range;
    }

    @Override
    public boolean isNull() {
        return this.rangeValues == null;
    }

    @Override
    public Object getValue() {
        return this.rangeValues;
    }

    @Override
    public int getMinSize() {
        return this.rangeValues[0];
    }

    @Override
    public int getMaxSize() {
        return this.rangeValues[1];
    }

    @Override
    public String toString() {
        return Integer.toString(this.rangeValues[0]) + RangeInsert.SEPARATOR
                + Integer.toString(this.rangeValues[1]);
    }

    @Override
    public boolean match(Vertex v) {
        return (this.rangeValues[0] > 0);
    }

    @Override
    public boolean compareString(Insert other) {
        String string = (String) other.getValue();
        return (this.rangeValues[0] < string.length())
                && (this.rangeValues[1] > string.length());
    }

    @Override
    public boolean compareNumber(Insert other) {
        Integer number = (Integer) other.getValue();
        return (this.rangeValues[0] < number.intValue())
                && (this.rangeValues[1] > number.intValue());
    }

    @Override
    public boolean compareRange(Insert other) {
        int[] otherRange = (int[]) other.getValue();
        // array is (min, max) this number has to be greater than min and less
        // than max
        return (this.rangeValues[0] < otherRange[0])
                && (this.rangeValues[1] > otherRange[1]);
    }

    @Override
    public boolean compareRegex(Insert other) {
//        java.util.regex.Pattern regex = (java.util.regex.Pattern) other.getValue();
        // DO java.regex.Matcher/java.regex.Pattern stuff!
        return false;
    }

}
