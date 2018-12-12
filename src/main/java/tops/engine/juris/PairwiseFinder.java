package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PairwiseFinder {

    private Constrainer c;

    private Matcher m;

    private String maxpat;

    private int rounds;

    private float cNorm;

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
        this.rounds = 0;
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
            String result = report(pair1, pair2, couple);
            System.out.println(result);
           
            this.maxpat = ""; // null the pattern!!
        } else {
            System.out.println("ONE OF THE NAMES WAS NULL!");
        }
    }
    
    private String report(String pair1, String pair2, String[] couple) {
        StringBuilder builder = new StringBuilder();
        TParser tp = new TParser();
        builder.append(this.doCompression(this.c));
        builder.append('\t');
        // print out the pattern
        builder.append(pair1 + '\t' + pair2 + '\t'); // say which we are trying
        builder.append("pattern: " + this.maxpat);
        builder.append('\t');
        tp.load(couple[0]);
        builder.append(tp.getClassification());
        builder.append('\t');
        tp.load(couple[1]);
        builder.append(tp.getClassification());
        builder.append('\n');
        return builder.toString();
    }

    public float doCompression(Constrainer c) {
        
        // compression calculations
        int cPV = this.countPV(this.maxpat);
        int cPE = this.countPE(this.maxpat);

        int cvRaw = c.getTotalVert() - cPV;
        int ceRaw = c.getTotalEdge() - cPE;

        int tmp1 = c.getTotalThings() - (cvRaw + ceRaw);
        int tmp2 = c.getTotalThings() - c.getMaxThings();

        this.cNorm = 1 - ((float) tmp1 / (float) tmp2);
        return this.cNorm;
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
        
        if (this.rounds > 100000) {
            return;
        } else {
            this.rounds++;
        }
        if (gr.getESize() > 0) {
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

        if (gr.canAddEdge(1, this.c.getMaxAIn())) {
            if ((v = gr.getEdges(this.c.getMaxAOut(), 'A')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'A'));
                }
            }
        }

        if (gr.canAddEdge(3, this.c.getMaxPIn())) {
            if ((v = gr.getEdges(this.c.getMaxPOut(), 'P')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'P'));
                }
            }
        }

        if (gr.canAddEdge(5, this.c.getMaxRIn())) {
            if ((v = gr.getEdges(this.c.getMaxROut(), 'R')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'R'));
                }
            }
        }

        if (gr.canAddEdge(7, this.c.getMaxLIn())) {
            if ((v = gr.getEdges(this.c.getMaxLOut(), 'L')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'L'));
                }
            }
        }

        if (gr.canAddEdge(9, this.c.getMaxZIn())) {
            if ((v = gr.getEdges(this.c.getMaxZOut(), 'Z')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'Z'));
                }
            }
        }

        if (gr.canAddEdge(11, this.c.getMaxXIn())) {
            if ((v = gr.getEdges(this.c.getMaxXOut(), 'X')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(
                            new Grower(gr.toString()).add(v[i], 'X'));
                }
            }
        }

        if (gr.getNumUpperE() < this.c.getMaxEUpper()) {
            this.matchExtendRepeat(new Grower(gr.toString()).addUpStrand());
        }

        if (gr.getNumLowerE() < this.c.getMaxELower()) {
            this.matchExtendRepeat(new Grower(gr.toString()).addDownStrand());
        }

        if (gr.getNumUpperH() < this.c.getMaxHUpper()) {
            this.matchExtendRepeat(new Grower(gr.toString()).addUpHelix());
        }

        if (gr.getNumLowerH() < this.c.getMaxHLower()) {
            this.matchExtendRepeat(new Grower(gr.toString()).addDownHelix());
        }
    }
}
