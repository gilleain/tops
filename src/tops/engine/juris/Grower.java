package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;

class Grower {

    public int num_h, num_H, num_e, num_E, vsize, esize, old_esize, old_vsize,
            rHE, lHE;

    private StringBuffer pattern;

    private String[] edgeStrings;

    private List<int[]> edgeArrays;

    private int[] caster;

    public Grower() { // empty constructor - start with a null pattern!
        this.num_h = this.num_H = this.num_e = this.num_E = 0; // really!
        this.vsize = 0;
        this.esize = 0;
        this.old_vsize = 0;
        this.old_esize = 0;
        this.rHE = 0;
        this.lHE = 0;

        this.pattern = new StringBuffer("NC ");
        this.edgeArrays = new ArrayList<int[]>();
        this.edgeArrays.add(new int[12]); // dummy edge
    }

    public Grower(String p) {
        String body;
        int f;
        if ((f = p.indexOf(" ")) != -1) {
            body = p.substring(0, f);
            this.edgeStrings = this.splitEdges(p.substring(p.lastIndexOf(" ") + 1));
        } else {
            body = p;
            this.esize = 0;
        } // no edges!
    
        this.num_h = this.findMax(body, 'h');
        this.num_H = this.findMax(body, 'H');
        this.num_e = this.findMax(body, 'e');
        this.num_E = this.findMax(body, 'E');
        this.vsize = body.length() - 2;
        this.old_esize = 0;
        this.old_vsize = 0;
        this.rHE = this.vsize;
        this.lHE = 1; // wouldn't it be nice not to store the N & C terminii?
    
        this.pattern = new StringBuffer(p);
    
        this.edgeArrays = new ArrayList<int[]>();
        for (int i = 0; i < this.vsize + 1; ++i) {
            this.edgeArrays.add(this.calculate(i));
        }
    }

    public boolean canAddEdge(int idx, int max) { // for the RIGHT hand end!
        if (this.vsize < 2)
            return false; // you can't add edges to a single vertex!
        
        // better to not try adding h-bonds to helices!
        if (((this.pattern.charAt(this.rHE) == 'H') || (this.pattern.charAt(this.rHE) == 'h'))
                && ((idx != 5) || (idx != 7)))
            return false;
        this.caster = (int[]) (this.edgeArrays.get(this.rHE));
        return (this.caster[idx] < max);
    }

    // returns a list of the vertices to which an edge of this type can be
    // attached.
    public int[] getEdges(int max, char tp) {
        int[] ptr, vals;
        char lefttype, righttype;
        int idx = this.translateOut(tp);
        righttype = this.pattern.charAt(this.rHE);
        int counter = 0;
        int l;
        vals = new int[this.vsize];
    
        // note that the left hand end is being used as an incrementer, and is
        // SET by the search process
        // NO! this is not possible, as the only thing that can change is the
        // NEW grower object passed to the function
        for (l = 1; l < this.vsize; ++l) {
            ptr = (int[]) (this.edgeArrays.get(l));
            lefttype = this.pattern.charAt(l);
            // System.out.println("lHE = " + l + " rHE = " + rHE + " ptr[idx] =
            // " + ptr[idx] + " max = " + max + " ltype = " + lefttype + " rtype
            // = " + righttype);
            if ((ptr[idx] < max) && (this.notGot(l, this.rHE))
                    && (this.verify(lefttype, righttype, tp))) {
                // System.out.println("number " + counter + " is valid! ");
                vals[counter++] = l; // store the valid vertex pos
            }
        }
        return vals;
    }

    // NOTE: 'vsize' is actually the string position of the C terminus!
    // rHE != vsize
    // DON@T TRY ANY MORE VERTICES : LCS WORKS!
    public Grower addUpStrand() {
        this.pattern.insert(++this.vsize, 'E');
        this.edgeArrays.add(new int[12]);
        this.rHE++;
        this.num_E++;
        return this;
    }

    public Grower addDownStrand() {
        this.pattern.insert(++this.vsize, 'e');
        this.edgeArrays.add(new int[12]);
        this.rHE++;
        this.num_e++;
        return this;
    }

    public Grower addUpHelix() {
        this.pattern.insert(++this.vsize, 'H');
        this.edgeArrays.add(new int[12]);
        this.rHE++;
        this.num_H++;
        return this;
    }

    public Grower addDownHelix() {
        this.pattern.insert(++this.vsize, 'h');
        this.edgeArrays.add(new int[12]);
        this.rHE++;
        this.num_h++;
        return this;
    }

    public Grower add(int l, char t) {
        this.pattern.append(l).append(':').append(this.rHE).append(t);
        this.caster = (int[]) (this.edgeArrays.get(l));
        this.caster[this.translateOut(t)]++; // outgoing A
        this.caster = (int[]) (this.edgeArrays.get(this.rHE));
        this.caster[this.translateIn(t)]++; // incoming A
        this.esize++;
        return this;
    }

    @Override
    public String toString() {
        return this.pattern.toString();
    }

    private int[] calculate(int j) {
        int[] result = new int[12];
        String curr;
        char type;
        String sv = String.valueOf(j);
        int len = sv.length();
        for (int i = 0; i < this.esize; ++i) {
            curr = this.edgeStrings[i];
            type = curr.charAt(curr.length() - 1);
            if (curr.regionMatches(0, sv, 0, len)) { // outgoing edge from
                                                        // this vertex
                result[this.translateOut(type)]++;
            } else if (curr.regionMatches((curr.indexOf(':') + 1), sv, 0, len)) {
                // incoming edge to this vertex
                result[this.translateIn(type)]++;
            }
        }
        return result;
    }

    private String[] splitEdges(String e) {
        for (int i = 0; i < e.length(); ++i) {
            if (e.charAt(i) == ':')
                this.esize++;
        }

        String[] result = new String[this.esize];
        int counter = 0;
        int last = 0;
        for (int i = 0; i < e.length(); ++i) {
            if (Character.isLetter(e.charAt(i))) {
                result[counter++] = e.substring(last, i + 1);
                last = i + 1;
            }
        }
        return result;
    }

    private int findMax(String s, char n) {
        int c = 0;
        int last = 0;
        int next = 0;
        while ((next != -1)) {
            next = s.indexOf(n, last);
            last = next + 1;
            c++;
        }
        return c - 1;
    }

    private boolean verify(char l, char r, char t) {
        boolean result = true;
        // verify the edge against some rules.
        
        // for non-chiral edges, neither end can be H/h
        if ((t != 'R') || (t != 'L')) { 
            if ((l == 'h') || (l == 'H'))
                result = false;
            if ((r == 'h') || (r == 'H'))
                result = false;
        }

        if ((t == 'A') && (l == r))
            result = false; // antiparallel, but EE or ee

        if (((t == 'P') || (t == 'Z') || (t == 'X')) && (l != r))
            result = false; // parallel, but Ee or eE
            // System.out.println("verification : " + result);
        return result;
    }

    private boolean notGot(int lhe, int rhe) {
        String probe = new String();
        probe += String.valueOf(lhe) + ':' + String.valueOf(rhe);
        return ((this.pattern.toString()).indexOf(probe) == -1);
    }

    private int translateOut(char c) {
        switch (c) {
            case 'A':
                return 0;
            case 'P':
                return 2;
            case 'R':
                return 4;
            case 'L':
                return 6;
            case 'Z':
                return 8;
            case 'X':
                return 10;
        }
        return -1;
    }

    private int translateIn(char c) {
        switch (c) {
            case 'A':
                return 1;
            case 'P':
                return 3;
            case 'R':
                return 5;
            case 'L':
                return 7;
            case 'Z':
                return 9;
            case 'X':
                return 11;
        }
        return -1;
    }

    public static void main(String[] args) {
        Grower g = new Grower();
        System.out.println(g);
    }

}
