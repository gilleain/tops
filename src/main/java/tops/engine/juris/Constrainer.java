package tops.engine.juris;

import java.util.ArrayList;
import java.util.Iterator;

public class Constrainer {

    private String[] vstrs; 
    private String[] estrs;

    private int pop;

    public int maxAOut;

    public int maxPOut;

    public int maxLOut;

    public int maxROut;

    public int maxZOut;

    public int maxXOut;

    public int maxAIn;

    public int maxPIn;

    public int maxLIn;

    public int maxRIn;

    public int maxZIn;

    public int maxXIn;

    public int maxELower;

    public int maxEUpper;

    public int maxHLower;

    public int maxHUpper;

    public int max_vert;

    public int max_A;

    public int max_P;

    public int max_Z;

    public int max_X;

    public int max_R;

    public int max_L;

    public int max_edge;

    public int max_things;

    public int total_vert;

    public int total_edge;

    public int total_things;

    public Constrainer(String[] victims) {
        this.pop = victims.length;
        this.vstrs = new String[this.pop];
        this.estrs = new String[this.pop];
        TParser tp = new TParser(victims);
        for (int i = 0; i < this.pop; ++i) { // chop off N/C
            this.vstrs[i] = tp.getVertexString();
            this.estrs[i] = tp.getEdgeString();
            tp.next();
        }

        this.maxAOut = this.findMaxOutdex('A');
        this.maxPOut = this.findMaxOutdex('P');
        this.maxLOut = this.findMaxOutdex('L');
        this.maxROut = this.findMaxOutdex('R');
        this.maxZOut = this.findMaxOutdex('Z');
        this.maxXOut = this.findMaxOutdex('X');

        this.maxAIn = this.findMaxIndex('A');
        this.maxPIn = this.findMaxIndex('P');
        this.maxLIn = this.findMaxIndex('L');
        this.maxRIn = this.findMaxIndex('R');
        this.maxZIn = this.findMaxIndex('Z');
        this.maxXIn = this.findMaxIndex('X');

        this.maxELower = this.findMin('e', this.vstrs);
        this.maxEUpper = this.findMin('E', this.vstrs);
        this.maxHLower = this.findMin('h', this.vstrs);
        this.maxHUpper = this.findMin('H', this.vstrs);
        
     // !!!if e = E and h = H, then it doesn't matter which we sum!
        this.max_vert = this.maxELower + this.maxHUpper; 

        this.max_A = this.findMin('A', this.estrs);
        this.max_P = this.findMin('P', this.estrs);
        this.max_Z = this.findMin('Z', this.estrs);
        this.max_X = this.findMin('X', this.estrs);
        this.max_R = this.findMin('R', this.estrs);
        this.max_L = this.findMin('L', this.estrs);
        this.max_edge = this.max_A + this.max_P + this.max_Z + this.max_X + this.max_R + this.max_L;

        this.max_things = this.max_vert + this.max_edge;

        this.total_vert = this.sums(this.vstrs);
        this.total_edge = this.sums(this.estrs);
        this.total_things = this.total_vert + this.total_edge;
    }

    private int sums(String[] e) {
        int sum = 0;
        for (int i = 0; i < this.pop; ++i) {
            for (int j = 0; j < e[i].length(); ++j) {
                if (Character.isLetter(e[i].charAt(j)))
                    sum++;
            }
        }
        return sum;
    }

    private int findMin(char n, String[] vORe) {
        int min = 0; // store best result
        // N is the /opposite/ of n, in terms of case
        char N = (Character.isUpperCase(n)) ? Character.toLowerCase(n)
                : Character.toUpperCase(n);
        char ch;
        for (int i = 0; i < this.pop; ++i) {
            int c = 0;
            for (int j = 0; j < vORe[i].length(); ++j) {
                ch = vORe[i].charAt(j);
                if ((ch == n) || (ch == N))
                    c++;
            }
            if (c == 0)
                return 0;
            if ((c <= min) || ((c > 0) && (min == 0)))
                min = c;
        }
        return min;
    }

    private int findMaxOutdex(char type) {
        ArrayList<String> fragments = new ArrayList<String>();
        int max = 0;
        for (int i = 0; i < this.estrs.length; ++i) {
            int last = 0;
            for (int j = 0; j < this.estrs[i].length(); ++j) {
                char fr = this.estrs[i].charAt(j);
                if (Character.isLetter(fr)) {
                    // only add relevant edges
                    if (fr == type) {
                        fragments.add(this.estrs[i].substring(last, j + 1));
                    }
                    last = j + 1;
                }
            }

            if (fragments.isEmpty()) { // no point counting the empty edges
                continue;
            }
            
            // currmax stores the maximum degree for this graph
            int currmax = 0; 

            String temp = (String) (fragments.get(0)); // look at first
            String node = temp.substring(0, temp.indexOf(':'));

            int c = 0;
            Iterator<String> itr = fragments.iterator();
            while (itr.hasNext()) {
                String edge = (String) (itr.next());
                String current = edge.substring(0, edge.indexOf(':'));
                if (current.equals(node)) {
                    c++;
                    if (c > currmax)
                        currmax = c;
                } else {
                    node = current;
                    c = 1;
                } // switch to a new node
            }
            if (currmax > max)
                max = currmax; // has this graph been the winner so far?
            fragments.clear(); // reset for next edgetype

        }
        return max;
    }

    private int findMaxIndex(char type) {
        ArrayList<String> fragments = new ArrayList<String>();
        int max = 0;
        
        for (int i = 0; i < this.estrs.length; ++i) {
            int last = 0;
            for (int j = 0; j < this.estrs[i].length(); ++j) {
                char fr = this.estrs[i].charAt(j);
                if (Character.isLetter(fr)) {
                    // only add relevant edges
                    if (fr == type) {
                        fragments.add(this.estrs[i].substring(last, j + 1));
                    }
                    last = j + 1;
                }
            }

            if (fragments.isEmpty()) { // no point counting the empty edges
                continue;
            } 
            
            int currmax = 0;
            Iterator<String> itr = fragments.iterator();
            while (itr.hasNext()) { 
                String t = (String) (itr.next());
                String node = t.substring(t.indexOf(':'), t.length() - 1);
                itr.remove();
                int c = 1;
                while (itr.hasNext()) {
                    t = (String) (itr.next());
                    String current = t.substring(t.indexOf(':'), t.length() - 1);
                    if (node.equals(current)) {
                        itr.remove();
                        c++;
                    }
                }
                if (c > currmax)
                    currmax = c;
                itr = fragments.iterator(); // reset??
            }

            if (currmax > max)
                max = currmax; // has this graph been the winner so far?
            fragments.clear(); // reset for next edgetype
        }
        return max;
    }

    public String toString() {
        StringBuffer outstring = new StringBuffer("");
        outstring.append("max_e= ");
        outstring.append(this.maxELower);
        outstring.append("\nmax_E= ");
        outstring.append(this.maxEUpper);
        outstring.append("\nmax_h= ");
        outstring.append(this.maxHLower);
        outstring.append("\nmax_H= ");
        outstring.append(this.maxHUpper);
        outstring.append("\nmax_vert= ");
        outstring.append(this.max_vert);
        outstring.append("\nmax_A_out= ");
        outstring.append(this.maxAOut);
        outstring.append("\nmax_P_out= ");
        outstring.append(this.maxPOut);
        outstring.append("\nmax_L_out= ");
        outstring.append(this.maxLOut);
        outstring.append("\nmax_R_out= ");
        outstring.append(this.maxROut);
        outstring.append("\nmax_Z_out= ");
        outstring.append(this.maxZOut);
        outstring.append("\nmax_X_out= ");
        outstring.append(this.maxXOut);
        outstring.append("\nmax_A_in= ");
        outstring.append(this.maxAIn);
        outstring.append("\nmax_P_in= ");
        outstring.append(this.maxPIn);
        outstring.append("\nmax_L_in= ");
        outstring.append(this.maxLIn);
        outstring.append("\nmax_R_in= ");
        outstring.append(this.maxRIn);
        outstring.append("\nmax_Z_in= ");
        outstring.append(this.maxZIn);
        outstring.append("\nmax_X_in= ");
        outstring.append(this.maxXIn);
        outstring.append("\nmax_A= ");
        outstring.append(this.max_A);
        outstring.append("\nmax_P= ");
        outstring.append(this.max_P);
        outstring.append("\nmax_Z= ");
        outstring.append(this.max_Z);
        outstring.append("\nmax_X= ");
        outstring.append(this.max_X);
        outstring.append("\nmax_R= ");
        outstring.append(this.max_R);
        outstring.append("\nmax_L= ");
        outstring.append(this.max_L);
        outstring.append("\nmax_edge= ");
        outstring.append(this.max_edge);
        outstring.append("\ntotal_vert= ");
        outstring.append(this.total_vert);
        outstring.append("\ntotal_edge= ");
        outstring.append(this.total_edge);
        return outstring.toString();
    }
}
