package tops.cli.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.web.display.servlet.TopsFileManager;

public class FileManagerCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getHelp() {
        return "<className> <path> <pdbId> ..."; // TODO
    }

    @Override
    public void handle(String[] args) throws ParseException {
        TopsFileManager tfm = new TopsFileManager("./");
        String className = args[0];
        String path = args[1];
        String pdbId = args[2];

        tfm.addPathMapping(className, path);
        String chain = null;

        if (!args[3].equals("-")) {
            chain = args[3];
        }

        try {
            InputStreamReader input = null;
            if (path.endsWith("gz")) {
                String topsfile = pdbId + chain;
                output("getting " + topsfile);
                input = new InputStreamReader(tfm.getStreamFromZip(path, topsfile));
            } else {
                String[] names = tfm.getNames(className, pdbId, chain);
                for (int i = 0; i < names.length; i++) {
                    output("name " + i + " = " + names[i]);
                }
                input = new InputStreamReader(tfm.getStreamFromDir(className, names[0]));
            }
            stuff(input);
            
        } catch (FileNotFoundException fnf) {
            error(fnf);
        }

    }
    
    private void stuff(InputStreamReader input) {
        String line;
        try (BufferedReader br = new BufferedReader(input)) {
            while ((line = br.readLine()) != null) {
                output(line);
            }
        } catch (IOException ioe) {
            error(ioe);
        }
    }

}
