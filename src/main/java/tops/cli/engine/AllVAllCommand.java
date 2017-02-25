package tops.cli.engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.engine.Result;
import tops.engine.drg.AllVAllWrapper;

public class AllVAllCommand extends Command {

    @Override
    public String getDescription() {
        return "All-v-all comparison";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String filename = args[0];

        // read the examples from the filename supplied into a list
        List<String> examples = new ArrayList<String>();
        String line;
        try {
            BufferedReader buff = new BufferedReader(new FileReader(filename));
            while ((line = buff.readLine()) != null) {
                examples.add(line);
            }
            buff.close();
        } catch (IOException ioe) { System.out.println(ioe); }

        // run the wrapper on the examples to get an array of Result objects
        AllVAllWrapper allVAllWrapper = new AllVAllWrapper();
        List<String> names = allVAllWrapper.getNames(examples);
        Result[] results = allVAllWrapper.run(names, examples);

        // format / print the results
        if (args.length > 1) {
            if (args[1].equals("--oc-output")) {
                allVAllWrapper.printResultsAsOCInput(names, results, System.out);
            }
        } else {
            for (int i = 0; i < results.length; i++) {
                System.out.println(results[i]);
            }
        }
    }

}
