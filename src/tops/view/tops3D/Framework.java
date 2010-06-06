package tops.view.tops3D;

//this class is the base class for a kind of builder for <i>Structures</i>.
//extend this class to make specific frameworks for particular types of structure.

public abstract class Framework {

    public static int CHIRAL_NONE = 0;

    public static int CHIRAL_LEFT = 1;

    public static int CHIRAL_RIGHT = 2;

    public Framework() {
    }

    public abstract void addSSE(int order, char type);

    public abstract void connectSSEs(int firstIndex, int secondIndex,
            int chirality);

}
