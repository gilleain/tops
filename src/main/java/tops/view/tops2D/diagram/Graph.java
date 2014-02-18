package tops.view.tops2D.diagram;
import java.awt.Shape;
import java.awt.Color;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class Graph {

    private ArrayList<SSE> sses;
    private ArrayList<Edge> edges;
    private boolean isLaidOut;

    public Graph() {
        this.sses = new ArrayList<SSE>();
        this.edges = new ArrayList<Edge>();
        this.isLaidOut = false;
    }
    
    public Graph(String vertexString, String edgeString) {
        this();
        this.setVertices(vertexString);
        this.setEdges(edgeString);
    }
    
    public Graph(String vertexString, String edgeString, String highlightString) {
        this();
        this.setVertices(vertexString);
        this.setEdges(edgeString);
        this.setHighlights(highlightString);
    }
    
    public boolean needsLayout() {
        return !this.isLaidOut;
    }
    
    public void layout(double axis, int w) {
        int numberOfVertices = this.sses.size();
        int centerSeparation = w / numberOfVertices;
        int boxWidth = centerSeparation / 2;

        for (int i = 0; i < numberOfVertices; ++i) {
            int xpos = (i * centerSeparation) + (boxWidth / 2);
            Rectangle2D box = 
                new Rectangle2D.Double((double) xpos, axis, boxWidth, centerSeparation);
            SSE vertex = this.getVertex(i);
            vertex.setBounds(box);
        }
        this.isLaidOut = true;
    }

    public void setVertices(String vertices) {
        int numberOfVertices = vertices.length();

        for (int i = 0; i < numberOfVertices; ++i) {
            char v = vertices.charAt(i);
            switch (v) {
                case 'E':
                    this.sses.add(new Strand(false, i));
                    break;
                case 'e':
                    this.sses.add(new Strand(true, i));
                    break;
                case 'H':
                    this.sses.add(new Helix(false, i));
                    break;
                case 'h':
                    this.sses.add(new Helix(true, i));
                    break;
                default:
                    this.sses.add(new Term(false, i));
                    break;
            }
        }
    }

    public void setEdges(String estr) {
        int pos = 0;
        int last = 0;

        while (pos < estr.length()) { // for each char of the estr
            char ch = estr.charAt(pos);
            if (Character.isLetter(ch)) { // when you find a letter (A, P, R, L)
                String edgeStr = estr.substring(last, pos); // store the edge
                int Cpos = edgeStr.indexOf(':');
                
                int l = Integer.parseInt(edgeStr.substring(0, Cpos));
                int r = Integer.parseInt(edgeStr.substring(Cpos + 1, edgeStr.length()));
                
                SSE left = this.getVertex(l);
                SSE right = this.getVertex(r);
                
                switch (ch) {
                    case 'A':
                        this.edges.add(new HBond(left, right, Edge.ANTIPARALLEL_HBOND));
                        break;

                    case 'P':
                        this.edges.add(new HBond(left, right, Edge.PARALLEL_HBOND));
                        break;

                    case 'R':
                        this.edges.add(new Chiral(left, right, Edge.RIGHT_CHIRAL));
                        break;

                    case 'L':
                        this.edges.add(new Chiral(left, right, Edge.LEFT_CHIRAL));
                        break;

                    case 'Z':
                        this.edges.add(new HBond(left, right, Edge.PARALLEL_HBOND));
                        this.edges.add(new Chiral(left, right, Edge.RIGHT_CHIRAL));
                        break;

                    case 'X':
                        this.edges.add(new HBond(left, right, Edge.PARALLEL_HBOND));
                        this.edges.add(new Chiral(left, right, Edge.LEFT_CHIRAL));
                        break;
                }
                last = pos + 1;
            }
            ++pos;
        }
    }
    
    public void setHighlights(String highlights) {
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
            SSE vertex = (SSE) this.sses.get(vertexToHighlight.intValue());
            vertex.setColor(highlightColor);
        }
    }

    public int numberOfVertices() {
        return this.sses.size();
    }
    
    public SSE getVertex(int i) {
        return (SSE) this.sses.get(i);
    }

    public int numberOfEdges() {
        return this.edges.size();
    }
    
    public Edge getEdge(int i) {
        return (Edge) this.edges.get(i);
    }

    public boolean isEmpty() {
        return this.sses.isEmpty();
    }
    
    public String toPostscript(int w, int h, double axis) {
        StringBuilder ps = new StringBuilder();
        ps.append("%!PS-Adobe-3.0 EPSF-3.0\n");
        //ps.append("%!PS-Adobe-3.0\n");
        ps.append("%%Creator : tops.view.tops2D.diagram.DiagramDrawer\n");
        int x1 = 0;
        int y1 = 0;
        int x2 = w + x1;
        int y2 = h + y1;
        ps.append("%%BoundingBox: " + x1 + " " + y1 + " " + x2 + " " + y2 + "\n");
        ps.append("%%EndComments\n");
        ps.append("%%EndProlog\n");

        ps.append("1 -1 scale\n");    // flip the Y-axis
        ps.append("0 -" + h + " translate\n");    // translate back up

        int numberOfObjects = this.sses.size() + this.edges.size();
        Shape[] shapes = new Shape[numberOfObjects];
        Color[] colors = new Color[numberOfObjects];
        int index = 0;
        Iterator<SSE> sseItr = this.sses.iterator();

        while (sseItr.hasNext()) {
            SSE sse = (SSE) (sseItr.next());
            shapes[index] = sse.getShape();
            colors[index] = sse.getColor();
            index++;
        }

        Iterator<Edge> j = this.edges.iterator();
        while (j.hasNext()) {
            Edge edge = (Edge) (j.next());
            shapes[index] = edge.getShape(axis);
            colors[index] = edge.getColor();
            index++;
        }
        
        for (int i = 0; i < shapes.length; i++) {
            PathIterator pathIterator = shapes[i].getPathIterator(new AffineTransform());
            ps.append("newpath\n");
            while (!pathIterator.isDone()) {
                float[] coords = new float[6]; 
                int segmentType = pathIterator.currentSegment(coords);
                switch (segmentType) {
                    case PathIterator.SEG_MOVETO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]);
                        ps.append(" moveto\n");
                        break;
                    case PathIterator.SEG_LINETO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]);
                        ps.append(" lineto\n");
                        break;
                    case PathIterator.SEG_QUADTO:
                        ps.append("quadto\n");
                        break;
                    case PathIterator.SEG_CUBICTO:
                        ps.append(coords[0]).append(" ");
                        ps.append(coords[1]).append(" ");
                        ps.append(coords[2]).append(" ");
                        ps.append(coords[3]).append(" ");
                        ps.append(coords[4]).append(" ");
                        ps.append(coords[5]);
                        ps.append(" curveto\n");
                        break;
                    case PathIterator.SEG_CLOSE:
                        ps.append("closepath\n");
                        break;
                    default: break;
                }
                pathIterator.next();
            }
            //ps.append("1 setlinewidth\n");
            float[] rgb = colors[i].getRGBComponents(null);
            ps.append(rgb[0]).append(" ");
            ps.append(rgb[1]).append(" ");
            ps.append(rgb[2]).append(" setrgbcolor\n");
            ps.append("stroke\n");
        }
        ps.append("%EndDocument\n");
        ps.append("%EOF\n");

        return ps.toString();
    }
}
