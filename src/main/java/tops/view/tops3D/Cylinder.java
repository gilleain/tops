package tops.view.tops3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;

import javax.vecmath.Point3d;

//temporary class! probably better to subclass Primitive to make an Arrow Primitive and then use the Cylinder Primitive 

public class Cylinder {

    private static final double height = 1.5;

    private static final double halfHeight = Cylinder.height / 2;

    private static final double[] vertsF = { 0.0, 0.0 - Cylinder.halfHeight, 0.0, // vertex
                                                                            // 1
    };

    private static final double[] vertsS = { 0.0, 0.0 + Cylinder.halfHeight, 0.0, // vertex
                                                                            // 2
    };

    private static final Point3d vFirst = new Point3d(Cylinder.vertsF);

    private static final Point3d vSecond = new Point3d(Cylinder.vertsS);

    public static Shape3D createCylinder(int orientation, Point3d center,
            Appearance app) {
        LineArray line = new LineArray(2, GeometryArray.COORDINATES); // yes, a
                                                                    // straight
                                                                    // line
                                                                    // cylinder!

        Point3d first = new Point3d(center);
        first.add(Cylinder.vFirst);
        Point3d second = new Point3d(center);
        second.add(Cylinder.vSecond);

        if (orientation == SSE.UP) {
            line.setCoordinate(0, first);
            line.setCoordinate(1, second);
        } else {
            line.setCoordinate(0, second);
            line.setCoordinate(1, first);
        }

        Shape3D cylinder = new Shape3D(line);
        return cylinder;
    }
}
