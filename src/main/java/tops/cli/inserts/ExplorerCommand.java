package tops.cli.inserts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.engine.PlainFormatter;
import tops.engine.Result;
import tops.engine.TParser;
import tops.engine.TabFormatter;
import tops.engine.TopsStringFormatException;
import tops.engine.helix.Explorer;
import tops.engine.helix.Matcher;

public class ExplorerCommand extends BaseCommand {
    
    private Logger log = Logger.getLogger(ExplorerCommand.class.getName());

    @Override
    public String getDescription() {
        return "Explorer ... "; // TODO
    }

    @Override
    public String getHelp() {
        return "-f <file> OR -s <string> OR -d <string> OR -m <file> OR -c <file> <string>";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        if (args.length == 0) {
            System.exit(0);
        }
        
        // this is the second flag, depending on whether file is specified or not
        String mode = "";
        List<String> examples = new ArrayList<>(); // shared between the argument modes

        boolean matcherLogging = false;
        boolean explorerDebugLogging = false;

        if (args[0].equals("-f")) { // READ FROM A FILE
            String line;
            String filename = args[1];
            try (BufferedReader buff = new BufferedReader(new FileReader(filename))) {
                while ((line = buff.readLine()) != null) {
                    examples.add(line);
                }
            } catch (IOException ioe) {
                error(ioe);
                return;
            }
            mode = args[2];
        } else if (args[0].equals("-s")) { // USE A STRING
            matcherLogging = true;
            explorerDebugLogging = true;
            examples.add(args[1]);
            mode = args[2];
        } else if (args[0].equals("--")) { // -- TODO -- INTENDED TO PIPE IN DATA
            mode = args[1];
        }

        Explorer ex = new Explorer();
        
        if (explorerDebugLogging) { // TODO - clashing logging techniques...
            log.setLevel(Level.ALL);
            log.addHandler(new StreamHandler(System.err, new PlainFormatter()));
            log.setUseParentHandlers(false);
        } else {
            log.addHandler(new StreamHandler(System.out, new TabFormatter()));
            log.setUseParentHandlers(false);
        }

        try {
            if (mode.equals("-r")) { // REPRODUCE A LIST
                for (int i = 0; i < examples.size(); i++) {
                    String example = examples.get(i);
                    output("input....." + example);
                    ex.reproduce(example);
                    output("\n");
                }
            } else if (mode.equals("-m")) { // MATCH A FILE OF PATTERNS TO A TARGET (ARGS[2])
                if (args.length < 2)
                    error("please supply filename and probe");
                String target = args[3];
                Matcher ma = new Matcher();
                String[] patterns = examples.toArray(new String[0]);
                String[] results = ma.run(patterns, target);
                for (int k = 0; k < results.length; k++) {
                    output(results[k]);
                }
            } else if (mode.equals("-g")) { // FIND A PATTERN FOR A GROUP

                String result = ex.findPattern(examples.toArray(new String[0]), matcherLogging);
                output(result);
            } else if (mode.equals("-c")) { // COMPARE ARG[2] TO THE CONTENTS OF ARG[1]
                String probe = args[3];
                String[] exampleStrings = examples.toArray(new String[0]);
                Result[] results = ex.compare(exampleStrings, probe, matcherLogging);
                for (int i = 0; i < results.length; i++) {
                    output(results[i].toString());
                }
            } else if (mode.equals("-a")) { // ALL AGAINST ALL OF A FILE
                List<String[]> pairList = new ArrayList<>();
                Map<String, String> instMap = new HashMap<>();

                // use a map for the strings (more efficient lookup?)
                List<String> tmpList = new ArrayList<>();
                TParser tp = new TParser();

                for (String example : examples) {
                    tp.load(example);
                    String name = tp.getName();
                    tmpList.add(name);
                    instMap.put(name, example); // KEY is the head/domId
                }
                boolean verbose = false;
                // handle the indices to the loops
                int lowerindex;
                int upperindex;
                if (args.length > 3) {
                    lowerindex = Integer.parseInt(args[3]);
                    upperindex = Integer.parseInt(args[4]);
                    if (args[5].equals("v")) {
                        verbose = true;
                    }
                    // special case where this instance will be doing all the
                    // comparisons
                    if (lowerindex == -1)
                        lowerindex = 0;
                    if (upperindex == -1)
                        upperindex = tmpList.size();
                } else {
                    lowerindex = 0;
                    upperindex = tmpList.size();
                }

                for (int i = lowerindex; i < upperindex; i++) {
                    for (int j = i + 1; j < tmpList.size(); j++) {
                        if (i != j) {
                            String[] miniContainer = { tmpList.get(i), tmpList.get(j) };
                            pairList.add(miniContainer);
                        }
                    }
                }
                error("NOTE : " + pairList.size() + " comparisons!");
                ex.allVsAll(pairList, instMap, verbose);
            } else if (mode.equals("-p")) { // COMPARE FIRST IN A LIST TO THE
                                            // REST OF THE LIST, PAIRWISE
                String first = examples.get(0);
                ex.compare(examples.toArray(new String[0]), first, matcherLogging);
            } else if (mode.equals("-v")) {
                String pairFilename = args[3];
                String verboseFlag = args[4];
                
                versus(pairFilename, examples, verboseFlag);
            } else {
                error("-f <file> or -s <string>");
            }
        } catch (TopsStringFormatException tsfe) {
            error(tsfe);
        }
    }
    
    private void versus(String pairFilename, List<String> examples, String verboseFlag) throws TopsStringFormatException {
        Explorer ex = new Explorer();
        List<String[]> pairList = new ArrayList<>();
        Map<String, String> instMap = new HashMap<>();
       
        String line;

        try(BufferedReader buff = new BufferedReader(new FileReader(pairFilename))) {
            while ((line = buff.readLine()) != null) {
                int tab = line.indexOf('\t');
                String first = line.substring(0, tab);
                String second = line.substring(tab + 1, line.indexOf('\t', tab + 1));
                String[] miniContainer = { first, second };
                pairList.add(miniContainer);
            }
        } catch (IOException ioe) {
            error(ioe);
        }

        // use a map for the strings (more efficient lookup?)
        TParser tp = new TParser();
        for(String nextLine : examples) {
            tp.load(nextLine);
            String name = tp.getName();
            instMap.put(name, nextLine); // KEY is the head/domId
        }

        boolean isVerbose = verboseFlag.equals("-v");
        ex.allVsAll(pairList, instMap, isVerbose);
    }

}
