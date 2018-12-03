package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PairwiseFinder {

    private Constrainer c;

    private Matcher m;

    private String maxpat;

    private int cPV;
    private int cPE;
    private int CVraw;
    private int CEraw;
    private int tmp1;
    private int tmp2;
    private int Mtot;

    private float Cnorm;

    public PairwiseFinder(int lowerindex, int upperindex, List<String> names, Map<String, String> instMap) {
        this.maxpat = "";
        List<String[]> pairList = new ArrayList<>();
      
        // special case where this instance will be doing all the comparisons
        if (lowerindex == -1) {
            lowerindex = 0;
        }
        if (upperindex == -1) {
            upperindex = names.size();
        }

        for (int i = lowerindex; i < upperindex; i++) {
            for (int j = i + 1; j < names.size(); j++) {
                if (i != j) {
                    String[] pair = { names.get(i), names.get(j) };
                    pairList.add(pair);
                }
            }
        }

        // the domain names we are comparing are (pairKey, pairValue).
        String[][] pairs = pairList.toArray(new String[0][]);
        for (int i = 0; i < pairs.length; ++i) {
            doWork(i, pairs, instMap);
        }
    }
    
    /**
     * Terrible name, but hey.
     * 
     * @param i
     * @param pairs
     * @param instMap
     */
    private void doWork(int i, String[][] pairs, Map<String, String> instMap) {
        this.Mtot = 0;
        String pair1 = pairs[i][0];
        String pair2 = pairs[i][1];
        String[] couple = new String[2];
        couple[0] = instMap.get(pair1);
        couple[1] = instMap.get(pair2);

        if (couple[0] != null && couple[1] != null) {

            // do the real work!
            this.c = new Constrainer(couple); // making new objects every time!
            this.m = new Matcher(couple);
            this.matchExtendRepeat(new Grower());
            report(pair1, pair2, couple);
           
            this.maxpat = ""; // null the pattern!!
        } else {
            System.out.println("ONE OF THE NAMES WAS NULL!");
        }
    }
    
    private void report(String pair1, String pair2, String[] couple) {
        TParser tp = new TParser();
        System.out.print(this.doCompression(this.c, this.maxpat));
        System.out.print('\t');
        // print out the pattern
        System.out.print(pair1 + '\t' + pair2 + '\t'); // say which we are trying
        System.out.print("pattern: " + this.maxpat);
        System.out.print('\t');
        tp.load(couple[0]);
        System.out.print(tp.getClassification());
        System.out.print('\t');
        tp.load(couple[1]);
        System.out.print(tp.getClassification());
        System.out.print('\n');
    }

    public float doCompression(Constrainer c, String max) {
        // compression calculations
        this.cPV = this.countPV(this.maxpat);
        this.cPE = this.countPE(this.maxpat);

        this.CVraw = c.total_vert - this.cPV;
        this.CEraw = c.total_edge - this.cPE;

        this.tmp1 = c.total_things - (this.CVraw + this.CEraw);
        this.tmp2 = c.total_things - c.max_things;

        this.Cnorm = 1 - ((float) this.tmp1 / (float) this.tmp2);
        return this.Cnorm;
    }

    public int countPV(String p) { // count the number of pattern vertices
        int sum = 0;
        for (int i = p.indexOf('N') + 1; i < p.lastIndexOf('C'); ++i) {
            if ((p.charAt(i) != 'N') && (p.charAt(i) != 'C'))
                sum++;
        }
        return sum;
    }

    public int countPE(String p) { // count the number of pattern edges
        int sum = 0;
        for (int i = p.indexOf('C') + 1; i < p.length(); ++i) {
            if (Character.isLetter(p.charAt(i)))
                sum++;
        }
        return sum;
    }

    public void matchExtendRepeat(Grower gr) {
        int[] v; // this is the left hand end! - NO : this is now the points
                 // to add edges to!
        if (this.Mtot > 100000) {
            return;
        } else {
            this.Mtot++;
        }
        if (gr.esize > 0) {
            if (this.m.run(gr.toString(), false)) {
            } else {
                return;
            }
        } else {
            if (this.m.stringMatch(gr.toString())) {
            } else {
                return;
            }
        }

        if ((gr.toString()).length() > this.maxpat.length())
            this.maxpat = gr.toString();

        if (gr.canAddEdge(1, this.c.maxAIn)) {
            if ((v = gr.getEdges(this.c.maxAOut, 'A')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'A'));
                }
            }
        }

        if (gr.canAddEdge(3, this.c.maxPIn)) {
            if ((v = gr.getEdges(this.c.maxPOut, 'P')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'P'));
                }
            }
        }

        if (gr.canAddEdge(5, this.c.maxRIn)) {
            if ((v = gr.getEdges(this.c.maxROut, 'R')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'R'));
                }
            }
        }

        if (gr.canAddEdge(7, this.c.maxLIn)) {
            if ((v = gr.getEdges(this.c.maxLOut, 'L')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'L'));
                }
            }
        }

        if (gr.canAddEdge(9, this.c.maxZIn)) {
            if ((v = gr.getEdges(this.c.maxZOut, 'Z')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'Z'));
                }
            }
        }

        if (gr.canAddEdge(11, this.c.maxXIn)) {
            if ((v = gr.getEdges(this.c.maxXOut, 'X')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'X'));
                }
            }
        }

        // if (edgeJustAdded || gr.vsize < 2) { //ensure that we are in need of
        // a new vertex

        if (gr.num_E < this.c.maxEUpper) {
            this.matchExtendRepeat(new Grower(gr.toString()).addUpStrand());
        }

        if (gr.num_e < this.c.maxELower) {
            this.matchExtendRepeat(new Grower(gr.toString()).addDownStrand());
        }

        if (gr.num_H < this.c.maxHUpper) {
            this.matchExtendRepeat(new Grower(gr.toString()).addUpHelix());
        }

        if (gr.num_h < this.c.maxHLower) {
            this.matchExtendRepeat(new Grower(gr.toString()).addDownHelix());
        }
    }
}
