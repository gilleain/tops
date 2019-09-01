package tops.cli.translation;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Executer {
    
    private List<String> envpList = new ArrayList<>();
    
    public void addEnvironmentVariable(String key, String value) {
        envpList.add(key + "=" + value);
    }

    public void execute(String command, String directory) {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        System.err.println("executing : " + command + " in directory " + directory);
        
        String[] envp = envpList.toArray(new String[0]);
        try {
            process = runtime.exec(command, envp, new File(directory));

            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = null;
            while ((line = out.readLine()) != null) {
                System.out.println(line);
            }

            while ((line = err.readLine()) != null) {
                 System.err.println(line);
            }

            process.waitFor();

        } catch (Exception e) {
            System.err.println(e);
        }

    }
}
