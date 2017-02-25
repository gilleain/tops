package tops.cli;

import org.apache.commons.cli.ParseException;

/**
 * A command is a single command-line app, run through {@link Tops}.
 * 
 * @author maclean
 *
 */
public abstract class Command {
    
    /**
     * @return a readable description of the command
     */
    public abstract String getDescription();
    
    /**
     * @param args
     * @throws ParseException
     */
    public abstract void handle(String[] args) throws ParseException;

}
