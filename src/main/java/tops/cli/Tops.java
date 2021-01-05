package tops.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.ParseException;

import tops.cli.classification.CathTreeCommand;
import tops.cli.drawing.CartoonCommand;
import tops.cli.drawing.DiagramCommand;
import tops.cli.engine.drg.CompareCommand;
import tops.cli.engine.drg.MatcherCommand;
import tops.cli.translation.CATHDomainFileParserCommand;
import tops.cli.view.AnotherCartoonEditorCommand;
import tops.cli.view.DiagramEditorCommand;
import tops.cli.view.OriginalEditorCommand;
import tops.cli.view.SimpleCartoonEditorCommand;

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
        commands = new HashMap<>();
        
        // special command to print help
        commands.put("help", new HelpCommand(commands));
        
        commands.put(CartoonCommand.KEY, new CartoonCommand());
        commands.put("diagram", new DiagramCommand());
        commands.put("parse-cath-domain", new CATHDomainFileParserCommand());
        commands.put("parse-cath-tree", new CathTreeCommand());
        commands.put("edit-cartoon", new SimpleCartoonEditorCommand());
        commands.put("edit-diagram", new DiagramEditorCommand());
        commands.put("edit-original", new OriginalEditorCommand());
        commands.put("edit-another", new AnotherCartoonEditorCommand());
        
        commands.put(MatcherCommand.KEY, new MatcherCommand());
        commands.put(CompareCommand.KEY, new CompareCommand());
    }
    
    public boolean hasCommand(String arg) {
        return commands.containsKey(arg);
    }
    
    public void run(String arg, String[] args) throws ParseException {
        commands.get(arg).handle(args);
    }
    
    public static void main(String[] args) {
        Tops tops = new Tops();
        if (args.length < 1) {
            System.err.println("No command!");
            System.exit(0);
        }
        if (!tops.hasCommand(args[0])) {
            System.err.println("Command not known " + args[0]);
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
