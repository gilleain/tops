package tops.view.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.view.cartoon.CartoonDrawer;


/**
 * @author maclean
 *
 */
public class Cartoonist {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String outputType = args[0];
        String topsFilepath = args[1];
        String outputFilepath = args[2];

        int w = 300;
        int h = 200;

        System.err.println("Making " + outputType + " file");
        
        CartoonDrawer drawer = new CartoonDrawer();
        
        try {
            Protein protein = new Protein(new File(topsFilepath));
            Vector<DomainDefinition> dd = protein.getDomainDefs();
            Vector<SecStrucElement> ll = protein.getLinkedLists();

            if (outputType.equalsIgnoreCase("pdf") || outputType.equalsIgnoreCase("img")) {
                FileOutputStream fos = new FileOutputStream(outputFilepath);
                
                for (int i = 0; i < dd.size(); i++) {
//                    DomainDefinition d = (DomainDefinition) dd.get(i);
                    SecStrucElement root = (SecStrucElement) ll.get(i);
                    drawer.draw(topsFilepath, outputType, w, h, root, fos);
                }
                
            } else if (outputType.equalsIgnoreCase("svg") || outputType.equalsIgnoreCase("ps")) {
                PrintWriter pw = new PrintWriter(outputFilepath);

                for (int i = 0; i < dd.size(); i++) {
//                    DomainDefinition d = (DomainDefinition) dd.get(i);
                    SecStrucElement root = (SecStrucElement) ll.get(i);
                    drawer.draw(topsFilepath, outputType, root, pw);
                }
                pw.flush();
                pw.close();
            }
        
        } catch (TopsFileFormatException tffe) {
            System.err.println(tffe.toString());
        } catch (FileNotFoundException fnf) {
            System.err.println(fnf.toString());
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

}
