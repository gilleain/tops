package tops.port.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tertiary structure element - sheet, barrel, sandwich, and so on.
 * 
 * @author maclean
 *
 */
public class TSE {
    
    private FixedType type; // XXX refactor, but useful for now
    
    private List<SSE> elements;
    
    public TSE(FixedType type) {
        this.type = type;
        this.elements = new ArrayList<SSE>();
    }

    public FixedType getType() {
        return type;
    }

    public void setType(FixedType type) {
        this.type = type;
    }

    public List<SSE> getElements() {
        return elements;
    }

    public void setElements(List<SSE> elements) {
        this.elements = elements;
    }

}
