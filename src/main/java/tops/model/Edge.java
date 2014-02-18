package tops.model;

public class Edge {

    private String firstVertex;

    private String secondVertex;

    private String type;

    public Edge(String firstVertex, String secondVertex, String type) {
        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.firstVertex + ":" + this.secondVertex + this.type;
    }

}
