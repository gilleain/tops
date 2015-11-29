package tops.translation.model;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Plane {

    private double distanceFromZero;

    private Vector3d normal;

    public Plane(Point3d point, Vector3d normal) {
        this.normal = normal;
        this.normal.normalize();
        this.distanceFromZero = this.normal.dot(new Vector3d(point));
    }

    public Plane(Point3d a, Point3d b, Point3d c) {
        Vector3d ab = new Vector3d();
        ab.sub(b, a);

        Vector3d bc = new Vector3d();
        bc.sub(c, b);

        this.normal = new Vector3d();
        this.normal.cross(ab, bc);
        this.distanceFromZero = this.normal.dot(new Vector3d(a));
    }

    public Plane(Axis a) {
        this(a.getCentroid(), a.getAxisVector());
    }

    public Vector3d getNormal() {
        return this.normal;
    }

    // warning : need to wrap this is a call to Math.abs
    public double distance(Point3d point) {
        return this.normal.dot(new Vector3d(point)) - this.distanceFromZero;
    }

    public Point3d project(Point3d point) {
        Point3d projection = new Point3d(point);
        Vector3d subtraction = new Vector3d(this.normal);

        double distanceFromPlane = this.distance(point);
        subtraction.scale(distanceFromPlane);

        projection.sub(subtraction);
        return projection;
    }

    @Override
    public String toString() {
        String normalS = String.format("(%.2f, %.2f, %.2f)", this.normal.x,
                this.normal.y, this.normal.z);
        return String.format("%s %.2f", normalS, this.distanceFromZero);
    }
}
