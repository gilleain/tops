package tops.beans;

public class Hydrogen_Bond {

    public Hydrogen_Bond() {
    }

    private int SSE_No;

    private String Type;

    private String Chain_ID;

    private int SSE_NoC;

    public int getSSE_No() {
        return this.SSE_No;
    }

    public String getType() {
        return this.Type;
    }

    public String getChain_ID() {
        return this.Chain_ID;
    }

    public int getSSE_NoC() {
        return this.SSE_NoC;
    }

    public void setSSE_No(int SSE_No) {
        this.SSE_No = SSE_No;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public void setChain_ID(String Chain_ID) {
        this.Chain_ID = Chain_ID;
    }

    public void setSSE_NoC(int SSE_NoC) {
        this.SSE_NoC = SSE_NoC;
    }
}
