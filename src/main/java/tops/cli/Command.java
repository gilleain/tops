package tops.cli;

import org.apache.commons.cli.ParseException;

/**
 * A command is a single command-line app, run through {@link Tops}.
 * 
 * @author maclean
 *
 */
public interface Command {
    
    /**
     * @return a readable description of the command
     */
    public String getDescription();
    
    /**
     * @param args
     * @throws ParseException
     */
    public void handle(String[] args) throws ParseException;

}
