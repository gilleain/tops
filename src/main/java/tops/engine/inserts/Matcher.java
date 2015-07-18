package tops.engine.inserts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.engine.Edge;
import tops.engine.MatcherI;
import tops.engine.PatternI;
import tops.engine.Result;

public class Matcher implements MatcherI {

    private int k;

    private PatternI[] diagrams;

    private Stack<Integer> PVS, TVS;

    private static Logger logger = 
    						Logger.getLogger("tops.engine.inserts.Matcher");

    public Matcher() {
        Matcher.logger.setLevel(Level.OFF);
        Matcher.logger.setUseParentHandlers(false);
    }

    public Matcher(Pattern[] diag) {
        this();
        this.diagrams = diag;
    }

    public Matcher(String[] diag) {
        this();
        this.diagrams = new Pattern[diag.length];
        for (int p = 0; p < diag.length; ++p) {
            this.diagrams[p] = new Pattern(diag[p]);
        }
    }

    public String[] run(String[] p, String d) {
        Pattern diagram = new Pattern(d);
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < p.length; i++) {
            String result = this.matchOne(new Pattern(p[i]), diagram);
            if (!result.equals("")) {
                results.add(result);
            }
        }
        return (String[]) results.toArray(new String[0]);
    }

    public boolean singleMatch(Pattern p, Pattern d) {
        if (p.preProcess(d)) {
            if (this.match(p, d)) {
                return true;
            }
        }
        p.reset();
        // NOTE: Resetting now FLIPS too!
        if (p.preProcess(d)) {
            if (this.match(p, d)) {
                return true;
            }
        }
        p.reset();
        return false;
    }

    public String matchOne(Pattern p, Pattern d) {
        String result = new String();
        boolean matchfound = false;
        if (p.preProcess(d)) {
            if (this.match(p, d)) {
                matchfound = true;
                result += p.getMatchString();
                result += "\t";
                result += p.getInsertString(d);
                result += "\t";
                result += p.getClassification();
            }
        }
        p.reset();
        // NOTE: Resetting now FLIPS too!
        if (p.preProcess(d)) {
            if (this.match(p, d)) {
                if (!matchfound) {
                    matchfound = true;
                    result += p.getMatchString();
                    result += "\t";
                    result += p.getInsertString(d);
                }
            }
        }
        p.reset();
        if (matchfound)
            return p.toString() + "\t" + result;
        else
            return new String();
    }

    @Override
	public boolean matches(PatternI pattern, PatternI instance) {
		// TODO Auto-generated method stub
		return false;
	}

	// return a Result array rather than a String array
    public Result[] runResults(Pattern p) {
        List<Result> results = new ArrayList<Result>();
        for (int i = 0; i < this.diagrams.length; ++i) {
            Result result = new Result();
            result.setID(this.diagrams[i].getName());
            boolean matchfound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (match(p, this.diagrams[i])) {
                    matchfound = true;
                    result.setData(p.getInsertString(this.diagrams[i]) + "\t"
                            + p.getVertexMatchedString());
                }
            }
            p.reset();
            // NOTE: Resetting now FLIPS too!
            if (p.preProcess(this.diagrams[i])) {
                if (match(p, this.diagrams[i])) {
                    if (!matchfound) {
                        matchfound = true;
                        result.setData(p.getInsertString(this.diagrams[i]) + "\t"
                                + p.getVertexMatchedString());
                    }
                }
            }
            p.reset();
            if (matchfound)
                results.add(result);
        }
        return (Result[]) results.toArray(new Result[0]);
    }

    public String[] run(String s) {
        return this.run(new Pattern(s));
    }

    public String[] run(Pattern p) {
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < this.diagrams.length; ++i) {
            Matcher.logger.info("matching diagram : " + i);
            String result = new String(this.diagrams[i].toString());
            boolean matchfound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    matchfound = true;
                    result += p.getMatchString();
                    result += p.getInsertString(this.diagrams[i]);
                }
            }
            p.reset();
            // NOTE: Resetting now FLIPS too!
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    if (!matchfound) {
                        matchfound = true;
                        result += p.getMatchString();
                        result += p.getInsertString(this.diagrams[i]);
                    }
                }
            }
            p.reset();
            if (matchfound)
                results.add(result);
        }
        return (String[]) results.toArray(new String[0]);
    }

    // generate string arrays of the insert strings from an edge pattern
    public String[][] generateInserts(Pattern p) {
        String[][] results = new String[this.diagrams.length][];
        for (int i = 0; i < this.diagrams.length; ++i) {
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    results[i] = p.getInsertStringArr(this.diagrams[i], false);
                }
            }
            p.reset(); // and flip, of course
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    // this OVERWRITES the previous array if the pattern matches
                    // twice!
                    results[i] = p.getInsertStringArr(this.diagrams[i], true);
                }
            }
            p.reset();
        }
        return results;
    }

    public boolean runsSuccessfully(String s) {
        return this.runsSuccessfully(new Pattern(s));
    }

    public boolean runsSuccessfully(Pattern p) {
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.match(p, this.diagrams[i])) {
                        matchFound = true;
                    } else {
                        p.reset();
                        return false;
                    }
                } else {
                    p.reset();
                    return false;
                }
                p.reset();
            }

        }
        if (this.stringMatch(p.getVertexString(0, 0, false))) {
            return true;
        } else {
            return false;
        }
        // NOTE : now testing vertex match for each graph as it's matched
    }

    public int numberMatching(Pattern p) {
        boolean matchFound;
        int numberMatching = 0;
        String patternString = p.getVertexString(0, 0, false);
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    String vstr = this.diagrams[i].getVertexString(0, 0, false);
                    if (this.subSeqMatch(patternString, vstr)) {
                        matchFound = true;
                    }
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.match(p, this.diagrams[i])) {
                        String reverse_vstr = this.diagrams[i].getVertexString(0, 0,
                                false);
                        if (this.subSeqMatch(patternString, reverse_vstr)) {
                            matchFound = true;
                        }
                    }
                }
            }

            if (matchFound) {
                numberMatching++;
            }
            p.reset();

        }
        return numberMatching;
    }

    public boolean runChiral(Pattern p) {
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.match(p, this.diagrams[i])) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.match(p, this.diagrams[i])) {
                        matchFound = true;
                    } else {
                        p.reset();
                        return false;
                    }
                } else {
                    p.reset();
                    return false;
                }
                p.reset();
            }

        }
        return true;
    }

    public boolean subSeqMatch(String probe, String target) {
        int ptrP, ptrT;
        char c;
        ptrP = ptrT = 0;
        while (ptrP < probe.length()) {
            if (ptrT >= target.length()) {
                return false;
            }
            c = target.charAt(ptrT);
            if (probe.charAt(ptrP) == c) {
                ptrP++;
            }
            ptrT++;
        }
        return true;
    }

    public boolean subSeqMatch(PatternI p, Edge c, int last, PatternI diagram) {
        String probe = 
        		p.getVertexString(c.left.getPos() + 1, c.right.getPos(), false);
        String target = diagram.getVertexString(
                c.getCurrentMatch().left.getPos() + 1,
                c.getCurrentMatch().right.getPos(), false);
        return this.subSeqMatch(probe, target);
    }

    // I /think/ this returns false if true, and true if false! :(
    public boolean stringMatch(String p) {
        String target, reverse;
        for (int i = 0; i < this.diagrams.length; ++i) {
            target = this.diagrams[i].getVertexString(0, 0, false);
            reverse = this.diagrams[i].getVertexString(0, 0, true);
            if (this.subSeqMatch(p, target) || this.subSeqMatch(p, reverse))
                return true;
        }
        return false;
    }

    public int stringMatchUnion(String patt) {
        int numMatch = this.diagrams.length;
        if (patt.equals("NC "))
            return numMatch; // -!-
        String p = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        String target, reverse;
        for (int i = 0; i < this.diagrams.length; ++i) {
            target = this.diagrams[i].getVertexString(0, 0, false);
            reverse = this.diagrams[i].getVertexString(0, 0, true);
            if ((this.subSeqMatch(p, target) && this.subSeqMatch(p, reverse)))
                numMatch--;
        }
        return numMatch;
    }

    public boolean stringMatch(String patt, int i) { // variant for
                                                        // efficiency
        if (patt.equals("NC "))
            return true; // -!-
        String p = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        String target, reverse;
        target = this.diagrams[i].getVertexString(0, 0, false);
        reverse = this.diagrams[i].getVertexString(0, 0, true);
        if (this.subSeqMatch(p, target) && this.subSeqMatch(p, reverse))
            return false;
        return true;
    }

    public boolean match(PatternI p, PatternI diagram) {
        this.k = 0;
        int last = 1;
        while (this.k < p.esize()) { // Run through the pattern edges.
            Edge current = p.getEdge(this.k);
            if (!current.hasMoreMatches()) {
                return false;
            }
            if (findNextMatch(p, current) 
            		&& p.verticesIncrease()
                    && subSeqMatch(p, current, last, diagram)) {
                last = current.right.getPos() + 1; // aargh!
                ++this.k;
            } else {
                if (this.k > 0) {
                    if (!nextSmallestEdge(p, current)) {
                        return false;
                    }
                }
                p.setMovedUpTo(this.k);
                if (!advancePositions(p, current)) {
                    return false;
                }
            }
        }

        if (p.noEdges()) {
            return true;
        } else {
            Edge e = p.getEdge(this.k); // IE : get the last edge
            int rhe = e.getCurrentMatch().right.getPos();

            String doutCup = diagram.getVertexString(rhe + 1, 0, false);
            String doutCdn = diagram.getVertexString(rhe + 1, 0, true);
            String outsertC = p.getOutsertC(false);
            if (subSeqMatch(outsertC, doutCup)|| subSeqMatch(outsertC, doutCdn)) {
                return true;
            } else {
                return false;
            }
        }

    }

    public boolean findNextMatch(PatternI p, Edge current) {
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
            Edge last = (p.getEdge(this.k - 1)).getCurrentMatch();
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

    public boolean nextSmallestEdge(PatternI p, Edge current) {
        // System.out.println("nextSmallestMatch");
        boolean found = false;
        Edge last = p.getEdge(this.k - 1).getCurrentMatch();
        while ((current.hasMoreMatches()) && (found == false)) {
            Edge match = current.getCurrentMatch();
            if (match.greaterThan(last))
                found = true;
            else
                current.moveMatchPtr();
        }
        return found;
    }

    public boolean advancePositions(PatternI p, Edge current) {
        // 'isRight' tells you which end of backedge pvert is...
        // System.out.println("advancePositions");
        boolean found = false, isRight = false, attached = false;
        int wt2 = 0, vt2 = 0;
        int pvert = current.left.getPos();
        int tvert = (current.getCurrentMatch()).left.getPos();

        this.PVS = new Stack<Integer>();
        this.TVS = new Stack<Integer>();
        this.PVS.push(new Integer(pvert));
        this.TVS.push(new Integer(tvert));

        while (!this.PVS.empty()) {
            pvert = PVS.pop().intValue();
            tvert = TVS.pop().intValue();
            // FORALL EDGES LESS THAN THE STUCK EDGE
            for (int j = this.k - 1; j >= 0; --j) {
                Edge backedge = p.getEdge(j);
                isRight = false;
                attached = false;

                // IF THEY ARE ATTACHED TO SAID STUCK EDGE
                if (pvert == backedge.right.getPos()) {
                    attached = true;
                    isRight = true;
                } else if (pvert == backedge.left.getPos())
                    attached = true;

                if (attached) {
                    if (backedge.moved == false) {

                        backedge.moved = true;
                        found = false;
                        while ((backedge.hasMoreMatches()) && (found == false)) {
                            Edge e = (backedge.getCurrentMatch());
                            wt2 = e.right.getPos();
                            vt2 = e.left.getPos();
                            if ((wt2 >= tvert)
                                    || ((vt2 >= tvert) && (isRight == true))) {
                                if (isRight) {
                                    tvert = wt2;
                                    if (backedge.left.getMatch() != 0) {
                                        backedge.left.resetMatch();
                                        pvert = backedge.left.getPos();
                                        this.PVS.push(new Integer(pvert));
                                        this.TVS.push(new Integer(tvert));
                                    }
                                } else {
                                    tvert = vt2;
                                    if (backedge.right.getMatch() != 0) {
                                        backedge.right.resetMatch();
                                        pvert = backedge.right.getPos();
                                        this.PVS.push(new Integer(pvert));
                                        this.TVS.push(new Integer(tvert));
                                    }
                                }
                                found = true;
                            } else {
                                backedge.moveMatchPtr();
                            }
                        }
                    } else {
                        return false;
                    } // DANGER WIL ROBINSON!

                } // end if (attached)

            } // end for all edges less than current

        } // end while PVS != 0

        // move edgeIndex to the smallest edge with an unmatched end
        for (int l = 0; l < p.esize(); ++l) {
            if (((p.getEdge(l)).left.getMatch() == 0)
                    || ((p.getEdge(l)).right.getMatch() == 0)) {
                this.k = l;
                break;
            }
        }
        return found;

    }// end of backtrack

    public void setupLogging() {
        try {
            java.io.InputStream is = new java.io.FileInputStream("log.level");
            java.util.logging.LogManager.getLogManager().readConfiguration(is);
        } catch (java.io.IOException ioe) {
            System.err.println("NO LOGGING CONFIGURATION FILE FOUND : " + ioe);
        }
    }

    public static void main(String[] args) {
        ArrayList<String> l = new ArrayList<String>();
        String line;

        if (args.length == 0) {
            System.out.println("Usage : Matcher <pattern> <file>");
            System.exit(0);
        }

        String pattern = args[0];
        String filename = args[1];

        Level level = Level.OFF;
        if (args.length > 2) {
            String levelString = args[2];
            System.err.println("setting level to : " + levelString);
            level = Level.parse(levelString);
        }

        try {
            BufferedReader bu = new BufferedReader(new FileReader(filename));
            
            while ((line = bu.readLine()) != null) {
                l.add(line);
            }
            bu.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        String[] targets = (String[]) l.toArray(new String[0]);
        Matcher m = new Matcher(targets);
        Logger.getLogger("tops.engine.inserts.Pattern").setLevel(level);
        Logger.getLogger("tops.engine.inserts.Matcher").setLevel(level);
        // m.setupLogging();
        Pattern p = new Pattern(pattern);
        String[] results = m.run(p);
        for (int i = 0; i < results.length; i++) {
            System.out.println(results[i]);
        }
        System.out.println(m.numberMatching(p) + " matches");
    }

}

