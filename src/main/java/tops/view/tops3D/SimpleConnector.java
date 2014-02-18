package tops.view.tops3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;

import javax.vecmath.Point3d;

public class SimpleConnector {

    private SSE first;

    private SSE second;

    private double height;

    private double width;

    private Point3d nTerminalEnd;

    private Point3d cTerminalEnd;

    public SimpleConnector(SSE first, SSE second) {
        this.first = first;
        this.second = second;
        this.nTerminalEnd = first.getTopFixture(); // the n-terminus OF THE
                                                // CONNECTOR
        this.cTerminalEnd = second.getBottomFixture(); // the c-terminus of
                                                    // connector
        this.width = (first.getCenter()).distance(second.getCenter());
        this.height = this.width / 2;
    }

    public Shape3D getShape(Appearance app) {
        LineArray lineGeom = new LineArray(6, GeometryArray.COORDINATES);
        Point3d nConnectionPoint = this.first.getPointBeyond(this.height);
        Point3d cConnectionPoint = this.second.getPointBefore(this.height);

        lineGeom.setCoordinate(0, this.nTerminalEnd);
        lineGeom.setCoordinate(1, nConnectionPoint);
        lineGeom.setCoordinate(2, nConnectionPoint);
        lineGeom.setCoordinate(3, cConnectionPoint);
        lineGeom.setCoordinate(4, cConnectionPoint);
        lineGeom.setCoordinate(5, this.cTerminalEnd);

        return new Shape3D(lineGeom, app);
    }

    public TransformGroup getTransformGroup(Appearance app) {
        TransformGroup connectorGroup = new TransformGroup();

        Shape3D shape = this.getShape(app);
        connectorGroup.addChild(shape);
        return connectorGroup;
    }

    @Override
    public String toString() {
        return new String("connector : [" + this.nTerminalEnd + "],[" + this.cTerminalEnd
                + "]");
    }
}
