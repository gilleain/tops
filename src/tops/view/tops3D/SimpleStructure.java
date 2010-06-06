package tops.view.tops3D;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

//A container for <i>SSE</i>s that represents a whole tops.dw.protein
//Structures are built using a <i>Framework</i> class

public class SimpleStructure {

    private ArrayList sheets;

    private ArrayList connections; // these are connections /between/ sheets

    private SimpleSheet currentSheet;

    private Point3d sheetSeparation;

    public SimpleStructure() {
        this.sheets = new ArrayList();
        this.connections = new ArrayList();
        this.sheetSeparation = new Point3d(0.0, 0.0, 1.5);
    }

    public void addToCurrentSheet(int order, char type) {
        Point3d fixture = this.currentSheet.makeNextFixture();
        try {
            this.currentSheet.affix(order, type, fixture);

        } catch (SSEOverlapException soe) {
            System.out.println("overlap! " + soe);
        }
    }

    public void connectInCurrentSheet(int first, int second) {
        // could also check for clashes
        this.currentSheet.connect(first, second);
    }

    public void connectOutsideCurrentSheet(int first, int second, int chirality) {
        SSE firstSSE = this.currentSheet.getSSE(first);
        SSE secondSSE = this.currentSheet.getSSE(second);

        // get the orientation of the strands to connect
        int orientation = firstSSE.getOrientation();

        // now get the centers of the two sses
        Point3d firstCenter = firstSSE.getCenter();
        Point3d secondCenter = secondSSE.getCenter();
        Point3d connectingCenter = new Point3d();

        // new center is half way between the centers it connects
        connectingCenter.interpolate(firstCenter, secondCenter, 0.5);

        SimpleSheet sh = this.getSheet(chirality);
        System.out.println("got : " + sh);
        SSE connectingSSE = null;
        try {
            if (orientation == SSE.UP) {
                connectingSSE = sh.affix(0, 'h', connectingCenter); // could
                                                                    // have an
                                                                    // 'U'
                                                                    // element?
            } else if (orientation == SSE.DOWN) {
                connectingSSE = sh.affix(0, 'H', connectingCenter);
            }
        } catch (SSEOverlapException soe) {
            System.out.println("overlap! " + soe);
        }
        this.connect(firstSSE, connectingSSE);
        this.connect(connectingSSE, secondSSE);
    }

    public void connect(SSE first, SSE second) {
        SimpleConnector sc = new SimpleConnector(first, second);
        this.connections.add(sc);
    }

    public SimpleSheet getSheet(int chirality) {
        SimpleSheet s = null;
        if (chirality == Framework.CHIRAL_LEFT) {
            s = this.currentSheet.getLeft();
            if (s == null) {
                s = new SimpleSheet();
                s.shiftAlongZAxis(this.sheetSeparation, chirality);
                this.currentSheet.setLeft(s);
            }
        } else if (chirality == Framework.CHIRAL_RIGHT) {
            s = this.currentSheet.getRight();
            if (s == null) {
                s = new SimpleSheet();
                s.shiftAlongZAxis(this.sheetSeparation, chirality);
                this.currentSheet.setRight(s);
            }
        } else {
            System.out
                    .println("not allowed to get a CHIRAL_NONE! " + chirality);
        }
        this.sheets.add(s);
        return s;
    }

    public void makeNewSheet() {
        SimpleSheet s = new SimpleSheet();
        this.sheets.add(s);
        this.currentSheet = s;
    }

    public BranchGroup getSceneGraph() {
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(Group.ALLOW_CHILDREN_READ);
        objRoot.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        Transform3D zScaler = new Transform3D();
        zScaler.set(0.2);
        TransformGroup scaleToFit = new TransformGroup(zScaler);
        scaleToFit.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        scaleToFit.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRoot.addChild(scaleToFit);

        Appearance wireApp = new Appearance();

        // Material ma = new Material();
        // wireApp.setMaterial(ma);

        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        wireApp.setPolygonAttributes(pa);

        ColoringAttributes wireCa = new ColoringAttributes();
        wireCa.setColor(new Color3f(java.awt.Color.WHITE));
        wireCa.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
        wireApp.setColoringAttributes(wireCa);

        // debugging sphere nonsense
        // scaleToFit.addChild(new com.sun.j3d.utils.geometry.Sphere(1.0f,
        // wireApp));
        /*
         * Transform3D mover = new Transform3D(); Vector3d pnt = new
         * Vector3d(3.5,0.0,1.0); mover.setTranslation(pnt); TransformGroup tmp =
         * new TransformGroup(mover); tmp.addChild(new
         * com.sun.j3d.utils.geometry.Sphere(1.0f, wireApp));
         * scaleToFit.addChild(tmp);
         */

        Iterator i = this.sheets.iterator();
        while (i.hasNext()) {
            SimpleSheet s = (SimpleSheet) i.next();
            TransformGroup tg = s.getTransformGroup(wireApp);
            scaleToFit.addChild(tg);
        }

        Iterator j = this.connections.iterator();
        while (j.hasNext()) {
            SimpleConnector sc = (SimpleConnector) j.next();
            TransformGroup connectorTransform = sc.getTransformGroup(wireApp);
            scaleToFit.addChild(connectorTransform);
        }

        // objRoot.compile();
        return objRoot;
    }

    public Point3d getCenter() {
        SimpleSheet first = (SimpleSheet) this.sheets.get(0);
        if (this.sheets.size() == 1) {
            return first.getCenter();
        } else {
            SimpleSheet last = (SimpleSheet) this.sheets.get(this.sheets.size() - 1);
            Point3d structureCenter = new Point3d();
            structureCenter.interpolate(first.getCenter(), last.getCenter(),
                    0.5);
            return structureCenter;
        }
    }

    @Override
    public String toString() {
        StringBuffer stringify = new StringBuffer();
        stringify.append("Center : ").append(this.getCenter()).append("\n");
        Iterator i = this.sheets.iterator();
        while (i.hasNext()) {
            SimpleSheet nextSheet = (SimpleSheet) i.next();
            stringify.append(nextSheet.toString()).append("\n");
        }

        Iterator j = this.connections.iterator();
        while (j.hasNext()) {
            SimpleConnector nextCon = (SimpleConnector) j.next();
            stringify.append(nextCon.toString()).append("\n");
        }

        return stringify.toString();
    }
}
