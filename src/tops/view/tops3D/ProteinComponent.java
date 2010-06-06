package tops.view.tops3D;

//this class implements the Composite design pattern
//it's the base class for all Protein Components
//like Strands, Sheets, Domains etc

//should this be an interface?

import java.util.ArrayList;

public abstract class ProteinComponent {

    protected String typeLabel; // 'strand', 'sheet', 'barrel' etc

    protected ArrayList subComponents;

    protected int order; // 'order' is a generic positioning index

    public ProteinComponent(String typeLabel) {
        this.subComponents = new ArrayList();
        this.typeLabel = typeLabel;
        this.order = 0;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    // do nothing? 'Composite' extending classes overrride these methods
    // 'Leaf' classes do not (like Strand, Helix, etc)
    public void addSubComponent(int index, ProteinComponent sub) {
    }

    public ProteinComponent getSubComponent(ProteinComponent sub) {
        return null;
    }

    public void removeSubComponent(ProteinComponent sub) {
    }

    @Override
    public String toString() {
        return null;
    }
}
