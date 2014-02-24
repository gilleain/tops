package tops.translation;

import java.io.File;

public class RunTops extends Executer {

    private String path_to_tops;

    private String dssp_directory;

    private String output_directory;

    private String run_directory;

    public RunTops(String path_to_tops, String dssp_directory,
            String output_directory, String run_directory) {
        this.path_to_tops = path_to_tops;
        this.dssp_directory = dssp_directory;
        this.output_directory = output_directory;
        this.run_directory = run_directory;
    }

    public void convert(String pdbid, String chain, String tops_file_name,
            String domainFilePath) { // pdbid is the 4 character code the
                                        // tops program needs
        String tops_file_path = new File(this.output_directory, tops_file_name)
                .toString();
        String command;
        if (chain.equals("")) {
            //command = this.path_to_tops + " -P " + this.dssp_directory + " -B "
            //        + domainFilePath + " -t " + tops_file_path + " " + pdbid;
            command = this.path_to_tops + " -P " + this.dssp_directory +
                        " -t " + tops_file_path + " " + pdbid;
        } else {
            command = this.path_to_tops + " -P " + this.dssp_directory + " -B "
                    + domainFilePath + " -C " + chain + " -t " + tops_file_path
                    + " " + pdbid;

        }
        this.execute(command, this.run_directory);
    }

    public static void main(String[] args) {
        RunTops tops = new RunTops(args[0], args[1], args[1], args[1]);
        tops.convert(args[2], args[3], args[4], args[5]);
    }
}
