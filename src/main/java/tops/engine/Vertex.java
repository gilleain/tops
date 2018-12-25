package tops.engine;

public class Vertex implements Comparable<Vertex> {

    private int p;

    private Vertex matchpos;

    private int[] vIndex;

    private char type;
    
    public Vertex(char t, int p) {
        this.p = p;
        this.matchpos = null;
        this.type = t;
        this.vIndex = new int[8];
    }
    
    /*
     * Copy constructor.
     */
    public Vertex(Vertex other) {
    	this.p = other.p;
    	this.matchpos = null;
    	this.type = other.type;
    	this.vIndex = other.vIndex;
    }

    public int compareTo(Vertex o) {
        int otherPosition = o.getPos();
        return Integer.compare(p, otherPosition);
    }

    public char getType() {
        return this.type;
    }

    public void setPos(int p) {
        this.p = p;
    }

    public int getPos() {
        return this.p;
    }

    public int getMatch() {
        return (this.matchpos != null) ? this.matchpos.getPos() : 0;
    }

    public void setMatch(Vertex vtomatch) {
        this.matchpos = vtomatch;
    }
    
    public void resetMatch() {
        this.matchpos = null;
    }
    
    public boolean matchedTo(Vertex other) {
    	return this.matchpos == null || this.matchpos.getPos() == other.getPos();
    }

    public void setIndex(char t) {
        switch (t) {
            case 'L':
                this.vIndex[0]++;
                break;
            case 'R':
                this.vIndex[1]++;
                break;
            case 'P':
                this.vIndex[2]++;
                break;
            case 'A':
                this.vIndex[3]++;
                break;
            case 'l':
                this.vIndex[4]++;
                break;
            case 'r':
                this.vIndex[5]++;
                break;
            case 'p':
                this.vIndex[6]++;
                break;
            case 'a':
                this.vIndex[7]++;
                break;
            case 'X':
                this.vIndex[0]++;
                this.vIndex[2]++;
                break;
            case 'Z':
                this.vIndex[1]++;
                this.vIndex[2]++;
                break;
            case 'x':
                this.vIndex[4]++;
                this.vIndex[6]++;
                break;
            case 'z':
                this.vIndex[5]++;
                this.vIndex[6]++;
                break;
            default:
                break;
        }

    }

    public boolean vertexMatches(Vertex vt) {
    	
    	for (int i = 0; i < 8; i++) {
    		if (this.vIndex[i] > vt.vIndex[i]) {
    			return false;
    		}
    	}
        // make sure the TYPES match too!
        return (this.type == vt.type);

    }

    public void flip() {
        // is 'a' or greater?
        if (this.type >= 97) {
            // then shift down (IE: cApItAlIsE)
            this.type -= 32;
        } else {
            // shift up (marginalise? 'downcase' or 'regionalise' or 'Uncap')
            this.type += 32;
        }
    }

    @Override
    public String toString() {
        return this.type + ":" + this.p;
    }

}

