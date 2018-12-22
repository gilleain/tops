package tops.cli.drawing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import tops.cli.BaseCLIHandler;
import tops.cli.BaseCommand;
import tops.dw.io.TopsFileReader;
import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.dw.protein.TopsFileFormatException;
import tops.port.model.DomainDefinition;
import tops.view.cartoon.CartoonDrawer;

public class CartoonCommand extends BaseCommand {
    
    public static final String KEY = "cartoon";
    
    @Override
    public String getDescription() {
        return "Draw a tops cartoon as an image";
    }

    @Override
    public String getHelp() {
        return new CLIHandler().getHelp(KEY);
    }
    
    public void handle(String[] args) throws ParseException {
        CLIHandler handler = new CLIHandler().processArguments(args);
        
        final int w = 300;
        final int h = 200;

        error("Making " + handler.outputType 
                        + " file for " + handler.topsFilepath
                        + " in " + handler.outputFilepath);
        
        CartoonDrawer drawer = new CartoonDrawer();
        
        try {
            TopsFileReader topsFileReader = new TopsFileReader();
            Protein protein = topsFileReader.readTopsFile(new File(handler.topsFilepath));

            if (handler.outputType.equals("PDF") || handler.outputType.equals("IMG")) {
                handleBinaryFormat(handler, protein, drawer, w, h);
            } else if (handler.outputType.equals("SVG") || handler.outputType.equals("PS")) {
                handleVectorFormat(handler, protein, drawer);
            }
        
        } catch (TopsFileFormatException tffe) {
            error(tffe.toString());
        } catch (FileNotFoundException fnf) {
            error(fnf.toString());
        } catch (IOException ioe) {
            error(ioe.toString());
        }
    }
    
    private void handleBinaryFormat(CLIHandler handler, Protein protein, CartoonDrawer drawer, int w, int h) {
        List<DomainDefinition> dd = protein.getDomainDefs();
        List<Cartoon> ll = protein.getLinkedLists();
        try (FileOutputStream fos = new FileOutputStream(handler.outputFilepath)) {
        
            for (int i = 0; i < dd.size(); i++) {
                Cartoon cartoon = ll.get(i);
                drawer.draw(handler.topsFilepath, handler.outputType, w, h, cartoon, fos);
            }
        } catch (IOException ioe) {
            error(ioe);
        }
    }
    
    private void handleVectorFormat(CLIHandler handler, Protein protein,  CartoonDrawer drawer) throws IOException {
        List<DomainDefinition> dd = protein.getDomainDefs();
        List<Cartoon> ll = protein.getLinkedLists();
        try (PrintWriter pw = new PrintWriter(handler.outputFilepath)) {

            for (int i = 0; i < dd.size(); i++) {
                Cartoon cartoon = ll.get(i);
                drawer.draw(handler.topsFilepath, handler.outputType, cartoon, pw);
            }
            pw.flush();
        }
    }
    
    private class CLIHandler extends BaseCLIHandler {
        
        private String outputType;
        
        private String topsFilepath;
        
        private String outputFilepath;
        
        public CLIHandler() {
            opt("h", "Print help");
            opt("o", "type", "Output type");
            opt("t", "filepath", "Tops filepath");
            opt("f", "filepath", "Output filepath");
        }
        
        public CLIHandler processArguments(String[] args) throws ParseException {
            DefaultParser parser = new DefaultParser();
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
            error("Help");
        }
        
    }
}
