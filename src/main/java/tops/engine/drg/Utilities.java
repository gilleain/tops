package tops.engine.drg;

import java.util.List;

import tops.engine.TopsStringFormatException;

public class Utilities {
    
    private Utilities() {
        // prevent instantiation
    }
	
    public static String[] doInserts(String[][] inserts) {
        String[] result = null;
        int firstNonNull = -1;
        for (int n = 0; n < inserts.length; n++) {
            if (inserts[n] != null) {
                result = new String[inserts[n].length];
                firstNonNull = n;
                break;
            }
        }
        if (firstNonNull == -1)
            return new String[] {};
        
        // they should all be the same length!
        for (int i = 0; i < inserts[firstNonNull].length; i++) { 
            result[i] = inserts[firstNonNull][i]; 
            // fill with the first in matrix
            for (int j = 1; j < inserts.length; j++) { // for each array in the matrix
                int l = result[i].length();
                if (inserts[j] != null && inserts[j][i] != null) {
                    // the current position in the current row
                    String tmp = inserts[j][i]; 
                    try {
                        result[i] = getLCS(lcs(result[i], tmp), result[i], l, tmp.length());
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        System.out.println(aioobe);
                    }
                }
            }
        }
        return result;
    }

    public static int[][] lcs(String v, String w) {
        int n = v.length() + 1; // "l + 1" because of the terminal 0's
        int m = w.length() + 1;
        int[][] b = new int[n][m]; // 0 = DIAGONAL, 1 = UP, -1 = ACROSS
        int[][] s = new int[n][m];

        // SETUP
        for (int i = 0; i < n; ++i) {
            s[i][0] = 0;
        } // boundary conditions
        for (int i = 0; i < m; ++i) {
            s[0][i] = 0;
        }

        // COMPUTE
        for (int i = 1; i < n; ++i) { // "1 to n" because the 0's are needed
            for (int j = 1; j < m; ++j) {
            	
            	// "i - 1" because strings start at 0
                if (v.charAt(i - 1) == w.charAt(j - 1)) { 
                    s[i][j] = s[i - 1][j - 1] + 1;
                    b[i][j] = 0;

                } else if (s[i - 1][j] >= s[i][j - 1]) {

                    s[i][j] = s[i - 1][j];
                    b[i][j] = 1;

                } else {
                    s[i][j] = s[i][j - 1];
                    b[i][j] = -1;
                }
            }
        }
        return b;
    }

    public static String getLCS(int[][] b, String v, int i, int j) {
        String result = "";
        if ((i == 0) || (j == 0))
            return "";

        if (b[i][j] == 0)
            result += getLCS(b, v, i - 1, j - 1) + v.charAt(i - 1);

        else if (b[i][j] == 1)
            result += getLCS(b, v, i - 1, j);

        else
            result += getLCS(b, v, i, j - 1);

        return result;
    }

    public static int getMinThings(Pattern[] instances) {
        int nextSize;
        int currentMin = instances[0].vsize() + instances[0].esize();
        for (int i = 1; i < instances.length; i++) {
            nextSize = instances[i].vsize() + instances[i].esize();
            currentMin = Math.min(currentMin,nextSize);
        }
        return currentMin;
    }

    public static float doCompression(List<String> instances, Pattern p) throws TopsStringFormatException {
        Pattern[] pinstances = new Pattern[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            pinstances[i] = new Pattern(instances.get(i));
        }
        return doCompression(pinstances, p);
    }

    public static float doCompression(Pattern[] instances, Pattern p) {
        //compression calculations
        int elements = p.vsize() + p.esize();
        int totalThings = 0;
        for (int i = 0; i < instances.length; i++) { totalThings += instances[i].vsize() + instances[i].esize(); }
        int minThings = getMinThings(instances);
        int cRaw = totalThings - (elements * (instances.length - 1));

        int tmp1 = totalThings - cRaw;
        int tmp2 = totalThings - minThings;

        // normalise
        return  1 - ((float)tmp1 / (float)tmp2);
    }

    public static float doDrgCompression(Pattern[] instances, Pattern p) {
        int patternSSE = p.vsize();
        int patternHBond = p.getNumberOfHBonds();
        int patternChiral = p.getNumberOfChirals();

        int instanceTotalSSE = 0;
        int instanceTotalHBond = 0;
        int instanceTotalChiral = 0;

        int minimumSSE = Integer.MAX_VALUE;
        int minimumHBond = Integer.MAX_VALUE;
        int minimumChiral = Integer.MAX_VALUE;

        for (int i = 0; i < instances.length; i++) { 
            int instanceSSE = instances[i].vsize();
            int instanceHBond = instances[i].getNumberOfHBonds();
            int instanceChiral = instances[i].getNumberOfChirals();

            instanceTotalSSE += instanceSSE;
            instanceTotalHBond += instanceHBond;
            instanceTotalChiral += instanceChiral;

            minimumSSE = Math.min(minimumSSE, instanceSSE);
            minimumHBond = Math.min(minimumHBond, instanceHBond);
            minimumChiral = Math.min(minimumChiral, instanceChiral);
        }

        int cRawSSE = rawScore(instanceTotalSSE, patternSSE, instances.length);
        int cRawHBond = rawScore(instanceTotalHBond, patternHBond, instances.length);
        int cRawChiral = rawScore(instanceTotalChiral, patternChiral, instances.length);

        float cNormSSE = normalizedScore(instanceTotalSSE, cRawSSE, minimumSSE);
        float cNormHBond = normalizedScore(instanceTotalHBond, cRawHBond, minimumHBond);
        float cNormChiral = normalizedScore(instanceTotalChiral, cRawChiral, minimumChiral);

        if ((Float.valueOf(cNormSSE)).isNaN()) { cNormSSE = 0; }
        if ((Float.valueOf(cNormHBond)).isNaN()) { cNormHBond = 0; }
        if ((Float.valueOf(cNormChiral)).isNaN()) { cNormChiral = 0; }

        return (cNormSSE + cNormHBond + cNormChiral) / 3;
    }

    public static int rawScore(int instanceTotal, int patternTotal, int numberOfInstances) {
        return instanceTotal - (patternTotal * (numberOfInstances - 1));
    }

    public static float normalizedScore(int instanceTotal, int rawScore, int minimum) {
        return 1 - ((float)(instanceTotal - rawScore) / (float)(instanceTotal - minimum));
    }

}
