package tops.model.classification;

import tops.model.TopsParser;

public class Rep {

    private String levelName;

    private String pdbID;

    private String data;

    private String code;

    public Rep(String levelName, String topsString) {
        this.levelName = levelName;
        this.pdbID = TopsParser.getName(topsString);
        this.data = TopsParser.getDataSubstring(topsString);
        this.code = TopsParser.getClassification(topsString);
    }

    public Rep(String levelName, String code, String pdbID, String data) {
        this.levelName = levelName;
        this.pdbID = pdbID;
        this.data = data;
        this.code = code;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public String getPDBID() {
        return this.pdbID;
    }

    public String getData() {
        return this.data;
    }

    public String getCode() {
        return this.code;
    }

    public String getTopsString() {
        return this.pdbID + ' ' + this.data;
    }

    @Override
    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append(this.levelName).append(' ');
        stringbuffer.append(this.pdbID).append(' ');
        // stringbuffer.append(this.data).append(' ');
        stringbuffer.append(this.code).append(' ');
        return stringbuffer.toString();
    }
}
