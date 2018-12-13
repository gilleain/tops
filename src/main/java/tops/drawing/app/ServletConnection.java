package tops.drawing.app;


public class ServletConnection {
    
    private static final int TOPS_STRING_NAME = 0;
    private static final int NUM_RESULTS_TO_RETURN = 1;
    private static final int RESULTS_PER_PAGE = 2;
    private static final int SUB_CLASSES = 3;
    private static final int SUBMISSION_TYPE = 4;

    private static final String BASE_URL = "http://localhost:8080/tops/pattern/simple";
    
    private ServletConnection() {
        // prevent instantiation
    }

    public static String makeURL(String tBody, String tTail, String[] subData) {
        String tName = subData[TOPS_STRING_NAME];
        String topNum = subData[NUM_RESULTS_TO_RETURN];
        String pSize = subData[RESULTS_PER_PAGE];
        String subClass = subData[SUB_CLASSES];
        String tService = subData[SUBMISSION_TYPE];
        // convert to X and Z
        tTail = extract(tTail, tBody);
        String extraURL = generateExtra(tName, tBody, tTail, topNum, pSize, subClass, tService);
        StringBuilder builder = new StringBuilder(extraURL);
        replaceAll(extraURL, builder, " ", "%20");
        replaceAll(extraURL, builder, ",", "%2C");
        replaceAll(extraURL, builder, ":", "%3A");

        return BASE_URL + "?" + builder.toString();
    }

    private static void replaceAll(String s, StringBuilder sb, String stringToSearchFor, String stringToReplace) {
        int index = 0;
        while ((index = s.indexOf(stringToSearchFor, index)) != -1) {
            sb.replace(index, index + 1, stringToReplace);
        }
    }

    public static String generateExtra(String tName, String tBody, String tTail, String topNum, String pSize, String subClass, String tService) {
        String targetName = "target-name=" + tName;
        String targetBody = "target-body=" + tBody;
        String targetTail = "target-tail=" + tTail;
        String topNumSt = "topnum=" + topNum;
        String pageSize = "pagesize=" + pSize;
        String subClasses = "subclasses=" + subClass;
        String targetService = "targetService=" + tService;

        return targetName + "&" + targetBody + "&" + targetTail + "&" + topNumSt + "&" + pageSize + "&" + subClasses + "&" + targetService;
    }

    // this replaces a R-P with Z and L-P with X
    public static String extract(String bonds, String vertices) {

        int numOfFigs = vertices.length() - 2;

        for (int i = 1; i < numOfFigs + 1; i++) {
            for (int j = 1; j < numOfFigs + 1; j++) {
                StringBuilder builder = new StringBuilder(bonds);
                String pBond = i + ":" + j + "P";

                int pIndex = bonds.indexOf(pBond);
                if (pIndex >= 0) {
                    String rP = i + ":" + j + "R";

                    int rIndex = bonds.indexOf(rP);
                    if (rIndex >= 0) {
                        builder.replace(pIndex + 3, pIndex + 4, "Z");
                        builder.delete(rIndex, rIndex + 4);
                        bonds = builder.toString();
                    } else {
                        String lP = i + ":" + j + "L";
                        int lIndex = bonds.indexOf(lP);
                        if (lIndex >= 0) {
                            builder.replace(pIndex + 3, pIndex + 4, "X");
                            builder.delete(lIndex, lIndex + 4);
                            bonds = builder.toString();
                        }
                    }
                }
            }
        }
        return bonds;
    }
}
