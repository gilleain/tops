package tops.translation.model;

public class Terminus extends BackboneSegment {

    private String label;

    private char typeChar;

    public Terminus(String label, char typeChar) {
        this.label = label;
        this.typeChar = typeChar;
    }

    @Override
    public char getTypeChar() {
        return this.typeChar;
    }

    @Override
    public String getOrientation() {
        return "UP";
    }

    @Override
    public String toString() {
        return this.label;
    }

    @Override
    public String toFullString() {
        return this.toString();
    }

}
