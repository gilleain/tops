package tops.engine.helix;

import java.util.*;
import java.io.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import tops.engine.Edge;
import tops.engine.Result;
import tops.engine.PlainFormatter;
import tops.engine.TabFormatter;
import tops.engine.TopsStringFormatException;
import tops.engine.TParser;
import tops.engine.Vertex;

public class Explorer {

    private List<Vertex> vertices;

    private Stack<Sheet> sheets;

    private Sheet currentSheet;

    private Matcher m;

    private final static int FROMLEFT = 1;

    private final static int TORIGHT = 2;

    private final static int CYCLE = 3;

    private static final Edge[] types = {
            new Edge(new Vertex('E', 0), new Vertex('E', 0), 'P'),
            new Edge(new Vertex('e', 0), new Vertex('e', 0), 'P'),
            new Edge(new Vertex('e', 0), new Vertex('E', 0), 'A'),
            new Edge(new Vertex('E', 0), new Vertex('e', 0), 'A'),
            new Edge(new Vertex('H', 0), new Vertex('H', 0), 'P'),
            new Edge(new Vertex('H', 0), new Vertex('h', 0), 'P'),
            new Edge(new Vertex('h', 0), new Vertex('H', 0), 'P'),
            new Edge(new Vertex('h', 0), new Vertex('h', 0), 'P'),
            new Edge(new Vertex('H', 0), new Vertex('H', 0), 'A'),
            new Edge(new Vertex('h', 0), new Vertex('H', 0), 'A'),
            new Edge(new Vertex('H', 0), new Vertex('h', 0), 'A'),
            new Edge(new Vertex('h', 0), new Vertex('h', 0), 'A'), };

    private static final Edge[] chiral_types = {
            new Edge(new Vertex('E', 0), new Vertex('E', 0), 'L'),
            new Edge(new Vertex('E', 0), new Vertex('E', 0), 'R'),
            new Edge(new Vertex('e', 0), new Vertex('e', 0), 'L'),
            new Edge(new Vertex('e', 0), new Vertex('e', 0), 'R'),
            new Edge(new Vertex('H', 0), new Vertex('H', 0), 'L'),
            new Edge(new Vertex('H', 0), new Vertex('H', 0), 'R'),
            new Edge(new Vertex('h', 0), new Vertex('h', 0), 'L'),
            new Edge(new Vertex('h', 0), new Vertex('h', 0), 'R'), };

    private static Logger logger = Logger
            .getLogger("tops.engine.helix.Explorer");

    public Explorer() {
        this.sheets = new Stack<Sheet>();
        this.vertices = new ArrayList<Vertex>();
        this.vertices.add(new Vertex('N', 0));
        this.vertices.add(new Vertex('C', 1));
        this.currentSheet = null;
        Explorer.logger.setLevel(Level.WARNING);
    }

    public String findPattern(String[] instances, boolean logging)
            throws TopsStringFormatException {
        this.m = new Matcher(instances);
        this.m.setLogging(logging);

        this.currentSheet = this.makeSheet();
        while (this.currentSheet != null) {
            this.extendAllPossibleWays();
            this.currentSheet = this.makeSheet();
        }
        Pattern result = this.getPattern();
        String[][] ins = this.m.generateInserts(result);
        Dynamo dyn = new Dynamo();
        String[] fin = dyn.doInserts(ins);
        String spliced = result.splice(fin);
        Pattern chi = this.doChirals(new Pattern(spliced));
        chi.sortEdges();
        chi.rename("pattern:");
        return new String(this.doCompression(instances, chi) + "\t" + chi.toString());
    }

    public void clear() {
        this.sheets = new Stack<Sheet>();
        this.vertices = new ArrayList<Vertex>();
        this.vertices.add(new Vertex('N', 0));
        this.vertices.add(new Vertex('C', 1));
        this.currentSheet = null;
    }

    public void allVsAll(ArrayList<String[]> pairList, HashMap<String, String> instMap, boolean verbose)
            throws TopsStringFormatException {
        // the domain names we are comparing are (pairKey, pairValue).
        String pair1, pair2;
        String[][] pairs = (String[][]) pairList.toArray(new String[0][]);
        String[] couple = new String[2];
//        TParser tp = new TParser();

        for (int i = 0; i < pairs.length; ++i) {
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple[0] = (String) instMap.get(pair1);
            couple[1] = (String) instMap.get(pair2);

            if (couple[0] != null && couple[1] != null) {
                // do the real work!
                try {
                    this.m = new Matcher(couple);
                    this.currentSheet = this.makeSheet();
                    while (this.currentSheet != null) {
                        this.extendAllPossibleWays();
                        this.currentSheet = this.makeSheet();
                    }
                    Pattern result = this.getPattern();
                    String[][] ins = this.m.generateInserts(result);
                    Pattern commonPattern;
                    if (ins != null) {
                        Dynamo dyn = new Dynamo();
                        String[] fin = dyn.doInserts(ins);
                        if (fin != null) {
                            String spliced = result.splice(fin);
                            commonPattern = this.doChirals(new Pattern(spliced));
                        } else {
                            commonPattern = result;
                        }
                    } else {
                        commonPattern = result;
                    }
                    commonPattern.sortEdges();
                    commonPattern.rename("pattern:");
                    if (verbose) {
                        this.doVerbosePairCompression(couple, commonPattern,
                                pair1, pair2);
                    } else {
                        /*
                         * Object[] data = { new Float(doDrgCompression(couple,
                         * commonPattern)), //new Float(doCompression(couple,
                         * commonPattern)), pair1, pair2, commonPattern };
                         * this.logger.log(Level.INFO, "", data);
                         */
                        System.out.println(this.doDrgCompression(couple,
                                commonPattern)
                                + "\t"
                                + pair1
                                + "\t"
                                + pair2
                                + "\t"
                                + commonPattern);
                    }
                    this.clear();
                } catch (Exception e) {
                    System.err.println(pair1 + " , " + pair2 + ", " + e);
                    e.printStackTrace(System.err);
                }
            } else {
                Explorer.logger.log(Level.FINEST, "ONE OF THE NAMES WAS NULL! : ",
                        couple);
            }
        }
    }

    public Result[] compare(String[] examples, String probe, boolean logging)
            throws TopsStringFormatException {
        // String[] results = new String[examples.length];
        ArrayList<Result> results = new ArrayList<Result>(examples.length);
        Pattern[] pair = new Pattern[2];
        pair[0] = new Pattern(probe);
        System.out.println("for probe : \t" + probe);
        this.m = new Matcher();
        this.m.setLogging(logging);
        for (int e = 0; e < examples.length; e++) { // for each example, compare
                                                    // to pattern
            pair[1] = new Pattern(examples[e]);
            this.m.setDiagrams(pair);

            this.currentSheet = this.makeSheet();
            while (this.currentSheet != null) {
                this.extendAllPossibleWays();
                this.currentSheet = this.makeSheet();
            }
            Pattern result = this.getPattern();
            Explorer.logger.log(Level.INFO, "Finished extending : " + result);
//            String matchString = "[]";
            if (this.m.runsSuccessfully(result)) {
                // System.out.println("common pattern : " + result + "\tinstance
                // : " + examples[e] + "\tmatch : " + result.getMatchString());
//                String tmp = result.getMatchString();
//                if (!tmp.equals(" ]"))
//                    matchString = tmp;
            } else {
                Explorer.logger.log(Level.WARNING,
                        "SEVERE PROBLEM : FINAL PATTERN " + result
                                + " does not match " + examples[e]);
            } // !!
            String[][] ins = this.m.generateInserts(result);
            Dynamo dyn = new Dynamo();
            String[] fin = dyn.doInserts(ins);
            String spliced = result.splice(fin);
            Pattern chi = this.doChirals(new Pattern(spliced));
            chi.sortEdges();
            chi.rename("pattern:");

            // float c2 = doCompression(pair, chi);
            float c2 = this.doDrgCompression(pair, chi);

            Result r = new Result(
            		c2, pair[1].getName(), chi.toString(), pair[1].getClassification());

            results.add(r);
            this.clear(); // reset pattern edge stack etc
        }
        Collections.sort(results);
        return (Result[]) results.toArray(new Result[0]);
    }

    public int getMinThings(Pattern[] instances) {
        int nextSize;
        int currentMin = instances[0].vsize() + instances[0].esize();
        for (int i = 1; i < instances.length; i++) {
            nextSize = instances[i].vsize() + instances[i].esize();
            currentMin = Math.min(currentMin, nextSize);
        }
        return currentMin;
    }

    public float doCompression(String[] instances, Pattern p)
            throws TopsStringFormatException {
        Pattern[] pinstances = new Pattern[instances.length];
        for (int i = 0; i < instances.length; i++) {
            pinstances[i] = new Pattern(instances[i]);
        }
        return this.doCompression(pinstances, p);
    }

    public float doCompression(Pattern[] instances, Pattern p) {
        // compression calculations
        int elements = p.vsize() + p.esize();
        int total_things = 0;
        for (int i = 0; i < instances.length; i++) {
            total_things += instances[i].vsize() + instances[i].esize();
        }
        int min_things = this.getMinThings(instances);
        int Craw = total_things - (elements * (instances.length - 1));

        int tmp1 = total_things - Craw;
        int tmp2 = total_things - min_things;

        float Cnorm = 1 - ((float) tmp1 / (float) tmp2);
        return Cnorm;
    }

    public float doDrgCompression(String[] instances, Pattern p)
            throws TopsStringFormatException {
        Pattern[] pinstances = new Pattern[instances.length];
        for (int i = 0; i < instances.length; i++) {
            pinstances[i] = new Pattern(instances[i]);
        }
        return this.doDrgCompression(pinstances, p);
    }

    public void doVerbosePairCompression(String[] stringPair, Pattern p,
            String name0, String name1) throws TopsStringFormatException {
        Pattern[] pair = new Pattern[2];
        pair[0] = new Pattern(stringPair[0]);
        pair[1] = new Pattern(stringPair[1]);
        Object[] data = new Object[20];
        int index = 1; // index 0 will be the compression!

        int pattern_SSE = p.vsize();
        int pattern_HBond = p.getNumberOfHBonds();
        int pattern_HPP = p.getNumberOfHPP();
        int pattern_Chiral = p.getNumberOfChirals();

        data[index++] = new Integer(pattern_SSE);
        data[index++] = new Integer(pattern_HBond);
        data[index++] = new Integer(pattern_HPP);
        data[index++] = new Integer(pattern_Chiral);

        int pair0_SSE = pair[0].vsize();
        int pair0_HBond = pair[0].getNumberOfHBonds();
        int pair0_HPP = pair[0].getNumberOfHPP();
        int pair0_Chiral = pair[0].getNumberOfChirals();

        data[index++] = new Integer(pair0_SSE);
        data[index++] = new Integer(pair0_HBond);
        data[index++] = new Integer(pair0_HPP);
        data[index++] = new Integer(pair0_Chiral);

        int pair1_SSE = pair[1].vsize();
        int pair1_HBond = pair[1].getNumberOfHBonds();
        int pair1_HPP = pair[1].getNumberOfHPP();
        int pair1_Chiral = pair[1].getNumberOfChirals();

        data[index++] = new Integer(pair1_SSE);
        data[index++] = new Integer(pair1_HBond);
        data[index++] = new Integer(pair1_HPP);
        data[index++] = new Integer(pair1_Chiral);

        int instanceTotal_SSE = pair0_SSE + pair1_SSE;
        int instanceTotal_HBond = pair0_HBond + pair1_HBond;
        int instanceTotal_HPP = pair0_HPP + pair1_HPP;
        int instanceTotal_Chiral = pair0_Chiral + pair1_Chiral;

        int minimum_SSE = Math.min(pair0_SSE, pair1_SSE);
        int minimum_HBond = Math.min(pair0_HBond, pair1_HBond);
        int minimum_HPP = Math.min(pair0_HPP, pair1_HPP);
        int minimum_Chiral = Math.min(pair0_Chiral, pair1_Chiral);

        int cRaw_SSE = instanceTotal_SSE - pattern_SSE;
        int cRaw_HBond = instanceTotal_HBond - pattern_HBond;
        int cRaw_HPP = instanceTotal_HPP - pattern_HPP;
        int cRaw_Chiral = instanceTotal_Chiral - pattern_Chiral;

        float cNorm_SSE = this.normalizedScore(instanceTotal_SSE, cRaw_SSE,
                minimum_SSE);
        float cNorm_HBond = this.normalizedScore(instanceTotal_HBond,
                cRaw_HBond, minimum_HBond);
        float cNorm_HPP = this.normalizedScore(instanceTotal_HPP, cRaw_HPP,
                minimum_HPP);
        float cNorm_Chiral = this.normalizedScore(instanceTotal_Chiral,
                cRaw_Chiral, minimum_Chiral);

        data[index++] = new Float(cNorm_SSE);
        data[index++] = new Float(cNorm_HBond);
        data[index++] = new Float(cNorm_HPP);
        data[index++] = new Float(cNorm_Chiral);

        int divisor = 4;
        float cTotal = 0;

        if (cNorm_SSE == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_SSE;
        }

        if (cNorm_HBond == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_HBond;
        }

        if (cNorm_HPP == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_HPP;
        }

        if (cNorm_Chiral == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_Chiral;
        }

        // empty pattern!
        if (divisor == 0) {
            data[0] = new Integer(0);
        } else {
            data[0] = new Float(cTotal / divisor);
        }
        data[index++] = name0;
        data[index++] = name1;
        data[index++] = p.toString();
        Explorer.logger.log(Level.SEVERE, "", data);
    }

    public float doDrgCompression(Pattern[] instances, Pattern p) {
        int pattern_SSE = p.vsize();
        int pattern_HBond = p.getNumberOfHBonds();
        int pattern_HPP = p.getNumberOfHPP();
        int pattern_Chiral = p.getNumberOfChirals();

        int instanceTotal_SSE = 0;
        int instanceTotal_HBond = 0;
        int instanceTotal_HPP = 0;
        int instanceTotal_Chiral = 0;

        int minimum_SSE = Integer.MAX_VALUE;
        int minimum_HBond = Integer.MAX_VALUE;
        int minimum_HPP = Integer.MAX_VALUE;
        int minimum_Chiral = Integer.MAX_VALUE;

        for (int i = 0; i < instances.length; i++) {
            int instance_SSE = instances[i].vsize();
            int instance_HBond = instances[i].getNumberOfHBonds();
            int instance_HPP = instances[i].getNumberOfHPP();
            int instance_Chiral = instances[i].getNumberOfChirals();

            instanceTotal_SSE += instance_SSE;
            instanceTotal_HBond += instance_HBond;
            instanceTotal_HPP += instance_HPP;
            instanceTotal_Chiral += instance_Chiral;

            minimum_SSE = Math.min(minimum_SSE, instance_SSE);
            minimum_HBond = Math.min(minimum_HBond, instance_HBond);
            minimum_HPP = Math.min(minimum_HPP, instance_HPP);
            minimum_Chiral = Math.min(minimum_Chiral, instance_Chiral);
        }

        int cRaw_SSE = this.rawScore(instanceTotal_SSE, pattern_SSE,
                instances.length);
        int cRaw_HBond = this.rawScore(instanceTotal_HBond, pattern_HBond,
                instances.length);
        int cRaw_HPP = this.rawScore(instanceTotal_HPP, pattern_HPP,
                instances.length);
        int cRaw_Chiral = this.rawScore(instanceTotal_Chiral, pattern_Chiral,
                instances.length);

        float cNorm_SSE = this.normalizedScore(instanceTotal_SSE, cRaw_SSE,
                minimum_SSE);
        float cNorm_HBond = this.normalizedScore(instanceTotal_HBond,
                cRaw_HBond, minimum_HBond);
        float cNorm_HPP = this.normalizedScore(instanceTotal_HPP, cRaw_HPP,
                minimum_HPP);
        float cNorm_Chiral = this.normalizedScore(instanceTotal_Chiral,
                cRaw_Chiral, minimum_Chiral);

        int divisor = 4;
        float cTotal = 0;

        if (cNorm_SSE == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_SSE;
        }

        if (cNorm_HBond == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_HBond;
        }

        if (cNorm_HPP == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_HPP;
        }

        if (cNorm_Chiral == -1) {
            divisor -= 1;
        } else {
            cTotal += cNorm_Chiral;
        }

        // empty pattern!
        if (divisor == 0) {
            return 0;
        } else {
            return cTotal / divisor;
        }
    }

    public int rawScore(int instanceTotal, int patternTotal,
            int numberOfInstances) {
        return instanceTotal - (patternTotal * (numberOfInstances - 1));
    }

    public float normalizedScore(int instanceTotal, int rawScore, int minimum) {
        float score = 1 - ((float) (instanceTotal - rawScore) / (float) (instanceTotal - minimum));
        if ((new Float(score)).isNaN()) {
            return -1;
        } else {
            return score;
        }
    }

    public String reproduce(String example) throws TopsStringFormatException {
        String[] ex = { example };
//        int vertexcount = (example.substring(example.indexOf(' ') + 1, example.lastIndexOf(' '))).length() - 2;
        this.m = new Matcher(ex);

        this.currentSheet = this.makeSheet();
        while (this.currentSheet != null) {
            this.extendAllPossibleWays();
            this.currentSheet = this.makeSheet();
        }
        Pattern result = this.getPattern();
        result.rename(example.substring(0, example.indexOf(" ")));
        System.out.println("hbonds...." + result);
        String[][] ins = this.m.generateInserts(result);


        Dynamo dyn = new Dynamo();
        String[] fin = dyn.doInserts(ins);
        String spliced = result.splice(fin);
        System.out.println("spliced..." + spliced);
        Pattern chi = this.doChirals(new Pattern(spliced));
        chi.sortEdges();
        System.out.println("chirals..." + chi);
        return new String();
    }

    public String reproduce(String[] examples) throws TopsStringFormatException {
        this.m = new Matcher(examples);

        this.currentSheet = this.makeSheet();
        while (this.currentSheet != null) {
            this.extendAllPossibleWays();
            this.currentSheet = this.makeSheet();
        }
        Pattern result = this.getPattern();
        System.out.println("result : " + result);
        String[][] ins = this.m.generateInserts(result);
        Dynamo dyn = new Dynamo();
        String[] fin = dyn.doInserts(ins);
        String spliced = result.splice(fin);
        System.out.println("spliced : " + spliced);
        Pattern chi = this.doChirals(new Pattern(spliced));
        chi.sortEdges();
        return chi.toString();
    }

    /*
     * ###########################################################
     * ###########################################################
     * ###########################################################
     */
    public Pattern doChirals(Pattern p) throws TopsStringFormatException {
        int end = p.getCTermPosition();
        for (int i = 1; i < end - 2; i++) {
            for (int j = i + 2; j < end; j++) {
                if ((j - i) < 7) {
                    char vleft = this.vtype(p, i);
                    if (vleft == this.vtype(p, j)) {
                        for (int t = 0; t < Explorer.chiral_types.length; t++) {
                            char ltyp = Explorer.chiral_types[t].getLType();
//                            char tmp = Explorer.chiral_types[t].getType();
                            if (vleft == ltyp) {
                                if (this.tryChiral(p, i, j, Explorer.chiral_types[t]))
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return p;
    }

    public char vtype(Pattern p, int x) {
        return p.getVertex(x).getType();
    }

    public boolean tryChiral(Pattern p, int i, int j, Edge chiral)
            throws TopsStringFormatException {
        if (p.addChiral(i, j, chiral.getType())) {
            if (!this.m.runChiral(new Pattern(p.toString()))) {
                p.removeLastChiral();
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public Sheet makeSheet() {
        // int lhe = (currentSheet == null)? 1 : currentSheet.getLEndpoint() +
        // 1; //add one because inserting BEFORE this vertex
        int lhe = 1; // always try and add a new sheet anywhere
        int rhe = (this.currentSheet == null) ? 2 : this.getCTermPosition(); // absolute
                                                                    // right
                                                                    // hand end
                                                                    // -
                                                                    // vertices.size()
        Sheet result = null;

        for (int i = lhe; i <= rhe; i++) {
            for (int j = i + 1; j <= rhe + 1; j++) {
                for (int t = 0; t < Explorer.types.length; t++) {
                    Edge ed = Explorer.types[t];
                    char l = (ed.left).getType();
                    char r = (ed.right).getType();
                    char e = ed.getType();

                    result = this.addNewSheet(i, j, l, r, e);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    public int getCTermPosition() {
        return (this.vertices.size() - 1);
    }

    public void extendAllPossibleWays() {
        Explorer.logger.log(Level.INFO, "current pattern " + this.getPattern());

        if (this.extend(Explorer.TORIGHT)) {
            this.extendAllPossibleWays();
        }
        if (this.extend(Explorer.FROMLEFT)) {
            this.extendAllPossibleWays();
        }
        if (this.extend(Explorer.CYCLE)) {
            this.extendAllPossibleWays();
        }

    }

    public boolean extend(int dir) {
        int lhe = this.currentSheet.getLEndpoint();
        // int rhe = currentSheet.getREndpoint();
        int rhe = this.getCTermPosition(); // absolute right hand end -
                                        // vertices.size()

        switch (dir) {

            case CYCLE:
                for (int i = lhe; i < rhe; i++) {
                    for (int j = (lhe + 1); j <= rhe; j++) {
                        if (i < j) {
                            for (int t = 0; t < Explorer.types.length; t++) {
                                Edge ed = Explorer.types[t];
                                char l = (ed.left).getType();
                                char r = (ed.right).getType();
                                char e = ed.getType();
                                if (this.currentSheet.canCyclise(i, j, l, r)) {
                                    this.currentSheet.extend(i, j, e);
                                    if (this.tryMatchingNewCycle(this.getPattern()))
                                        return true;
                                }
                            }
                        }
                    }
                }
                break;

            case FROMLEFT:
                for (int i = lhe; i <= rhe; i++) {
                    for (int j = i + 1; j <= rhe; j++) {
                        if (i < j) {
                            for (int t = 0; t < Explorer.types.length; t++) {
                                Edge ed = Explorer.types[t];
                                char l = (ed.left).getType();
                                char r = (ed.right).getType();
                                char e = ed.getType();
                                if (this.currentSheet.canExtend(i, l)) {
                                    this.currentSheet.insertBefore(j, r);
                                    this.currentSheet.extend(i, j, e);
                                    if (this.tryMatchingNewExtension(this.getPattern()))
                                        return true;
                                }
                            }
                        }
                    }
                }
                break;

            case TORIGHT:
                for (int i = 1; i <= rhe; i++) { // LESS THAN rhe. never want
                                                    // to insert before C!
                    // for (int j = lhe + 2; j <= rhe; j++) {
                    for (int j = lhe; j <= rhe; j++) {
                        if (i < j) {
                            for (int t = 0; t < Explorer.types.length; t++) {
                                Edge ed = Explorer.types[t];
                                char l = (ed.left).getType();
                                char r = (ed.right).getType();
                                char e = ed.getType();
                                if (this.currentSheet.canExtend(j - 1, r)) {
                                    this.currentSheet.insertBefore(i, l);
                                    this.currentSheet.extend(i, j, e);
                                    if (this.tryMatchingNewExtension(this.getPattern()))
                                        return true;
                                }
                            }
                        }
                    }
                }
                break;
        }

        return false;
    }

    public boolean tryMatchingNewCycle(Pattern currentPattern) {
        Explorer.logger.log(Level.INFO, "matching new cycle");
        if (this.m.runsSuccessfully(currentPattern)) {
            return true;
        } else {
            this.currentSheet.undoLastCycle();
            return false;
        }
    }

    public boolean tryMatchingNewExtension(Pattern currentPattern) {
        Explorer.logger.log(Level.INFO, "matching new extension");
        if (this.m.runsSuccessfully(currentPattern)) {
            return true;
        } else {
            this.currentSheet.undoLastMove();
            return false;
        }
    }

    public boolean newSheetMatches(Pattern currentPattern) {
        Explorer.logger.log(Level.INFO, "matching new sheet");
        if (this.m.runsSuccessfully(currentPattern)) {
            return true;
        } else {
            ((Sheet) this.sheets.pop()).remove(); // remove the underlying
                                                // vertices
            return false;
        }
    }

    public Sheet addNewSheet(int lpos, int rpos, char lvtype, char rvtype,
            char etype) {
        Vertex l = new Vertex(lvtype, lpos);
        Vertex r = new Vertex(rvtype, rpos);
        Sheet s = new Sheet(l, r, etype, this.vertices);
        this.sheets.add(s);

        if (this.newSheetMatches(this.getPattern()))
            return s;
        else
            return null;
    }

    public Pattern getPattern() {
        Pattern pToReturn = new Pattern();

        pToReturn.addVertices(this.vertices);
        for (Sheet s : this.sheets) {
            pToReturn.addEdges(s.getEdges());
        }
        pToReturn.sortEdges();
        return pToReturn;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out
                    .println("-f <file> OR -s <string> OR -d <string> OR -m <file> OR -c <file> <string>");
            System.exit(0);
        }
        String mode = new String(); // this is the second flag, depending on
                                    // whether file is specified or not
        ArrayList<String> examples = new ArrayList<String>(); // shared between the argument
                                                // modes

        boolean matcherLogging = false;
        boolean explorerDebugLogging = false;

        if (args[0].equals("-f")) { // READ FROM A FILE
            String line;
            String filename = args[1];
            try {
                BufferedReader buff = new BufferedReader(new FileReader(filename));
                while ((line = buff.readLine()) != null) {
                    examples.add(line);
                }
                buff.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
            mode = args[2];
        } else if (args[0].equals("-s")) { // USE A STRING
            matcherLogging = true;
            explorerDebugLogging = true;
            examples.add(args[1]);
            mode = args[2];
        } else if (args[0].equals("--")) { // -- TODO -- INTENDED TO PIPE IN
                                            // DATA
            mode = args[1];
        }

        Explorer ex = new Explorer();
        Logger exLogger = Logger.getLogger("tops.engine.helix.Explorer");
        if (explorerDebugLogging) {
            exLogger.setLevel(Level.ALL);
            exLogger.addHandler(new StreamHandler(System.err,
                    new PlainFormatter()));
            exLogger.setUseParentHandlers(false);
        } else {
            exLogger.addHandler(new StreamHandler(System.out,
                    new TabFormatter()));
            exLogger.setUseParentHandlers(false);
        }

        try {
            if (mode.equals("-r")) { // REPRODUCE A LIST
                for (int i = 0; i < examples.size(); i++) {
                    String example = (String) examples.get(i);
                    System.out.println("input....." + example);
                    ex.reproduce(example);
                    System.out.println();
                }
            } else if (mode.equals("-m")) { // MATCH A FILE OF PATTERNS TO A
                                            // TARGET (ARGS[2])
                if (args.length < 2)
                    System.out.println("please supply filename and probe");
                String target = args[3];
                Matcher ma = new Matcher();
                String[] patterns = (String[]) examples.toArray(new String[0]);
                String[] results = ma.run(patterns, target);
                for (int k = 0; k < results.length; k++) {
                    System.out.println(results[k]);
                }
            } else if (mode.equals("-g")) { // FIND A PATTERN FOR A GROUP

                String result = ex.findPattern((String[]) examples
                        .toArray(new String[0]), matcherLogging);
                System.out.println(result);
            } else if (mode.equals("-c")) { // COMPARE ARG[2] TO THE CONTENTS OF
                                            // ARG[1]
                String probe = args[3];
                String[] exampleStrings = (String[]) examples
                        .toArray(new String[0]);
                Result[] results = ex.compare(exampleStrings, probe,
                        matcherLogging);
                for (int i = 0; i < results.length; i++) {
                    System.out.println(results[i]);
                }
            } else if (mode.equals("-a")) { // ALL AGAINST ALL OF A FILE
                ArrayList<String[]> pairList = new ArrayList<String[]>();
                HashMap<String, String> instMap = new HashMap<String, String>(3000);

                // use a map for the strings (more efficient lookup?)
                ArrayList<String> tmpList = new ArrayList<String>();
                TParser tp = new TParser();

                Iterator<String> itr = examples.iterator();
                while (itr.hasNext()) {
                    String nextLine = (String) itr.next();
                    tp.load(nextLine);
                    String name = tp.getName();
                    tmpList.add(name);
                    instMap.put(name, nextLine); // KEY is the head/domId
                }
                boolean verbose = false;
                // handle the indices to the loops
                int lowerindex, upperindex;
                if (args.length > 3) {
                    lowerindex = Integer.parseInt(args[3]);
                    upperindex = Integer.parseInt(args[4]);
                    if (args[5].equals("v")) {
                        verbose = true;
                    }
                    // special case where this instance will be doing all the
                    // comparisons
                    if (lowerindex == -1)
                        lowerindex = 0;
                    if (upperindex == -1)
                        upperindex = tmpList.size();
                } else {
                    lowerindex = 0;
                    upperindex = tmpList.size();
                }

                for (int i = lowerindex; i < upperindex; i++) {
                    for (int j = i + 1; j < tmpList.size(); j++) {
                        if (i != j) {
                            String[] miniContainer = { (String) tmpList.get(i),
                                    (String) tmpList.get(j) };
                            pairList.add(miniContainer);
                        }
                    }
                }
                System.err.println("NOTE : " + pairList.size()
                        + " comparisons!");
                ex.allVsAll(pairList, instMap, verbose);
            } else if (mode.equals("-p")) { // COMPARE FIRST IN A LIST TO THE
                                            // REST OF THE LIST, PAIRWISE
                String first = (String) examples.get(0);
                ex.compare((String[]) examples.toArray(new String[0]), first,
                        matcherLogging);
            } else if (mode.equals("-v")) {
                ArrayList<String[]> pairList = new ArrayList<String[]>();
                HashMap<String, String> instMap = new HashMap<String, String>(3000);
                String pairFilename = args[3];
                String verboseFlag = args[4];
                String line;

                try {
                    BufferedReader buff = new BufferedReader(new FileReader(pairFilename));
                    while ((line = buff.readLine()) != null) {
                        int tab = line.indexOf("\t");
                        String first = line.substring(0, tab);
                        String second = line.substring(tab + 1, line.indexOf(
                                "\t", tab + 1));
                        // String second = line.substring(tab + 1);
                        String[] miniContainer = { first, second };
                        pairList.add(miniContainer);
                    }
                    buff.close();
                } catch (IOException ioe) {
                    System.out.println(ioe);
                }

                // use a map for the strings (more efficient lookup?)
                TParser tp = new TParser();
                Iterator<String> itr = examples.iterator();
                while (itr.hasNext()) {
                    String nextLine = (String) itr.next();
                    tp.load(nextLine);
                    String name = tp.getName();
                    instMap.put(name, nextLine); // KEY is the head/domId
                }

                if (verboseFlag.equals("-v")) {
                    ex.allVsAll(pairList, instMap, true);
                } else {
                    ex.allVsAll(pairList, instMap, false);
                }
            } else {
                System.out.println("-f <file> or -s <string>");
            }
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe.getMessage());
            tsfe.printStackTrace();
        }
    }
}
