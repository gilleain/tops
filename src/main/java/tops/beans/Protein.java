package tops.beans;

public class Protein {

    public Protein() {
    }

    private String Header;

    private double Resolution;

    private String PDB_ID;

    private String Exp_Method;

    public String getHeader() {
        return this.Header;
    }

    public double getResolution() {
        return this.Resolution;
    }

    public String getPDB_ID() {
        return this.PDB_ID;
    }

    public String getExp_Method() {
        return this.Exp_Method;
    }

    public void setHeader(String Header) {
        this.Header = Header;
    }

    public void setResolution(double Resolution) {
        this.Resolution = Resolution;
    }

    public void setPDB_ID(String PDB_ID) {
        this.PDB_ID = PDB_ID;
    }

    public void setExp_Method(String Exp_Method) {
        this.Exp_Method = Exp_Method;
    }
}
