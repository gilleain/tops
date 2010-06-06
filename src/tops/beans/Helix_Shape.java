package tops.beans;

public class Helix_Shape {

    public Helix_Shape() {
    }

    private int SSE_No;

    private String Chain_ID;

    private float MaxBendAngle;

    private String Geometry;

    public int getSSE_No() {
        return this.SSE_No;
    }

    public String getChain_ID() {
        return this.Chain_ID;
    }

    public float getMaxBendAngle() {
        return this.MaxBendAngle;
    }

    public String getGeometry() {
        return this.Geometry;
    }

    public void setSSE_No(int SSE_No) {
        this.SSE_No = SSE_No;
    }

    public void setChain_ID(String Chain_ID) {
        this.Chain_ID = Chain_ID;
    }

    public void setMaxBendAngle(float MaxBendAngle) {
        this.MaxBendAngle = MaxBendAngle;
    }

    public void setGeometry(String Geometry) {
        this.Geometry = Geometry;
    }
}
