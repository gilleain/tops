package tops.model.classification;

/**
 * Simple container for cath data.
 */

public class CATHNumber {

    private int C;

    private int A;

    private int T;

    private int H;

    private int S35;

    private int S95;

    private int S100;

    private String domainID;

    public CATHNumber(int C, int A, int T, int H, int S35, int S95, int S100,
            String domainID) {
        this.C = C;
        this.A = A;
        this.T = T;
        this.H = H;
        this.S35 = S35;
        this.S95 = S95;
        this.S100 = S100;
        this.domainID = domainID;
    }

    /**
     * Get the code of this cath number at the specified level <code>name</code>.
     * 
     * @param name
     *            the level name (C, A, T, H...) at which to return a code
     * @return the code at level <code>name</code>
     */

    public int getCodeForName(int name) {
        switch (name) {
            case 0:
                return this.C;
            case 1:
                return this.A;
            case 2:
                return this.T;
            case 3:
                return this.H;
            case 4:
                return this.S35;
            case 5:
                return this.S95;
            case 6:
                return this.S100;
        }
        return -1;
    }

    /**
     * Provide the domain ID associated with this cathnumber
     * 
     * @return the domain ID
     */
    public String getDomainID() {
        return this.domainID;
    }

    public String getFullCode() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.C).append(".");
        stringBuffer.append(this.A).append(".");
        stringBuffer.append(this.T).append(".");
        stringBuffer.append(this.H).append(".");
        stringBuffer.append(this.S35).append(".");
        stringBuffer.append(this.S95).append(".");
        stringBuffer.append(this.S100);

        return stringBuffer.toString();
    }

    /**
     * Return a String version.
     * 
     * @return a String
     */

    @Override
    public String toString() {
        return this.getFullCode() + " " + this.domainID;

    }
}
