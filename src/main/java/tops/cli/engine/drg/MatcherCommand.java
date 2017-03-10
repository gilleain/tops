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
import tops.engine.TopsStringFormatException;
import tops.engine.drg.Matcher;
import tops.engine.drg.Pattern;

public class MatcherCommand implements Command {
    
    public static final String KEY = "drg-matcher";

    @Override
    public String getDescription() {
        return "DRG Matching";
    }

    @Override
    public String getHelp() {
        return new CLIHandler().getHelp(KEY);
    }

    @Override
    public void handle(String[] args) throws ParseException {
        CLIHandler handler = new CLIHandler().processArguments(args);
        Matcher m = new Matcher();
        try {
            if (handler.pattern == null) {
                runMultiplePatterns(m, handler.patternFilename, handler.target);
            } else {
                runSinglePattern(m, handler.pattern, handler.targetFilename);
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe.getMessage());
            tsfe.printStackTrace();
        }
    }
    
    public void runSinglePattern(Matcher m, String pattern, String targetFile) throws TopsStringFormatException, IOException {
        Pattern p = new Pattern(pattern);
        BufferedReader bu = new BufferedReader(new FileReader(targetFile));
        String line;
        while ((line = bu.readLine()) != null) {
            String result = m.match(p, new Pattern(line));
            if (!result.equals("")) {
                System.out.println(result);
            }
        }
        bu.close();
    }
    
    public void runMultiplePatterns(Matcher m, String patternFile, String target) throws TopsStringFormatException, IOException  {
        BufferedReader bu = new BufferedReader(new FileReader(patternFile));
        List<String> patterns = new ArrayList<String>();
        String line;
        while ((line = bu.readLine()) != null) {
            patterns.add(line);
        }
        List<String> results = m.run(patterns, target);
        for (int k = 0; k < results.size(); k++) {
            System.out.println(results.get(k));
        }
        bu.close();
    }
    
    private class CLIHandler extends BaseCLIHandler {
        
        private String pattern;
        private String patternFilename;
        private String targetFilename;
        private String target;
        
        public CLIHandler() {
            opt("p", "pattern", "Input pattern");
            opt("pf", "pattern", "Filename of patterns");
            opt("tf", "targetFilename", "Filename of targets");
            opt("t", "target", "Target");
        }
        
        public CLIHandler processArguments(String[] args) throws ParseException {
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args, true);
            
            if (line.hasOption("p")) {
                pattern = line.getOptionValue("p");
            }
            
            if (line.hasOption("pf")) {
                patternFilename = line.getOptionValue("pf");
            }
            
            if (line.hasOption("tf")) {
                targetFilename = line.getOptionValue("tf");
            }
            
            if (line.hasOption("t")) {
                target = line.getOptionValue("t");
            }
    
            return this;
        }
    }
}
