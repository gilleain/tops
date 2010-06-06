package tops.view.tops3D;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import javax.media.j3d.*;
import javax.vecmath.*;

public class HelloUniverse extends Applet {

    private SimpleUniverse u = null;

    public BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        Appearance filledApp = new Appearance();
        ColoringAttributes filledCa = new ColoringAttributes();
        filledCa.setColor(new Color3f(0.4f, 0.4f, 0.4f)); // GRAY
        filledApp.setColoringAttributes(filledCa);

        Appearance wireApp = new Appearance();
        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        wireApp.setPolygonAttributes(pa);
        ColoringAttributes wireCa = new ColoringAttributes();
        wireCa.setColor(new Color3f(1.0f, 0.0f, 0.0f)); // RED
        wireApp.setColoringAttributes(wireCa);

        Transform3D translation = new Transform3D();
        Transform3D rotation = new Transform3D();
        translation.set(new Vector3d(0.0, 0.0, 0.0));
        rotation.rotX(0.0);
        TransformGroup translateGroup = new TransformGroup(translation);
        TransformGroup rotateGroup = new TransformGroup(rotation);

        Shape3D arrowAf = Arrow.createArrow(SSE.UP, new Point3d(), filledApp);
        Shape3D arrowAw = Arrow.createArrow(SSE.UP, new Point3d(), wireApp);

        // Shape3D arrowBf = a.createArrow(filledApp);
        // Shape3D arrowBw = a.createArrow(wireApp);

        TransformGroup sheet = new TransformGroup();
        sheet.addChild(arrowAf);
        sheet.addChild(arrowAw);
        // sheet.addChild(arrowBf);
        // sheet.addChild(arrowBw);
        sheet.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        sheet.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        sheet.setCapability(Node.ENABLE_PICK_REPORTING);

        rotateGroup.addChild(sheet);
        translateGroup.addChild(rotateGroup);

        objRoot.addChild(translateGroup);

        // Have Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    }

    public HelloUniverse() {
    }

    @Override
    public void init() {
        this.setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse
                .getPreferredConfiguration();

        Canvas3D c = new Canvas3D(config);
        this.add("Center", c);

        // Create a simple scene and attach it to the virtual universe
        BranchGroup scene = this.createSceneGraph();
        this.u = new SimpleUniverse(c);
        this.u.addBranchGraph(scene);

        OrbitBehavior ob = new OrbitBehavior();
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.5, 0.0, 0.0),
                50.0);
        ob.setSchedulingBounds(bounds);
        ob.setRotationCenter(new Point3d(0.5, 0.0, 0.0));
        ob.setReverseRotate(true);
        ob.setReverseTranslate(true);

        this.u.getViewingPlatform().setViewPlatformBehavior(ob);
        this.u.getViewingPlatform().setNominalViewingTransform();
    }

    @Override
    public void destroy() {
        this.u.cleanup();
    }

    public static void main(String[] args) {
        new MainFrame(new HelloUniverse(), 256, 256);
    }
}
