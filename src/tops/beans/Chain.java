package tops.beans;

public class Chain {

    public Chain() {
    }

    private String PDB_ID;

    private String Compound;

    private String Type;

    private String EC_Number;

    private String Chain_ID;

    private String Sequence;

    public String getPDB_ID() {
        return this.PDB_ID;
    }

    public String getCompound() {
        return this.Compound;
    }

    public String getType() {
        return this.Type;
    }

    public String getEC_Number() {
        return this.EC_Number;
    }

    public String getChain_ID() {
        return this.Chain_ID;
    }

    public String getSequence() {
        return this.Sequence;
    }

    public void setPDB_ID(String PDB_ID) {
        this.PDB_ID = PDB_ID;
    }

    public void setCompound(String Compound) {
        this.Compound = Compound;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public void setEC_Number(String EC_Number) {
        this.EC_Number = EC_Number;
    }

    public void setChain_ID(String Chain_ID) {
        this.Chain_ID = Chain_ID;
    }

    public void setSequence(String Sequence) {
        this.Sequence = Sequence;
    }
}
