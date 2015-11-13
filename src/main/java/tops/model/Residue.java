package tops.model;

import javax.vecmath.Point3d;

public class Residue {
    
    private String residueName;
    
    private int residueNumber;
    
    private Point3d caPosition;
    
    public Residue(String residueName, int residueNumber, Point3d caPosition) {
        this.residueNumber = residueNumber;
        this.residueName = residueName;
        this.caPosition = caPosition;
    }

    public String getResidueName() {
        return residueName;
    }

    public int getResidueNumber() {
        return residueNumber;
    }

    public Point3d getCaPosition() {
        return caPosition;
    }

}
