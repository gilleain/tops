package tops.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

public abstract class Command {
    
    public abstract void handle(String[] args) throws ParseException;
    
    public class CLIHandler {
        
        @SuppressWarnings("static-access")
        public Option opt(String o, String desc) {
            return OptionBuilder.withDescription(desc).create(o);
        }
        
        @SuppressWarnings("static-access")
        public Option opt(String o, String argName, String desc) {
            return OptionBuilder.hasArg()
                                .withDescription(desc)
                                .withArgName(argName)
                                .create(o);
        }
    }

}
