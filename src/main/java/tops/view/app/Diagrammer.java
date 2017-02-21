package tops.view.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.Reader;

import tops.view.diagram.DiagramDrawer;

import java.io.IOException;

/**
 * @author maclean
 *
 */
public class Diagrammer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String string_filename = args[0];
        int width = 300;
        int height = 200;
        if (args.length > 1) {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
        }
        
        DiagramDrawer drawer = new DiagramDrawer(width, height);
        
        try {
            Reader reader;
            if (string_filename.equals("-")) {
                reader = new InputStreamReader(System.in);
            } else {
                reader = new FileReader(string_filename);
            }

            BufferedReader bufferedReader = new BufferedReader(reader);
            TParser tParser = new TParser();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                tParser.setCurrent(line);
                drawer.setData(tParser.getVertexString(), tParser.getEdgeString(), null);    

                FileWriter fileWriter = new FileWriter(tParser.getName() + ".eps");
                String postscript = drawer.toPostscript();
                fileWriter.write(postscript, 0, postscript.length());
                fileWriter.flush();
                fileWriter.close();
            }
            
        } catch (FileNotFoundException fnf) {
            System.err.println(fnf.toString());
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

}
