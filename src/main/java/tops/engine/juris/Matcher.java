package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import tops.engine.Edge;

class Matcher {

    int k;

    Pattern p;

    Pattern[] diagrams;

    int[][] matches;

    Stack<Integer> PVS, TVS;

    Matcher(String[] diag) {
        this.diagrams = new Pattern[diag.length];
        for (int p = 0; p < diag.length; ++p) {
            this.diagrams[p] = new Pattern(diag[p]);
        }
    }

    Matcher(String patt, String[] diag) {
        if (patt.charAt(0) == 'N')
            patt = "head " + patt; // name anonymous patterns!
        this.p = new Pattern(patt);
        this.diagrams = new Pattern[diag.length];
        for (int p = 0; p < diag.length; ++p) {
            this.diagrams[p] = new Pattern(diag[p]);
        }
    }

    String[] run() {
        List<String> results = new ArrayList<String>(this.diagrams.length);
        boolean noInserts = false; // this was making the final match results
                                    // different from expected
        String tmp;
        for (int i = 0; i < this.diagrams.length; ++i) {
            if (this.p.preProcess(this.diagrams[i], noInserts)) {
                if (this.match(this.diagrams[i], noInserts)) {
                    tmp = this.diagrams[i].head;
                    tmp += this.p.getMatchString();
                    tmp += this.p.getInsertString(this.diagrams[i]);
                    results.add(tmp);
                }
            }
            this.p.reset();
            // NOTE: Resetting now FLIPS too!
            if (this.p.preProcess(this.diagrams[i], noInserts)) {
                if (this.match(this.diagrams[i], noInserts)) {
                    tmp = this.diagrams[i].head;
                    tmp += this.p.getMatchString();
                    tmp += this.p.getInsertString(this.diagrams[i]);
                    results.add(tmp);
                }
            }
            this.p.reset();
        }
        // System.out.println(p.toString());
        return (String[]) results.toArray(new String[0]);
    }

    // generate string arrays of the insert strings from an edgepattern
    String[][] generateInserts() {
        String[][] results = new String[this.diagrams.length][];
        for (int i = 0; i < this.diagrams.length; ++i) {
            if (this.p.preProcess(this.diagrams[i], true)) {
                if (this.match(this.diagrams[i], true)) {
                    results[i] = this.p.getInsertStringArr(this.diagrams[i]);
                }
            }
            this.p.reset(); // and flip, of course
            if (this.p.preProcess(this.diagrams[i], true)) {
                if (this.match(this.diagrams[i], true)) {
                    results[i] = this.p.getInsertStringArr(this.diagrams[i]);
                }
            }
            this.p.reset();
        }
        return results;
    }

    boolean run(String patt, boolean noInserts) {
        if (patt.charAt(patt.length() - 2) == 'C')
            return true; // match the null pattern!
        if (patt.charAt(0) == 'N')
            patt = "head " + patt; // name anonymous patterns!
            // System.out.println("Running : " + patt + " ");

        this.p = new Pattern(patt); // destructive (re)-assignment
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (this.p.preProcess(this.diagrams[i], noInserts)) {
                // System.out.println("Pre-processed : " + p.toString() + " with
                // " + diagrams[i].toString());
                if (this.match(this.diagrams[i], noInserts))
                    matchFound = true;
                // else System.out.println("Didn't match : " + p.toString() + "
                // with " + diagrams[i].toString());
            } // else { System.out.println("Didn't Pre-process : " +
                // p.toString() + " with " + diagrams[i].toString()); }
            this.p.reset();
            // NOTE: Resetting now FLIPS too!
            if (!matchFound) {
                if (this.p.preProcess(this.diagrams[i], noInserts)) {
                    // System.out.println("Pre-processed : " + p.toString() + "
                    // (flipped) with " + diagrams[i].toString());
                    if (this.match(this.diagrams[i], noInserts)) {
                        matchFound = true; // everything OK
                    } else {
                        // System.out.println("Matching : NO!");
                        return false;
                    } // no match
                } else {
                    // System.out.println("Pre-processed : NO!");
                    return false;
                } // no pre-processing
            }
            this.p.reset();
        }
        // System.out.println("Fragment = " + patt.substring(patt.indexOf('N') +
        // 1, patt.lastIndexOf('C')));
        // System.out.println("edge matching complete");
        if (this.stringMatch(patt.substring(patt.indexOf('N'),
                patt.lastIndexOf('C') + 1)))
            return true;
        else {
            return false;
        }
        // NOTE : now testing vertex match for each graph as it's matched
    }

    String runUnion(String diagram, boolean noInserts) { // run a SINGLE
                                                            // example against
                                                            // the SET of
                                                            // patterns
        Pattern instance = new Pattern(diagram); // muhahaha!

        boolean matchFound;
//        String pbody, pstr;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            this.p = this.diagrams[i]; // DANGER WIL ROBINSON!
            if (this.p.preProcess(instance, noInserts)) {
                if (this.match(instance, noInserts))
                    matchFound = true;
                if (!this.stringMatch(this.p.toString(), diagram))
                    matchFound = false;
            }
            this.p.reset();
            if (!matchFound) {
                if (this.p.preProcess(instance, noInserts)) {
                    if (this.match(instance, noInserts))
                        matchFound = true;
                    if (!this.stringMatch(this.p.toString(), diagram))
                        matchFound = false;
                }
            }
            this.p.reset();
            if (matchFound)
                return this.p.head; // the name of the first pattern that matched
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
                pstr = "head " + pstr; // name anonymous patterns!
            parr[j] = new Pattern(pstr);
        }
        boolean matchFound;
        int cP;
        String pbody, pstr;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            cP = 0;
            while ((cP < parr.length) && (!matchFound)) {
                this.p = parr[cP];
                pstr = patt[cP];
                pbody = pstr.substring(pstr.indexOf('N'),
                        pstr.lastIndexOf('C') + 1);
                if (this.p.preProcess(this.diagrams[i], noInserts)) {
                    if (this.match(this.diagrams[i], noInserts))
                        matchFound = true;
                }
                this.p.reset();
                if (!matchFound) {
                    if (this.p.preProcess(this.diagrams[i], noInserts)) {
                        if (this.match(this.diagrams[i], noInserts))
                            matchFound = true;
                    }
                }
                this.p.reset();
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
        // System.out.println("attempting : probe [" + probe + "] and target ["
        // + target + "]");
        int ptrP, ptrT;
        char c;
        ptrP = ptrT = 0;
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
        String probe = this.p.getVertexString(last, c.getLeftVertex().getPos(), false);
        String target = 
        		d.getVertexString(1, c.getCurrentMatch().getLeftVertex().getPos(), false);
        // System.out.println("attempting : probe [" + probe + "] and target ["
        // + target + "]");
        int ptrP, ptrT;
        char ch;
        ptrP = ptrT = 0;
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
        String p = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        String d = diag.substring(diag.indexOf('N') + 1, diag.lastIndexOf('C'));
        return !(this.subSeqMatch(p, d));
    }

    boolean stringMatch(String patt) {
        // System.out.println("patt : [" + patt + "]");
        if (patt.equals("NC "))
            return true; // -!-
        String p = patt.substring(patt.indexOf('N') + 1, patt.lastIndexOf('C'));
        String target, reverse;
        for (int i = 0; i < this.diagrams.length; ++i) {
            target = this.diagrams[i].getVertexString(0, 0, false);
            reverse = this.diagrams[i].getVertexString(0, 0, true);
            // System.out.println("Matching : [" + p + "] to : " + target + "
            // and " + reverse);
            if (this.subSeqMatch(p, target) && this.subSeqMatch(p, reverse))
                return false;
            // System.out.println("Matched : " + i);
        }
        return true;
    }

    int stringMatchUnion(String patt) {
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

    boolean stringMatch(String patt, int i) { // variant for efficiency
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

    boolean match(Pattern d, boolean noInserts) {
        // System.out.println("Attempting : " + p.toString() + " with " +
        // d.toString());
        this.k = 0;
        int last = 1;
        while (this.k < this.p.size()) { // Run through the pattern edges.
            Edge current = this.p.getEdge(this.k);
            if (!current.hasMoreMatches()) {
                // System.out.println("out of matches!");
                return false;
            }

            if (this.findNextMatch(current) && this.verticesIncrease(current)
                    && ((noInserts) || this.subSeqMatch(current, last, d))) {
                // System.out.println("K = " + k);
                last = current.getRightVertex().getPos() + 1; // aargh!
                ++this.k;
            } else {
                // System.out.println("advancing?");
                if (this.k > 0) {
                    // System.out.println("k > 0");
                    if (!this.nextSmallestEdge(current))
                        return false;
                }
                this.p.setMovedUpTo(this.k);
                if (!this.advancePositions(current))
                    return false;
            }
        } // end while(k)

        // System.out.println("K = " + k);
        int rhe;
        if (this.p.noEdges() || noInserts)
            rhe = 0;
        else {
            Edge e = this.p.getEdge(this.k);
            rhe = e.getCurrentMatch().getRightVertex().getPos();

            String doutCup = d.getVertexString(rhe + 1, 0, false);
            String doutCdn = d.getVertexString(rhe + 1, 0, true);
            // System.out.println("OUTSERTS : pattern = " + p.outsertC + "
            // target = " + doutCup + " or " + doutCdn);
            if (this.subSeqMatch(this.p.outsertC, doutCup)
                    && this.subSeqMatch(this.p.outsertC, doutCdn))
                return false;
            else
                return true;
        }
        return true; // noedges or no in/out-serts
    }

    // ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

    boolean findNextMatch(Edge current) {
        int c = 0;
        // System.out.println("findNextMatch");
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
            Edge last = (this.p.getEdge(this.k - 1)).getCurrentMatch();
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
        // System.out.println("nextSmallestMatch");
        boolean found = false;
        Edge last = (this.p.getEdge(this.k - 1)).getCurrentMatch();
        while ((current.hasMoreMatches()) && (found == false)) {
            Edge match = current.getCurrentMatch();
            if (match.greaterThan(last))
                found = true;
            else
                current.moveMatchPtr();
        }
        return found;
    }

    boolean verticesIncrease(Edge current) {
        int last = 0;
        // int end = current.right.getPos();
        int[] m = this.p.getMatches();
        // System.out.print('[');
        // for (int j = 0; j < m.length; ++j) { System.out.print(m[j] + ", "); }
        // System.out.print("] ");
        for (int i = 0; i < m.length; i++) {
            if (m[i] != 0) {
                if (m[i] <= last) {
                    // System.out.print(m[i] + " not greater than " + last);
                    return false;
                } else {
                    // if (last != 0) System.out.print(m[i] + " > " + last + "
                    // (i = " + i + ") , ");
                    last = m[i];
                }
            }
        }
        // System.out.print(" Done.\n");
        return true;
    }

    boolean advancePositions(Edge current) {
        // 'isRight' tells you which end of backedge pvert is...
        // System.out.println("advancePositions");
        boolean found = false, isRight = false, attached = false;
        int wt2 = 0, vt2 = 0;
        int pvert = current.getLeftVertex().getPos();
        int tvert = (current.getCurrentMatch()).getLeftVertex().getPos();

        this.PVS = new Stack<Integer>();
        this.TVS = new Stack<Integer>();
        this.PVS.push(new Integer(pvert));
        this.TVS.push(new Integer(tvert));

        while (!this.PVS.empty()) {
            pvert = ((Integer) (this.PVS.pop())).intValue();
            tvert = ((Integer) (this.TVS.pop())).intValue();
            // System.out.println("pvert, tvert = " + pvert + ", " + tvert);
            // FORALL EDGES LESS THAN THE STUCK EDGE
            for (int j = this.k - 1; j >= 0; --j) {
                Edge backedge = this.p.getEdge(j);
                isRight = false;
                attached = false;

                // IF THEY ARE ATTACHED TO SAID STUCK EDGE
                if (pvert == backedge.getRightVertex().getPos()) {
                    attached = true;
                    isRight = true;
                } else if (pvert == backedge.getLeftVertex().getPos())
                    attached = true;

                if (attached) {
                    if (backedge.moved == false) {

                        backedge.moved = true;
                        found = false;
                        while ((backedge.hasMoreMatches()) && (found == false)) {
                            Edge e = (backedge.getCurrentMatch());
                            wt2 = e.getRightVertex().getPos();
                            vt2 = e.getLeftVertex().getPos();
                            if ((wt2 >= tvert)
                                    || ((vt2 >= tvert) && (isRight == true))) {
                                if (isRight) {
                                    tvert = wt2;
                                    if (backedge.getLeftVertex().getMatch() != 0) {
                                        backedge.getLeftVertex().resetMatch();
                                        pvert = backedge.getLeftVertex().getPos();
                                        this.PVS.push(new Integer(pvert));
                                        this.TVS.push(new Integer(tvert));
                                    }
                                } else {
                                    tvert = vt2;
                                    if (backedge.getRightVertex().getMatch() != 0) {
                                        backedge.getRightVertex().resetMatch();
                                        pvert = backedge.getRightVertex().getPos();
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
        for (int l = 0; l < this.p.size(); ++l) {
            if (((this.p.getEdge(l)).getLeftVertex().getMatch() == 0)
                    || ((this.p.getEdge(l)).getRightVertex().getMatch() == 0)) {
                this.k = l;
                break;
            }
        }
        return found;

    }// end of backtrack

}// EOC
