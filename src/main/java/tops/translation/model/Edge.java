package tops.translation.model;


public class Edge implements Comparable<Edge> {
    private BackboneSegment first;
    private BackboneSegment second;
    private char type;

    public Edge(BackboneSegment first, BackboneSegment second, char type) {
        this.first = first;
        this.second = second;
        this.type = type;
    }

    public int compareTo(Edge other) {
        int firstCompare = this.first.compareTo((other).first);
        if (firstCompare == 0) {
            return this.second.compareTo((other).second);
        } else {
            return firstCompare;
        }
    }

    public boolean equals(Object other) {
        if (other instanceof Edge) {
            Edge otherEdge = (Edge) other;
            return this.first == otherEdge.first && this.second == otherEdge.second;
        }
        return false;
    }
    
    public int hashCode() {
        return this.first.hashCode() * this.second.hashCode() * type;
    }

    public void mergeWith(Edge chiral) {
        if (chiral.type == 'R') {
            this.type = 'Z';
        } else {
            this.type = 'X';
        }
    }

    public boolean containedIn(Domain domain) {
        return domain.contains(this.first) && domain.contains(this.second);
    }

    public char getType() { 
        return this.type;
    }

    public String toString() {
        return this.first.getNumber() + ":" + this.second.getNumber() + this.type;
    }        
}
