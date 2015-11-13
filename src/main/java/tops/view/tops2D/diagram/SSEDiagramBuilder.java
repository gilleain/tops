package tops.view.tops2D.diagram;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import tops.model.Chain;
import tops.model.HBondSet;
import tops.model.Protein;
import tops.model.SSE;

/**
 * Builds {@link Graph} objects with SSEs as vertices and hBond ladders 
 * and chiralities as edges.
 *  
 * @author maclean
 *
 */
public class SSEDiagramBuilder {
    
    public Graph build(Protein protein) {
        Graph graph = new Graph();
        for (Chain chain : protein.getChains()) {
            int position = 0;
            Map<SSE, Integer> sseMap = new HashMap<SSE, Integer>();
            for (SSE sse : chain.getSSES()) {
                Vertex vertex = toVertex(sse, position);
                if (vertex != null) {
                    graph.addVertex(vertex);
                    sseMap.put(sse, position);
                    position++;
                }
            }
            for (HBondSet hBondSet : chain.getHBondSets()) {
                Integer startPos = sseMap.get(hBondSet.getStart());
                Integer endPos = sseMap.get(hBondSet.getEnd());
                if (startPos != null && endPos != null) {
                    Vertex left = graph.getVertex(startPos);
                    Vertex right = graph.getVertex(endPos);
                    // XXX type...
                    graph.addEdge(new HBond(left, right, Edge.ANTIPARALLEL_HBOND));
                }
            }
        }
        return graph;
    }
    
    private Vertex toVertex(SSE sse, int position) {
        boolean isDown = false; // XXX TODO
        switch (sse.getType()) {
            case ALPHA_HELIX: return new Helix(isDown, position);
            case EXTENDED: return new Strand(isDown, position);
            case TURN:
            default: return null;
        }
    }
    
    public Graph build(String vertexString, String edgeString, String highlightString) {
        Graph graph = new Graph();
        setVertices(graph, vertexString);
        setEdges(graph, edgeString);
        setHighlights(graph, highlightString);
        return graph;
    }
    
    private void setVertices(Graph graph, String vertices) {
        int numberOfVertices = vertices.length();

        for (int position = 0; position < numberOfVertices; position++) {
            char v = vertices.charAt(position);
            switch (v) {
                case 'E':
                    graph.addVertex(new Strand(false, position));
                    break;
                case 'e':
                    graph.addVertex(new Strand(true, position));
                    break;
                case 'H':
                    graph.addVertex(new Helix(false, position));
                    break;
                case 'h':
                    graph.addVertex(new Helix(true, position));
                    break;
                default:
                    graph.addVertex(new Term(position));
                    break;
            }
        }
    }
    
    private void setEdges(Graph graph, String estr) {
        int pos = 0;
        int last = 0;

        while (pos < estr.length()) { // for each char of the estr
            char ch = estr.charAt(pos);
            if (Character.isLetter(ch)) { // when you find a letter (A, P, R, L)
                String edgeStr = estr.substring(last, pos); // store the edge
                int Cpos = edgeStr.indexOf(':');
                
                int l = Integer.parseInt(edgeStr.substring(0, Cpos));
                int r = Integer.parseInt(edgeStr.substring(Cpos + 1, edgeStr.length()));
                
                Vertex left = graph.getVertex(l);
                Vertex right = graph.getVertex(r);
                
                switch (ch) {
                    case 'A':
                        graph.addEdge(new HBond(left, right, Edge.ANTIPARALLEL_HBOND));
                        break;

                    case 'P':
                        graph.addEdge(new HBond(left, right, Edge.PARALLEL_HBOND));
                        break;

                    case 'R':
                        graph.addEdge(new Chiral(left, right, Edge.RIGHT_CHIRAL));
                        break;

                    case 'L':
                        graph.addEdge(new Chiral(left, right, Edge.LEFT_CHIRAL));
                        break;

                    case 'Z':
                        graph.addEdge(new HBond(left, right, Edge.PARALLEL_HBOND));
                        graph.addEdge(new Chiral(left, right, Edge.RIGHT_CHIRAL));
                        break;

                    case 'X':
                        graph.addEdge(new HBond(left, right, Edge.PARALLEL_HBOND));
                        graph.addEdge(new Chiral(left, right, Edge.LEFT_CHIRAL));
                        break;
                }
                last = pos + 1;
            }
            ++pos;
        }
    }
    
    private void setHighlights(Graph graph, String highlights) {
        if (highlights == null || highlights.equals(""))
            return;
        // parse a string like '[1-3,2-4,3-5]'
        // remove enclosing []
        String bracketsOff = highlights.substring(1, highlights.length() - 1);
        StringTokenizer st = new StringTokenizer(bracketsOff, ",");
        
        Color highlightColor = Color.red;
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int dash = pair.indexOf("-");
            Integer vertexToHighlight = Integer.valueOf(pair.substring(dash + 1));
            Vertex vertex = graph.getVertex(vertexToHighlight.intValue());
            vertex.setColor(highlightColor);
        }
    }

}
