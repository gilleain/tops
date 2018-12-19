package tops.cli.translation;

import java.io.File;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;

public class RunDssp extends Executer implements Command {

    private String pathToDssp;

    private String pdbDirectory;

    private String outputDirectory;

    private String runDirectory;

    public RunDssp(String pathToDssp, String pdbDirectory, String outputDirectory, String runDirectory) {
        this.pathToDssp = pathToDssp;
        this.pdbDirectory = pdbDirectory;
        this.outputDirectory = outputDirectory;
        this.runDirectory = runDirectory;
    }

    public void convert(String pdbFilename, String dsspFilename) {
        String pdbFilepath = new File(this.pdbDirectory, pdbFilename).toString();
        String dsspFilepath = new File(this.outputDirectory, dsspFilename).toString();
        String command = this.pathToDssp + " " + pdbFilepath + " " + dsspFilepath;
        this.execute(command, this.runDirectory);
    }

    @Override
    public String getDescription() {
        return "Run the dssp executable";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String pathToDssp = args[0];
        String outputDir = args[1];
        String pdbFilename = args[2];
        String dsspFilename = args[3];
        RunDssp dssp = new RunDssp(pathToDssp, outputDir, outputDir, outputDir);
        dssp.convert(pdbFilename, dsspFilename);
    }

    @Override
    public String getHelp() {
        return "<pathToDssp> <outputDir> <pdbFilename> <dsspFilename>";
    }
}
