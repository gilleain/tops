package tops.drawing.app;


public class ServletConnection {
    
    private static int TOPS_STRING_NAME = 0;
    private static int NUM_RESULTS_TO_RETURN = 1;
    private static int RESULTS_PER_PAGE = 2;
    private static int SUB_CLASSES = 3;
    private static int SUBMISSION_TYPE = 4;

    private static final String base_URL = "http://compbio.dcs.gla.ac.uk/tops/pattern/simple";
    //private static final String base_URL = "http://localhost:8080/tops/pattern/simple";

    public static String makeURL(String tBody, String tTail, String[] sub_Data) {
        String tName = sub_Data[TOPS_STRING_NAME];
        String topNum = sub_Data[NUM_RESULTS_TO_RETURN];
        String pSize = sub_Data[RESULTS_PER_PAGE];
        String subClass = sub_Data[SUB_CLASSES];
        String tService = sub_Data[SUBMISSION_TYPE];
        // convert to X and Z
        tTail = extract(tTail, tBody);
        String extra_URL = generateExtra(tName, tBody, tTail, topNum, pSize, subClass, tService);
        StringBuffer tmp = new StringBuffer(extra_URL);
        replaceAll(extra_URL, tmp, " ", "%20");
        replaceAll(extra_URL, tmp, ",", "%2C");
        replaceAll(extra_URL, tmp, ":", "%3A");

        String URL = base_URL + "?" + tmp.toString();

        return URL;
    }

    private static void replaceAll(String s, StringBuffer sb, String stringToSearchFor, String stringToReplace) {
        int index = 0;
        while ((index = s.indexOf(stringToReplace, index)) != -1) {
            sb.replace(index, index + 1, stringToReplace);
        }
    }

    public static String generateExtra(String tName, String tBody, String tTail, String topNum, String pSize, String subClass, String tService) {
        String targetName = "target-name=" + tName;
        String targetBody = "target-body=" + tBody;
        String targetTail = "target-tail=" + tTail;
        String topNum_st = "topnum=" + topNum;
        String pageSize = "pagesize=" + pSize;
        String subClasses = "subclasses=" + subClass;
        String targetService = "targetService=" + tService;

        String EXTRA = targetName + "&" + targetBody + "&" + targetTail + "&" + topNum_st + "&" + pageSize + "&" + subClasses + "&" + targetService;

        return EXTRA;
    }

    // this replaces a R-P with Z and L-P with X
    public static String extract(String bonds, String vertices) {

        int num_of_figs = vertices.length() - 2;

        for (int i = 1; i < num_of_figs + 1; i++) {
            for (int j = 1; j < num_of_figs + 1; j++) {
                StringBuffer temp = new StringBuffer(bonds);
                String p_bond = i + ":" + j + "P";

                int pIndex = bonds.indexOf(p_bond);
                if (pIndex >= 0) {
                    String r_p = i + ":" + j + "R";

                    int rIndex = bonds.indexOf(r_p);
                    if (rIndex >= 0) {
                        temp.replace(pIndex + 3, pIndex + 4, "Z");
                        temp.delete(rIndex, rIndex + 4);
                        bonds = temp.toString();
                    } else {
                        String l_p = i + ":" + j + "L";
                        int lIndex = bonds.indexOf(l_p);
                        if (lIndex >= 0) {
                            temp.replace(pIndex + 3, pIndex + 4, "X");
                            temp.delete(lIndex, lIndex + 4);
                            bonds = temp.toString();
                        }
                    }
                }
            }
        }
        return bonds;
    }
}
