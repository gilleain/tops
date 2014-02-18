package tops.view.tops3D;

//this is the builder for the simplest possible representation of a tops.dw.protein structure
//it is really only a kind of test class

public class SimpleFramework extends Framework {

    private SimpleStructure structure;

    public SimpleFramework() {
        super();
        this.structure = new SimpleStructure();
        this.structure.makeNewSheet();
    }

    @Override
    public void addSSE(int order, char type) {
        this.structure.addToCurrentSheet(order, type);
    }

    @Override
    public void connectSSEs(int firstIndex, int secondIndex, int chirality) {
        if (chirality == Framework.CHIRAL_NONE) {
            this.structure.connectInCurrentSheet(firstIndex, secondIndex);
        } else {
            this.structure.connectOutsideCurrentSheet(firstIndex, secondIndex,
                    chirality);
        }
    }

    public SimpleStructure getStructure() {
        return this.structure;
    }
}
