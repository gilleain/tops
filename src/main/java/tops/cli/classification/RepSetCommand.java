package tops.cli.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.engine.inserts.Pattern;
import tops.model.classification.Rep;
import tops.model.classification.RepSet;

public class RepSetCommand extends BaseCommand {
    
    private Logger log = Logger.getLogger(RepSetCommand.class.getName());

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getHelp() {
        return "<filename> <levelName>";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String filename = args[0];
        String levelName = args[1];

        String line;
        RepSet repSet = new RepSet();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            while ((line = bufferedReader.readLine()) != null) {
                Rep rep = new Rep(levelName, line);
                repSet.addRep(rep);
            }
        } catch (IOException e) {
            log.log(Level.ALL, e.toString());
        }

        output(repSet.toString());

        int halfSize = repSet.size() / 2;

        output("1st random subset : ");
        RepSet firstHalf = repSet.randomSubset(halfSize);
        output(firstHalf.toString());
        output(firstHalf.generatePattern().toString());

        output("2nd random subset : ");
        RepSet secondHalf = repSet.randomSubset(halfSize);
        output(secondHalf.toString());
        output(secondHalf.generatePatternWithInserts().toString());

        repSet.resetBitSet();

        Pattern pattern = repSet.generatePatternWithInserts();
        boolean matched = repSet.matches(pattern);
        output(pattern + " matches = " + matched);
    }

}
