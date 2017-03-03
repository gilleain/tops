package tops.cli.drawing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import tops.cli.BaseCLIHandler;
import tops.cli.Command;
import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsFileFormatException;
import tops.view.cartoon.CartoonDrawer;

public class CartoonCommand implements Command {
    
    @Override
    public String getDescription() {
        return "Draw a tops cartoon as an image";
    }
    
    public void handle(String[] args) throws ParseException {
        CLIHandler handler = new CLIHandler().processArguments(args);
        
        int w = 300;
        int h = 200;

        System.err.println("Making " + handler.outputType 
                        + " file for " + handler.topsFilepath
                        + " in " + handler.outputFilepath);
        
        CartoonDrawer drawer = new CartoonDrawer();
        
        try {
            Protein protein = new Protein(new File(handler.topsFilepath));
            Vector<DomainDefinition> dd = protein.getDomainDefs();
            Vector<SecStrucElement> ll = protein.getLinkedLists();

            if (handler.outputType.equals("PDF") || handler.outputType.equals("IMG")) {
                FileOutputStream fos = new FileOutputStream(handler.outputFilepath);
                
                for (int i = 0; i < dd.size(); i++) {
//                    DomainDefinition d = (DomainDefinition) dd.get(i);
                    SecStrucElement root = ll.get(i);
                    drawer.draw(handler.topsFilepath, handler.outputType, w, h, root, fos);
                }
                
            } else if (handler.outputType.equals("SVG") || handler.outputType.equals("PS")) {
                PrintWriter pw = new PrintWriter(handler.outputFilepath);

                for (int i = 0; i < dd.size(); i++) {
//                    DomainDefinition d = (DomainDefinition) dd.get(i);
                    SecStrucElement root = ll.get(i);
                    drawer.draw(handler.topsFilepath, handler.outputType, root, pw);
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
    
    private class CLIHandler extends BaseCLIHandler {

        private Options options;
        
        private String outputType;
        
        private String topsFilepath;
        
        private String outputFilepath;
        
        public CLIHandler() {
            options = new Options();
            options.addOption(opt("h", "Print help"));
            options.addOption(opt("o", "type", "Output type"));
            options.addOption(opt("t", "filepath", "Tops filepath"));
            options.addOption(opt("f", "filepath", "Output filepath"));
        }
        
        public CLIHandler processArguments(String[] args) throws ParseException {
            PosixParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args, true);
            
            if (line.hasOption("h")) {
                printHelp();
                return this;
            }
            
            if (line.hasOption("o")) {
                outputType = line.getOptionValue("o").toUpperCase();
            }
            
            if (line.hasOption("t")) {
                topsFilepath = line.getOptionValue("t");
            }
            
            if (line.hasOption("f")) {
                outputFilepath = line.getOptionValue("f");
            }
            
            return this;
        }
        
        private void printHelp() {
            System.err.println("Help");
        }
        
    }

}
