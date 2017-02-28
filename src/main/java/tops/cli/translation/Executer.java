package tops.cli.translation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

public class Executer {

    public void execute(String command, String directory) {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        System.err.println("executing : " + command + " in directory "
                + directory);
        try {
            process = runtime.exec(command, null, new File(directory));

            BufferedReader err = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            BufferedReader out = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));

            String line = null;
            while ((line = out.readLine()) != null) {
                System.out.println(line);
            }

            while ((line = err.readLine()) != null) {
                // System.err.println(line);
            }

            process.waitFor();

        } catch (Exception e) {
            System.err.println(e);
        }

    }
}
