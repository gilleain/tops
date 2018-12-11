package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;

class Grower {

    private int numLowerH;
    private int numUpperH;
    private int numLowerE;
    private int numUpperE;
    private int vSize;
    private int eSize;
    private int oldEsize;
    private int oldVsize;
    private int rHE;
    private int lHE;

    private StringBuilder pattern;

    private String[] edgeStrings;

    private List<int[]> edgeArrays;

    private int[] caster;

    public Grower() { // empty constructor - start with a null pattern!
        this.setNumLowerH(this.setNumUpperH(this.setNumLowerE(this.setNumUpperE(0)))); // really!
        this.setvSize(0);
        this.seteSize(0);
        this.setOldVsize(0);
        this.setOldEsize(0);
        this.setrHE(0);
        this.setlHE(0);

        this.pattern = new StringBuilder("NC ");
        this.edgeArrays = new ArrayList<>();
        this.edgeArrays.add(new int[12]); // dummy edge
    }

    public Grower(String p) {
        String body;
        int f;
        if ((f = p.indexOf(' ')) != -1) {
            body = p.substring(0, f);
            this.edgeStrings = this.splitEdges(p.substring(p.lastIndexOf(' ') + 1));
        } else {
            body = p;
            this.seteSize(0);
        } // no edges!
    
        this.setNumLowerH(this.findMax(body, 'h'));
        this.setNumUpperH(this.findMax(body, 'H'));
        this.setNumLowerE(this.findMax(body, 'e'));
        this.setNumUpperE(this.findMax(body, 'E'));
        this.setvSize(body.length() - 2);
        this.setOldEsize(0);
        this.setOldVsize(0);
        this.setrHE(this.getvSize());
        this.setlHE(1); // wouldn't it be nice not to store the N & C terminii?
    
        this.pattern = new StringBuilder(p);
    
        this.edgeArrays = new ArrayList<>();
        for (int i = 0; i < this.getvSize() + 1; ++i) {
            this.edgeArrays.add(this.calculate(i));
        }
    }

    public boolean canAddEdge(int idx, int max) { // for the RIGHT hand end!
        if (this.getvSize() < 2)
            return false; // you can't add edges to a single vertex!
        
        // better to not try adding h-bonds to helices!
        if (((this.pattern.charAt(this.getrHE()) == 'H') || (this.pattern.charAt(this.getrHE()) == 'h'))
                && ((idx != 5) || (idx != 7)))
            return false;
        this.caster = (int[]) (this.edgeArrays.get(this.getrHE()));
        return (this.caster[idx] < max);
    }

    // returns a list of the vertices to which an edge of this type can be
    // attached.
    public int[] getEdges(int max, char tp) {
        int[] ptr;
        int[] vals;
        char lefttype;
        char righttype;
        int idx = this.translateOut(tp);
        righttype = this.pattern.charAt(this.getrHE());
        int counter = 0;
        int l;
        vals = new int[this.getvSize()];
    
        // note that the left hand end is being used as an incrementer, and is
        // SET by the search process
        // NO! this is not possible, as the only thing that can change is the
        // NEW grower object passed to the function
        for (l = 1; l < this.getvSize(); ++l) {
            ptr = (int[]) (this.edgeArrays.get(l));
            lefttype = this.pattern.charAt(l);
            if ((ptr[idx] < max) && (this.notGot(l, this.getrHE()))
                    && (this.verify(lefttype, righttype, tp))) {
                vals[counter++] = l; // store the valid vertex pos
            }
        }
        return vals;
    }

    // NOTE: 'vsize' is actually the string position of the C terminus!
    // rHE != vsize
    // DON@T TRY ANY MORE VERTICES : LCS WORKS!
    public Grower addUpStrand() {
        this.pattern.insert(this.setvSize(this.getvSize() + 1), 'E');
        this.edgeArrays.add(new int[12]);
        this.setrHE(this.getrHE() + 1);
        this.setNumUpperE(this.getNumUpperE() + 1);
        return this;
    }

    public Grower addDownStrand() {
        this.pattern.insert(this.setvSize(this.getvSize() + 1), 'e');
        this.edgeArrays.add(new int[12]);
        this.setrHE(this.getrHE() + 1);
        this.setNumLowerE(this.getNumLowerE() + 1);
        return this;
    }

    public Grower addUpHelix() {
        this.pattern.insert(this.setvSize(this.getvSize() + 1), 'H');
        this.edgeArrays.add(new int[12]);
        this.setrHE(this.getrHE() + 1);
        this.setNumUpperH(this.getNumUpperH() + 1);
        return this;
    }

    public Grower addDownHelix() {
        this.pattern.insert(this.setvSize(this.getvSize() + 1), 'h');
        this.edgeArrays.add(new int[12]);
        this.setrHE(this.getrHE() + 1);
        this.setNumLowerH(this.getNumLowerH() + 1);
        return this;
    }

    public Grower add(int l, char t) {
        this.pattern.append(l).append(':').append(this.getrHE()).append(t);
        this.caster = (int[]) (this.edgeArrays.get(l));
        this.caster[this.translateOut(t)]++; // outgoing A
        this.caster = (int[]) (this.edgeArrays.get(this.getrHE()));
        this.caster[this.translateIn(t)]++; // incoming A
        this.seteSize(this.geteSize() + 1);
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
        for (int i = 0; i < this.geteSize(); ++i) {
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
                this.seteSize(this.geteSize() + 1);
        }

        String[] result = new String[this.geteSize()];
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
        return result;
    }

    private boolean notGot(int lhe, int rhe) {
        String probe = String.valueOf(lhe) + ':' + String.valueOf(rhe);
        return this.pattern.toString().indexOf(probe) == -1;
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
            default:
                return -1;
        }
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
            default:
                return -1;
        }
    }

    public int getNumLowerH() {
        return numLowerH;
    }

    public void setNumLowerH(int numLowerH) {
        this.numLowerH = numLowerH;
    }

    public int getNumUpperH() {
        return numUpperH;
    }

    public int setNumUpperH(int numUpperH) {
        this.numUpperH = numUpperH;
        return numUpperH;
    }

    public int getNumLowerE() {
        return numLowerE;
    }

    public int setNumLowerE(int numLowerE) {
        this.numLowerE = numLowerE;
        return numLowerE;
    }

    public int getNumUpperE() {
        return numUpperE;
    }

    public int setNumUpperE(int numUpperE) {
        this.numUpperE = numUpperE;
        return numUpperE;
    }

    public int getvSize() {
        return vSize;
    }

    public int setvSize(int vSize) {
        this.vSize = vSize;
        return vSize;
    }

    public int geteSize() {
        return eSize;
    }

    public void seteSize(int eSize) {
        this.eSize = eSize;
    }

    public int getOldEsize() {
        return oldEsize;
    }

    public void setOldEsize(int oldEsize) {
        this.oldEsize = oldEsize;
    }

    public int getOldVsize() {
        return oldVsize;
    }

    public void setOldVsize(int oldVsize) {
        this.oldVsize = oldVsize;
    }

    public int getrHE() {
        return rHE;
    }

    public void setrHE(int rHE) {
        this.rHE = rHE;
    }

    public int getlHE() {
        return lHE;
    }

    public void setlHE(int lHE) {
        this.lHE = lHE;
    }

}
