package tops.engine.juris;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import tops.engine.Edge;

class Matcher {

    private int k;

    private Pattern pattern;

    private Pattern[] diagrams;

    private static final String HEAD = "head ";

    public Matcher(String[] diag) {
        this.diagrams = new Pattern[diag.length];
        for (int patternIndex = 0; patternIndex < diag.length; patternIndex++) {
            this.diagrams[patternIndex] = new Pattern(diag[patternIndex]);
        }
    }

    public Matcher(String patt, String[] diag) {
        if (patt.charAt(0) == 'N')
            patt = HEAD + patt; // name anonymous patterns!
        this.pattern = new Pattern(patt);
        this.diagrams = new Pattern[diag.length];
        for (int patternIndex = 0; patternIndex < diag.length; patternIndex++) {
            this.diagrams[patternIndex] = new Pattern(diag[patternIndex]);
        }
    }

    public String[] run() {
        List<String> results = new ArrayList<>(this.diagrams.length);
        boolean noInserts = false; // this was making the final match results
                                    // different from expected
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < this.diagrams.length; ++i) {
            if (this.pattern.preProcess(this.diagrams[i], noInserts)
                && this.match(this.diagrams[i], noInserts)) {
                    tmp.append(this.diagrams[i].getHead());
                    tmp.append(this.pattern.getMatchString());
                    tmp.append(this.pattern.getInsertString(this.diagrams[i]));
                    results.add(tmp.toString());
            }
            this.pattern.reset();
            // NOTE: Resetting now FLIPS too!
            if (this.pattern.preProcess(this.diagrams[i], noInserts)
                && this.match(this.diagrams[i], noInserts)) {
                    tmp.append(this.diagrams[i].getHead());
                    tmp.append(this.pattern.getMatchString());
                    tmp.append(this.pattern.getInsertString(this.diagrams[i]));
                    results.add(tmp.toString());
            }
            this.pattern.reset();
        }
        return results.toArray(new String[0]);
    }

    // generate string arrays of the insert strings from an edgepattern
    String[][] generateInserts() {
        String[][] results = new String[this.diagrams.length][];
        for (int i = 0; i < this.diagrams.length; ++i) {
            if (this.pattern.preProcess(this.diagrams[i], true)
                    && this.match(this.diagrams[i], true)) {
                    results[i] = this.pattern.getInsertStringArr(this.diagrams[i]);
            }
            this.pattern.reset(); // and flip, of course
            if (this.pattern.preProcess(this.diagrams[i], true)
                && this.match(this.diagrams[i], true)) {
                    results[i] = this.pattern.getInsertStringArr(this.diagrams[i]);
            }
            this.pattern.reset();
        }
        return results;
    }

    boolean run(String patt, boolean noInserts) {
        if (patt.charAt(patt.length() - 2) == 'C')
            return true; // match the null pattern!
        if (patt.charAt(0) == 'N')
            patt = HEAD + patt; // name anonymous patterns!

        this.pattern = new Pattern(patt); // destructive (re)-assignment
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (this.pattern.preProcess(this.diagrams[i], noInserts)
                && this.match(this.diagrams[i], noInserts)) {
                    matchFound = true;
            } 
            this.pattern.reset();
            // NOTE: Resetting now FLIPS too!
            if (!matchFound) {
                if (this.pattern.preProcess(this.diagrams[i], noInserts)) {
                    if (this.match(this.diagrams[i], noInserts)) {
                        matchFound = true; // everything OK
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            this.pattern.reset();
        }
        return this.stringMatch(patt.substring(patt.indexOf('N'), patt.lastIndexOf('C') + 1));
        // NOTE : now testing vertex match for each graph as it's matched
    }

    String runUnion(String diagram, boolean noInserts) { // run a SINGLE
                                                            // example against
                                                            // the SET of
                                                            // patterns
        Pattern instance = new Pattern(diagram); // muhahaha!

        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            this.pattern = this.diagrams[i]; // DANGER WIL ROBINSON!
            if (this.pattern.preProcess(instance, noInserts)) {
                if (this.match(instance, noInserts))
                    matchFound = true;
                if (!this.stringMatch(this.pattern.toString(), diagram))
                    matchFound = false;
            }
            this.pattern.reset();
            if (!matchFound) {
                if (this.pattern.preProcess(instance, noInserts)) {
                    if (this.match(instance, noInserts))
                        matchFound = true;
                    if (!this.stringMatch(this.pattern.toString(), diagram))
                        matchFound = false;
                }
            }
            this.pattern.reset();
            if (matchFound) {
                return this.pattern.getHead(); // the name of the first pattern that matched
            }
        }
        return "none"; // we can only reach this point by avoiding returning
                        // true, it must be false.
    }

    boolean runUnion(String[] patt, boolean noInserts) {

        // initialise the array of patterns from the array of strings
        Pattern[] parr = new Pattern[patt.length];
        for (int j = 0; j < parr.length; j++) {
            String pstr = patt[j];
            if (pstr.charAt(0) == 'N')
                pstr = HEAD + pstr; // name anonymous patterns!
            parr[j] = new Pattern(pstr);
        }
        boolean matchFound;
        int cP;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            cP = 0;
            while (cP < parr.length && !matchFound) {
                this.pattern = parr[cP];
                String pstr = patt[cP];
                String pbody = pstr.substring(pstr.indexOf('N'), pstr.lastIndexOf('C') + 1);
                if (this.pattern.preProcess(this.diagrams[i], noInserts)
                    && this.match(this.diagrams[i], noInserts)) {
                        matchFound = true;
                }
                this.pattern.reset();
                if (!matchFound
                     && this.pattern.preProcess(this.diagrams[i], noInserts)
                     && this.match(this.diagrams[i], noInserts)) {
                            matchFound = true;
                }
                this.pattern.reset();
                if (!this.stringMatch(pbody, i))
                    matchFound = false;
            }
            if (!matchFound)
                return false; // none of the patterns matched this example
        }
        return true; // we can only reach this point by avoiding returning
                        // false, it must be true.
    }

    boolean subSeqMatch(String probe, String target) {
        int ptrP = 0;
        int ptrT = 0;
        char c;
        while (ptrP < probe.length()) {
            if (ptrT >= target.length())
                return true;
            c = target.charAt(ptrT);
            if (probe.charAt(ptrP) == c) {
                ptrP++;
            }
            ptrT++;
        }
        return false;
    }

    boolean subSeqMatch(Edge c, int last, Pattern d) {
        String probe = this.pattern.getVertexString(last, c.getLeftVertex().getPos(), false);
        String target = 
        		d.getVertexString(1, c.getCurrentMatch().getLeftVertex().getPos(), false);
        int ptrP = 0;
        int ptrT = 0;
        char ch;
        while (ptrP < probe.length()) {
            if (ptrT >= target.length())
                return false;
            ch = target.charAt(ptrT);
            if (probe.charAt(ptrP) == ch) {
                ptrP++;
            }
            ptrT++;
        }
        return true;
    }

    boolean stringMatch(String patt, String diag) {
        String p1 = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        String d = diag.substring(diag.indexOf('N') + 1, diag.lastIndexOf('C'));
        return !(this.subSeqMatch(p1, d));
    }

    boolean stringMatch(String patt) {
        if (patt.equals("NC "))
            return true; // -!-
        String p1 = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        for (int i = 0; i < this.diagrams.length; ++i) {
            String target = this.diagrams[i].getVertexString(0, 0, false);
            String reverse = this.diagrams[i].getVertexString(0, 0, true);
            if (this.subSeqMatch(p1, target) && this.subSeqMatch(p1, reverse)) {
                return false;
            }
        }
        return true;
    }

    int stringMatchUnion(String patt) {
        int numMatch = this.diagrams.length;
        if (patt.equals("NC "))
            return numMatch; // -!-
        String p1 = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        for (int i = 0; i < this.diagrams.length; ++i) {
            String target = this.diagrams[i].getVertexString(0, 0, false);
            String reverse = this.diagrams[i].getVertexString(0, 0, true);
            if ((this.subSeqMatch(p1, target) && this.subSeqMatch(p1, reverse))) {
                numMatch--;
            }
        }
        return numMatch;
    }

    boolean stringMatch(String patt, int i) { // variant for efficiency
        if (patt.equals("NC "))
            return true; // -!-
        String p1 = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        String target = this.diagrams[i].getVertexString(0, 0, false);
        String reverse = this.diagrams[i].getVertexString(0, 0, true);
        return this.subSeqMatch(p1, target) && this.subSeqMatch(p1, reverse);
    }

    boolean match(Pattern d, boolean noInserts) {
        this.k = 0;
        int last = 1;
        while (this.k < this.pattern.size()) { // Run through the pattern edges.
            Edge current = this.pattern.getEdge(this.k);
            if (!current.hasMoreMatches()) {
                return false;
            }

            if (this.findNextMatch(current) && this.verticesIncrease(current)
                    && ((noInserts) || this.subSeqMatch(current, last, d))) {
                last = current.getRightVertex().getPos() + 1; // aargh!
                ++this.k;
            } else {
                if (this.k > 0 && !this.nextSmallestEdge(current)) {
                    return false;
                }
                this.pattern.setMovedUpTo(this.k);
                if (!this.advancePositions(current)) {
                    return false;
                }
            }
        }

        int rhe;
        if (this.pattern.noEdges() || noInserts) {
            rhe = 0;
        } else {
            Edge e = this.pattern.getEdge(this.k);
            rhe = e.getCurrentMatch().getRightVertex().getPos();

            String doutCup = d.getVertexString(rhe + 1, 0, false);
            String doutCdn = d.getVertexString(rhe + 1, 0, true);
            if (this.subSeqMatch(this.pattern.getOutsertC(), doutCup)
                    && this.subSeqMatch(this.pattern.getOutsertC(), doutCdn)) {
                return false;
            } else {
                return true;
            }
        }
        return true; // noedges or no in/out-serts
    }

    // ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

    boolean findNextMatch(Edge current) {
        int c = 0;
        if (this.k == 0) {
            while (current.hasMoreMatches()) {
                Edge match = current.getCurrentMatch();
                if (current.alreadyMatched(match)) {
                    current.setEndMatches(match);
                    return true;
                } else {
                    current.moveMatchPtr();
                    c++;
                }
            }
            current.resetMatchPtr(-c); // Move it BACK!! doh! idiot : (
            return false;
        } else {
            Edge last = (this.pattern.getEdge(this.k - 1)).getCurrentMatch();
            while (current.hasMoreMatches()) {
                Edge match = current.getCurrentMatch();
                if ((current.alreadyMatched(match))
                        && (match.greaterThan(last))) {
                    current.setEndMatches(match);
                    return true;
                } else {
                    current.moveMatchPtr();
                    c++;
                }
            }
            current.resetMatchPtr(-c); // Move it BACK!! doh! idiot : (
            return false;
        }
    }

    boolean nextSmallestEdge(Edge current) {
        boolean found = false;
        Edge last = (this.pattern.getEdge(this.k - 1)).getCurrentMatch();
        while ((current.hasMoreMatches()) && !found) {
            Edge match = current.getCurrentMatch();
            if (match.greaterThan(last)) {
                found = true;
            } else {
                current.moveMatchPtr();
            }
        }
        return found;
    }

    boolean verticesIncrease(Edge current) {
        int last = 0;
        int[] m = this.pattern.getMatches();
        for (int i = 0; i < m.length; i++) {
            if (m[i] != 0) {
                if (m[i] <= last) {
                    return false;
                } else {
                    last = m[i];
                }
            }
        }
        return true;
    }

    boolean advancePositions(Edge current) {
        // 'isRight' tells you which end of backedge pvert is...
        boolean found = false;
        boolean isRight = false;
        boolean attached = false;
        int wt2 = 0;
        int vt2 = 0;
        int pvert = current.getLeftVertex().getPos();
        int tvert = (current.getCurrentMatch()).getLeftVertex().getPos();

        Deque<Integer> patternVertexStack = new ArrayDeque<>();
        Deque<Integer> targetVertexStack = new ArrayDeque<>();
        patternVertexStack.push(pvert);
        targetVertexStack.push(tvert);

        while (!patternVertexStack.isEmpty()) {
            pvert = patternVertexStack.pop();
            tvert = targetVertexStack.pop();
            // FORALL EDGES LESS THAN THE STUCK EDGE
            for (int j = this.k - 1; j >= 0; --j) {
                Edge backedge = this.pattern.getEdge(j);
                isRight = false;
                attached = false;

                // IF THEY ARE ATTACHED TO SAID STUCK EDGE
                if (pvert == backedge.getRightVertex().getPos()) {
                    attached = true;
                    isRight = true;
                } else if (pvert == backedge.getLeftVertex().getPos())
                    attached = true;

                if (attached) {
                    if (!backedge.moved) {

                        backedge.moved = true;
                        found = false;
                        while ((backedge.hasMoreMatches()) && !found) {
                            Edge e = (backedge.getCurrentMatch());
                            wt2 = e.getRightVertex().getPos();
                            vt2 = e.getLeftVertex().getPos();
                            if ((wt2 >= tvert)
                                    || ((vt2 >= tvert) && isRight)) {
                                if (isRight) {
                                    tvert = wt2;
                                    if (backedge.getLeftVertex().getMatch() != 0) {
                                        backedge.getLeftVertex().resetMatch();
                                        pvert = backedge.getLeftVertex().getPos();
                                        patternVertexStack.push(pvert);
                                        targetVertexStack.push(tvert);
                                    }
                                } else {
                                    tvert = vt2;
                                    if (backedge.getRightVertex().getMatch() != 0) {
                                        backedge.getRightVertex().resetMatch();
                                        pvert = backedge.getRightVertex().getPos();
                                        patternVertexStack.push(pvert);
                                        targetVertexStack.push(tvert);
                                    }
                                }
                                found = true;
                            } else {
                                backedge.moveMatchPtr();
                            }
                        }
                    } else {
                        return false;
                    }
                } 
            } 
        }

        // move edgeIndex to the smallest edge with an unmatched end
        for (int l = 0; l < this.pattern.size(); ++l) {
            if (((this.pattern.getEdge(l)).getLeftVertex().getMatch() == 0)
                    || ((this.pattern.getEdge(l)).getRightVertex().getMatch() == 0)) {
                this.k = l;
                break;
            }
        }
        return found;

    }
}
