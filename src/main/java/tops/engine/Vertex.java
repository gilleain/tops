package tops.engine;

public class Vertex implements Comparable<Vertex> {

    private int p;

    private Vertex matchpos;

    private int[] Vindex;

    private char type;
    
    public Vertex(char t, int p) {
        this.p = p;
        this.matchpos = null;
        this.type = t;
        this.Vindex = new int[8];
    }
    
    /*
     * Copy constructor.
     */
    public Vertex(Vertex other) {
    	this.p = other.p;
    	this.matchpos = null;
    	this.type = other.type;
    	this.Vindex = other.Vindex;
    }

    public int compareTo(Vertex o) {
        int otherPosition = o.getPos();
        return (new Integer(this.p)).compareTo(new Integer(otherPosition));
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
                this.Vindex[0]++;
                break;
            case 'R':
                this.Vindex[1]++;
                break;
            case 'P':
                this.Vindex[2]++;
                break;
            case 'A':
                this.Vindex[3]++;
                break;
            case 'l':
                this.Vindex[4]++;
                break;
            case 'r':
                this.Vindex[5]++;
                break;
            case 'p':
                this.Vindex[6]++;
                break;
            case 'a':
                this.Vindex[7]++;
                break;
            case 'X':
                this.Vindex[0]++;
                this.Vindex[2]++;
                break;
            case 'Z':
                this.Vindex[1]++;
                this.Vindex[2]++;
                break;
            case 'x':
                this.Vindex[4]++;
                this.Vindex[6]++;
                break;
            case 'z':
                this.Vindex[5]++;
                this.Vindex[6]++;
                break;
        }

    }

    public boolean vertexMatches(Vertex vt) {
    	
    	for (int i = 0; i < 8; i++) {
    		if (this.Vindex[i] > vt.Vindex[i]) {
    			return false;
    		}
    	}
//    	System.out.println(p + "," + type + " matches " + vt.p + "," + vt.type);

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
        return new String(this.type + ":" + this.p);
    }

}

