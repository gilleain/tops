package tops.cli.inserts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.PlainFormatter;
import tops.engine.Result;
import tops.engine.TParser;
import tops.engine.TabFormatter;
import tops.engine.TopsStringFormatException;
import tops.engine.helix.Explorer;
import tops.engine.helix.Matcher;

public class ExplorerCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        if (args.length == 0) {
            System.out.println("-f <file> OR -s <string> OR -d <string> OR -m <file> OR -c <file> <string>");
            System.exit(0);
        }
        String mode = new String(); // this is the second flag, depending on
                                    // whether file is specified or not
        ArrayList<String> examples = new ArrayList<String>(); // shared between the argument
                                                // modes

        boolean matcherLogging = false;
        boolean explorerDebugLogging = false;

        if (args[0].equals("-f")) { // READ FROM A FILE
            String line;
            String filename = args[1];
            try {
                BufferedReader buff = new BufferedReader(new FileReader(filename));
                while ((line = buff.readLine()) != null) {
                    examples.add(line);
                }
                buff.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
            mode = args[2];
        } else if (args[0].equals("-s")) { // USE A STRING
            matcherLogging = true;
            explorerDebugLogging = true;
            examples.add(args[1]);
            mode = args[2];
        } else if (args[0].equals("--")) { // -- TODO -- INTENDED TO PIPE IN
                                            // DATA
            mode = args[1];
        }

        Explorer ex = new Explorer();
        Logger exLogger = Logger.getLogger("tops.engine.helix.Explorer");
        if (explorerDebugLogging) {
            exLogger.setLevel(Level.ALL);
            exLogger.addHandler(new StreamHandler(System.err,
                    new PlainFormatter()));
            exLogger.setUseParentHandlers(false);
        } else {
            exLogger.addHandler(new StreamHandler(System.out,
                    new TabFormatter()));
            exLogger.setUseParentHandlers(false);
        }

        try {
            if (mode.equals("-r")) { // REPRODUCE A LIST
                for (int i = 0; i < examples.size(); i++) {
                    String example = (String) examples.get(i);
                    System.out.println("input....." + example);
                    ex.reproduce(example);
                    System.out.println();
                }
            } else if (mode.equals("-m")) { // MATCH A FILE OF PATTERNS TO A
                                            // TARGET (ARGS[2])
                if (args.length < 2)
                    System.out.println("please supply filename and probe");
                String target = args[3];
                Matcher ma = new Matcher();
                String[] patterns = (String[]) examples.toArray(new String[0]);
                String[] results = ma.run(patterns, target);
                for (int k = 0; k < results.length; k++) {
                    System.out.println(results[k]);
                }
            } else if (mode.equals("-g")) { // FIND A PATTERN FOR A GROUP

                String result = ex.findPattern((String[]) examples
                        .toArray(new String[0]), matcherLogging);
                System.out.println(result);
            } else if (mode.equals("-c")) { // COMPARE ARG[2] TO THE CONTENTS OF
                                            // ARG[1]
                String probe = args[3];
                String[] exampleStrings = (String[]) examples
                        .toArray(new String[0]);
                Result[] results = ex.compare(exampleStrings, probe,
                        matcherLogging);
                for (int i = 0; i < results.length; i++) {
                    System.out.println(results[i]);
                }
            } else if (mode.equals("-a")) { // ALL AGAINST ALL OF A FILE
                ArrayList<String[]> pairList = new ArrayList<String[]>();
                HashMap<String, String> instMap = new HashMap<String, String>(3000);

                // use a map for the strings (more efficient lookup?)
                ArrayList<String> tmpList = new ArrayList<String>();
                TParser tp = new TParser();

                Iterator<String> itr = examples.iterator();
                while (itr.hasNext()) {
                    String nextLine = (String) itr.next();
                    tp.load(nextLine);
                    String name = tp.getName();
                    tmpList.add(name);
                    instMap.put(name, nextLine); // KEY is the head/domId
                }
                boolean verbose = false;
                // handle the indices to the loops
                int lowerindex, upperindex;
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
                            String[] miniContainer = { (String) tmpList.get(i),
                                    (String) tmpList.get(j) };
                            pairList.add(miniContainer);
                        }
                    }
                }
                System.err.println("NOTE : " + pairList.size()
                        + " comparisons!");
                ex.allVsAll(pairList, instMap, verbose);
            } else if (mode.equals("-p")) { // COMPARE FIRST IN A LIST TO THE
                                            // REST OF THE LIST, PAIRWISE
                String first = (String) examples.get(0);
                ex.compare((String[]) examples.toArray(new String[0]), first,
                        matcherLogging);
            } else if (mode.equals("-v")) {
                ArrayList<String[]> pairList = new ArrayList<String[]>();
                HashMap<String, String> instMap = new HashMap<String, String>(3000);
                String pairFilename = args[3];
                String verboseFlag = args[4];
                String line;

                try {
                    BufferedReader buff = new BufferedReader(new FileReader(pairFilename));
                    while ((line = buff.readLine()) != null) {
                        int tab = line.indexOf("\t");
                        String first = line.substring(0, tab);
                        String second = line.substring(tab + 1, line.indexOf(
                                "\t", tab + 1));
                        // String second = line.substring(tab + 1);
                        String[] miniContainer = { first, second };
                        pairList.add(miniContainer);
                    }
                    buff.close();
                } catch (IOException ioe) {
                    System.out.println(ioe);
                }

                // use a map for the strings (more efficient lookup?)
                TParser tp = new TParser();
                Iterator<String> itr = examples.iterator();
                while (itr.hasNext()) {
                    String nextLine = (String) itr.next();
                    tp.load(nextLine);
                    String name = tp.getName();
                    instMap.put(name, nextLine); // KEY is the head/domId
                }

                if (verboseFlag.equals("-v")) {
                    ex.allVsAll(pairList, instMap, true);
                } else {
                    ex.allVsAll(pairList, instMap, false);
                }
            } else {
                System.out.println("-f <file> or -s <string>");
            }
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe.getMessage());
            tsfe.printStackTrace();
        }
    }

}
