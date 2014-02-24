package tops.beans;

public class ConnectionPoints {

    public ConnectionPoints() {
    }

    private int SSE_No;

    private String Source;

    private String Chain_ID;

    private String DOM_ID;

    private int ConnectionPointY;

    private int ConnectionPointX;

    private int CP_No;

    public int getSSE_No() {
        return this.SSE_No;
    }

    public String getSource() {
        return this.Source;
    }

    public String getChain_ID() {
        return this.Chain_ID;
    }

    public String getDOM_ID() {
        return this.DOM_ID;
    }

    public int getConnectionPointY() {
        return this.ConnectionPointY;
    }

    public int getConnectionPointX() {
        return this.ConnectionPointX;
    }

    public int getCP_No() {
        return this.CP_No;
    }

    public void setSSE_No(int SSE_No) {
        this.SSE_No = SSE_No;
    }

    public void setSource(String Source) {
        this.Source = Source;
    }

    public void setChain_ID(String Chain_ID) {
        this.Chain_ID = Chain_ID;
    }

    public void setDOM_ID(String DOM_ID) {
        this.DOM_ID = DOM_ID;
    }

    public void setConnectionPointY(int ConnectionPointY) {
        this.ConnectionPointY = ConnectionPointY;
    }

    public void setConnectionPointX(int ConnectionPointX) {
        this.ConnectionPointX = ConnectionPointX;
    }

    public void setCP_No(int CP_No) {
        this.CP_No = CP_No;
    }
}
