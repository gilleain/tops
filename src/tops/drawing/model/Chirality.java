package tops.drawing.model;



/**
 * @author maclean
 *
 */
public class Chirality {

    private SSE first;
    private SSE second;
    private SSE third;
    private char type;
    
    /**
     * @param first
     * @param second
     * @param third
     */
    public Chirality(SSE first, SSE second, SSE third) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.type = 'R';
    }
    
    /**
     * @param first
     * @param second
     * @param third
     * @param type
     */
    public Chirality(SSE first, SSE second, SSE third, char type) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.type = type;
    }
    
    public boolean hasSSES(SSE a, SSE b) {
        return this.first == a && this.third == b;
    }
    
    public SSE getFirst() {
        return this.first;
    }
    
    public SSE getSecond() {
        return this.second;
    }
    
    public SSE getThird() {
        return this.third;
    }
    
    public char getType() {
        return this.type;
    }
}
