package tops.view.diagram;

import java.awt.Shape;
import java.awt.geom.GeneralPath;


public class Strand extends OrientedVertex {

    public Strand(boolean isDown, int position) {
        super(position, isDown);
    }

    @Override
    public Shape createShape() {
        int bx = (int) (this.bb.getX());
        int by = (int) (this.bb.getY());
        int bh = (int) (this.bb.getHeight());
        int bw = (int) (this.bb.getWidth());
        int pnty = (this.isDown()) ? (by + bh) : by;

        GeneralPath gp = new GeneralPath();
        gp.moveTo(bx + (bw / 2), pnty);
        gp.lineTo(bx + bw, by + (bh / 2));
        gp.lineTo(bx, by + (bh / 2));
        gp.closePath();
        return gp;
    }

}
