package tops.engine.juris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Finder {
    
    private Logger log = Logger.getLogger(Finder.class.getName());

    private FileReader inFile;

    private BufferedReader buffy;

    private List<String> instances;

    private Constrainer c;

    private Matcher m;

    private String instr;
    private String maxpat;

    public Finder(String filename) {
        this.maxpat = "";
        this.instances = new ArrayList<>();

        try {
            this.inFile = new FileReader(filename);
        } catch (FileNotFoundException f) {
            log.info("No such file");
        }
        this.buffy = new BufferedReader(this.inFile);

        try {
            while ((this.instr = this.buffy.readLine()) != null)
                this.instances.add(this.instr);
        }

        catch (IOException ioe) {
            log.info("Major Error: " + ioe);
        }
        String[] inst = this.instances.toArray(new String[0]);

        this.c = new Constrainer(inst);
        this.m = new Matcher(inst);
        this.matchExtendRepeat(new Grower());

        // compression calculations
        int cP = this.countP(this.maxpat);
        int num = inst.length - 1;
        int cRaw = this.c.getTotalEdge() - (cP * num);

        int tmp1 = this.c.getTotalEdge() - cRaw;
        int tmp2 = this.c.getTotalEdge() - this.c.getMaxEdge();
        float cNorm = 1 - ((float) tmp1 / (float) tmp2);
        String fullP = "pattern= ";

        fullP += this.maxpat;

        int indy = this.maxpat.indexOf(' ') + 1;

        if (indy != this.maxpat.length() - 1) {
            log.log(Level.INFO, "{0}\t{1}\t{2}%n", new Object[] { fullP, cRaw, cNorm });
        }
    }

    // prettify the output to conform to the standard DMamtora format
    public String convert(String s) {
        StringBuilder bloat = new StringBuilder();
        int sp = s.indexOf(' '); // first space
        int ssp = s.indexOf("<Dom_Inserts>");
        bloat.append("<Dom_ID>"); // domain Id 'tag'
        bloat.append(s.substring(0, sp)); // domain Id (head or pdbname)
        bloat.append("</Dom_ID>\n");
        bloat.append("<Corr>").append(s.substring(sp + 1, ssp)).append(
                "</Corr>\n");
        return bloat.toString();
    }

    public int countP(String p) {
        int sum = 0;
        for (int i = p.indexOf('C') + 1; i < p.length(); ++i) {
            if (Character.isLetter(p.charAt(i)))
                sum++;
        }
        return sum;
    }

    public String convertPatt(String s) {
        StringBuilder bloat = new StringBuilder();
        int sp = s.indexOf(' '); // first space
        int ssp = s.indexOf(' ', sp + 1); // second space
        bloat.append("<Patt_ID>"); // primary key
        bloat.append(s.substring(0, sp)); // patt_ID
        bloat.append("</Patt_ID>\n"); // end primary key
        // do pattern sses
        for (int i = sp; i < ssp - 2; ++i) {
            if (s.charAt(i) == '[')
                bloat.append("<Min>").append(s.charAt(i + 1)).append("</Min>");
            if (s.charAt(i) == ',')
                bloat.append("<Max>").append(s.charAt(i + 1)).append("</Max>");
            if (s.charAt(i) == ']')
                bloat.append("<Patt_SSE>").append(s.charAt(i + 1)).append(
                        "</Patt_SSE>");
        }
        bloat.append('\n'); // separate the data a bit
        return bloat.toString();
    }

    String doEdges(String s) {
        // do edges
        StringBuilder bloat = new StringBuilder();
        int lastT = 0;
        int lastC = lastT;
        for (int j = 0; j < s.length(); ++j) {
            if (s.charAt(j) == ':') {
                bloat.append("<End>").append(s.substring(lastT, j)).append(
                        "</End>");
                lastC = j + 1;
            }
            if (Character.isLetter(s.charAt(j))) {
                bloat.append("<Oth_End>").append(s.substring(lastC, j)).append(
                        "</Oth_End>");
                bloat.append("<Type>").append(s.charAt(j)).append("</Type>\n");
                lastT = j + 1;
            }
        }
        bloat.append('\n'); // separate the data a bit
        return bloat.toString();
    }

    public String getMax(String[] r) {
        StringBuilder maxStr = new StringBuilder("<Patt_SSE>N</Patt_SSE>");
        String tmp;
        int last = (this.maxpat.indexOf(' ') == -1) ? this.maxpat.length() : this.maxpat
                .indexOf(' ') - 1;
        for (int i = 0; i < last; ++i) {
            int max = 0; // reset
            int offset;
            int n = (i * 4); // the insert pattern is 4 times the size of the
                                // uninserted one
            int current = 0;
            for (int j = 0; j < r.length; ++j) {
                offset = r[j].indexOf("<Dom_Inserts>") + 13;
                tmp = r[j].substring(n + 2 + offset, r[j].indexOf(']', n
                        + offset));
                current = Integer.parseInt(tmp);
                if (current > max)
                    max = current;
            }
            maxStr.append("<Max>").append(max).append("</Max>\n");
            maxStr.append("<Patt_SSE>").append(this.maxpat.charAt(i + 1)).append(
                    "</Patt_SSE>"); // copy the vertex
        }
        maxStr.append('\n');
        return maxStr.toString();
    }

    public void matchExtendRepeat(Grower gr) {
        int[] v; // this is the left hand end! - NO : this is now the points to add edges to!

        if (gr.getESize() > 0) {
            if (!this.m.run(gr.toString(), false)) {
                return;
            }
        } else {
            if (!this.m.stringMatch(gr.toString())) {
                return;
            }
        }

        if ((gr.toString()).length() > this.maxpat.length())
            this.maxpat = gr.toString();
        // first, do the edges

        if (gr.canAddEdge(1, this.c.getMaxAIn())) {
            v = gr.getEdges(this.c.getMaxAOut(), 'A');
            if (v != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'A'));
                }
            }
        }

        if (gr.canAddEdge(3, this.c.getMaxPIn())) {
            if ((v = gr.getEdges(this.c.getMaxPOut(), 'P')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'P'));
                }
            }
        }

        if (gr.canAddEdge(5, this.c.getMaxRIn())) {
            if ((v = gr.getEdges(this.c.getMaxROut(), 'R')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'R'));
                }
            }
        }

        if (gr.canAddEdge(7, this.c.getMaxLIn())) {
            if ((v = gr.getEdges(this.c.getMaxLOut(), 'L')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'L'));
                }
            }
        }

        if (gr.canAddEdge(9, this.c.getMaxZIn())) {
            if ((v = gr.getEdges(this.c.getMaxZOut(), 'Z')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'Z'));
                }
            }
        }

        if (gr.canAddEdge(11, this.c.getMaxXIn())) {
            if ((v = gr.getEdges(this.c.getMaxXOut(), 'X')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'X'));
                }
            }
        }

        // Now, do the vertices
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
