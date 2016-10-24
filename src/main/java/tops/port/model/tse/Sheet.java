package tops.port.model.tse;

import java.util.List;

import tops.port.model.FixedType;
import tops.port.model.SSE;

public class Sheet extends BaseTSE {

    public Sheet() {
        super(FixedType.SHEET); // hmmm
    }
    
    public Sheet(List<SSE> component) {
        this();
        for (SSE sse : component) {
            add(sse);
        }
    }
    
    public int span() {
        return getElements().size();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        int index = 0;
        for (SSE sse : getElements()) {
            sb.append(sse.getSymbolNumber());
            if (index < size() - 1) {
                sb.append("-");
            } else {
                sb.append("]");
            }
            index++;
        }
        return sb.toString();
    }

}
