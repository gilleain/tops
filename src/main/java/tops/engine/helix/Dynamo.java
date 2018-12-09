package tops.engine.helix;

import java.util.logging.Logger;

public class Dynamo {
    
    private Logger log = Logger.getLogger(Dynamo.class.getName());

    private String bestguess;

    private String[] results;

    public Dynamo() {
    }

    public Dynamo(String[] stringlist) {
        String v;
        String w;
        this.results = new String[stringlist.length];
        v = this.strip(stringlist[1]);

        // first, get the bestguess by doing pairwise comparison
        for (int k = 0; k < stringlist.length; ++k) {
            w = this.strip(stringlist[k]);
            v = this.getLCS(this.lcs(v, w), v, v.length(), w.length());
            v = this.compress(v);
        }

        this.setBestguess(v);

        // now, using the bestguess, get the matchpositions.
        for (int l = 0; l < stringlist.length; ++l) {
            w = this.strip(stringlist[l]);
            this.results[l] = this.getLCS(this.lcs(w, v), w, w.length(), v.length());
        }

    }

    // overloaded constructor for insert string [][]s
    String[] doInserts(String[][] inserts) {
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
        String tmp;
        int l;
        // they should all be the same length!
        for (int i = 0; i < inserts[firstNonNull].length; i++) { 
            result[i] = inserts[firstNonNull][i]; // fill with the first in matrix
            for (int j = 1; j < inserts.length; j++) { // for each array in the matrix
                l = result[i].length();
                if (inserts[j] != null && inserts[j][i] != null) {
                    tmp = inserts[j][i]; // the current position in the current row
                    try {
                        result[i] = this.getLCS(this.lcs(result[i], tmp), result[i],
                                l, tmp.length());
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        log.warning(aioobe.toString());
                    }
                }
            }
        }
        return result;
    }

    String ask() {
        return this.getBestguess() + " ";
    }

    boolean canAddStrand(char tp, int current) {
        return (current + 1 < this.getBestguess().length() 
              && this.getBestguess().charAt(current + 1) == tp);
    }

    String[] getResults() {
        return this.results;
    }

    // assume that it has head, body, tail. Also, that there are no " " double
    // spaces.
    String strip(String graph) {
        if (graph.indexOf(' ') == -1)
            return graph;
        else
            return graph.substring(graph.indexOf(' ') + 1, graph.lastIndexOf(' '));
    }

    String compress(String lcs) {
        StringBuilder comp = new StringBuilder();
        for (int i = 0; i < lcs.length(); ++i) {
            if ((lcs.charAt(i) != '-') && (lcs.charAt(i) != '|'))
                comp.append(lcs.charAt(i));
        }
        return comp.toString();
    }

    int[] convert(String lcs) {
        int[] result = new int[lcs.length()];
        int j = 0;
        for (int i = 1; i < lcs.length() - 1; ++i) {
            if (lcs.charAt(i) != '-')
                result[j++] = i;
        }
        if (j < lcs.length() - 2) {
            int[] trim = new int[j];
            System.arraycopy(result, 0, trim, 0, j);
            return trim;
        } else {
            return result;
        }
    }

    int[][] getMatches() {
        int[][] result = new int[this.results.length][];
        for (int i = 0; i < this.results.length; ++i) {
            result[i] = this.convert(this.results[i]);
        }
        return result;
    }

    int[][] lcs(String vStr, String wStr) {
        int n = vStr.length() + 1; // "l + 1" because of the terminal 0's
        int m = wStr.length() + 1;
        int[][] b = new int[n][m]; // 0 = DIAGONAL, 1 = UP, -1 = ACROSS
        int[][] s = new int[n][m];

        // SETUP
        for (int i = 0; i < n; ++i) {
            s[i][0] = 0;
        } // boundary condtions
        for (int i = 0; i < m; ++i) {
            s[0][i] = 0;
        }

        // COMPUTE
        for (int i = 1; i < n; ++i) { // "1 to n" becase the 0's are needed
            for (int j = 1; j < m; ++j) {
                if (vStr.charAt(i - 1) == wStr.charAt(j - 1)) { // "i - 1" because
                                                            // strings start at
                                                            // 0
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

    String getLCS(int[][] b, String v, int i, int j) {
        StringBuilder result = new StringBuilder();
        if ((i == 0) || (j == 0)) {
            return "";
        }

        if (b[i][j] == 0) {
            result.append(getLCS(b, v, i - 1, j - 1) + v.charAt(i - 1));
        } else if (b[i][j] == 1) {
            result.append(getLCS(b, v, i - 1, j)); // add '-'
        } else {
            result.append(getLCS(b, v, i, j - 1)); // add '|'
        }

        return result.toString();
    }

    public String getBestguess() {
        return bestguess;
    }

    public void setBestguess(String bestguess) {
        this.bestguess = bestguess;
    }
}

