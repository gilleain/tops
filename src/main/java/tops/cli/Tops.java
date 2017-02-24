package tops.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.ParseException;

/**
 * Main CLI class to run commands.
 * 
 * @author maclean
 *
 */
public class Tops {
    
    /**
     * Commands keyed by command string arguments.
     */
    private Map<String, Command> commands;
    
    public Tops() {
        commands = new HashMap<String, Command>();
        commands.put("cartoon", new CartoonCommand());
        commands.put("diagram", new DiagramCommand());
    }
    
    public boolean hasCommand(String arg) {
        return commands.containsKey(arg);
    }
    
    public void run(String arg, String[] args) throws ParseException {
        commands.get(arg).handle(args);
    }
    
    public static void main(String[] args) {
        Tops tops = new Tops();
        if (args.length < 1 || !tops.hasCommand(args[0])) {
            System.err.println("No command or command not known");
            System.exit(0);
        }
        String arg = args[0];
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        System.out.println(Arrays.toString(remainingArgs));
        try {
            tops.run(arg, remainingArgs);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
