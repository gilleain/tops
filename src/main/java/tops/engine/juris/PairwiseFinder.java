package tops.engine.juris;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

public class PairwiseFinder {

    private FileReader inFile;

    private BufferedReader buffy;

    private Map<String, String> instMap;

    private List<String[]> pairList;

    private Constrainer c;

    private Matcher m;

    private String instr, maxpat;

    private int cPV, cPE, CVraw, CEraw, tmp1, tmp2, Mtot;

    private float Cnorm;
    
//    private float Mratio;

//    private long startTime, endTime;

    public PairwiseFinder(int lowerindex, int upperindex, String instFi) {
        this.maxpat = "";
        this.pairList = new ArrayList<String[]>();
        this.instMap = new HashMap<String, String>(3000);

        try {
            this.inFile = new FileReader(instFi);
        } catch (FileNotFoundException f) {
            System.out.println("No such string file");
        }

        this.buffy = new BufferedReader(this.inFile);

        // use a map for the strings (more efficient lookup?)
        ArrayList<String> tmpList = new ArrayList<String>();
        TParser tp = new TParser();
        try {
            while ((this.instr = this.buffy.readLine()) != null) {
                tp.load(this.instr);
                String name = tp.getName();
                tmpList.add(name);
                this.instMap.put(name, this.instr); // KEY is the head/domId
            }
        } catch (IOException IOE) {
            System.out.println("Major Error while reading pair file : " + IOE);
        }

        // special case where this instance will be doing all the comparisons
        if (lowerindex == -1)
            lowerindex = 0;
        if (upperindex == -1)
            upperindex = tmpList.size();

        for (int i = lowerindex; i < upperindex; i++) {
            for (int j = i + 1; j < tmpList.size(); j++) {
                if (i != j) {
                    String[] miniContainer = { (String) tmpList.get(i),
                            (String) tmpList.get(j) };
                    this.pairList.add(miniContainer);
                }
            }
        }

        // the domain names we are comparing are (pairKey, pairValue).
        String pair1, pair2;
        String[][] pairs = (String[][]) this.pairList.toArray(new String[0][]);
        String[] couple = new String[2];

        for (int i = 0; i < pairs.length; ++i) {
            this.Mtot = 0;
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple[0] = (String) this.instMap.get(pair1);
            couple[1] = (String) this.instMap.get(pair2);

            if (couple[0] != null && couple[1] != null) {

                // do the real work!
                this.c = new Constrainer(couple); // making new objects every
                                                // time!
                this.m = new Matcher(couple);
                this.matchExtendRepeat(new Grower());

                System.out.print(this.doCompression(this.c, this.maxpat));
                System.out.print('\t');
                // print out the pattern
                System.out.print(pair1 + '\t' + pair2 + '\t'); // say which we
                                                                // are trying
                System.out.print("pattern: " + this.maxpat);
                System.out.print('\t');
                tp.load(couple[0]);
                System.out.print(tp.getClassification());
                System.out.print('\t');
                tp.load(couple[1]);
                System.out.print(tp.getClassification());
                System.out.print('\n');

                this.maxpat = ""; // null the pattern!!
            } else {
                System.out.println("ONE OF THE NAMES WAS NULL!");
            }
        }

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
        if (this.Mtot > 100000)
            return;
        else
            this.Mtot++;
        if (gr.esize > 0) {
            // Metot++;
            if (this.m.run(gr.toString(), false)) {
            }// Megood++;System.out.println("P= " + gr.toString());}
            else {
                return;
            }
        } else {
            // Mvtot++;
            if (this.m.stringMatch(gr.toString())) {
            }// Mvgood++; System.out.println("P= " + gr.toString());}
            else {
                return;
            }
        }

        if ((gr.toString()).length() > this.maxpat.length())
            this.maxpat = gr.toString();

        if (gr.canAddEdge(1, this.c.max_A_in)) {
            if ((v = gr.getEdges(this.c.max_A_out, 'A')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'A'));
                }
            }
        }

        if (gr.canAddEdge(3, this.c.max_P_in)) {
            if ((v = gr.getEdges(this.c.max_P_out, 'P')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'P'));
                }
            }
        }

        if (gr.canAddEdge(5, this.c.max_R_in)) {
            if ((v = gr.getEdges(this.c.max_R_out, 'R')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'R'));
                }
            }
        }

        if (gr.canAddEdge(7, this.c.max_L_in)) {
            if ((v = gr.getEdges(this.c.max_L_out, 'L')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'L'));
                }
            }
        }

        if (gr.canAddEdge(9, this.c.max_Z_in)) {
            if ((v = gr.getEdges(this.c.max_Z_out, 'Z')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'Z'));
                }
            }
        }

        if (gr.canAddEdge(11, this.c.max_X_in)) {
            if ((v = gr.getEdges(this.c.max_X_out, 'X')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'X'));
                }
            }
        }

        // if (edgeJustAdded || gr.vsize < 2) { //ensure that we are in need of
        // a new vertex

        if (gr.num_E < this.c.max_E) {
            this.matchExtendRepeat(new Grower(gr.toString()).addUpStrand());
        }

        if (gr.num_e < this.c.max_e) {
            this.matchExtendRepeat(new Grower(gr.toString()).addDownStrand());
        }

        if (gr.num_H < this.c.max_H) {
            this.matchExtendRepeat(new Grower(gr.toString()).addUpHelix());
        }

        if (gr.num_h < this.c.max_h) {
            this.matchExtendRepeat(new Grower(gr.toString()).addDownHelix());
        }

        // }

        return;
    }// end of mER

    public static void main(String[] args) {
        if (args.length == 1) {
            new PairwiseFinder(-1, -1, args[0]);
        } else {
            new PairwiseFinder(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
        }
    }

}
// EOC
