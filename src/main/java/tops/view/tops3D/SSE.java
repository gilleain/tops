package tops.view.tops3D;

import javax.vecmath.Point3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Shape3D;

//the base class for secondary structure elements. Extensions of this class are added to a Structure.

public abstract class SSE {

    public static final int UP = 0;

    public static final int DOWN = 1;

    protected int orientation;

    private String label;

    private int chainOrder;

    private Point3d nTerm;

    private Point3d cTerm;

    protected Point3d center;

    public SSE() {
        this.label = new String();
        this.chainOrder = 0;
    }

    public SSE(int order, int orientation, double length, Point3d center) {
        this();
        this.setOrder(order);
        this.setOrientation(orientation);
        this.nTerm = new Point3d(center);
        this.cTerm = new Point3d(center);
        this.center = center;
        double halfLength = length / 2;
        Point3d halfLengthPoint = new Point3d(0.0, halfLength, 0.0);
        if (orientation == SSE.UP) {
            this.nTerm.sub(halfLengthPoint);
            this.cTerm.add(halfLengthPoint);
        } else { // flipped
            this.nTerm.add(halfLengthPoint);
            this.cTerm.sub(halfLengthPoint);
        }
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setOrder(int order) {
        this.chainOrder = order;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public abstract Point3d getCenter();

    public String getLabel() {
        return this.label;
    }

    public int getOrder() {
        return this.chainOrder;
    }

    public boolean clashesWith(SSE other) {
        return false;
    } // TODO!

    public TransformGroup getTransformGroup(Appearance app) {
        TransformGroup sseGroup = new TransformGroup();

        // first, add a rotation based on the orientation of the strand
        /*
         * Transform3D sseRotateTransform = new Transform3D(); if (orientation ==
         * UP) { sseRotateTransform.rotX(Math.toRadians(0.0)); } //isn't this
         * pointless? else if (orientation == DOWN) {
         * sseRotateTransform.rotX(Math.toRadians(180.0)); } TransformGroup
         * sseRotateGroup = new TransformGroup(sseRotateTransform);
         * sseGroup.addChild(sseRotateGroup);
         */
        // next add a translation based on the Point3D center
        /*
         * Transform3D sseTranslateTransform = new Transform3D(); Vector3d t =
         * new Vector3d(); this.getCenter().get(t); //t.negate(); //!!!what is
         * going on!? sseTranslateTransform.setTranslation(t); TransformGroup
         * sseTranslateGroup = new TransformGroup(sseTranslateTransform);
         * sseRotateGroup.addChild(sseTranslateGroup);
         */
        // now, add the actual Shape3D, with associated geometry and appearance
        Shape3D shape = this.getShape(app);
        // sseTranslateGroup.addChild(shape);
        // sseRotateGroup.addChild(shape);
        sseGroup.addChild(shape); // TEMPORARY. testing

        return sseGroup;
    }

    public abstract Shape3D getShape(Appearance app);

    public Point3d getTopFixture() {
        return this.cTerm;
    }

    public Point3d getBottomFixture() {
        return this.nTerm;
    }

    // method to get the connection point /length/ past the cTerminus
    public Point3d getPointBeyond(double connectionLength) {
        Point3d connectionPoint = new Point3d();
        double length = this.getLength();
        double fraction = (length + connectionLength) / length;
        connectionPoint.interpolate(this.nTerm, this.cTerm, fraction);
        // connectionPoint.add(center); //shift to proper position!
        return connectionPoint;
    }

    // method to get the connection point /length/ before the nTerminus
    public Point3d getPointBefore(double connectionLength) {
        Point3d connectionPoint = new Point3d();
        double length = this.getLength();
        double fraction = (length + connectionLength) / length;
        connectionPoint.interpolate(this.cTerm, this.nTerm, fraction);
        // connectionPoint.add(center); //shift to proper position!
        return connectionPoint;
    }

    public double getLength() {
        return this.nTerm.distance(this.cTerm);
    }

    @Override
    public String toString() {
        return new String(this.getOrder() + ":" + this.getOrientation() + " ["
                + this.getCenter() + "], [" + this.nTerm + "], [" + this.cTerm + "]\n");
    }
}
