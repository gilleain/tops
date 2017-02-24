package tops.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

/**
 * A command is a single command-line app, run through {@link Tops}.
 * 
 * @author maclean
 *
 */
public abstract class Command {
    
    /**
     * @param args
     * @throws ParseException
     */
    public abstract void handle(String[] args) throws ParseException;
    
    /**
     * Convenience methods for command-line argument handling
     */
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
