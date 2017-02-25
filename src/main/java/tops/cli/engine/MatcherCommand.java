package tops.cli.engine;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.drg.Matcher;

public class MatcherCommand extends Command {

    @Override
    public String getDescription() {
        return "Matching";  // XXX TODO
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String pattern = args[0];
        String filename = args[1];
        
        Matcher m = new Matcher();
        m.runToStdOut(filename, pattern);
    }

}
