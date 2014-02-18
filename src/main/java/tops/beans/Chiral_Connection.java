package tops.beans;

public class Chiral_Connection {

    public Chiral_Connection() {
    }

    private String Hand;

    private int SSE_No;

    private String Chain_ID;

    public String getHand() {
        return this.Hand;
    }

    public int getSSE_No() {
        return this.SSE_No;
    }

    public String getChain_ID() {
        return this.Chain_ID;
    }

    public void setHand(String Hand) {
        this.Hand = Hand;
    }

    public void setSSE_No(int SSE_No) {
        this.SSE_No = SSE_No;
    }

    public void setChain_ID(String Chain_ID) {
        this.Chain_ID = Chain_ID;
    }
}
