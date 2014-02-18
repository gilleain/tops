package tops.engine.helix;

public class Dynamo {

    public String bestguess;

    private String[] results;

    public Dynamo() {
    }

    public Dynamo(String[] stringlist) {
        String v, w;
        this.results = new String[stringlist.length];
        v = this.strip(stringlist[1]);

        // first, get the bestguess by doing pairwise comparison
        for (int k = 0; k < stringlist.length; ++k) {
            w = this.strip(stringlist[k]);
            v = this.getLCS(this.LCS(v, w), v, v.length(), w.length());
            v = this.compress(v);
            System.out.println("compressed v = " + v);
        }

        this.bestguess = v;

        // now, using the bestguess, get the matchpositions.
        for (int l = 0; l < stringlist.length; ++l) {
            w = this.strip(stringlist[l]);
            this.results[l] = this.getLCS(this.LCS(w, v), w, w.length(), v.length());
            System.out.println("For graph : " + stringlist[l] + " LCS = ["
                    + this.results[l] + "]");
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
            return null;
        String tmp;
        int l;
        for (int i = 0; i < inserts[firstNonNull].length; i++) { // they
                                                                    // should
                                                                    // all be
                                                                    // the same
                                                                    // length!
            result[i] = inserts[firstNonNull][i]; // fill with the first in
                                                    // matrix
            for (int j = 1; j < inserts.length; j++) { // for each array in the
                                                        // matrix
                l = result[i].length();
                // System.out.println("insert : " + i + "," + j);
                if (inserts[j] != null) {
                    if (inserts[j][i] != null) {
                        tmp = inserts[j][i]; // the current position in the
                                                // current row
                        // System.out.println("i, j = " + i +"," + j + " insert
                        // : " + tmp + " result(i) : " + result[i] + " l = " +
                        // l);
                        try {
                            result[i] = this.getLCS(this.LCS(result[i], tmp), result[i],
                                    l, tmp.length());
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            System.out.println(aioobe);
                        }
                    }
                }
            }
            // System.out.println("result(i) :" + result[i]);
        }
        return result;
    }

    String ask() {
        return this.bestguess + " ";
    }

    boolean canAddStrand(char tp, int current) {
        if ((current + 1) < this.bestguess.length()) {
            // System.out.println("Bestguess = " + bestguess + " current = " +
            // current);
            if (this.bestguess.charAt(current + 1) == tp)
                return true;
            else
                return false;
        }
        return false;
    }

    String[] getResults() {
        return this.results;
    }

    // assume that it has head, body, tail. Also, that there are no " " double
    // spaces.
    String strip(String graph) {
        if (graph.indexOf(" ") == -1)
            return graph;
        else
            return graph.substring(graph.indexOf(" ") + 1, graph
                    .lastIndexOf(" "));
    }

    String compress(String LCS) {
        StringBuffer comp = new StringBuffer();
        for (int i = 0; i < LCS.length(); ++i) {
            if ((LCS.charAt(i) != '-') && (LCS.charAt(i) != '|'))
                comp.append(LCS.charAt(i));
        }
        return comp.toString();
    }

    int[] convert(String LCS) {
        int[] result = new int[LCS.length()];
        int j = 0;
        for (int i = 1; i < LCS.length() - 1; ++i) {
            if (LCS.charAt(i) != '-')
                result[j++] = i;
        }
        if (j < LCS.length() - 2) {
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

    int[][] LCS(String V, String W) {
        int n = V.length() + 1; // "l + 1" because of the terminal 0's
        int m = W.length() + 1;
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
                if (V.charAt(i - 1) == W.charAt(j - 1)) { // "i - 1" because
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

    String getLCS(int[][] b, String V, int i, int j) {
        String result = "";
        if ((i == 0) || (j == 0))
            return new String();

        if (b[i][j] == 0)
            result += this.getLCS(b, V, i - 1, j - 1) + V.charAt(i - 1);

        else if (b[i][j] == 1)
            result += this.getLCS(b, V, i - 1, j); // + '-';

        else
            result += this.getLCS(b, V, i, j - 1); // + '|';

        return result;
    }

//    static void main(String[] args) {
//        Dynamo d = new Dynamo(args);
//    }

}// EOC

