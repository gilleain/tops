package tops.cli;

import java.util.Map;

import org.apache.commons.cli.ParseException;

public class HelpCommand implements Command {
    
    private Map<String, Command> commands;
    
    public HelpCommand(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public String getDescription() {
        return "Print help for commands";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        if (args.length == 0) {
            System.err.println("Commands:");
            for (String keyword : commands.keySet()) {
                Command command = commands.get(keyword);
                System.err.println(keyword + " : " + command.getDescription());
            }
        } else if (args.length == 1) {
            String commandKey = args[0];
            Command command = commands.get(commandKey);
            System.err.println(commandKey + " " + command.getHelp());
        }
    }

    @Override
    public String getHelp() {
        return "<command>";
    }

}
