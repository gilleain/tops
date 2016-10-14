package tops.port.model.tse;

import java.util.List;

import tops.port.model.FixedType;
import tops.port.model.SSE;

public class Barrel extends BaseTSE {

    public Barrel() {
        super(FixedType.BARREL);
    }
    
    public Barrel(List<SSE> sses) {
        this();
        for (SSE sse : sses) {
            add(sse);
        }
    }

}
