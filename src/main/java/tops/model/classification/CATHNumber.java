package tops.model.classification;

/**
 * Simple container for cath data.
 */

public class CATHNumber {

    private int classNumber;

    private int architectureNumber;

    private int topologyNumber;

    private int homologyNumber;

    private int S35;

    private int S95;

    private int S100;

    private String domainID;

    public CATHNumber(int classNumber, int architectureNumber, int topologyNumber, int homologyNumber, int S35, int S95, int S100, String domainID) {
        this.classNumber = classNumber;
        this.architectureNumber = architectureNumber;
        this.topologyNumber = topologyNumber;
        this.homologyNumber = homologyNumber;
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
                return this.classNumber;
            case 1:
                return this.architectureNumber;
            case 2:
                return this.topologyNumber;
            case 3:
                return this.homologyNumber;
            case 4:
                return this.S35;
            case 5:
                return this.S95;
            case 6:
                return this.S100;
            default:
                return -1;
        }
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
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(this.classNumber).append(".");
        stringBuffer.append(this.architectureNumber).append(".");
        stringBuffer.append(this.topologyNumber).append(".");
        stringBuffer.append(this.homologyNumber).append(".");
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
