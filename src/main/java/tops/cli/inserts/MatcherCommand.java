package tops.cli.inserts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.engine.inserts.Matcher;
import tops.engine.inserts.Pattern;

public class MatcherCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Match a pattern to a file of strings";
    }

    @Override
    public String getHelp() {
        return "<pattern> <file>";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        List<String> targets = new ArrayList<>();
        String line;

        String pattern = args[0];
        String filename = args[1];

        Level level = Level.OFF;
        if (args.length > 2) {
            String levelString = args[2];
            output("setting level to : " + levelString);
            level = Level.parse(levelString);
        }

        try (BufferedReader bu = new BufferedReader(new FileReader(filename))) {
            
            while ((line = bu.readLine()) != null) {
                targets.add(line);
            }
        } catch (Exception e) {
            error(e);
            return;
        }

        Matcher m = new Matcher(targets);
        Logger.getLogger("tops.engine.inserts.Pattern").setLevel(level);
        Logger.getLogger("tops.engine.inserts.Matcher").setLevel(level);
        Pattern p = new Pattern(pattern);
        String[] results = m.run(p);
        for (int i = 0; i < results.length; i++) {
            output(results[i]);
        }
        output(m.numberMatching(p) + " matches");
    }

}
