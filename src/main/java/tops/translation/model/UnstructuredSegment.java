package tops.translation.model;

public class UnstructuredSegment extends BackboneSegment {

    public UnstructuredSegment() {
        super();
    }

    public UnstructuredSegment(Residue r) {
        super(r);
    }

    @Override
    public char getTypeChar() {
        return 'U';
    }

    @Override
    public String toString() {
        return "Unstructured from " + this.firstResidue().getPDBNumber()
                + " to " + this.lastResidue().getPDBNumber();
    }

    @Override
    public String toFullString() {
        return this.toString();
    }
}
