package tops.cli.engine.drg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import tops.cli.BaseCLIHandler;
import tops.cli.Command;
import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.drg.Comparer;

public class CompareCommand implements Command {
    
    public static final String KEY = "drg-compare";
    
    @Override
    public String getDescription() {
        return "Various comparison commands";
    }

    @Override
    public String getHelp() {
        return new CLIHandler().getHelp(KEY);
    }
    
    private List<String> getExamples(CLIHandler handler) {
        List<String> examples = new ArrayList<String>();
        if (handler.filename != null) {             // READ FROM A FILE
            getExamples(handler.filename, examples);
        } else if (handler.patternString != null) { // USE A STRING
            examples.add(handler.patternString);
        }
        
        return examples;
    }
    
    private void getExamples(String filename, List<String> examples) {
        if (filename.equals("--")) {
            // TODO - pipe data
        } else {
            String line;
            try {
                BufferedReader buff = new BufferedReader(new FileReader(filename));
                while ((line = buff.readLine()) != null) {
                    examples.add(line);
                }
                buff.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
    }
    
    private void reproduceList(List<String> examples) throws TopsStringFormatException {
        Comparer ex = new Comparer();
        for (int i = 0; i < examples.size(); i++) {
            String example = examples.get(i);
            System.out.println("input....." + example);
            ex.reproduce(example);
            System.out.println();
            ex.clear();
        }
    }

    @Override
    public void handle(String[] args) throws ParseException {
        CLIHandler handler = new CLIHandler().processArguments(args);
        
        //this is the second flag, depending on whether file is specified or not
        List<String> examples = getExamples(handler);   
       
        try {
            if (handler.reproduceMode) {                // REPRODUCE A LIST
                reproduceList(examples);
            } else if (handler.groupMode) {             // FIND A PATTERN FOR A GROUP
                Comparer ex = new Comparer();
                System.out.println(ex.findPatternAndDoCompression(examples, false));
            } else if (handler.groupWithInsertsMode) {  // FIND A PATTERN FOR A GROUP (with inserts)
                Comparer ex = new Comparer();
                System.out.println(ex.findPatternAndDoCompression(examples, true));
            } else if (handler.compareMode) {           // COMPARE ARG[2] TO THE CONTENTS OF ARG[1]
                Comparer ex = new Comparer();
                List<Result> results = ex.compare(handler.patternString, examples);
                for (Result result : results) {
                    System.out.println(result);
                }
            } else if (handler.pairwiseMode) {          // COMPARE FIRST IN A LIST TO 
                                                        // THE REST OF THE LIST, PAIRWISE
                String first = examples.get(0);
                Comparer ex = new Comparer();
                ex.compare(first, examples);
            } else {
                System.out.println("-f <file> or -s <string>");
            }
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe.getMessage());
            tsfe.printStackTrace();
        }
    }
    
    private class CLIHandler extends BaseCLIHandler {
        private String filename;
        private String patternString;
        private boolean compareMode;
        private boolean reproduceMode;
        private boolean groupMode;
        private boolean groupWithInsertsMode;
        private boolean pairwiseMode;
        
        public CLIHandler() {
            opt("f", "filename", "Tops strings filename");
            opt("c", "Compare");
            opt("r", "Reproduce");
            opt("g", "Group");
            opt("gi", "Group with inserts");
            opt("p", "Pairwise mode");
        }
        
        public CLIHandler processArguments(String[] args) throws ParseException {
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args, true);
            
            if (line.hasOption("f")) {
                filename = line.getOptionValue("f");
            }
            
            if (line.hasOption("c")) {
                compareMode = line.hasOption("c");
            }
            
            if (line.hasOption("r")) {
                reproduceMode = line.hasOption("r");
            }
            
            if (line.hasOption("g")) {
                groupMode = line.hasOption("g");
            }
            
            if (line.hasOption("gi")) {
                groupWithInsertsMode = line.hasOption("g");
            }
            
            if (line.hasOption("p")) {
                pairwiseMode = line.hasOption("p");
            }
            
            return this;
        }
        
    }
        
}
