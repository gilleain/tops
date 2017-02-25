package tops.cli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import tops.view.app.TParser;
import tops.view.diagram.DiagramDrawer;

public class DiagramCommand extends Command {
    
    @Override
    public String getDescription() {
        return "Draw a tops graph as an image";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        CLIHandler handler = new CLIHandler().processArguments(args);
        
        DiagramDrawer drawer = new DiagramDrawer(handler.width, handler.height);
        
        try {
            Reader reader;
            if (handler.fileString.equals("-")) {
                reader = new InputStreamReader(System.in);
            } else {
                reader = new FileReader(handler.fileString);
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
    
    private class CLIHandler extends BaseCLIHandler {
        
        private int width = 300;
        
        private int height = 200;
        
        private String fileString; // either the file name or a string
        
        private Options options;
        
        public CLIHandler() {
            options = new Options();
//            options.addOption(opt("h", "Print help")); // XXX - conflicts with height!!
            options.addOption(opt("w", "width", "Diagram width"));
            options.addOption(opt("h", "height", "Diagram height"));
        }
        
        public CLIHandler processArguments(String[] args) throws ParseException {
            PosixParser parser = new PosixParser();
            CommandLine line = parser.parse(options, args, true);
            
            if (line.hasOption("w")) {
                width = Integer.parseInt(line.getOptionValue("w"));
            }
            
            if (line.hasOption("h")) {
                height = Integer.parseInt(line.getOptionValue("h"));
            }
            
            @SuppressWarnings("unchecked")
            List<String> remainingArgs = (List<String>)line.getArgList();
            fileString = remainingArgs.get(0);
            
            return this;
        }
        
    }

}
