package tops.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Convenience methods for command-line argument handling
 */
public class BaseCLIHandler {
    
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