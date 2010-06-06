package tops.engine.inserts;

import tops.engine.Vertex;

public class RegexInsert extends Insert {

    private java.util.regex.Pattern pattern;

    public RegexInsert(String regex) {
        super(Insert.REGEX);
        this.pattern = java.util.regex.Pattern.compile(regex);
    }

    @Override
    public boolean isNull() {
        return this.pattern.pattern().equals("");
    }

    @Override
    public Object getValue() {
        return this.pattern;
    }

    @Override
    public int getMinSize() {
        return 0;
    }

    @Override
    public int getMaxSize() {
        return 0;
    }

    @Override
    public String toString() {
        return this.pattern.pattern();
    }

    @Override
    public boolean match(Vertex v) {
        return false;
    }

    @Override
    public boolean compareString(Insert other) {
        String string = (String) other.getValue();
        java.util.regex.Matcher matcher = this.pattern.matcher(string);
        return matcher.matches();
    }

    // what does it mean to match an integer to a regex?
    @Override
    public boolean compareNumber(Insert other) {
        // Integer number = (Integer) other.getValue();
        return false;
    }

    // what does it mean to match an integer range to a regex?
    @Override
    public boolean compareRange(Insert other) {
        // int[] otherArray = (int[]) other.getValue();
        return false;
    }

    @Override
    public boolean compareRegex(Insert other) {
//        java.util.regex.Pattern otherRegex = (java.util.regex.Pattern) other.getValue();
        // DO java.regex.Matcher/java.regex.Pattern stuff!
        return false;
    }

}
