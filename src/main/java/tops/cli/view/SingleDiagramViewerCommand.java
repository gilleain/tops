package tops.cli.view;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.view.app.TParser;
import tops.view.app.TinyViewer;

public class SingleDiagramViewerCommand implements Command {

    private static int DEFAULT_WIDTH = 300;

    private static int DEFAULT_HEIGHT = 200;

    @Override
    public String getDescription() {
        return "View single diagrams";
    }

    /**
     * The main method. It attempts to do some parameter analysis by looking at
     * the length of the argument vector. If there is only one argument, it is
     * assumed to be a string in comments ""; if there are three, it is assumed
     * that the string is uncommented (like : head vertex_string tail); if there
     * are five arguments, it assumes that the last two are width and height.
     * 
     * @param args the argument vector
     */
    @Override
    public void handle(String[] args) throws ParseException {
        if (args.length == 0) {
            System.out.println("Usage : 'TinyViewer <string>' where '<string>' is a TOPS string");
        }

        String name = null;
        String vertices = null;
        String edges = null;
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

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
