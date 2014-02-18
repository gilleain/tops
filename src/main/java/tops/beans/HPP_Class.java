package tops.beans;

public class HPP_Class {

    public HPP_Class() {
    }

    private String Rotation;

    private float Propensity;

    private String RelOrientation;

    private float GlobalAngleMax;

    private float GlobalAngleMin;

    private int Class_ID;

    public String getRotation() {
        return this.Rotation;
    }

    public float getPropensity() {
        return this.Propensity;
    }

    public String getRelOrientation() {
        return this.RelOrientation;
    }

    public float getGlobalAngleMax() {
        return this.GlobalAngleMax;
    }

    public float getGlobalAngleMin() {
        return this.GlobalAngleMin;
    }

    public int getClass_ID() {
        return this.Class_ID;
    }

    public void setRotation(String Rotation) {
        this.Rotation = Rotation;
    }

    public void setPropensity(float Propensity) {
        this.Propensity = Propensity;
    }

    public void setRelOrientation(String RelOrientation) {
        this.RelOrientation = RelOrientation;
    }

    public void setGlobalAngleMax(float GlobalAngleMax) {
        this.GlobalAngleMax = GlobalAngleMax;
    }

    public void setGlobalAngleMin(float GlobalAngleMin) {
        this.GlobalAngleMin = GlobalAngleMin;
    }

    public void setClass_ID(int Class_ID) {
        this.Class_ID = Class_ID;
    }
}
