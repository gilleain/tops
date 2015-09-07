package tops.drawing.model;


/**
 * @author maclean
 *
 */
public abstract class SSE {
    
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
    
    public String getStringRange() {
        return String.format("%s-%s", start, end);
    }

    private int start;
    
    private int end;
    
    public abstract boolean isUp();
    
    public boolean isParallelTo(SSE other) {
        return (this.isUp() && other.isUp()) || (!this.isUp() && !other.isUp());
    }
    
    public abstract int getSSENumber();
    
    public abstract boolean isNumber(int n);
    
    public abstract boolean equals(SSE other);

}
