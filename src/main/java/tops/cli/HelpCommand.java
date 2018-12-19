package tops.cli;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.ParseException;

public class HelpCommand extends BaseCommand {
    
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
            error("Commands:");
            for (Entry<String, Command> entry : commands.entrySet()) {
                String keyword = entry.getKey();
                Command command = commands.get(keyword);
                error(keyword + " : " + command.getDescription());
            }
        } else if (args.length == 1) {
            String commandKey = args[0];
            Command command = commands.get(commandKey);
            error(commandKey + " " + command.getHelp());
        }
    }

    @Override
    public String getHelp() {
        return "<command>";
    }

}
