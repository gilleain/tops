package tops.port.model.tse;

import tops.port.model.FixedType;

public class Sandwich extends BaseTSE {
    
    private BaseTSE sheetA;
    
    private BaseTSE sheetB;

    public Sandwich() {
        super(FixedType.SANDWICH);
    }

}
