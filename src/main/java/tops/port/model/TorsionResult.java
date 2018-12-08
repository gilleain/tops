package tops.port.model;

import javax.vecmath.Vector3d;

public class TorsionResult {
    
    private Vector3d pk;
    private Vector3d pj;
    private double  sk;
    private double  sj;
    private double torsion;
    
    public TorsionResult() {
        this.setPk(null);
        this.setPj(null);
        this.setSk(0);
        this.setSj(0);
        this.setTorsion(0);
    }
    
    public TorsionResult(Vector3d pk, Vector3d pj, double sk, double sj, double torsion) {
        super();
        this.setPk(pk);
        this.setPj(pj);
        this.setSk(sk);
        this.setSj(sj);
        this.setTorsion(torsion);
    }

    public Vector3d getPk() {
        return pk;
    }

    public void setPk(Vector3d pk) {
        this.pk = pk;
    }

    public Vector3d getPj() {
        return pj;
    }

    public void setPj(Vector3d pj) {
        this.pj = pj;
    }

    public double getSk() {
        return sk;
    }

    public void setSk(double sk) {
        this.sk = sk;
    }

    public double getSj() {
        return sj;
    }

    public void setSj(double sj) {
        this.sj = sj;
    }

    public double getTorsion() {
        return torsion;
    }

    public void setTorsion(double torsion) {
        this.torsion = torsion;
    }

}
