package tops.cli;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Convenience methods for command-line argument handling
 */
public class BaseCLIHandler {
    
    protected Options options;
    
    public BaseCLIHandler() {
        options = new Options();
    }
    
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
    

    public String getHelp(String command) {
        return getHelp(command, options);
    }
    
    public String getHelp(String command, Options options) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter); 
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(command, options);
        printWriter.close();
        return stringWriter.toString();
    }
}