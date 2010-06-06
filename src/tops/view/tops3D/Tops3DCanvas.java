package tops.view.tops3D;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;

import javax.vecmath.Point3d;

public class Tops3DCanvas extends Canvas3D {

    private SimpleUniverse u = null;

    private Point3d center = new Point3d();

    public Tops3DCanvas() {
        super(SimpleUniverse.getPreferredConfiguration());
    }

    public void setCenter(Point3d center) {
        this.center = center;
    }

    public void setScene(BranchGroup scene) {
        BoundingSphere bounds = new BoundingSphere(this.center, 100.0);

        /*
         * DirectionalLight light = new DirectionalLight();
         * light.setInfluencingBounds(bounds); scene.addChild(light);
         * scene.compile();
         */
        this.u = new SimpleUniverse(this);
        this.u.addBranchGraph(scene);

        ViewingPlatform viewingPlatform = this.u.getViewingPlatform();
        this.u.getViewingPlatform().setNominalViewingTransform();
        this.setSize(100, 100);
        OrbitBehavior orbit = new OrbitBehavior(this, OrbitBehavior.REVERSE_ALL);

        orbit.setSchedulingBounds(bounds);
        this.center.scale(0.2f); // !!Danger
        orbit.setRotationCenter(this.center);

        // MouseZoom mz = new
        // MouseZoom(viewingPlatform.getViewPlatformTransform());
        // mz.setSchedulingBounds(bounds);

        KeyNavigatorBehavior keyNavigator = new KeyNavigatorBehavior(
                viewingPlatform.getViewPlatformTransform());
        keyNavigator.setSchedulingBounds(bounds);

        BranchGroup actions = new BranchGroup();
        actions.addChild(keyNavigator);
        this.u.addBranchGraph(actions);

        viewingPlatform.setViewPlatformBehavior(orbit);
    }

}
