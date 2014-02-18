package tops.engine.inserts;

import tops.engine.Vertex;

public class RangeInsert extends Insert {

    private int[] range;

    public static String separator = ":";

    public RangeInsert(String range) {
        super(Insert.RANGE);
        if (range.equals("")) {
            this.range = null;
        } else {
            int separatorIndex = range.indexOf(RangeInsert.separator);
            String first = range.substring(0, separatorIndex);
            String last = range.substring(separatorIndex + 1, range.length());
            int min = Integer.parseInt(first);
            int max = Integer.parseInt(last);

            this.range = new int[2];
            this.range[0] = min;
            this.range[1] = max;
        }
    }

    public RangeInsert(int[] range) {
        super(Insert.RANGE);
        this.range = range;
    }

    @Override
    public boolean isNull() {
        return this.range == null;
    }

    @Override
    public Object getValue() {
        return this.range;
    }

    @Override
    public int getMinSize() {
        return this.range[0];
    }

    @Override
    public int getMaxSize() {
        return this.range[1];
    }

    @Override
    public String toString() {
        return Integer.toString(this.range[0]) + RangeInsert.separator
                + Integer.toString(this.range[1]);
    }

    @Override
    public boolean match(Vertex v) {
        return (this.range[0] > 0);
    }

    @Override
    public boolean compareString(Insert other) {
        String string = (String) other.getValue();
        return (this.range[0] < string.length())
                && (this.range[1] > string.length());
    }

    @Override
    public boolean compareNumber(Insert other) {
        Integer number = (Integer) other.getValue();
        return (this.range[0] < number.intValue())
                && (this.range[1] > number.intValue());
    }

    @Override
    public boolean compareRange(Insert other) {
        int[] otherRange = (int[]) other.getValue();
        // array is (min, max) this number has to be greater than min and less
        // than max
        return (this.range[0] < otherRange[0])
                && (this.range[1] > otherRange[1]);
    }

    @Override
    public boolean compareRegex(Insert other) {
//        java.util.regex.Pattern regex = (java.util.regex.Pattern) other.getValue();
        // DO java.regex.Matcher/java.regex.Pattern stuff!
        return false;
    }

}
