package tops.cli.translation;

import java.io.File;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;

public class RunTops extends Executer implements Command {

    private String pathToTops;

    private String dsspDirectory;

    private String outputDirectory;

    private String runDirectory;

    public RunTops(String pathToTops, String dsspDirectory, String outputDirectory, String runDirectory) {
        this.pathToTops = pathToTops;
        this.dsspDirectory = dsspDirectory;
        this.outputDirectory = outputDirectory;
        this.runDirectory = runDirectory;
    }

    public void convert(String pdbid, String chain, String topsFilename, String domainFilePath) { 
        // pdbid is the 4 character code the tops program needs
        String topsFilepath = new File(this.outputDirectory, topsFilename).toString();
        String command;
        if (chain.equals("")) {
            command = this.pathToTops + " -P " + this.dsspDirectory +
                        " -t " + topsFilepath + " " + pdbid;
        } else {
            command = this.pathToTops + " -P " + this.dsspDirectory + " -B "
                    + domainFilePath + " -C " + chain + " -t " + topsFilepath
                    + " " + pdbid;

        }
        this.execute(command, this.runDirectory);
    }

    @Override
    public String getDescription() {
        return "Run the c-tops executable";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String pathToTops = args[0];
        String dsspDir = args[1];
        String pdbid = args[2];
        String chain = args[3];
        String topsFilename = args[4];
        String domainFilePath = args[5];
        RunTops tops = new RunTops(pathToTops, dsspDir, dsspDir, dsspDir);
        tops.convert(pdbid, chain, topsFilename, domainFilePath);
    }

    @Override
    public String getHelp() {
        return "<pdbid> <chain> <topsFilename> <domainFilePath>";
    }
}
