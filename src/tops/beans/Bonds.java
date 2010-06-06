package tops.beans;

public class Bonds {

    public Bonds() {
    }

    private String Type;

    private String Atom_IDC;

    private String Atom_ID;

    private float Distance;

    public String getType() {
        return this.Type;
    }

    public String getAtom_IDC() {
        return this.Atom_IDC;
    }

    public String getAtom_ID() {
        return this.Atom_ID;
    }

    public float getDistance() {
        return this.Distance;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public void setAtom_IDC(String Atom_IDC) {
        this.Atom_IDC = Atom_IDC;
    }

    public void setAtom_ID(String Atom_ID) {
        this.Atom_ID = Atom_ID;
    }

    public void setDistance(float Distance) {
        this.Distance = Distance;
    }
}
