package tops.engine.drg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import tops.engine.CountingHandler;
import tops.engine.Edge;
import tops.engine.MatchHandler;
import tops.engine.MatcherI;
import tops.engine.PatternI;
import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.Vertex;

public class Matcher implements MatcherI {

    private int k;

    private PatternI[] diagrams;

    private Stack<Integer> PVS;
    private Stack<Integer> TVS;

    public Matcher() {
    	this.PVS = new Stack<Integer>();
        this.TVS = new Stack<Integer>();
    }

    public Matcher(Pattern[] diag) {
    	this();
        this.diagrams = diag;
    }

    public Matcher(List<String> diag) throws TopsStringFormatException {
    	this();
        this.diagrams = new PatternI[diag.size()];
        for (int p = 0; p < diag.size(); ++p) {
            this.diagrams[p] = new Pattern(diag.get(p));
        }
    }

    @Override
	public void match(PatternI pattern, PatternI instance, MatchHandler matchHandler) {
		boolean matchFound = false;
		boolean shouldReset = false;
		if (pattern.preProcess(instance)) {
			if (matches(pattern, instance) && stringMatch(pattern, instance)) {
				matchHandler.handle(pattern, instance);
				matchFound = true;
			}
		}
		if (!matchFound) {
			pattern.reset();	// flip
			shouldReset = true;
			if (matches(pattern, instance) && stringMatch(pattern, instance)) {
				matchHandler.handle(pattern, instance);
				matchFound = true;
			}
		}
		if (shouldReset) {
			pattern.reset();
		}
	}

	public void setDiagrams(Pattern[] diagrams) {
        this.diagrams = diagrams;
    }

    public void setDiagrams(List<String> diag) throws TopsStringFormatException {
        this.diagrams = new Pattern[diag.size()];
        for (int i = 0; i < diag.size(); ++i) {
            this.diagrams[i] = new Pattern(diag.get(i));
        }
    }

    // XXX : the match method adds the pattern to the beginning for some reason!
    public List<String> run(List<String> patterns, String target)
            throws TopsStringFormatException {
        Pattern diagram = new Pattern(target);
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < patterns.size(); i++) {
            String result = this.match(new Pattern(patterns.get(i)), diagram);
            if (!result.equals(""))
                results.add(result);
        }
        return results;
    }

    // synthesise insert ranges from the matches of the pattern to the set of
    // targets
    public String matchAndGetInserts(Pattern pattern) {
        List<int[]> ranges = new ArrayList<int[]>();
        Pattern p = pattern;
        try {
            p = new Pattern(pattern.toString()); // !!ARRRGH!
        } catch (TopsStringFormatException tsfe) {
            System.err.println("tsfe! " + tsfe);
        }
        boolean isSetup = false; // used to initialise the ranges
        for (int i = 0; i < this.diagrams.length; i++) {
            boolean hasMatched = false;
            PatternI d = this.diagrams[i];
            // System.err.println("matching " + p + " to " + d);
            String[] inserts = null;
            if (p.preProcess(d)) {
                // System.err.println("preprocessed " + p + " with " + d);
                if (matches(p, d)) {
                    // System.err.println("matched " + p + " to " + d);
                    setUnattachedVertexMatches(p, d);
                    inserts = p.getInsertStringArr(d, false);
                    hasMatched = true;
                }
            }
            p.reset();
            if (!hasMatched) {
                if (p.preProcess(d)) {
                    // System.err.println("preprocessed " + p + " with " + d);
                    if (this.matches(p, d)) {
                        // System.err.println("matched " + p + " to " + d);
                        this.setUnattachedVertexMatches(p, d);
                        inserts = p.getInsertStringArr(d, true);
                        hasMatched = true;
                    }
                }
            }
            p.reset();
            if (inserts == null) {
                System.err.println("inserts null! hasMatched = " + hasMatched
                        + " pattern = " + pattern.toString());
                return "";
            }
            // System.err.print("diagram: " + d.head + "[");
            for (int j = 0; j < inserts.length; j++) {
                int[] minmax;
                if (isSetup) {
                    minmax = (int[]) ranges.get(j);
                } else {
                    minmax = new int[2];
                    minmax[0] = -1;
                    minmax[1] = -1;
                    ranges.add(minmax);
                }
                int insertLength = inserts[j].length();
                // System.err.print(insertLength + ",");
                
                // expand the minimum of the range
                if (minmax[0] == -1 || insertLength < minmax[0]) { 
                    minmax[0] = insertLength;
                }

                // expand the maximum of the range
                if (minmax[1] == -1 || insertLength > minmax[1]) { 
                    minmax[1] = insertLength;
                }
            }
            // System.err.println("]");
            isSetup = true;
        }
        StringBuffer patternString = new StringBuffer();
        patternString.append("p ");
        for (int k = 0; k < p.vsize() - 1; k++) {
            Vertex v = p.getVertex(k);
            int[] range = (int[]) ranges.get(k);
            patternString.append(v.getType());
            if (range[0] == range[1]) { // if the min and max are equal, use a
                                        // single number
                patternString.append("[").append(range[0]).append("]");
            } else {
                patternString.append("[").append(range[0]).append("-").append(
                        range[1]).append("]");
            }
        }
        patternString.append("C").append(" ");
        Edge e;
        for (int j = 0; j < p.esize(); ++j) {
            e = p.getEdge(j);
            patternString.append(e.getLeftVertex().getPos());
            patternString.append(':');
            patternString.append(e.getRightVertex().getPos());
            patternString.append(e.getType());
        }
        return patternString.toString();
    }

    public void setUnattachedVertexMatches(PatternI p, PatternI d) {
        this.setUnattachedVertexMatches(p, d, 0, p.vsize(), 0, d.vsize());
    }

    public void setUnattachedVertexMatches(PatternI p, PatternI d,
            int patternStart, int patternStop, int targetStart, int targetStop) {
        // go through the vertices, setting any that are not set
        int k = targetStart + 1;
        for (int j = patternStart + 1; j < patternStop; j++) {
            Vertex pV = p.getVertex(j);
            // System.err.println("pattern vertex " + j + " = " + pV);
            while (k < targetStop) {
                Vertex tV = d.getVertex(k);
                // System.err.println("target vertex " + k + " = " + tV);
                k++;
                if (pV.getType() == tV.getType()) {
                    int matchPos = pV.getMatch();
                    if (matchPos == 0) { // this must be an unattached vertex
                        // System.err.println("setting : " + pV + " to match " +
                        // tV);
                        pV.setMatch(tV); // set this to the first thing it
                                            // matches!
                    } else {
                        k = matchPos + 1; // start the search from the match!
                    }
                    break;
                }
            }
        }
    }
    
    public void matchAll(PatternI pattern, MatchHandler matchHandler) {
    	for (PatternI instance : diagrams) {
    		match(pattern, instance, matchHandler);
    	}
    }
    
    public String match(Pattern p, Pattern d) {
        String result = new String();
        boolean matchfound = false;
        if (p.preProcess(d)) {
            if (this.matches(p, d)) {
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
            if (this.matches(p, d)) {
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
//            return p.toString() + "\t" + result;		// TODO can these be requested?
        	return d.toString() + "\t" + result;
        else
            return new String();
    }

    // return a Result array rather than a String array
    public List<Result> runResults(Pattern p) {
        List<Result> results = new ArrayList<Result>();
        for (int i = 0; i < this.diagrams.length; ++i) {
            Result result = new Result();
            result.setID(this.diagrams[i].getName());
            boolean matchfound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchfound = true;
                    result.setData(p.getInsertString(this.diagrams[i]) + "\t"
                            + p.getVertexMatchedString());
                }
            }
            p.reset();
            // NOTE: Resetting now FLIPS too!
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
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
        return results;
    }

    public String[] run(String pattern) {
        try {
            return this.run(new Pattern(pattern));
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe);
            return null;
        }
    }
    
    public String[] run(Pattern p) {
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < this.diagrams.length; ++i) {
            String result = new String(this.diagrams[i].toString());
            boolean matchfound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchfound = true;
                    result += p.getMatchString();
                    result += p.getInsertString(this.diagrams[i]);
                }
            }
            p.reset();
            // NOTE: Resetting now FLIPS too!
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
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

    // generate string arrays of the insert strings from an edgepattern
    public String[][] generateInserts(Pattern p) {
        String[][] results = new String[diagrams.length][];
        for (int i = 0; i < diagrams.length; ++i) {
            if (p.preProcess(diagrams[i])) {
                if (matches((PatternI)p, diagrams[i])) {
                    results[i] = p.getInsertStringArr(diagrams[i], false);
                }
            }
            p.reset(); // and flip, of course
            if (p.preProcess(this.diagrams[i])) {
                if (matches((PatternI)p, (PatternI)this.diagrams[i])) {
                    // this OVERWRITES the previous array if the pattern matches
                    // twice!
                    results[i] = p.getInsertStringArr(this.diagrams[i], true);
                }
            }
            p.reset();
        }
        return results;
    }
    
    public boolean runsSuccessfully(Pattern p) {
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.matches(p, this.diagrams[i])) {
                        matchFound = true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            p.reset();
            if (!matchFound) {
                return false;
            }
        }
//        return this.stringMatch(p.getVertexString(0, 0, false));
        return this.stringMatch(p);
        // NOTE : now testing vertex match for each graph as it's matched
    }

    public int numberMatching(Pattern p) {
    	CountingHandler handler = new CountingHandler();
    	matchAll(p, handler);
    	return handler.getCount();
    }

    public boolean runChiral(Pattern p) {
        p.reset();
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.matches(p, this.diagrams[i])) {
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
        // System.err.println("run chiral successful for " + p);
        return true;
    }
    
    // TODO : test to ensure this works!
    public boolean subSeqMatch(Edge c, int last, PatternI p, PatternI d) {
        Edge match = c.getCurrentMatch();
        return p.subSequenceCompare(c.getLeft() + 1, c.getRight(), 
        		match.getLeft() + 1, match.getRight(), d, false);
    }

    // TODO : why does this try to find at least one match, and not all?
    public boolean stringMatch(Pattern pattern) {
        for (int i = 0; i < this.diagrams.length; ++i) {
            PatternI target = this.diagrams[i];
            if (stringMatch(pattern, target)) {
            	return true;
            }
        }
        return false;
    }
    
    // TODO - this should really replace the above method?
    private boolean stringMatch(PatternI pattern, PatternI target) {
    	return (pattern.subSequenceCompare(0, 0, 0, 0, target, false) 
        		|| pattern.subSequenceCompare(0, 0, 0, 0, target, true));
    }

    public boolean matches(PatternI p, PatternI d) {
        this.k = 0;
        int last = 1;
        while (this.k < p.esize()) { // Run through the pattern edges.
            Edge current = p.getEdge(this.k);
            if (!current.hasMoreMatches()) {
                return false;
            }

            if (findNextMatch(p, current) 
            		&& verticesIncrease(p, current)
            		&& subSeqMatch(current, last, p, d)) {
                last = current.getRight() + 1;
                this.k++;
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
            int rhe = e.getCurrentMatch().getRight() + 1;
            int pl = p.getLastEdgeVertexPosition() + 1;
            return p.subSequenceCompare(pl, 0, rhe, 0, d, false) 
            	|| p.subSequenceCompare(pl, 0, rhe, 0, d, true);
        }

    }
    
    private boolean findNextMatch(PatternI p, Edge current) {
    	int storedValue = current.getMatchPtr();
        Edge last = null;
        if (this.k > 0) {
        	last = (p.getEdge(this.k - 1)).getCurrentMatch();
        }
        while (current.hasMoreMatches()) {
        	Edge match = current.getCurrentMatch();
        	if (current.alreadyMatched(match)
        			&& (this.k == 0 || (match.greaterThan(last)))) {
        		current.setEndMatches(match);
        		return true;
        	} else {
        		current.moveMatchPtr();
        	}
        }
        current.setMatchPtr(storedValue);
        return false;
    }

    private boolean nextSmallestEdge(PatternI p, Edge current) {
        Edge last = (p.getEdge(this.k - 1)).getCurrentMatch();
        while (current.hasMoreMatches()) {
            Edge match = current.getCurrentMatch();
            if (match.greaterThan(last)) {
            	return true;
            } else {
                current.moveMatchPtr();
            }
        }
        return false;
    }

    private boolean verticesIncrease(PatternI p, Edge current) {
        int last = 0;
        for (int i = 1; i < p.vsize() - 1; i++) {
        	int m = p.getVertex(i).getMatch();
        	if (m != 0) {
                if (m <= last) {
                    return false;
                } else {
                    last = m;
                }
            }
        }
        return true;
    }

    private boolean advancePositions(PatternI p, Edge current) {
        // 'isRight' tells you which end of backedge pvert is...
        boolean found = false, isRight = false, attached = false;

        this.PVS.clear();
        this.PVS.push(new Integer(current.getLeft()));
        
        this.TVS.clear();
        this.TVS.push(new Integer((current.getCurrentMatch()).getLeft()));

        while (!this.PVS.empty()) {
            int pvert = this.PVS.pop().intValue();
            int tvert = this.TVS.pop().intValue();
            // FORALL EDGES LESS THAN THE STUCK EDGE
            for (int j = this.k - 1; j >= 0; --j) {
                Edge backedge = p.getEdge(j);
                isRight = false;
                attached = false;

                // IF THEY ARE ATTACHED TO SAID STUCK EDGE
                if (pvert == backedge.getRight()) {
                    attached = true;
                    isRight = true;
                } else if (pvert == backedge.getLeft()) {
                    attached = true;
                }

                if (attached) {
                	if (backedge.moved == false) {

                		backedge.moved = true;
                		found = false;
                		while ((backedge.hasMoreMatches()) && (found == false)) {
                			Edge e = backedge.getCurrentMatch();
                			int wt2 = e.getRight();
                			int vt2 = e.getLeft();
                			if ((wt2 >= tvert)
                					|| ((vt2 >= tvert) && (isRight == true))) {
                				if (isRight) {
                					tvert = wt2;
                					if (backedge.getLeftVertex().getMatch() != 0) {
                						backedge.getLeftVertex().resetMatch();
                						pvert = backedge.getLeft();
                						this.PVS.push(new Integer(pvert));
                						this.TVS.push(new Integer(tvert));
                					}
                				} else {
                					tvert = vt2;
                					if (backedge.getRightVertex().getMatch() != 0) {
                						backedge.getRightVertex().resetMatch();
                						pvert = backedge.getRight();
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
                	}
                } 
            } 
        }

        // move edgeIndex to the smallest edge with an unmatched end
        int unmatchedEdgeIndex = p.indexOfFirstUnmatchedEdge();
        if (unmatchedEdgeIndex != -1) {
        	this.k = unmatchedEdgeIndex;
        }
        
        return found;
    }

}

