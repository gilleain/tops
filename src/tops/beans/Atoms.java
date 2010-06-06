package tops.beans;

public class Atoms {

    public Atoms() {
    }

    private String Element;

    private String Res_ID;

    private float Z;

    private float Y;

    private String Atom_Name;

    private String Atom_ID;

    private float X;

    private int Atom_Num;

    public String getElement() {
        return this.Element;
    }

    public String getRes_ID() {
        return this.Res_ID;
    }

    public float getZ() {
        return this.Z;
    }

    public float getY() {
        return this.Y;
    }

    public String getAtom_Name() {
        return this.Atom_Name;
    }

    public String getAtom_ID() {
        return this.Atom_ID;
    }

    public float getX() {
        return this.X;
    }

    public int getAtom_Num() {
        return this.Atom_Num;
    }

    public void setElement(String Element) {
        this.Element = Element;
    }

    public void setRes_ID(String Res_ID) {
        this.Res_ID = Res_ID;
    }

    public void setZ(float Z) {
        this.Z = Z;
    }

    public void setY(float Y) {
        this.Y = Y;
    }

    public void setAtom_Name(String Atom_Name) {
        this.Atom_Name = Atom_Name;
    }

    public void setAtom_ID(String Atom_ID) {
        this.Atom_ID = Atom_ID;
    }

    public void setX(float X) {
        this.X = X;
    }

    public void setAtom_Num(int Atom_Num) {
        this.Atom_Num = Atom_Num;
    }
}
