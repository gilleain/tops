package tops.engine;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Matches a single pattern to an instance.
 * 
 * @author maclean
 *
 */
public class LinearMatcher {
    
    private int currentEdgeIndex;
    
    private class SingleMatchHandler implements MatchHandler {
        
        public boolean isMatch;
        
        @Override
        public void handle(PatternI pattern, PatternI instance) {
            isMatch = true;
        }
    }
    
    public boolean matches(PatternI pattern, PatternI instance) {
        SingleMatchHandler handler = new SingleMatchHandler();
        match(pattern, instance, handler);
        return handler.isMatch;
    }

    /**
     * Match a pattern to an instance, handling any matches using the matchHandler.
     * 
     * @param pattern
     * @param instance
     * @param matchHandler
     */
    public void match(PatternI pattern, PatternI instance, MatchHandler matchHandler) {
        boolean matchfound = false;
        if (preProcess(pattern, instance)) {
            matchfound = match(pattern, instance);
        }
        if (!matchfound) {
            pattern.reset();
            matchfound = match(pattern, instance);
            pattern.reset();
        }
        if (matchfound) {
            matchHandler.handle(pattern, instance);
        }
    }

    private boolean match(PatternI pattern, PatternI instance) {
        currentEdgeIndex = 0;
        while (currentEdgeIndex < pattern.esize()) {
            Edge current = pattern.getEdge(currentEdgeIndex);
            if (!current.hasMoreMatches()) {
                return false;
            }
            if (findNextMatch(pattern, current) 
                    && pattern.verticesIncrease()
                    && subSeqMatch(pattern, current, instance)) {
                ++currentEdgeIndex;
            } else {
                if (currentEdgeIndex > 0 && !nextSmallestEdge(pattern, current)) {
                    return false;
                }
                pattern.setMovedUpTo(currentEdgeIndex);
                if (!advancePositions(pattern, current)) {
                    return false;
                }
            }
        }

        if (pattern.noEdges()) {
            return true;
        } else {
            Edge lastEdge = pattern.getEdge(currentEdgeIndex);
            int rhe = lastEdge.getCurrentMatch().getRightVertex().getPos();

            String doutCup = instance.getVertexString(rhe + 1, 0, false);
            String doutCdn = instance.getVertexString(rhe + 1, 0, true);
            String outsertC = pattern.getOutsertC(false);
            return subSeqMatch(outsertC, doutCup)|| subSeqMatch(outsertC, doutCdn);
        }
    }
    
    private boolean findNextMatch(PatternI pattern, Edge current) {
        int c = 0;
        if (this.currentEdgeIndex == 0) {
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
            Edge last = pattern.getEdge(this.currentEdgeIndex - 1).getCurrentMatch();
            while (current.hasMoreMatches()) {
                Edge match = current.getCurrentMatch();
                if (current.alreadyMatched(match) && match.greaterThan(last)) {
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
    
    private boolean subSeqMatch(String probe, String target) {
        int ptrP;
        int ptrT;
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
    
    private boolean nextSmallestEdge(PatternI p, Edge current) {
        boolean found = false;
        Edge last = p.getEdge(currentEdgeIndex - 1).getCurrentMatch();
        while (current.hasMoreMatches() && !found) {
            Edge match = current.getCurrentMatch();
            if (match.greaterThan(last)) {
                found = true;
            } else {
                current.moveMatchPtr();
            }
        }
        return found;
    }
    
    private boolean subSeqMatch(PatternI p, Edge c, PatternI diagram) {
        String probe = 
                p.getVertexString(
                        c.getLeftVertex().getPos() + 1,
                        c.getRightVertex().getPos(), false);
        String target = diagram.getVertexString(
                c.getCurrentMatch().getLeftVertex().getPos() + 1,
                c.getCurrentMatch().getRightVertex().getPos(), false);
        return this.subSeqMatch(probe, target);
    }
    
    private boolean advancePositions(PatternI pattern, Edge current) {
        // 'isRight' tells you which end of backedge pvert is...
        boolean found = false;
        boolean isRight = false;
        boolean attached = false;
        int wt2 = 0;
        int vt2 = 0;
        int pvert = current.getLeftVertex().getPos();
        int tvert = current.getCurrentMatch().getLeftVertex().getPos();

        Deque<Integer> patternVertexStack = new ArrayDeque<>();
        Deque<Integer> targetVertexStack = new ArrayDeque<>();
        patternVertexStack.push(pvert);
        targetVertexStack.push(tvert);

        while (!patternVertexStack.isEmpty()) {
            pvert = patternVertexStack.pop().intValue();
            tvert = targetVertexStack.pop().intValue();
            // FORALL EDGES LESS THAN THE STUCK EDGE
            for (int j = currentEdgeIndex - 1; j >= 0; --j) {
                Edge backedge = pattern.getEdge(j);
                isRight = false;
                attached = false;

                // IF THEY ARE ATTACHED TO SAID STUCK EDGE
                if (pvert == backedge.getRightVertex().getPos()) {
                    attached = true;
                    isRight = true;
                } else if (pvert == backedge.getLeftVertex().getPos()) {
                    attached = true;
                }

                if (attached) {
                    if (!backedge.moved) {

                        backedge.moved = true;
                        found = false;
                        while (backedge.hasMoreMatches() && !found) {
                            Edge e = backedge.getCurrentMatch();
                            wt2 = e.getLeftVertex().getPos();
                            vt2 = e.getLeftVertex().getPos();
                            if ((wt2 >= tvert) || ((vt2 >= tvert) && isRight)) {
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
                    } // DANGER WIL ROBINSON!

                }
            } 
        } 

        // move edgeIndex to the smallest edge with an unmatched end
        for (int l = 0; l < pattern.esize(); ++l) {
            Edge e = pattern.getEdge(l); 
            if (e.getLeftVertex().getMatch() == 0 || e.getRightVertex().getMatch() == 0) {
                this.currentEdgeIndex = l;
                break;
            }
        }
        return found;
    }


    private boolean preProcess(PatternI p, PatternI d) {
        return true;
    }

}
