package tops.cli.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.web.display.servlet.TopsFileManager;

public class FileManagerCommand implements Command {

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        TopsFileManager tfm = new TopsFileManager("./");
        System.out.println(Arrays.toString(args));
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
                System.out.println("getting " + topsfile);
                input = new InputStreamReader(tfm.getStreamFromZip(path, topsfile));
            } else {
                String[] names = tfm.getNames(className, pdbId, chain);
                for (int i = 0; i < names.length; i++) {
                    System.out.println("name " + i + " = " + names[i]);
                }
                input = new InputStreamReader(tfm.getStreamFromDir(className, names[0]));
            }
            BufferedReader br = new BufferedReader(input);
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    System.out.print(line);
                }
                br.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf);
        }

    }

}
