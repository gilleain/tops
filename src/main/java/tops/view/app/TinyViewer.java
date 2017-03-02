package tops.view.app;

import javax.swing.JFrame;

/**
 * A very simple viewer for TOPS strings. Given a string and - optionally -
 * width and height parameters, it will display the graph on a LinearViewPanel.
 * 
 * @author GMT
 * @see tops.view.app.LinearViewPanel
 */
public class TinyViewer extends JFrame {

    private LinearViewPanel view;

    /**
     * create a new TinyViewer to show these vertices and edges
     * 
     * @param name
     *            the name of the structure
     * @param vertices
     *            the vertex string
     * @param edges
     *            the edge string
     * @param width
     *            the width of both the Panel and the Frame
     * @param height
     *            the height of both the Panel and the Frame
     */
    public TinyViewer(String name, String vertices, String edges, int width, int height) {
        super(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.view = new LinearViewPanel(width, height);
        this.view.setSize(width, height);
        this.view.setShowLabelNumbers(true);
        this.getContentPane().add(this.view);
        this.view.renderGraph(vertices, edges, null);
        this.setSize(width, height);
        this.setVisible(true);
    }
}
