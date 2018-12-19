package tops.cli.translation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.translation.Tops2String;

public class Tops2StringCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Convert TOPS file to graph strings";
    }

    @Override
    public void handle(String[] args) throws ParseException {

        if (args[0].equals("-d")) {
            // 'directory' mode - convert every .tops file in a directory, no renaming
           runForDir(args);
        } else {
            // 'single' mode - convert only the file supplied as an argument,
            // renaming if the second argument is not "", scheme in args[2]
            runForFile(args);
        }
    }
    
    private void runForFile(String[] args) {
        Tops2String t2s = new Tops2String(".");
        try {
            String[] results = t2s.convert(args[0], args[1], args[2]);
            for (int i = 0; i < results.length; i++) {
                output(results[i]);
            }
        } catch (IOException ioe) {
            error(ioe);
        }
    }
    
    private void runForDir(String[] args) {
        if (args.length == 1) {
            error("Use -d <directoryname> <CATH|SCOP>");
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
                        output(results[j]);
                    }
                } catch (IOException ioe) {
                    error(ioe);
                }
            }
        } else {
            error("Use -d <directoryname>");
            System.exit(0);
        }
    }

    @Override
    public String getHelp() {
        return "<filename> <string_name> <CATH|SCOP> or -d <directoryname> <CATH|SCOP>";
    }

}
