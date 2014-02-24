package tops.view.tops3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

import javax.vecmath.Point3d;

public class Helix extends SSE {

    private static final double length = 1.5;

    // private static final double radius = 0.5;

    public Helix() {
        super();
        this.setLabel("H");
    }

    public Helix(int order, int orientation, Point3d center) {
        super(order, orientation, Helix.length, center);
        this.setLabel("H");
    }

    @Override
    public Shape3D getShape(Appearance app) {
        return Cylinder.createCylinder(this.orientation, this.center, app);
    }

    @Override
    public Point3d getCenter() {
        return this.center;
    }

    public void setCenter(Point3d center) {
        System.out.println("setting center to " + center);
        this.center = center;
    }
}
