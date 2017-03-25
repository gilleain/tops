package tops.port.model;

public class Segment {
    public int startIndex;
    public int endIndex;
    public char startChain;
    public char endChain;
    
    public Segment(char startChain, int startIndex, char endChain, int endIndex) {
        this.startChain = startChain;
        this.startIndex = startIndex;
        this.endChain = endChain;
        this.endIndex = endIndex;
    }
    
    public boolean overlaps(Segment other) {

        if (this.startChain == this.endChain) {
            if (other.startChain == other.endChain) {
                if (other.startChain == this.startChain) {
                    if (other.startIndex >= this.startIndex && other.startIndex <= this.endIndex)
                        return true;
                    if (other.endIndex >= this.startIndex && other.endIndex <= this.endIndex)
                        return true;
                } else {
                    if (other.startChain == this.startChain) {
                        if (other.startIndex <= this.endIndex)
                            return true;
                    } else if (other.endChain == this.startChain) {
                        if (other.endIndex >= this.startIndex)
                            return true;
                    }
                }
            }
        } else {
            if (other.startChain == other.endChain) {
                if (this.startChain == other.startChain) {
                    if (this.startIndex <= other.endIndex)
                        return true;
                }
            } else if (this.endChain == other.startChain) {
                if (this.endIndex >= other.startIndex)
                    return true;
            } else {
                if (this.startChain == other.startChain)
                    return true;
                if (this.endChain == other.endChain)
                    return true;
                if (this.endChain == other.startChain) {
                    if (this.endIndex >= other.startIndex)
                        return true;
                }
                if (this.startChain == other.endChain) {
                    if (this.startIndex <= other.endIndex)
                        return true;
                }
            }
        }
        return false;
    }
    
}
