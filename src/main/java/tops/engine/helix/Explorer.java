package tops.engine.helix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.engine.Edge;
import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.Vertex;

public class Explorer {

    private static final String PATTERN_NAME = "pattern:";

    private List<Vertex> vertices;

    private Deque<Sheet> sheets;

    private Sheet currentSheet;

    private Matcher m;

    private static final int FROMLEFT = 1;

    private static final int TORIGHT = 2;

    private static final int CYCLE = 3;

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

    private static Logger logger = Logger.getLogger(Explorer.class.getName());

    public Explorer() {
        this.sheets = new ArrayDeque<>();
        this.vertices = new ArrayList<>();
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
        chi.rename(PATTERN_NAME);
        return this.doCompression(instances, chi) + "\t" + chi.toString();
    }

    public void clear() {
        this.sheets.clear();
        this.vertices.clear();
        this.vertices.add(new Vertex('N', 0));
        this.vertices.add(new Vertex('C', 1));
        this.currentSheet = null;
    }

    public void allVsAll(List<String[]> pairList, Map<String, String> instMap, boolean verbose)
            throws TopsStringFormatException {
        // the domain names we are comparing are (pairKey, pairValue).
        String pair1;
        String pair2;
        String[][] pairs = pairList.toArray(new String[0][]);
        String[] couple = new String[2];

        for (int i = 0; i < pairs.length; ++i) {
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple[0] = instMap.get(pair1);
            couple[1] = instMap.get(pair2);

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
                    commonPattern.rename(PATTERN_NAME);
                    if (verbose) {
                        this.doVerbosePairCompression(couple, commonPattern,
                                pair1, pair2);
                    } else {
                        logger.log(Level.INFO, "{0}\t{1}\t{2}\t{3}", new Object[] {
                                this.doDrgCompression(couple, commonPattern),
                                pair1,
                                pair2,
                                commonPattern});
                    }
                    this.clear();
                } catch (Exception e) {
                    logger.warning(pair1 + " , " + pair2 + ", " + e);
                }
            } else {
                Explorer.logger.log(Level.FINEST, "ONE OF THE NAMES WAS NULL! : ",
                        couple);
            }
        }
    }

    public Result[] compare(String[] examples, String probe, boolean logging)
            throws TopsStringFormatException {
        List<Result> results = new ArrayList<>(examples.length);
        Pattern[] pair = new Pattern[2];
        pair[0] = new Pattern(probe);
        logger.log(Level.INFO, "for probe : \t{0}", probe);
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
            logger.log(Level.INFO, "Finished extending : {0}", result);
            if (this.m.runsSuccessfully(result)) {
                logger.info("success");
            } else {
                logger.log(Level.WARNING,
                        "SEVERE PROBLEM : FINAL PATTERN {0} does not match  {1}", new Object[] {result, examples[e] });
            } // !!
            String[][] ins = this.m.generateInserts(result);
            Dynamo dyn = new Dynamo();
            String[] fin = dyn.doInserts(ins);
            String spliced = result.splice(fin);
            Pattern chi = this.doChirals(new Pattern(spliced));
            chi.sortEdges();
            chi.rename(PATTERN_NAME);

            float c2 = this.doDrgCompression(pair, chi);

            Result r = new Result(
            		c2, pair[1].getName(), chi.toString(), pair[1].getClassification());

            results.add(r);
            this.clear(); // reset pattern edge stack etc
        }
        Collections.sort(results);
        return results.toArray(new Result[0]);
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
        int totalThings = 0;
        for (int i = 0; i < instances.length; i++) {
            totalThings += instances[i].vsize() + instances[i].esize();
        }
        int minThings = this.getMinThings(instances);
        int cRaw = totalThings - (elements * (instances.length - 1));

        int tmp1 = totalThings - cRaw;
        int tmp2 = totalThings - minThings;

        return 1 - ((float) tmp1 / (float) tmp2);
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

        int patternSSE = p.vsize();
        int patternHBond = p.getNumberOfHBonds();
        int patternHPP = p.getNumberOfHPP();
        int patternChiral = p.getNumberOfChirals();

        data[index++] = patternSSE;
        data[index++] = patternHBond;
        data[index++] = patternHPP;
        data[index++] = patternChiral;

        int pair0SSE = pair[0].vsize();
        int pair0HBond = pair[0].getNumberOfHBonds();
        int pair0HPP = pair[0].getNumberOfHPP();
        int pair0Chiral = pair[0].getNumberOfChirals();

        data[index++] = pair0SSE;
        data[index++] = pair0HBond;
        data[index++] = pair0HPP;
        data[index++] = pair0Chiral;

        int pair1SSE = pair[1].vsize();
        int pair1HBond = pair[1].getNumberOfHBonds();
        int pair1HPP = pair[1].getNumberOfHPP();
        int pair1Chiral = pair[1].getNumberOfChirals();

        data[index++] = pair1SSE;
        data[index++] = pair1HBond;
        data[index++] = pair1HPP;
        data[index++] = pair1Chiral;

        int instanceTotalSSE = pair0SSE + pair1SSE;
        int instanceTotalHBond = pair0HBond + pair1HBond;
        int instanceTotalHPP = pair0HPP + pair1HPP;
        int instanceTotalChiral = pair0Chiral + pair1Chiral;

        int minimumSSE = Math.min(pair0SSE, pair1SSE);
        int minimumHBond = Math.min(pair0HBond, pair1HBond);
        int minimumHPP = Math.min(pair0HPP, pair1HPP);
        int minimumChiral = Math.min(pair0Chiral, pair1Chiral);

        int cRawSSE = instanceTotalSSE - patternSSE;
        int cRawHBond = instanceTotalHBond - patternHBond;
        int cRawHPP = instanceTotalHPP - patternHPP;
        int cRawChiral = instanceTotalChiral - patternChiral;

        float cNormSSE = this.normalizedScore(instanceTotalSSE, cRawSSE, minimumSSE);
        float cNormHBond = this.normalizedScore(instanceTotalHBond, cRawHBond, minimumHBond);
        float cNormHPP = this.normalizedScore(instanceTotalHPP, cRawHPP, minimumHPP);
        float cNormChiral = this.normalizedScore(instanceTotalChiral, cRawChiral, minimumChiral);

        data[index++] = cNormSSE;
        data[index++] = cNormHBond;
        data[index++] = cNormHPP;
        data[index++] = cNormChiral;

        int divisor = 4;
        float cTotal = 0;

        if (cNormSSE == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormSSE;
        }

        if (cNormHBond == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormHBond;
        }

        if (cNormHPP == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormHPP;
        }

        if (cNormChiral == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormChiral;
        }

        // empty pattern!
        if (divisor == 0) {
            data[0] = 0;
        } else {
            data[0] = cTotal / divisor;
        }
        data[index++] = name0;
        data[index++] = name1;
        data[index] = p.toString();
        logger.log(Level.SEVERE, "", data);
    }

    public float doDrgCompression(Pattern[] instances, Pattern p) {
        int patternSSE = p.vsize();
        int patternHBond = p.getNumberOfHBonds();
        int patternHPP = p.getNumberOfHPP();
        int patternChiral = p.getNumberOfChirals();

        int instanceTotalSSE = 0;
        int instanceTotalHBond = 0;
        int instanceTotalHPP = 0;
        int instanceTotalChiral = 0;

        int minimumSSE = Integer.MAX_VALUE;
        int minimumHBond = Integer.MAX_VALUE;
        int minimumHPP = Integer.MAX_VALUE;
        int minimumChiral = Integer.MAX_VALUE;

        for (int i = 0; i < instances.length; i++) {
            int instanceSSE = instances[i].vsize();
            int instanceHBond = instances[i].getNumberOfHBonds();
            int instanceHPP = instances[i].getNumberOfHPP();
            int instanceChiral = instances[i].getNumberOfChirals();

            instanceTotalSSE += instanceSSE;
            instanceTotalHBond += instanceHBond;
            instanceTotalHPP += instanceHPP;
            instanceTotalChiral += instanceChiral;

            minimumSSE = Math.min(minimumSSE, instanceSSE);
            minimumHBond = Math.min(minimumHBond, instanceHBond);
            minimumHPP = Math.min(minimumHPP, instanceHPP);
            minimumChiral = Math.min(minimumChiral, instanceChiral);
        }

        int cRawSSE = rawScore(instanceTotalSSE, patternSSE, instances.length);
        int cRawHBond = rawScore(instanceTotalHBond, patternHBond, instances.length);
        int cRawHPP = rawScore(instanceTotalHPP, patternHPP, instances.length);
        int cRawChiral = this.rawScore(instanceTotalChiral, patternChiral, instances.length);

        float cNormSSE = this.normalizedScore(instanceTotalSSE, cRawSSE, minimumSSE);
        float cNormHBond = this.normalizedScore(instanceTotalHBond, cRawHBond, minimumHBond);
        float cNormHPP = this.normalizedScore(instanceTotalHPP, cRawHPP, minimumHPP);
        float cNormChiral = this.normalizedScore(instanceTotalChiral, cRawChiral, minimumChiral);

        int divisor = 4;
        float cTotal = 0;

        if (cNormSSE == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormSSE;
        }

        if (cNormHBond == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormHBond;
        }

        if (cNormHPP == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormHPP;
        }

        if (cNormChiral == -1) {
            divisor -= 1;
        } else {
            cTotal += cNormChiral;
        }

        // empty pattern!
        if (divisor == 0) {
            return 0;
        } else {
            return cTotal / divisor;
        }
    }

    public int rawScore(int instanceTotal, int patternTotal, int numberOfInstances) {
        return instanceTotal - (patternTotal * (numberOfInstances - 1));
    }

    public float normalizedScore(int instanceTotal, int rawScore, int minimum) {
        float score = 1 - ((float) (instanceTotal - rawScore) / (float) (instanceTotal - minimum));
        if ((Float.valueOf(score)).isNaN()) {
            return -1;
        } else {
            return score;
        }
    }

    public String reproduce(String example) throws TopsStringFormatException {
        String[] ex = { example };
        this.m = new Matcher(ex);

        this.currentSheet = this.makeSheet();
        while (this.currentSheet != null) {
            this.extendAllPossibleWays();
            this.currentSheet = this.makeSheet();
        }
        Pattern result = this.getPattern();
        result.rename(example.substring(0, example.indexOf(' ')));
        logger.log(Level.INFO, "hbonds.... {0}", result);
        String[][] ins = this.m.generateInserts(result);

        Dynamo dyn = new Dynamo();
        String[] fin = dyn.doInserts(ins);
        String spliced = result.splice(fin);
        logger.log(Level.INFO, "spliced... {0}", spliced);
        Pattern chi = this.doChirals(new Pattern(spliced));
        chi.sortEdges();
        logger.log(Level.INFO, "chirals... {0}", chi);
        return "";
    }

    public String reproduce(String[] examples) throws TopsStringFormatException {
        this.m = new Matcher(examples);

        this.currentSheet = this.makeSheet();
        while (this.currentSheet != null) {
            this.extendAllPossibleWays();
            this.currentSheet = this.makeSheet();
        }
        Pattern result = this.getPattern();
        logger.log(Level.INFO, "result : {0}", result);
        String[][] ins = this.m.generateInserts(result);
        Dynamo dyn = new Dynamo();
        String[] fin = dyn.doInserts(ins);
        String spliced = result.splice(fin);
        logger.log(Level.INFO, "spliced : {0}", spliced);
        Pattern chi = this.doChirals(new Pattern(spliced));
        chi.sortEdges();
        return chi.toString();
    }

    public Pattern doChirals(Pattern p) throws TopsStringFormatException {
        int end = p.getCTermPosition();
        for (int i = 1; i < end - 2; i++) {
            for (int j = i + 2; j < end; j++) {
                if ((j - i) < 7) {
                    char vleft = this.vtype(p, i);
                    if (vleft == this.vtype(p, j)) {
                        for (int t = 0; t < Explorer.chiral_types.length; t++) {
                            char ltyp = Explorer.chiral_types[t].getLType();
                            Edge tType = Explorer.chiral_types[t];
                            if (vleft == ltyp && this.tryChiral(p, i, j, tType)) {
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
        int lhe = 1; // always try and add a new sheet anywhere
        int rhe = (this.currentSheet == null) ? 2 : this.getCTermPosition(); 
        // absolute right hand end - vertices.size()
        Sheet result = null;

        for (int i = lhe; i <= rhe; i++) {
            for (int j = i + 1; j <= rhe + 1; j++) {
                for (int t = 0; t < Explorer.types.length; t++) {
                    Edge ed = Explorer.types[t];
                    char l = ed.getLeftVertex().getType();
                    char r = ed.getRightVertex().getType();
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
        int rhe = this.getCTermPosition(); // absolute right hand end - vertices.size()

        switch (dir) {

            case CYCLE:
                for (int i = lhe; i < rhe; i++) {
                    for (int j = (lhe + 1); j <= rhe; j++) {
                        if (i < j) {
                            for (int t = 0; t < Explorer.types.length; t++) {
                                Edge ed = Explorer.types[t];
                                char l = ed.getLeftVertex().getType();
                                char r = ed.getRightVertex().getType();
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
                                char l = ed.getLeftVertex().getType();
                                char r = ed.getRightVertex().getType();
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
                    for (int j = lhe; j <= rhe; j++) {
                        if (i < j) {
                            for (int t = 0; t < Explorer.types.length; t++) {
                                Edge ed = Explorer.types[t];
                                char l = ed.getLeftVertex().getType();
                                char r = ed.getRightVertex().getType();
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
                
                default: 
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
        logger.info("matching new sheet");
        if (this.m.runsSuccessfully(currentPattern)) {
            return true;
        } else {
            this.sheets.pop().remove(); // remove the underlying vertices
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
}
