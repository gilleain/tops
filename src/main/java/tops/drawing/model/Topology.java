package tops.drawing.model;

import java.util.ArrayList;
import java.util.List;



/**
 * @author maclean
 *
 */
public class Topology {
    
    private List<SSE> sses;
    private List<Connection> connections;
    private List<TSE> tses;
    private List<Chirality> chiralities;
    
    public Topology() {
        this.sses = new ArrayList<SSE>();
        this.connections = new ArrayList<Connection>();
        this.tses = new ArrayList<TSE>();
        this.chiralities = new ArrayList<Chirality>();
    }
    
    public Topology(String topsString) {
        this();
        
        TParser parser = new TParser(topsString);
        String vertices = parser.getVertexString();
        String[] edges = parser.getEdges();
        
        // convert the vertex string into sses
        for (int i = 0; i < vertices.length(); i++) {
            char c = vertices.charAt(i);
            SSE sse = null;
            if (c == 'E') {
                sse = new Strand(i, true);
            } else if (c == 'e') {
                sse = new Strand(i, false);
            } else if (c == 'H') {
                sse = new Helix(i, true);
            } else if (c == 'h') {
                sse = new Helix(i, false);
            } else if (c == 'N') {
                sse = new Terminus("N");
            } else if (c == 'C') {
                sse = new Terminus("C");
            } else {
                System.err.println("Unknown character: " + c);
            }
            
            if (sse != null) {
                this.sses.add(sse);
                System.err.println(sse);
            }
        }
        
        // create the logical connections between sses
        for (int i = 0; i < this.sses.size() - 1; i++) {
            SSE sseA = this.sses.get(i);
            SSE sseB = this.sses.get(i + 1);
            this.connections.add(new Connection(sseA, sseB));
        }
        
        // convert the edges into sheets and chiralities
        for (int j = 0; j < edges.length; j += 3) {
            int l = Integer.parseInt(edges[j]);
            int r = Integer.parseInt(edges[j + 1]);
            char t = edges[j + 2].charAt(0);
            
            if (t == 'P' || t == 'A') {
                TSE leftSheet = this.findTSE(l);
                TSE rightSheet = this.findTSE(r);
                Strand leftSSE = (Strand) sses.get(l);
                Strand rightSSE = (Strand) sses.get(r);

                if (leftSheet == null && rightSheet == null) {
                    this.tses.add(new TSE(leftSSE, rightSSE));
                } else if (rightSheet == null) {
                    leftSheet.add(leftSSE, rightSSE);
                } else if (leftSheet == null) {
                    rightSheet.add(leftSSE, rightSSE);
                } else if (leftSheet != rightSheet) {
                    if (leftSheet.size() > rightSheet.size()) {
                        leftSheet.merge(rightSheet, leftSSE, rightSSE);
                        this.tses.remove(rightSheet);
                    } else {
                        rightSheet.merge(leftSheet, rightSSE, leftSSE);
                        this.tses.remove(leftSheet);
                    }
                } else {
                    // TODO : barrels
                }
            } else if (t == 'R' || t == 'L') {
                SSE leftSSE = sses.get(l);
                SSE rightSSE = sses.get(r);
                
                // XXX : we are assuming that the chiral is between n and n+2!!
                assert l + 1 == r;
                int mid = (l + r) / 2;
                SSE middleSSE = sses.get(mid);
                this.chiralities.add(new Chirality(leftSSE, middleSSE, rightSSE, t));
            }
        }
        
        // determine any chiralities that are missing
        for (int i = 0; i < this.sses.size() - 2; i++) {
            SSE a = this.sses.get(i);
            SSE b = this.sses.get(i + 1);
            SSE c = this.sses.get(i + 2);
            
            if (a.isParallelTo(c)) {
                if (!this.chiralityExistsBetween(a, c)) {
                    this.chiralities.add(new Chirality(a, b, c));
                }
            }
        }
        
        // 'layout' the TSEs according to the chirality info
        for (int i = 0; i < this.chiralities.size(); i++) {
            Chirality chirality = this.chiralities.get(i);
            SSE middleSSE = chirality.getSecond();
            if (middleSSE instanceof Helix) {
                SSE first = chirality.getFirst();
                SSE third = chirality.getThird();
                if (first instanceof Strand && third instanceof Strand) {
                    TSE sheet = this.findTSE(first, third);
                    int o = sheet.orientation(first, third);
                    char t = chirality.getType();
                    int z = this.getZ(first.isUp(), o, t);
                    int endpointIndex = this.tses.indexOf(sheet);
                    
                    // This is a bit magic.. The return value for getZ is
                    // also the direction in the tses ArrayList. 
                    int newIndex = endpointIndex + z;
                    if (newIndex > this.tses.size() - 1) {
                        // new tse needed beyond the end of the list
                        TSE helixSheet = new TSE(middleSSE);
                        this.tses.add(helixSheet);
                    } else if (newIndex < 0) {
                        TSE helixSheet = new TSE(middleSSE);
                        this.tses.add(0, helixSheet);
                    } else {
                        TSE helixSheet = this.tses.get(newIndex);
                        helixSheet.add(middleSSE);
                    }
                }
            }
        }
    }
    
    public List<SSE> getSSES() {
        return this.sses;
    }
    
    public List<TSE> getTSES() {
        return this.tses;
    }
    
    public List<Connection> getConnections() {
        return this.connections;
    }
    
    /**
     * Determines the Z-layer of an SSE given orientation and chirality.
     * The return value is an int (1/-1) indicating either 'down'/'up'
     *  (for a cartoon) or 'back'/'front' (for a layer diagram). 
     * 
     * @param isUp true if the structure is pointing up
     * @param o the orientation of the structure
     * @param t the chiral type
     * @return -1 or 1
     */
    public int getZ(boolean isUp, int o, char t) {
        if (isUp && o == 1) {
            if (t == 'R') {
                return 1;
            } else {
                return -1;
            }
        } else if (isUp && o == -1) {
            if (t == 'R') {
                return -1;
            } else {
                return 1;
            }
        } else if (!isUp && o == 1) {
            if (t == 'R') {
                return -1;
            } else {
                return 1;
            }
        } else if (!isUp && o == -1) {
            if (t == 'R') {
                return 1;
            } else {
                return -1;
            }
        } else {
            return 0;   // XXX throw exception
        }
    }
    
    private TSE findTSE(SSE first, SSE second) {
        for (int i = 0; i < this.tses.size(); i++) {
            TSE tse = this.tses.get(i);
            if (tse.contains(first, second)) {
                return tse;
            }
        }
        return null;
    }

    public boolean inSameTSE(SSE first, SSE second) {
        for (int i = 0; i < this.tses.size(); i++) {
            TSE tse = this.tses.get(i);
            if (tse.contains(first, second)) {
                return true;
            }
        }
        return false;
    }
    
    private TSE findTSE(int number) {
        for (int i = 0; i < this.tses.size(); i++) {
            TSE tse = this.tses.get(i);
            if (tse.contains(number)) {
                return tse;
            }
        }
        return null;
    }
    
    private boolean chiralityExistsBetween(SSE l, SSE r) {
        for (int i = 0; i < this.chiralities.size(); i++) {
            Chirality chirality = this.chiralities.get(i);
            if (chirality.hasSSES(l, r)) {
                return true;
            }
        }
        return false;
    }
}
