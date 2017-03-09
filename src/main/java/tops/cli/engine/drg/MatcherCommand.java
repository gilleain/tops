package tops.cli.engine.drg;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import tops.cli.BaseCLIHandler;
import tops.cli.Command;
import tops.engine.drg.Matcher;

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
        m.runToStdOut(handler.filename, handler.pattern);
    }
    
    private class CLIHandler extends BaseCLIHandler {
        
        private String pattern;
        private String filename;
        
        public CLIHandler() {
            opt("p", "pattern", "Input pattern");
            opt("f", "filename", "Filename of targets");
        }
        
        public CLIHandler processArguments(String[] args) throws ParseException {
            DefaultParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args, true);
            
            if (line.hasOption("p")) {
                pattern = line.getOptionValue("p");
            }
            
            if (line.hasOption("f")) {
                filename = line.getOptionValue("f");
            }
    
            return this;
        }
    }
}
