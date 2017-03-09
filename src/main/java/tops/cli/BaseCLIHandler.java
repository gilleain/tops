package tops.cli;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Convenience methods for command-line argument handling
 */
public class BaseCLIHandler {
    
    protected Options options;
    
    public BaseCLIHandler() {
        options = new Options();
    }
    
    public void opt(String flag, String desc) {
        options.addOption(Option.builder(flag).desc(desc).build());
    }
    
    public void opt(String flag, String argName, String desc) {
        options.addOption(Option.builder(flag)
                                .hasArg()
                                .desc(desc)
                                .argName(argName)
                                .build());
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