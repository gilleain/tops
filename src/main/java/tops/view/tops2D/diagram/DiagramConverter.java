package tops.view.tops2D.diagram;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import tops.drawing.Diagram;
import tops.model.Chain;
import tops.model.HBondSet;
import tops.model.Protein;
import tops.model.SSE;

/**
 * Converts to and from {@link Diagram} objects:
 * 
 * From SSEs to vertices and hBond ladders and chiralities as edges.
 *  
 * @author maclean
 *
 */
public class DiagramConverter {
    
    public String toTopsString(Graph graph) {
        StringBuilder builder = new StringBuilder();
        builder.append("Name"); /// XXX TODO
        builder.append(" N");
        for (int index = 0; index < graph.numberOfVertices(); index++) {
            Vertex vertex = graph.getVertex(index); 
            if (vertex instanceof Strand) {
                if (((Strand) vertex).isDown()) {
                    builder.append("e");
                } else {
                    builder.append("E");
                }
            } else if (vertex instanceof Helix) {
                if (((Helix) vertex).isDown()) {
                    builder.append("h");
                } else {
                    builder.append("H");
                }
            }
        }
        builder.append("C ");
        List<Edge> edges = graph.getEdges();
        Collections.sort(edges, new Comparator<Edge>() {

            @Override
            public int compare(Edge o1, Edge o2) {
                int o1l = o1.left.getPos();
                int o1r = o1.right.getPos();
                int o2l = o2.left.getPos();
                int o2r = o2.right.getPos();
                if (o1l == o2l) {
                    if (o1r < o2r) {
                        return -1;
                    } else if (o1r > o2r) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    if (o1l < o2l) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            
        });
        for (Edge edge : edges) {
            builder.append(edge.left.getPos() + 1)
                   .append(":")
                   .append(edge.right.getPos() + 1)
                   .append(edge.type.getCode())
                   .append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
    
    public Graph toDiagram(Protein protein) {
        Graph graph = new Graph();
        for (Chain chain : protein.getChains()) {
            int position = 0;
            Map<SSE, Integer> sseMap = new HashMap<SSE, Integer>();
            for (SSE sse : chain.getSSES()) {
                Vertex vertex = toVertex(sse, position);
                if (vertex != null) {
                    graph.addVertex(vertex);
                    System.out.println("adding vertex " + vertex.getClass().getSimpleName() + " at position "+  position);
                    sseMap.put(sse, position);
                    position++;
                }
            }
            for (HBondSet hBondSet : chain.getHBondSets()) {
                SSE start = hBondSet.getStart();
                SSE end = hBondSet.getEnd();
                Integer startPos = sseMap.get(start);
                Integer endPos = sseMap.get(end);
                boolean isHH = startPos == endPos && isHelix(start) && isHelix(end); 
                if (startPos != null && endPos != null && !isHH) {
                    Vertex left = graph.getVertex(startPos);
                    Vertex right = graph.getVertex(endPos);
                    // XXX type...
                    System.out.println("Adding hbond between " 
                            + startPos + " (" + start + ") and "
                            + endPos + " (" + end + ")");
                    graph.addEdge(new HBond(left, right, Edge.Type.ANTIPARALLEL_HBOND));
                }
            }
        }
        return graph;
    }
    
    private boolean isHelix(SSE sse) {
        return sse.getType() == SSE.Type.ALPHA_HELIX
                || sse.getType() == SSE.Type.HELIX_310
                || sse.getType() == SSE.Type.PI_HELIX;
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
    
    public Graph toDiagram(String vertexString, String edgeString, String highlightString) {
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
                        graph.addEdge(new HBond(left, right, Edge.Type.ANTIPARALLEL_HBOND));
                        break;

                    case 'P':
                        graph.addEdge(new HBond(left, right, Edge.Type.PARALLEL_HBOND));
                        break;

                    case 'R':
                        graph.addEdge(new Chiral(left, right, Edge.Type.RIGHT_CHIRAL));
                        break;

                    case 'L':
                        graph.addEdge(new Chiral(left, right, Edge.Type.LEFT_CHIRAL));
                        break;

                    case 'Z':
                        graph.addEdge(new HBond(left, right, Edge.Type.PARALLEL_HBOND));
                        graph.addEdge(new Chiral(left, right, Edge.Type.RIGHT_CHIRAL));
                        break;

                    case 'X':
                        graph.addEdge(new HBond(left, right, Edge.Type.PARALLEL_HBOND));
                        graph.addEdge(new Chiral(left, right, Edge.Type.LEFT_CHIRAL));
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
