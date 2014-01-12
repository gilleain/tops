package tops.engine.juris;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Constrainer {

    private String[] vstrs, estrs;

    private int pop;

    public int max_A_out;

    public int max_P_out;

    public int max_L_out;

    public int max_R_out;

    public int max_Z_out;

    public int max_X_out;

    public int max_A_in;

    public int max_P_in;

    public int max_L_in;

    public int max_R_in;

    public int max_Z_in;

    public int max_X_in;

    public int max_e;

    public int max_E;

    public int max_h;

    public int max_H;

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

        this.max_A_out = this.findMaxOutdex('A');
        this.max_P_out = this.findMaxOutdex('P');
        this.max_L_out = this.findMaxOutdex('L');
        this.max_R_out = this.findMaxOutdex('R');
        this.max_Z_out = this.findMaxOutdex('Z');
        this.max_X_out = this.findMaxOutdex('X');

        this.max_A_in = this.findMaxIndex('A');
        this.max_P_in = this.findMaxIndex('P');
        this.max_L_in = this.findMaxIndex('L');
        this.max_R_in = this.findMaxIndex('R');
        this.max_Z_in = this.findMaxIndex('Z');
        this.max_X_in = this.findMaxIndex('X');

        this.max_e = this.findMin('e', this.vstrs);
        this.max_E = this.findMin('E', this.vstrs);
        this.max_h = this.findMin('h', this.vstrs);
        this.max_H = this.findMin('H', this.vstrs);
        
     // !!!if e = E and h = H, then it doesn't matter which we sum!
        this.max_vert = this.max_e + this.max_H; 

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
        outstring.append(this.max_e);
        outstring.append("\nmax_E= ");
        outstring.append(this.max_E);
        outstring.append("\nmax_h= ");
        outstring.append(this.max_h);
        outstring.append("\nmax_H= ");
        outstring.append(this.max_H);
        outstring.append("\nmax_vert= ");
        outstring.append(this.max_vert);
        outstring.append("\nmax_A_out= ");
        outstring.append(this.max_A_out);
        outstring.append("\nmax_P_out= ");
        outstring.append(this.max_P_out);
        outstring.append("\nmax_L_out= ");
        outstring.append(this.max_L_out);
        outstring.append("\nmax_R_out= ");
        outstring.append(this.max_R_out);
        outstring.append("\nmax_Z_out= ");
        outstring.append(this.max_Z_out);
        outstring.append("\nmax_X_out= ");
        outstring.append(this.max_X_out);
        outstring.append("\nmax_A_in= ");
        outstring.append(this.max_A_in);
        outstring.append("\nmax_P_in= ");
        outstring.append(this.max_P_in);
        outstring.append("\nmax_L_in= ");
        outstring.append(this.max_L_in);
        outstring.append("\nmax_R_in= ");
        outstring.append(this.max_R_in);
        outstring.append("\nmax_Z_in= ");
        outstring.append(this.max_Z_in);
        outstring.append("\nmax_X_in= ");
        outstring.append(this.max_X_in);
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
    
    public static void main(String[] args) {
        java.io.File file = new java.io.File(args[0]);
        try {
            ArrayList<String> instances = new ArrayList<String>();
            BufferedReader buffy = new BufferedReader(new FileReader(file));

            String instr;
            while ((instr = buffy.readLine()) != null) {
                instances.add(instr);
            }
            buffy.close();

            String[] inst = (String[]) instances.toArray(new String[0]);
            Constrainer graphConstraints = new Constrainer(inst);
            System.out.println(graphConstraints);
        } catch (IOException IOE) {
            System.err.println(IOE.toString());
        }
    }

}// EOC
