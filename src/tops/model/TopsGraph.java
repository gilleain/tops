package tops.model;

public class TopsGraph {

    private String name;

    private Vertex[] vertices;

    private Edge[] edges;

//    private TopsGraph[] subGraphs;

    public TopsGraph() {
        this.name = null;
        this.vertices = null;
        this.edges = null;
//        this.subGraphs = null;
    }

    public TopsGraph(String name, Vertex[] vertices, Edge[] edges) {
        this.name = name;
        this.vertices = vertices;
        this.edges = edges;
        this.findConnectedSubComponents();
    }

    public void findConnectedSubComponents() {

    }

    public int getNumberOfSheets() {
        return 0;
    }

    @Override
    public String toString() {
        return TopsParser.toString(this.name, this.vertices, this.edges);
    }

}
