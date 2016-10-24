package tops.port.model.tse;

import tops.port.model.FixedType;

public class Sandwich extends BaseTSE {
    
    private Sheet sheetA;
    
    private Sheet sheetB;

    public Sandwich(Sheet sheetA, Sheet sheetB) {
        super(FixedType.SANDWICH);
        this.sheetA = sheetA;
        this.sheetB = sheetB;
    }

}
