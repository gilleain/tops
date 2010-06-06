package tops.view.tops2D.app;

import java.awt.Graphics;

import javax.swing.JPanel;

import tops.view.tops2D.diagram.DiagramDrawer;

public class LinearViewPanel extends JPanel {

    private int width, height;

    private DiagramDrawer dd;

    public LinearViewPanel() {
        this.width = 0;
        this.height = 0;
        this.dd = null;
    }

    public LinearViewPanel(int width, int height) {
        this.width = width;
        this.height = height;
        this.dd = new DiagramDrawer(this.width, this.height);
    }
    
    public void setShowLabelNumbers(boolean val) {
        this.dd.setShowLabelNumbers(val);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if ((this.width == 0) || (this.height == 0)) {
            this.width = this.getWidth();
            this.height = this.getHeight();
        } else {
            if (this.dd == null) {
                this.dd = new DiagramDrawer(this.width, this.height);
            }
            this.dd.paint(g);
        }
    }
    
    public void renderGraph(String vStr, String eStr, String hStr) {
        if (this.dd != null) {
            this.dd.setData(vStr, eStr, hStr);
        }
    }
}
