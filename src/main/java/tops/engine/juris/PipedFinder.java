package tops.engine.juris;

public class PipedFinder {

    private Constrainer c;

    private Matcher m;

    private String maxpat;

    public PipedFinder(String name, String[] inst) {
        this.maxpat = "";

        this.c = new Constrainer(inst);
        this.m = new Matcher(inst);
        this.matchExtendRepeat(new Grower());

        // compression calculations

        int cPV = this.countPV(this.maxpat);
        int cPE = this.countPE(this.maxpat);
        int num = inst.length - 1;

        int CVraw = this.c.getTotalVert() - (cPV * num);
        int CEraw = this.c.getTotalEdge() - (cPE * num);

        System.out.println("Craw(V,E) = (" + CVraw + "," + CEraw + ")");

        int tmp1 = this.c.getTotalThings() - (CVraw + CEraw);
        int tmp2 = this.c.getTotalThings() - this.c.getMaxThings();

        float Cnorm = 1 - ((float) tmp1 / (float) tmp2);
        System.out.println("Cnorm = " + Cnorm);

        String fullP = name + " ";

        fullP += this.maxpat;

        int spaceIndex = this.maxpat.indexOf(" ") + 1;

        if (spaceIndex != this.maxpat.length() - 1) {
            this.m = new Matcher(this.maxpat, inst);
            String[] res = this.m.run();
            for (int i = 0; i < res.length; ++i) {
                System.out.println(res[i]);
            }
            System.out.println(fullP);
        }

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
        // first, do the edges

        if (gr.canAddEdge(1, this.c.getMaxAIn())) {
            if ((v = gr.getEdges(this.c.getMaxAOut(), 'A')) != null) {
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

        return;
    }
}
