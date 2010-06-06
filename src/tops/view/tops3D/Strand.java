package tops.view.tops3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

import javax.vecmath.Point3d;

public class Strand extends SSE {

    public Strand() {
        super();
        this.setLabel("E");
    }

    public Strand(int order, int orientation, Point3d center) {
        super(order, orientation, Arrow.getLength(), center);
        this.setLabel("E");
    }

    @Override
    public Shape3D getShape(Appearance app) {
        return Arrow.createArrow(this.orientation, this.center, app);
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
