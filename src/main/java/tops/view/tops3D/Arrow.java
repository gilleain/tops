package tops.view.tops3D;

//import com.sun.j3d.utils.geometry.GeometryInfo;
//import com.sun.j3d.utils.geometry.NormalGenerator;
//import com.sun.j3d.utils.geometry.Stripifier;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.QuadArray;

import javax.vecmath.Point3d;

public class Arrow {

    static final double bodyWidth = 0.5;

    static final double bodyHeight = 1.0;

    static final double bodyDepth = 0.25;

    static final double headWidth = 1.0;

    static final double headHeight = 0.5;

    static final double headDepth = 0.5;

    static final double tipWidth = 0.1;

    static final double fullHeight = Arrow.bodyHeight + Arrow.headHeight;

    static final double halfBodyWidth = Arrow.bodyWidth / 2;

    static final double halfBodyHeight = Arrow.bodyHeight / 2;

    static final double halfBodyDepth = Arrow.bodyDepth / 2;

    static final double halfHeadWidth = Arrow.headWidth / 2;

    static final double halfHeadHeight = Arrow.headHeight / 2;

    static final double halfHeadDepth = Arrow.headDepth / 2;

    static final double halfTipWidth = Arrow.tipWidth / 2;

    static final double halfFullHeight = Arrow.fullHeight / 2;

    // the distance between the center and the base of the arrow head
    static final double remainderHeight = Arrow.halfFullHeight - Arrow.headHeight;

    static final Point3d top = new Point3d(0.0, Arrow.halfFullHeight, 0.0);

    static final Point3d bot = new Point3d(0.0, 0.0 - Arrow.halfFullHeight, 0.0);

    private static final double[] bodyVertsUp = {
    Arrow.// Front Face
            halfBodyWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfBodyDepth, // v1
            Arrow.halfBodyWidth, Arrow.remainderHeight, Arrow.halfBodyDepth, // v2
            0.0 - Arrow.halfBodyWidth, Arrow.remainderHeight, Arrow.halfBodyDepth, // v3
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfBodyDepth, // v4
            Arrow.// Back Face
            halfBodyWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v5
            Arrow.halfBodyWidth, Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v6
            0.0 - Arrow.halfBodyWidth, Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v7
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v8
            Arrow.// Right Face
            halfBodyWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfBodyDepth, // v1
            Arrow.halfBodyWidth, Arrow.remainderHeight, Arrow.halfBodyDepth, // v2
            Arrow.halfBodyWidth, Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v6
            Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v5
            // Left Face
            0.0 - Arrow.halfBodyWidth, Arrow.remainderHeight, Arrow.halfBodyDepth, // v3
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfBodyDepth, // v4
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v8
            0.0 - Arrow.halfBodyWidth, Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v7
            Arrow.// Top Face
            halfBodyWidth, Arrow.remainderHeight, Arrow.halfBodyDepth, // v2
            0.0 - Arrow.halfBodyWidth, Arrow.remainderHeight, Arrow.halfBodyDepth, // v3
            0.0 - Arrow.halfBodyWidth, Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v7
            Arrow.halfBodyWidth, Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v6
            Arrow.// Bottom Face
            halfBodyWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfBodyDepth, // v1
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfBodyDepth, // v4
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v8
            Arrow.halfBodyWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v5

    };

    private static final double[] bodyVertsDn = {
    Arrow.// Front Face
            halfBodyWidth, Arrow.halfFullHeight, Arrow.halfBodyDepth, // v1
            Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, Arrow.halfBodyDepth, // v2
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, Arrow.halfBodyDepth, // v3
            0.0 - Arrow.halfBodyWidth, Arrow.halfFullHeight, Arrow.halfBodyDepth, // v4
            Arrow.// Back Face
            halfBodyWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v5
            Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v6
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth,// v7
            0.0 - Arrow.halfBodyWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v8
            Arrow.// Right Face
            halfBodyWidth, Arrow.halfFullHeight, Arrow.halfBodyDepth, // v1
            Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, Arrow.halfBodyDepth, // v2
            Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v6
            Arrow.halfBodyWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v5
            // Left Face
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, Arrow.halfBodyDepth, // v3
            0.0 - Arrow.halfBodyWidth, Arrow.halfFullHeight, Arrow.halfBodyDepth, // v4
            0.0 - Arrow.halfBodyWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v8
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth,// v7
            Arrow.// Top Face
            halfBodyWidth, 0.0 - Arrow.remainderHeight, Arrow.halfBodyDepth, // v2
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, Arrow.halfBodyDepth, // v3
            0.0 - Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth,// v7
            Arrow.halfBodyWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfBodyDepth, // v6
            Arrow.// Bottom Face
            halfBodyWidth, Arrow.halfFullHeight, Arrow.halfBodyDepth, // v1
            0.0 - Arrow.halfBodyWidth, Arrow.halfFullHeight, Arrow.halfBodyDepth, // v4
            0.0 - Arrow.halfBodyWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v8
            Arrow.halfBodyWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfBodyDepth, // v5
    };

    private static final double[] headVertsUp = {
    // Head Front Face
            0.0 - Arrow.halfHeadWidth, Arrow.remainderHeight, Arrow.halfHeadDepth, // v9
            Arrow.halfHeadWidth, Arrow.remainderHeight, Arrow.halfHeadDepth, // v10
            Arrow.halfTipWidth, Arrow.halfFullHeight, Arrow.halfHeadDepth, // v11
            0.0 - Arrow.halfTipWidth, Arrow.halfFullHeight, Arrow.halfHeadDepth, // v12
            // Head Back Face
            0.0 - Arrow.halfHeadWidth, Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v13
            Arrow.halfHeadWidth, Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v14
            Arrow.halfTipWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v15
            0.0 - Arrow.halfTipWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v16
            Arrow.// Head Right Face
            halfHeadWidth, Arrow.remainderHeight, Arrow.halfHeadDepth, // v10
            Arrow.halfTipWidth, Arrow.halfFullHeight, Arrow.halfHeadDepth, // v11
            Arrow.halfTipWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v15
            Arrow.halfHeadWidth, Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v14
            // Head Left Face
            0.0 - Arrow.halfHeadWidth, Arrow.remainderHeight, Arrow.halfHeadDepth, // v9
            0.0 - Arrow.halfTipWidth, Arrow.halfFullHeight, Arrow.halfHeadDepth, // v12
            0.0 - Arrow.halfTipWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v16
            0.0 - Arrow.halfHeadWidth, Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v13
            Arrow.// Head Tip/Top Face
            halfTipWidth, Arrow.halfFullHeight, Arrow.halfHeadDepth, // v11
            0.0 - Arrow.halfTipWidth, Arrow.halfFullHeight, Arrow.halfHeadDepth, // v12
            0.0 - Arrow.halfTipWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v16
            Arrow.halfTipWidth, Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v15
            // Head Bottom Face
            0.0 - Arrow.halfHeadWidth, Arrow.remainderHeight, Arrow.halfHeadDepth, // v9
            Arrow.halfHeadWidth, Arrow.remainderHeight, Arrow.halfHeadDepth, // v10
            Arrow.halfHeadWidth, Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v14
            0.0 - Arrow.halfHeadWidth, Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v13
    };

    private static final double[] headVertsDn = {
    // Head Front Face
            0.0 - Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, Arrow.halfHeadDepth, // v9
            Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, Arrow.halfHeadDepth, // v10
            Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfHeadDepth, // v11
            0.0 - Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfHeadDepth, // v12
            // Head Back Face
            0.0 - Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth,// v13
            Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v14
            Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v15
            0.0 - Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v16
            Arrow.// Head Right Face
            halfHeadWidth, 0.0 - Arrow.remainderHeight, Arrow.halfHeadDepth, // v10
            Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfHeadDepth, // v11
            Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v15
            Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v14
            // Head Left Face
            0.0 - Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, Arrow.halfHeadDepth, // v9
            0.0 - Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfHeadDepth, // v12
            0.0 - Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v16
            0.0 - Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth,// v13
            Arrow.// Head Tip/Top Face
            halfTipWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfHeadDepth, // v11
            0.0 - Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, Arrow.halfHeadDepth, // v12
            0.0 - Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v16
            Arrow.halfTipWidth, 0.0 - Arrow.halfFullHeight, 0.0 - Arrow.halfHeadDepth, // v15
            // Head Bottom Face
            0.0 - Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, Arrow.halfHeadDepth, // v9
            Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, Arrow.halfHeadDepth, // v10
            Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth, // v14
            0.0 - Arrow.halfHeadWidth, 0.0 - Arrow.remainderHeight, 0.0 - Arrow.halfHeadDepth,// v13
    };

    private static final Point3d v1U = new Point3d(Arrow.bodyVertsUp[0],
            Arrow.bodyVertsUp[1], Arrow.bodyVertsUp[2]);

    private static final Point3d v2U = new Point3d(Arrow.bodyVertsUp[3],
            Arrow.bodyVertsUp[4], Arrow.bodyVertsUp[5]);

    private static final Point3d v3U = new Point3d(Arrow.bodyVertsUp[6],
            Arrow.bodyVertsUp[7], Arrow.bodyVertsUp[8]);

    private static final Point3d v4U = new Point3d(Arrow.bodyVertsUp[9],
            Arrow.bodyVertsUp[10], Arrow.bodyVertsUp[11]);

    private static final Point3d v5U = new Point3d(Arrow.bodyVertsUp[12],
            Arrow.bodyVertsUp[13], Arrow.bodyVertsUp[14]);

    private static final Point3d v6U = new Point3d(Arrow.bodyVertsUp[15],
            Arrow.bodyVertsUp[16], Arrow.bodyVertsUp[17]);

    private static final Point3d v7U = new Point3d(Arrow.bodyVertsUp[18],
            Arrow.bodyVertsUp[19], Arrow.bodyVertsUp[20]);

    private static final Point3d v8U = new Point3d(Arrow.bodyVertsUp[21],
            Arrow.bodyVertsUp[22], Arrow.bodyVertsUp[23]);

    private static final Point3d v9U = new Point3d(Arrow.headVertsUp[0],
            Arrow.headVertsUp[1], Arrow.headVertsUp[2]);

    private static final Point3d v10U = new Point3d(Arrow.headVertsUp[3],
            Arrow.headVertsUp[4], Arrow.headVertsUp[5]);

    private static final Point3d v11U = new Point3d(Arrow.headVertsUp[6],
            Arrow.headVertsUp[7], Arrow.headVertsUp[8]);

    private static final Point3d v12U = new Point3d(Arrow.headVertsUp[9],
            Arrow.headVertsUp[10], Arrow.headVertsUp[11]);

    private static final Point3d v13U = new Point3d(Arrow.headVertsUp[12],
            Arrow.headVertsUp[13], Arrow.headVertsUp[14]);

    private static final Point3d v14U = new Point3d(Arrow.headVertsUp[15],
            Arrow.headVertsUp[16], Arrow.headVertsUp[17]);

    private static final Point3d v15U = new Point3d(Arrow.headVertsUp[18],
            Arrow.headVertsUp[19], Arrow.headVertsUp[20]);

    private static final Point3d v16U = new Point3d(Arrow.headVertsUp[21],
            Arrow.headVertsUp[22], Arrow.headVertsUp[23]);

    private static final Point3d v1D = new Point3d(Arrow.bodyVertsDn[0],
            Arrow.bodyVertsDn[1], Arrow.bodyVertsDn[2]);

    private static final Point3d v2D = new Point3d(Arrow.bodyVertsDn[3],
            Arrow.bodyVertsDn[4], Arrow.bodyVertsDn[5]);

    private static final Point3d v3D = new Point3d(Arrow.bodyVertsDn[6],
            Arrow.bodyVertsDn[7], Arrow.bodyVertsDn[8]);

    private static final Point3d v4D = new Point3d(Arrow.bodyVertsDn[9],
            Arrow.bodyVertsDn[10], Arrow.bodyVertsDn[11]);

    private static final Point3d v5D = new Point3d(Arrow.bodyVertsDn[12],
            Arrow.bodyVertsDn[13], Arrow.bodyVertsDn[14]);

    private static final Point3d v6D = new Point3d(Arrow.bodyVertsDn[15],
            Arrow.bodyVertsDn[16], Arrow.bodyVertsDn[17]);

    private static final Point3d v7D = new Point3d(Arrow.bodyVertsDn[18],
            Arrow.bodyVertsDn[19], Arrow.bodyVertsDn[20]);

    private static final Point3d v8D = new Point3d(Arrow.bodyVertsDn[21],
            Arrow.bodyVertsDn[22], Arrow.bodyVertsDn[23]);

    private static final Point3d v9D = new Point3d(Arrow.headVertsDn[0],
            Arrow.headVertsDn[1], Arrow.headVertsDn[2]);

    private static final Point3d v10D = new Point3d(Arrow.headVertsDn[3],
            Arrow.headVertsDn[4], Arrow.headVertsDn[5]);

    private static final Point3d v11D = new Point3d(Arrow.headVertsDn[6],
            Arrow.headVertsDn[7], Arrow.headVertsDn[8]);

    private static final Point3d v12D = new Point3d(Arrow.headVertsDn[9],
            Arrow.headVertsDn[10], Arrow.headVertsDn[11]);

    private static final Point3d v13D = new Point3d(Arrow.headVertsDn[12],
            Arrow.headVertsDn[13], Arrow.headVertsDn[14]);

    private static final Point3d v14D = new Point3d(Arrow.headVertsDn[15],
            Arrow.headVertsDn[16], Arrow.headVertsDn[17]);

    private static final Point3d v15D = new Point3d(Arrow.headVertsDn[18],
            Arrow.headVertsDn[19], Arrow.headVertsDn[20]);

    private static final Point3d v16D = new Point3d(Arrow.headVertsDn[21],
            Arrow.headVertsDn[22], Arrow.headVertsDn[23]);

    private static final Point3d[] bodyPointsUp = { Arrow.v1U, Arrow.v2U, Arrow.v3U, Arrow.v4U, Arrow.v5U,
            Arrow.v6U, Arrow.v7U, Arrow.v8U, Arrow.v1U, Arrow.v2U, Arrow.v6U, Arrow.v5U, Arrow.v3U, Arrow.v4U, Arrow.v8U, Arrow.v7U, Arrow.v2U, Arrow.v3U,
            Arrow.v7U, Arrow.v6U, Arrow.v1U, Arrow.v4U, Arrow.v8U, Arrow.v5U };

    private static final Point3d[] headPointsUp = { Arrow.v9U, Arrow.v10U, Arrow.v11U, Arrow.v12U,
            Arrow.v13U, Arrow.v14U, Arrow.v15U, Arrow.v16U, Arrow.v10U, Arrow.v11U, Arrow.v15U, Arrow.v14U, Arrow.v9U, Arrow.v12U, Arrow.v16U,
            Arrow.v13U, Arrow.v11U, Arrow.v12U, Arrow.v16U, Arrow.v15U, Arrow.v9U, Arrow.v10U, Arrow.v14U, Arrow.v13U };

    private static final Point3d[] bodyPointsDn = { Arrow.v1D, Arrow.v2D, Arrow.v3D, Arrow.v4D, Arrow.v5D,
            Arrow.v6D, Arrow.v7D, Arrow.v8D, Arrow.v1D, Arrow.v2D, Arrow.v6D, Arrow.v5D, Arrow.v3D, Arrow.v4D, Arrow.v8D, Arrow.v7D, Arrow.v2D, Arrow.v3D,
            Arrow.v7D, Arrow.v6D, Arrow.v1D, Arrow.v4D, Arrow.v8D, Arrow.v5D };

    private static final Point3d[] headPointsDn = { Arrow.v9D, Arrow.v10D, Arrow.v11D, Arrow.v12D,
            Arrow.v13D, Arrow.v14D, Arrow.v15D, Arrow.v16D, Arrow.v10D, Arrow.v11D, Arrow.v15D, Arrow.v14D, Arrow.v9D, Arrow.v12D, Arrow.v16D,
            Arrow.v13D, Arrow.v11D, Arrow.v12D, Arrow.v16D, Arrow.v15D, Arrow.v9D, Arrow.v10D, Arrow.v14D, Arrow.v13D };

    public static Shape3D createArrow(int orientation, Point3d center,
            Appearance app) {

        QuadArray body = new QuadArray(24, GeometryArray.COORDINATES);
        if (orientation == SSE.UP) {
            for (int i = 0; i < Arrow.bodyPointsUp.length; i++) {
                Point3d memoryBurn = new Point3d();
                memoryBurn.add(center, Arrow.bodyPointsUp[i]);
                body.setCoordinate(i, memoryBurn);
            }
        } else {
            for (int i = 0; i < Arrow.bodyPointsDn.length; i++) {
                Point3d memoryBurn = new Point3d();
                memoryBurn.add(center, Arrow.bodyPointsDn[i]);
                body.setCoordinate(i, memoryBurn);
            }
        }

//        GeometryInfo gi = new GeometryInfo(body);
//        // generate normals
//        NormalGenerator ng = new NormalGenerator();
//        ng.generateNormals(gi);
//        // stripify
//        Stripifier st = new Stripifier();
//        st.stripify(gi);
//        GeometryArray bodyResult = gi.getGeometryArray();
//
//        QuadArray head = new QuadArray(24, GeometryArray.COORDINATES);
//        if (orientation == SSE.UP) {
//            for (int j = 0; j < Arrow.headPointsUp.length; j++) {
//                Point3d memoryBurn = new Point3d();
//                memoryBurn.add(center, Arrow.headPointsUp[j]);
//                head.setCoordinate(j, memoryBurn);
//            }
//        } else {
//            for (int i = 0; i < Arrow.headPointsDn.length; i++) {
//                Point3d memoryBurn = new Point3d();
//                memoryBurn.add(center, Arrow.headPointsDn[i]);
//                head.setCoordinate(i, memoryBurn);
//            }
//        }

//        gi.reset(head);
//        // generate normals
//        ng.generateNormals(gi);
//        // stripify
//        st.stripify(gi);
//        GeometryArray headResult = gi.getGeometryArray();
//
//        Shape3D arrow = new Shape3D();
//        arrow.addGeometry(bodyResult);
//        arrow.addGeometry(headResult);
//        arrow.setAppearance(app);
//        return arrow;
        return null;

    }

    public static double getLength() {
        return Arrow.fullHeight;
    }

    public static Point3d getTopPoint() {
        return Arrow.top;
    }

    public static Point3d getBottomPoint() {
        return Arrow.bot;
    }

}
