package tops.translation;

public class Edge implements Comparable {

    private BackboneSegment first;

    private BackboneSegment second;

    private char type;

    public Edge(BackboneSegment first, BackboneSegment second, char type) {
        this.first = first;
        this.second = second;
        this.type = type;
    }

    public int compareTo(Object other) {
        int firstCompare = this.first.compareTo(((Edge) other).first);
        if (firstCompare == 0) {
            return this.second.compareTo(((Edge) other).second);
        } else {
            return firstCompare;
        }
    }

    @Override
    public boolean equals(Object other) {
        return this.first == ((Edge) other).first
                && this.second == ((Edge) other).second;
    }

    public void mergeWith(Edge chiral) {
        if (chiral.type == 'R') {
            this.type = 'Z';
        } else {
            this.type = 'X';
        }
    }

    public char getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.first.getNumber() + ":" + this.second.getNumber()
                + this.type;
    }
}
