package tops.cli.engine.drg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.drg.Comparer;
import tops.engine.drg.Matcher;

public class CompareCommand implements Command {
    
    @Override
    public String getDescription() {
        return "DRG Compare?";
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private String getExamples(String[] args, List<String> examples) {
        String mode = "";
        if (args[0].equals("-f")) {     //READ FROM A FILE
            String line;
            String filename = args[1];
            try {
                BufferedReader buff = new BufferedReader(new FileReader(filename));
                while ((line = buff.readLine()) != null) {
                    examples.add(line);
                }
                buff.close();
            } catch (IOException ioe) { System.out.println(ioe); }
            mode = args[2];
        } else if (args[0].equals("-s")) { //USE A STRING
            examples.add(args[1]);
            mode = args[2]; 
        } else if (args[0].equals("--")) {  //-- TODO -- INTENDED TO PIPE IN DATA
            mode = args[1];
        }
        
        return mode;
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
        if (args.length == 0) {
            System.out.println("-f <file> OR -s <string> OR -d <string> OR -m <file> OR -c <file> <string>");
            System.exit(0);
        }
        List<String> examples = new ArrayList<String>();
        //this is the second flag, depending on whether file is specified or not
        String mode = getExamples(args, examples);   
       
        try {
            if (mode.equals("-r")) {        //REPRODUCE A LIST
                reproduceList(examples);
            } else if (mode.equals("-m")) { //MATCH A FILE OF PATTERNS TO A TARGET (ARGS[2])
                if (args.length < 2) System.out.println("please supply filename and probe");
                String target = args[3];
                Matcher ma = new Matcher();
                List<String> results = ma.run(examples, target);
                for (int k = 0; k < results.size(); k++) {
                    System.out.println(results.get(k));
                }
            } else if (mode.equals("-g")) {     //FIND A PATTERN FOR A GROUP
                Comparer ex = new Comparer();
                String result = ex.findPatternAndDoCompression(examples, false);
                System.out.println(result);
            } else if (mode.equals("-gi")) {        //FIND A PATTERN FOR A GROUP (with inserts)
                Comparer ex = new Comparer();
                String result = ex.findPatternAndDoCompression(examples, true);
                System.out.println(result);
            } else if (mode.equals("-c")) {     //COMPARE ARG[2] TO THE CONTENTS OF ARG[1]
                Comparer ex = new Comparer();
                List<Result> results = ex.compare(args[3], examples);
                for (Result result : results) {
                    System.out.println(result);
                }
            } else if (mode.equals("-p")) { // COMPARE FIRST IN A LIST TO THE REST OF THE LIST, PAIRWISE
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
}
