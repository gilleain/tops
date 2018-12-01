package tops.cli.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.cli.ParseException;

import com.sun.istack.internal.logging.Logger;

import tops.cli.Command;
import tops.engine.inserts.Pattern;
import tops.model.classification.Rep;
import tops.model.classification.RepSet;

public class RepSetCommand implements Command {
    
    private Logger log = Logger.getLogger(RepSetCommand.class);

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
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
            log.logException(e, Level.ALL);
        }

        log.info(repSet.toString());

        int halfSize = repSet.size() / 2;

        log.info("1st random subset : ");
        RepSet firstHalf = repSet.randomSubset(halfSize);
        log.info(firstHalf.toString());
        log.info(firstHalf.generatePattern().toString());

        log.info("2nd random subset : ");
        RepSet secondHalf = repSet.randomSubset(halfSize);
        log.info(secondHalf.toString());
        log.info(secondHalf.generatePatternWithInserts().toString());

        repSet.resetBitSet();

        Pattern pattern = repSet.generatePatternWithInserts();
        boolean matched = repSet.matches(pattern);
        log.info(pattern + " matches = " + matched);
    }

}
