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

}
