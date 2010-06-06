package tops.drawing.model;


/**
 * @author maclean
 *
 */
public abstract class SSE {
    
    public abstract boolean isUp();
    
    public boolean isParallelTo(SSE other) {
        return (this.isUp() && other.isUp()) || (!this.isUp() && !other.isUp());
    }
    
    public abstract int getSSENumber();
    
    public abstract boolean isNumber(int n);
    
    public abstract boolean equals(SSE other);

}
