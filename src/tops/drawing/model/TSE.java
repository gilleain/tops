package tops.drawing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;



/**
 * @author maclean
 *
 */
public class TSE {
    
    private ArrayList elements;
    
    public TSE() {
        this.elements = new ArrayList();
    }
    
    public TSE(SSE initial) {
        this();
        this.elements.add(initial);
    }
    
    public TSE(SSE first, SSE second) {
        this();
        this.elements.add(first);
        this.elements.add(second);
    }
    
    public void add(SSE sse) {
        this.elements.add(sse);
    }
    
    public void add(SSE first, SSE second) {
        
        SSE leftEdgeSSE = (SSE) this.elements.get(0);
        SSE rightEdgeSSE = (SSE) this.elements.get(this.elements.size() - 1);
        if (first.equals(leftEdgeSSE)) {
            this.elements.add(0, second);
        } else if (first.equals(rightEdgeSSE)) {
            this.elements.add(second);
        } else if (second.equals(leftEdgeSSE)) {
            this.elements.add(0, first);
        } else if (second.equals(rightEdgeSSE)) {
            this.elements.add(first);
        }
    }
    
    public void merge(TSE other, SSE thisSSE, SSE otherSSE) {
        //  let this = [a, b] and other = [c, d]
        if (thisSSE == this.elements.get(0)) {
            
            if (otherSSE == other.elements.get(0)) {
                // [b, a, c, d]
                Collections.reverse(this.elements);
                this.elements.addAll(other.elements);
            } else {
                // [c, d, a, b] 
                this.elements.addAll(0, other.elements);
            }
        } else if (thisSSE == this.elements.get(this.size() - 1)) {
            if (otherSSE == other.elements.get(0)) {
                // [a, b, c, d]
                this.elements.addAll(other.elements);
            } else {
                // [a, b, d, c]
                Collections.reverse(other.elements);
                this.elements.addAll(other.elements);
            }
        }
    }
    
    public int distance(SSE a, SSE b) {
        int aIndex = this.elements.indexOf(a);
        int bIndex = this.elements.indexOf(b);
        return Math.abs(aIndex - bIndex);
    }
    
    public int orientation(SSE first, SSE second) {
        boolean firstFound = false;
        boolean secondFound = false;
        for (int j = 0; j < this.elements.size(); j++) {
            SSE sse = (SSE) this.elements.get(j);
            if (sse == first) {
                if (secondFound) {
                    return -1;      // second before first
                } else {
                    firstFound = true;
                }
            } else if (sse == second) {
                if (firstFound) {
                    return 1;       // first before second
                } else {
                    secondFound = true;
                }
            }

        }
        
        return 0;   // not found?
    }

    public boolean contains(int sseNumber) {
        Iterator itr = this.elements.iterator();
        while (itr.hasNext()) {
            SSE strand = (SSE) itr.next();
            if (strand.isNumber(sseNumber)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(SSE a, SSE b) {
        return this.contains(a.getSSENumber()) && this.contains(b.getSSENumber());
    }
    
    public ArrayList getSSEs() {
        return this.elements;
    }
    
    public ArrayList getElements() {
        return this.elements;
    }
    
    public SSE getSSE(int i) {
        return (SSE) this.elements.get(i);
    }
    
    public SSE getElement(int i) {
        return (SSE) this.elements.get(i);
    }
    
    public int size() {
        return this.elements.size();
    }
   
}
