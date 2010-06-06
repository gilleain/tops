package tops.translation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import cern.colt.matrix.linalg.EigenvalueDecomposition;

import cern.jet.math.Functions;

public class Geometer {

    public static Plane leastSquarePlane(ArrayList points) {
        Axis a = Geometer.leastSquareAxis(points);
        // System.out.println("Axis : " + a + " for " + points.size() + "
        // points");
        return new Plane(a);
    }

    // find the chirality of b with respect to a and c and the UP vector v
    // it is assumed that a->c is the FORWARD direction
    // this could be done with 4 points instead and determinants ... I think
    // this is easier (no matrices)
    public static char chirality(Point3d a, Point3d b, Point3d c, Vector3d v) {
        /*
         * // get the FORWARD direction a->c Vector3d ac = new Vector3d();
         * ac.sub(c, a);
         *  // find the projection of b onto a->c Vector3d ob = new Vector3d(b);
         * double acDotob = ac.dot(ob); Point3d projectionOfB = new Point3d(ac);
         * projectionOfB.scale(acDotob);
         *  // make the vector projection(b, ac)->b Vector3d bb = new
         * Vector3d(); bb.sub(b, projectionOfB);
         *  // find the cross product of ac and proj(b, ac)->b Vector3d
         * crossProduct = new Vector3d(); crossProduct.cross(ac, bb);
         *  // finally, determine whether this cross product is UP or DOWN
         * double angle = Math.toDegrees(crossProduct.angle(v));
         * System.out.println("Angle of " + crossProduct + " with " + v + " = " +
         * angle); if (angle < 90.0) { return 'R'; } else { return 'L'; }
         */
        // get the FORWARD direction a->c
        Vector3d ac = new Vector3d();
        ac.sub(c, a);

        // get the plane normal
        Vector3d normal = new Vector3d();
        normal.cross(ac, v);

        // make a plane and find the distance
        Plane plane = new Plane(a, normal);
        double distanceToPlane = plane.distance(b);
        // System.out.println("Points " + a + ", " + b + ", " + c + ", " + v);
        // System.out.println("Distance to plane = " + distanceToPlane);
        // System.out.println("Plane = " + plane);

        // could use an epsilon to allow for floating point errors?
        if (distanceToPlane < 0.0) {
            return 'L';
        } else if (distanceToPlane > 0.0) {
            return 'R';
        } else {
            // System.err.println("Points coplanar : " + a + ", " + b + ", " +
            // c);
            return 'R';
        }
    }

    public static Point3d averagePoints(Collection points) {
        Point3d averagePoint = new Point3d();
        Iterator itr = points.iterator();
        while (itr.hasNext()) {
            Point3d nextPoint = (Point3d) itr.next();
            averagePoint.add(nextPoint);
        }
        averagePoint.scale(1.0 / points.size());
        return averagePoint;
    }

    public static Axis leastSquareAxis(ArrayList points) {
        int numberOfPoints = points.size();
        // System.out.println("Running leastSquareAxis on " + numberOfPoints + "
        // points");
        if (numberOfPoints == 0) {
            return new Axis();
        } else if (numberOfPoints < 2) {
            // no good solution for a single point
            return new Axis((Point3d) points.get(0), (Point3d) points.get(0));
        } else if (numberOfPoints == 2) {
            // take the difference of two points
            return new Axis((Point3d) points.get(1), (Point3d) points.get(0));
        }

        // otherwise, make a DoubleMatrix2D
        DoubleMatrix2D pointMatrix = DoubleFactory2D.dense.make(points.size(),
                3);
        for (int i = 0; i < points.size(); i++) {
            Point3d point = (Point3d) points.get(i);
            pointMatrix.set(i, 0, point.x);
            pointMatrix.set(i, 1, point.y);
            pointMatrix.set(i, 2, point.z);
        }

        // find the centroid, also apply points -centroid
        double num = (new Integer(numberOfPoints)).doubleValue();

        // for x
        DoubleMatrix1D xColumn = pointMatrix.viewColumn(0);
        double xAverage = xColumn.aggregate(Functions.plus, Functions.identity) / num;
        xColumn.assign(Functions.minus(xAverage));

        // for y
        DoubleMatrix1D yColumn = pointMatrix.viewColumn(1);
        double yAverage = yColumn.aggregate(Functions.plus, Functions.identity) / num;
        yColumn.assign(Functions.minus(yAverage));

        // for z
        DoubleMatrix1D zColumn = pointMatrix.viewColumn(2);
        double zAverage = zColumn.aggregate(Functions.plus, Functions.identity) / num;
        zColumn.assign(Functions.minus(zAverage));

        // since we've gone to the trouble of calculating these, we might as
        // well store them!
        Point3d centroid = new Point3d(xAverage, yAverage, zAverage);
        // System.out.println("Centroid = " + centroid);

        // transpose : m = multiply(transpose(pointMatrix), pointMatrix)
        DoubleMatrix2D symmetricMatrix = pointMatrix.zMult(pointMatrix, null,
                1, 0, true, false);

        // find eigenvectors, eigenvalues
        EigenvalueDecomposition eig = new EigenvalueDecomposition(
                symmetricMatrix);
        DoubleMatrix1D eigenvalues = eig.getRealEigenvalues();
        DoubleMatrix2D eigenvectors = eig.getV();
        // System.out.println("eigenvectors = " + eigenvectors);

        // use the maximum value in the eigenvalues to get the index in the
        // eigenvectors
        int maxIndex = 0;
        int maxValue = 0;
        for (int j = 0; j < eigenvalues.size(); j++) {
            if (eigenvalues.get(j) > maxValue)
                maxIndex = j;
        }
        // both the sign and the order of the matrix are wrong!?
        // DoubleMatrix1D v = eigenvectors.viewRow(maxIndex);
        DoubleMatrix1D v = eigenvectors.viewColumn(maxIndex); // so we view
                                                                // COLUMN, not
                                                                // row
        // v.assign(F.mult( -1)); //and we multiply all by -1..
        // System.out.println("v = " + v);

        // finally construct the axis from this principal eigenvector and the
        // centroid
        Vector3d axisVector = new Vector3d(v.toArray());
        return new Axis(centroid, axisVector);
    }

    public static double angle(Point3d a, Point3d b, Point3d c) {
        Vector3d ab = new Vector3d();
        ab.sub(a, b);
        Vector3d cb = new Vector3d();
        cb.sub(c, b);
        return Math.toDegrees(ab.angle(cb));
    }

    // ugh! in python you can do this in one line :
    // 'return centroid + (axis * (axis * (point - centroid)))'..
    // oh for overloaded operators!
    public static Point3d scalePoint(Point3d point, Vector3d axis,
            Point3d centroid) {
        Vector3d pointVector = new Vector3d(point);
        pointVector.sub(centroid);
        double axisDotPoint = axis.dot(pointVector);
        Vector3d tmp = new Vector3d(axis);
        tmp.scaleAdd(axisDotPoint, centroid);
        return new Point3d(tmp);
    }

    public static double torsion(Point3d a, Point3d b, Point3d c, Point3d d) {
        if (a == null || b == null || c == null || d == null) {
            // System.err.println("null point in torsion calculation!");
            return 0.0;
        }

        Vector3d ab = new Vector3d();
        ab.sub(b, a);

        Vector3d bc = new Vector3d();
        bc.sub(c, b);

        Vector3d l = new Vector3d();
        l.cross(ab, bc);

        Vector3d cd = new Vector3d();
        cd.sub(d, c);

        Vector3d cb = new Vector3d();
        cb.sub(b, c);

        Vector3d r = new Vector3d();
        r.cross(cd, cb);

        double angle = Math.toDegrees(l.angle(r));

        Vector3d lr = new Vector3d();
        lr.cross(l, r);
        if (lr.dot(bc) < 0.0)
            angle = -angle;

        return angle;
    }

}
