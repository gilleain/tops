package tops.view.tops3D;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class AWTZoomBehavior extends Behavior implements ActionListener {

    private TransformGroup transformGroup;

    private Transform3D trans = new Transform3D();

    private WakeupCriterion criterion;

    private Transform3D currXform = new Transform3D();

    // create a new AWTInteractionBehavior
    public AWTZoomBehavior(TransformGroup tg) {
        this.transformGroup = tg;
    }

    // initialize the behavior to wakeup on a behavior post with the id
    // MouseEvent.MOUSE_CLICKED
    @Override
    public void initialize() {
        this.criterion = new WakeupOnBehaviorPost(this, MouseEvent.MOUSE_CLICKED);
        this.wakeupOn(this.criterion);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void processStimulus(Enumeration criteria) {

        this.trans.setScale(0.9);

        this.transformGroup.getTransform(this.currXform);
        Matrix4d mat = new Matrix4d();
        // Remember old matrix
        this.currXform.get(mat);

        // Translate to origin
        this.trans.setTranslation(new Vector3d(0.0, 0.0, 0.0));
        this.currXform.mul(this.trans, this.currXform);

        this.transformGroup.setTransform(this.currXform);
        this.wakeupOn(this.criterion);
    }

    // when the mouse is clicked, postId for the behavior
    public void actionPerformed(ActionEvent e) {
        this.postId(MouseEvent.MOUSE_CLICKED);
    }
}