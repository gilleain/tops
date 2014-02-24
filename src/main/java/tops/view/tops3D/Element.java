package tops.view.tops3D;

//a generic leaf of the composite ProteinComponent class
//represents Strands, Helices, etc.

public class Element extends ProteinComponent {

    public Element() {
        super("Element");
    }

    public Element(String l) {
        super(l);
    }

    @Override
    public String toString() {
        return new String(this.typeLabel + " ChainOrder [" + this.order + "]");
    }
}
