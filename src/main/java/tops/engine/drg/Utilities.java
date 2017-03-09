package tops.engine.drg;

import java.util.List;

import tops.engine.TopsStringFormatException;

public class Utilities {
	
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
            return null;
        
        // they should all be the same length!
        for (int i = 0; i < inserts[firstNonNull].length; i++) { 
            result[i] = inserts[firstNonNull][i]; 
            // fill with the first in matrix
            for (int j = 1; j < inserts.length; j++) { // for each array in the matrix
                int l = result[i].length();
                if (inserts[j] != null) {
                    if (inserts[j][i] != null) {
                    	// the current position in the current row
                        String tmp = inserts[j][i]; 
                        try {
                            result[i] = Utilities.getLCS(
                            		Utilities.LCS(result[i], tmp), result[i], l, tmp.length());
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            System.out.println(aioobe);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static int[][] LCS(String V, String W) {
        int n = V.length() + 1; // "l + 1" because of the terminal 0's
        int m = W.length() + 1;
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
                if (V.charAt(i - 1) == W.charAt(j - 1)) { 
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

    public static String getLCS(int[][] b, String V, int i, int j) {
        String result = "";
        if ((i == 0) || (j == 0))
            return new String();

        if (b[i][j] == 0)
            result += Utilities.getLCS(b, V, i - 1, j - 1) + V.charAt(i - 1);

        else if (b[i][j] == 1)
            result += Utilities.getLCS(b, V, i - 1, j); // + '-';

        else
            result += Utilities.getLCS(b, V, i, j - 1); // + '|';

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
        int total_things = 0;
        for (int i = 0; i < instances.length; i++) { total_things += instances[i].vsize() + instances[i].esize(); }
        int min_things = getMinThings(instances);
        int Craw = total_things - (elements * (instances.length - 1));

        int tmp1 = total_things - Craw;
        int tmp2 = total_things - min_things;

        float Cnorm = 1 - ((float)tmp1 / (float)tmp2);
        return Cnorm;
    }

    public static float doDrgCompression(Pattern[] instances, Pattern p) {
        int pattern_SSE = p.vsize();
        int pattern_HBond = p.getNumberOfHBonds();
        int pattern_Chiral = p.getNumberOfChirals();

        int instanceTotal_SSE = 0;
        int instanceTotal_HBond = 0;
        int instanceTotal_Chiral = 0;

        int minimum_SSE = Integer.MAX_VALUE;
        int minimum_HBond = Integer.MAX_VALUE;
        int minimum_Chiral = Integer.MAX_VALUE;

        for (int i = 0; i < instances.length; i++) { 
            int instance_SSE = instances[i].vsize();
            int instance_HBond = instances[i].getNumberOfHBonds();
            int instance_Chiral = instances[i].getNumberOfChirals();

            instanceTotal_SSE += instance_SSE;
            instanceTotal_HBond += instance_HBond;
            instanceTotal_Chiral += instance_Chiral;

            minimum_SSE = Math.min(minimum_SSE, instance_SSE);
            minimum_HBond = Math.min(minimum_HBond, instance_HBond);
            minimum_Chiral = Math.min(minimum_Chiral, instance_Chiral);
        }

        int cRaw_SSE = rawScore(instanceTotal_SSE, pattern_SSE, instances.length);
        int cRaw_HBond = rawScore(instanceTotal_HBond, pattern_HBond, instances.length);
        int cRaw_Chiral = rawScore(instanceTotal_Chiral, pattern_Chiral, instances.length);

        float cNorm_SSE = normalizedScore(instanceTotal_SSE, cRaw_SSE, minimum_SSE);
        float cNorm_HBond = normalizedScore(instanceTotal_HBond, cRaw_HBond, minimum_HBond);
        float cNorm_Chiral = normalizedScore(instanceTotal_Chiral, cRaw_Chiral, minimum_Chiral);

        if ((new Float(cNorm_SSE)).isNaN()) { cNorm_SSE = 0; }
        if ((new Float(cNorm_HBond)).isNaN()) { cNorm_HBond = 0; }
        if ((new Float(cNorm_Chiral)).isNaN()) { cNorm_Chiral = 0; }

        float cTotal = (cNorm_SSE + cNorm_HBond + cNorm_Chiral) / 3;
        return cTotal;
        //return cTotal + "\t" + cNorm_SSE + "\t" + cNorm_HBond + "\t" + cNorm_Chiral;
    }

    public static int rawScore(int instanceTotal, int patternTotal, int numberOfInstances) {
        return instanceTotal - (patternTotal * (numberOfInstances - 1));
    }

    public static float normalizedScore(int instanceTotal, int rawScore, int minimum) {
        return 1 - ((float)(instanceTotal - rawScore) / (float)(instanceTotal - minimum));
    }

}
