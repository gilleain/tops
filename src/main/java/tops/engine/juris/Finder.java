package tops.engine.juris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Finder {

    private FileReader inFile;

    private BufferedReader buffy;

    private List<String> instances;

    private Constrainer c;

    private Matcher m;

    private String instr, maxpat;

    private boolean debugging = false;

//    private int totalMatchNum, goodMatchNum;

    public Finder(String fi, String flag) {
        if (flag.equals("full"))
            this.debugging = true;
//        totalMatchNum = goodMatchNum = 0;
        this.maxpat = "";
        this.instances = new ArrayList<String>();

        try {
            this.inFile = new FileReader(fi);
        } catch (FileNotFoundException f) {
            System.out.println("No such file");
        }
        this.buffy = new BufferedReader(this.inFile);

        try {
            while ((this.instr = this.buffy.readLine()) != null)
                this.instances.add(this.instr);
        }

        catch (IOException IOE) {
            System.out.println("Major Error: " + IOE);
        }
        String[] inst = (String[]) this.instances.toArray(new String[0]);

        this.c = new Constrainer(inst);
        this.m = new Matcher(inst);
//        long startTime = System.currentTimeMillis();
        this.matchExtendRepeat(new Grower());
//        long endTime = System.currentTimeMillis() - startTime;

        // compression calculations
        int cP = this.countP(this.maxpat);
        int num = inst.length - 1;
        int Craw = this.c.total_edge - (cP * num);
        // System.out.println("<Patt_ID></Patt_ID>");
        // System.out.println("Pattern edge number = " + cP);
        // System.out.println("Input set edge number = " + c.total_edge + " for
        // " + num + " instances");
        // System.out.println("<Compression_RAW>" + Craw +
        // "</Compression_RAW>");
        // System.out.println("Craw = " + Craw);

        int tmp1 = this.c.total_edge - Craw;
        int tmp2 = this.c.total_edge - this.c.max_edge;
        // System.out.println("tmp1 = " + tmp1 + " tmp2 = " + tmp2);
        float Cnorm = 1 - ((float) tmp1 / (float) tmp2);
        // System.out.println("Cnorm = 1 - [(" + c.total_edge + " - " + Craw +
        // ") / (" + c.total_edge + " - "+ c.max_edge + ")]");
        // System.out.println("<Compression_NORM>" + Cnorm +
        // "</Compression_NORM>");
        // System.out.println("Cnorm = " + Cnorm);

        // String fullP = "patt_ID ";
        String fullP = "pattern= ";

        fullP += this.maxpat;
        // System.out.println("Time taken = " + endTime + " NumM = " +
        // totalMatchNum + " SuccM = " + goodMatchNum);

        // print out the input file!
        // for (int j = 0; j < inst.length; ++j) {
        // System.out.println(inst[j]);
        // }
        int indy = this.maxpat.indexOf(" ") + 1;

        if (indy != this.maxpat.length() - 1) {
            /*
             * m = new Matcher(maxpat, inst); String[] res = m.run(); for (int i =
             * 0; i < res.length; ++i) { //System.out.print(convert(res[i]));
             * System.out.println(res[i]); }
             */
            // fullP = getMax(res) + doEdges(maxpat.substring(indy)); //collage!
            // System.out.println('\n' + convertPatt(fullP));
            System.out.print(fullP + "\t" + Craw + "\t" + Cnorm + "\n");
        }
    }

    // prettify the output to conform to the standard DMamtora format
    public String convert(String s) {
        StringBuffer bloat = new StringBuffer();
        int sp = s.indexOf(' '); // first space
        int ssp = s.indexOf("<Dom_Inserts>");
        bloat.append("<Dom_ID>"); // domain Id 'tag'
        bloat.append(s.substring(0, sp)); // domain Id (head or pdbname)
        bloat.append("</Dom_ID>\n");
        // do vertices
        /*-----------------------------cut!-----------------------
         for (int i = sp; i < ssp - 2; ++i) {
         if (s.charAt(i) == '[') bloat.append("<INS>").append(s.charAt(i + 1)).append('\n');
         if (s.charAt(i) == ']') bloat.append("<SSE>").append(s.charAt(i + 1)).append('\n');
         }
         //do correspondances
         for (int j = ssp; j < s.length(); ++j) {
         if (s.charAt(j) == '[') bloat.append("\n<COR>").append(s.charAt(j + 1)).append('\n');
         if (s.charAt(j) == ',') bloat.append("<COR>").append(s.charAt(j + 2)).append('\n');
         }
         ---------------------------cut!---------------------------*/
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
        StringBuffer bloat = new StringBuffer();
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
        StringBuffer bloat = new StringBuffer();
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
        StringBuffer maxStr = new StringBuffer("<Patt_SSE>N</Patt_SSE>");
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
        int[] v; // this is the left hand end! - NO : this is now the points
                    // to add edges to!
        if (this.debugging)
            System.out.println(" Pattern at start = " + gr.toString());

        if (gr.esize > 0) {
            if (this.m.run(gr.toString(), false)) {
                if (this.debugging)
                    System.out.println("!!!!!!!!!!!!!!!!");
            } else {
                if (this.debugging)
                    System.out
                            .println("-------------------no EDGE match!----------");
                return;
            }
        } else {
            if (this.m.stringMatch(gr.toString())) {
                if (this.debugging)
                    System.out.println("#################");
            } else {
                if (this.debugging)
                    System.out
                            .println("--------------no VERTEX match!--------------");
                return;
            }
        }

        if ((gr.toString()).length() > this.maxpat.length())
            this.maxpat = gr.toString();
        // first, do the edges

        if (gr.canAddEdge(1, this.c.maxAIn)) {
            if ((v = gr.getEdges(this.c.maxAOut, 'A')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'A'));
                }
            }
        }

        if (gr.canAddEdge(3, this.c.maxPIn)) {
            if ((v = gr.getEdges(this.c.maxPOut, 'P')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'P'));
                }
            }
        }

        if (gr.canAddEdge(5, this.c.maxRIn)) {
            if ((v = gr.getEdges(this.c.maxROut, 'R')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'R'));
                }
            }
        }

        if (gr.canAddEdge(7, this.c.maxLIn)) {
            if ((v = gr.getEdges(this.c.maxLOut, 'L')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'L'));
                }
            }
        }

        if (gr.canAddEdge(9, this.c.maxZIn)) {
            if ((v = gr.getEdges(this.c.maxZOut, 'Z')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'Z'));
                }
            }
        }

        if (gr.canAddEdge(11, this.c.maxXIn)) {
            if ((v = gr.getEdges(this.c.maxXOut, 'X')) != null) {
                for (int i = 0; i < v.length && v[i] != 0; i++) {
                    this.matchExtendRepeat(new Grower(gr.toString()).add(v[i], 'X'));
                }
            }
        }

        // Now, do the vertices
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

        return;
    }

}
