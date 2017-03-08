package tops.cli.translation;

import java.io.File;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;

public class RunDssp extends Executer implements Command {

    private String path_to_dssp;

    private String pdb_directory;

    private String output_directory;

    private String run_directory;

    public RunDssp(String path_to_dssp, String pdb_directory, 
    			   String output_directory, String run_directory) {
        this.path_to_dssp = path_to_dssp;
        this.pdb_directory = pdb_directory;
        this.output_directory = output_directory;
        this.run_directory = run_directory;
    }

    public void convert(String pdb_file_name, String dssp_file_name) {
        String pdb_file_path = new File(this.pdb_directory, pdb_file_name).toString();
        String dssp_file_path = new File(this.output_directory, dssp_file_name).toString();
        String command = this.path_to_dssp + " " + pdb_file_path + " " + dssp_file_path;
        this.execute(command, this.run_directory);
    }

    @Override
    public String getDescription() {
        return "Run the dssp executable";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        RunDssp dssp = new RunDssp(args[0], args[1], args[1], args[1]);
        dssp.convert(args[2], args[3]);   
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }
}
