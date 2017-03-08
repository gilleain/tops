package tops.cli.inserts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.inserts.Matcher;
import tops.engine.inserts.Pattern;

public class MatcherCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
        List<String> l = new ArrayList<String>();
        String line;

        if (args.length == 0) {
            System.out.println("Usage : Matcher <pattern> <file>");
            System.exit(0);
        }

        String pattern = args[0];
        String filename = args[1];

        Level level = Level.OFF;
        if (args.length > 2) {
            String levelString = args[2];
            System.err.println("setting level to : " + levelString);
            level = Level.parse(levelString);
        }

        try {
            BufferedReader bu = new BufferedReader(new FileReader(filename));
            
            while ((line = bu.readLine()) != null) {
                l.add(line);
            }
            bu.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        String[] targets = (String[]) l.toArray(new String[0]);
        Matcher m = new Matcher(targets);
        Logger.getLogger("tops.engine.inserts.Pattern").setLevel(level);
        Logger.getLogger("tops.engine.inserts.Matcher").setLevel(level);
        // m.setupLogging();
        Pattern p = new Pattern(pattern);
        String[] results = m.run(p);
        for (int i = 0; i < results.length; i++) {
            System.out.println(results[i]);
        }
        System.out.println(m.numberMatching(p) + " matches");
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
