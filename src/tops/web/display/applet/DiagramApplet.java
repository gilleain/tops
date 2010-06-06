package tops.web.display.applet;

import java.applet.Applet;

import java.awt.Graphics;
import java.awt.Graphics2D;

import tops.view.tops2D.diagram.DiagramDrawer;

/**
 * Diagram display applet that takes TOPS strings and displays as linear graphs.
 * 
 * @author maclean
 */
public class DiagramApplet extends Applet {

    String head;

    DiagramDrawer dd;

    /**
     * get the applet parameters, parse them, and store them in arrays of ints,
     * chars, and booleans.
     */
    @Override
    public void init() {
        int w = this.getWidth();
        int h = this.getHeight();
        this.head = this.getParameter("head");
        String body = this.getParameter("body");
        String tail = this.getParameter("tail");
        String matches = this.getParameter("match");
        this.dd = new DiagramDrawer(body, tail, matches, w, h);
    }

    @Override
    public void paint(Graphics g) {
        g.drawString(this.head, 20, 10);
        this.dd.paint((Graphics2D) g);
    }
}
