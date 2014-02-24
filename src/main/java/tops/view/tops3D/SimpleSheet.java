package tops.view.tops3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import javax.vecmath.Point3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.TransformGroup;

public class SimpleSheet {

    private ArrayList<SSE> sses;

    private ArrayList<SimpleConnector> connections;

    private HashMap<Integer, Integer> sseMap;

    private Point3d zShift; // the shift along the z-axis

    private Point3d currentFixture;

    private Point3d separation; // the distance between SSEs
    // private String orientation; //the orientation relative to the global
    // standard

    private SimpleSheet left; // the sheet (if any) to the left (relative to
                                // standard)

    private SimpleSheet right; // the sheet (if any) to the right (relative to
                                // standard)

    public SimpleSheet() {
        this.sses = new ArrayList<SSE>();
        this.connections = new ArrayList<SimpleConnector>();
        this.sseMap = new HashMap<Integer, Integer>();
        this.zShift = new Point3d();
        this.currentFixture = new Point3d();
        // orientation = new String();
        this.separation = new Point3d(1.5, 0.0, 0.0);
        this.left = null;
        this.right = null;
    }

    public void shiftAlongZAxis(Point3d separation, int chirality) {
        if (chirality == Framework.CHIRAL_LEFT) {
            this.zShift.sub(separation);
        } else if (chirality == Framework.CHIRAL_RIGHT) {
            this.zShift.add(separation);
        }
    }

    public SSE getSSE(int index) {
        return (SSE) this.sses.get(this.getMapping(index));
    }

    // 'fix' the SSEs to an imaginary sheet
    public SSE affix(int order, char type, Point3d fixturePoint)
            throws SSEOverlapException {
        SSE sse = null;
        fixturePoint.add(this.zShift); // important!
        if (type == 'E') {
            System.out.println("making an up strand with center : "
                    + fixturePoint);
            sse = new Strand(order, SSE.UP, fixturePoint);
        } else if (type == 'e') {
            System.out.println("making a down strand with center : "
                    + fixturePoint);
            sse = new Strand(order, SSE.DOWN, fixturePoint);
        } else if (type == 'H') {
            System.out.println("making an up helix with center : "
                    + fixturePoint);
            sse = new Helix(order, SSE.UP, fixturePoint);
        } else if (type == 'h') {
            System.out.println("making an up helix with center : "
                    + fixturePoint);
            sse = new Helix(order, SSE.DOWN, fixturePoint);
        }
        this.checkOverlap(sse);
        int numberOfStrands = this.sses.size(); // add to the end of the list
        this.sseMap.put(new Integer(numberOfStrands), new Integer(order));
        this.sses.add(sse);
        return sse;
    }

    // get and set the references to left and right packing sheets
    public void setLeft(SimpleSheet left) {
        this.left = left;
    }

    public void setRight(SimpleSheet right) {
        this.right = right;
    }

    public SimpleSheet getLeft() {
        return this.left;
    }

    public SimpleSheet getRight() {
        return this.right;
    }

    private int getMapping(int num) {
        return ((Integer) this.sseMap.get(new Integer(num))).intValue() - 1;
    }

    public void connect(int first, int second) {
        SSE firstSSE = (SSE) this.sses.get(this.getMapping(first));
        SSE secondSSE = (SSE) this.sses.get(this.getMapping(second));
        SimpleConnector con = new SimpleConnector(firstSSE, secondSSE);
        this.connections.add(con);
    }

    public TransformGroup getTransformGroup(Appearance app) {
        TransformGroup sheetTransform = new TransformGroup();
        Iterator<SSE> i = this.sses.iterator();
        while (i.hasNext()) {
            SSE s = (SSE) i.next();
            TransformGroup sseTransform = s.getTransformGroup(app);
            sheetTransform.addChild(sseTransform);
        }

        Iterator<SimpleConnector> j = this.connections.iterator();
        while (j.hasNext()) {
            SimpleConnector sc = (SimpleConnector) j.next();
            TransformGroup connectorTransform = sc.getTransformGroup(app);
            sheetTransform.addChild(connectorTransform);
        }

        return sheetTransform;
    }

    // 'extend' the sheet by adding a new fixture point
    public Point3d makeNextFixture() {
        Point3d nextFixture = new Point3d();
        nextFixture.add(this.currentFixture, this.separation);
        this.currentFixture = nextFixture;
        System.out.println("new fixture, fixture now : " + this.currentFixture);
        return nextFixture;
    }

    public Point3d getCenter() {
        Point3d sheetCenter = new Point3d();
        SSE first = (SSE) this.sses.get(0);
        if (this.sses.size() == 1)
            return first.getCenter();
        SSE last = (SSE) this.sses.get(this.sses.size() - 1);
        sheetCenter.interpolate(first.getCenter(), last.getCenter(), 0.5); // get
                                                                            // midpoint
        return sheetCenter;
    }

    // checks that the SSE sse does not overlap with any of the others (so far)
    private void checkOverlap(SSE sse) throws SSEOverlapException {
        Iterator<SSE> i = this.sses.iterator();
        while (i.hasNext()) {
            SSE nextSSE = (SSE) i.next();
            if (sse.clashesWith(nextSSE))
                throw new SSEOverlapException(sse.toString(), nextSSE
                        .toString());
        }
    }

    @Override
    public String toString() {
        StringBuffer stringified = new StringBuffer("sheet\n");
        Iterator<SSE> i = this.sses.iterator();
        while (i.hasNext()) {
            SSE nextSSE = (SSE) i.next();
            stringified.append("SSE : ").append(nextSSE.toString());
        }
        Iterator<SimpleConnector> j = this.connections.iterator();
        while (j.hasNext()) {
            SimpleConnector nextCon = (SimpleConnector) j.next();
            stringified.append(nextCon.toString()).append(",");
        }
        return stringified.toString();
    }
}
