package tops.cli.classification;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.inserts.Pattern;
import tops.model.classification.Rep;
import tops.model.classification.RepSet;

public class RepSetCommand implements Command {

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
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            while ((line = bufferedReader.readLine()) != null) {
                Rep rep = new Rep(levelName, line);
                repSet.addRep(rep);
            }
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println(repSet);

        int half_size = repSet.size() / 2;

        System.out.println("1st random subset : ");
        RepSet firstHalf = repSet.randomSubset(half_size);
        System.out.println(firstHalf);
        System.out.println(firstHalf.generatePattern());

        System.out.println("2nd random subset : ");
        RepSet secondHalf = repSet.randomSubset(half_size);
        System.out.println(secondHalf);
        System.out.println(secondHalf.generatePatternWithInserts());

        repSet.resetBitSet();

        Pattern pattern = repSet.generatePatternWithInserts();
        boolean matched = repSet.matches(pattern);
        System.out.println(pattern + " matches = " + matched);
    }

}
