package python.model;

import javax.vecmath.Vector3d;

public class TorsionResult {
    
    public Vector3d pk;
    public Vector3d pj;
    public double  sk;
    public double  sj;
    public double torsion;
    
    public TorsionResult() {
        this.pk = null;
        this.pj = null;
        this.sk = 0;
        this.sj = 0;
        this.torsion = 0;
    }
    
    public TorsionResult(Vector3d pk, Vector3d pj, double sk, double sj, double torsion) {
        super();
        this.pk = pk;
        this.pj = pj;
        this.sk = sk;
        this.sj = sj;
        this.torsion = torsion;
    }

}
