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

    private static int DEFAULT_WIDTH = 300;

    private static int DEFAULT_HEIGHT = 200;

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

    /**
     * The main method. It attempts to do some parameter analysis by looking at
     * the length of the argument vector. If there is only one argument, it is
     * assumed to be a string in comments ""; if there are three, it is assumed
     * that the string is uncommented (like : head vertex_string tail); if there
     * are five arguments, it assumes that the last two are width and height.
     * 
     * @param args
     *            the argument vector
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage : 'TinyViewer <string>' where '<string>' is a TOPS string");
        }

        String name = null;
        String vertices = null;
        String edges = null;
        int width = TinyViewer.DEFAULT_WIDTH;
        int height = TinyViewer.DEFAULT_HEIGHT;

        if (args.length == 1) { // we assume that all parts are in the same string
            String topsString = args[0];
            TParser parser = new TParser(topsString);
            name = parser.getName();
            vertices = parser.getVertexStringSafely();
            edges = parser.getEdgeString();

        }

        if (args.length == 3) { // we assume that each part is a separate
                                // string, or a string and 2 numbers
            try {
                width = Integer.parseInt(args[1]);
                height = Integer.parseInt(args[2]);
                String topsString = args[0];
                TParser parser = new TParser(topsString);
                name = parser.getName();
                vertices = parser.getVertexStringSafely();
                edges = parser.getEdgeString();
            } catch (NumberFormatException nfe) {
                name = args[0];
                vertices = args[1];
                edges = args[2];
                TParser parser = new TParser(name + " " + vertices + " " + edges);
                vertices = parser.getVertexStringSafely();
            }
        }

        if (args.length == 5) { // we assume that each part is a separate string
            name = args[0];
            vertices = args[1];
            edges = args[2];
            TParser parser = new TParser(name + " " + vertices + " " + edges);
            vertices = parser.getVertexStringSafely();
            width = Integer.parseInt(args[3]);
            height = Integer.parseInt(args[4]);
        }

        new TinyViewer(name, vertices, edges, width, height);
    }
}
