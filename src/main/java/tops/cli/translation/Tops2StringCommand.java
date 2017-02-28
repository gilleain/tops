package tops.cli.translation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.translation.Tops2String;

public class Tops2StringCommand implements Command {

    @Override
    public String getDescription() {
        return "Convert TOPS file to graph strings";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        if (args.length == 0 || args[0].equals("-h")) {
            System.err.println(
            "Usage : java tops.translation.Tops2String <filename> <string_name> <CATH|SCOP>");
            System.err.println(
            "or    : java tops.translation.Tops2String -d <directoryname> <CATH|SCOP>");
            System.exit(0);
        }

        // 'directory' mode - convert every .tops file in a directory, no renaming
        if (args[0].equals("-d")) {
            if (args.length == 1) {
                System.err.println("java tops.translation.Tops2String -d <directoryname> <CATH|SCOP>");
                System.exit(0);
            }
            File topsDirectory = new File(args[1]);
            if (topsDirectory.isDirectory()) {
                Tops2String t2s = new Tops2String(args[1]);
                String[] fileList = topsDirectory.list();
                for (int i = 0; i < fileList.length; i++) {
                    String topsFileName = fileList[i];
                    try {
                        String[] results = t2s.convert(topsFileName, "", args[2]);
                        for (int j = 0; j < results.length; j++) {
                            System.out.println(results[j]);
                        }
                    } catch (IOException ioe) {
                        System.err.println(ioe);
                    }
                }
            } else {
                System.err.println("java tops.translation.Tops2String -d <directoryname>");
                System.exit(0);
            }

            // 'single' mode - convert only the file supplied as an argument,
            // renaming if the second argument is not "", scheme in args[2]
        } else {
            Tops2String t2s = new Tops2String(".");
            try {
                String[] results = t2s.convert(args[0], args[1], args[2]);
                for (int i = 0; i < results.length; i++) {
                    System.out.println(results[i]);
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
        
    }

}
