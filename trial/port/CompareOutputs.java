package port;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

public class CompareOutputs {
    
    @Test
    public void compareGoldenToPort() throws FileNotFoundException {
        String baseDir = "/home/gilleain/Data/topsstrings/";
        File goldenFile = new File(baseDir, "golden.txt");
        File portFile = new File(baseDir, "port.txt");
        
        Map<String, String> gMap = toMap(goldenFile);
        Map<String, String> pMap = toMap(portFile);
         
        int identityCount = 0;
        for (String key : gMap.keySet()) {
            if (pMap.containsKey(key)) {
                String gString = gMap.get(key);
                String pString = pMap.get(key);
                
                System.out.println(gString);
                System.out.println(pString);
                if (gString.equals(pString)) {
                    identityCount++;
                }
            }
        }
        System.out.println(identityCount + " identical");
    }

    private Map<String, String> toMap(File file) throws FileNotFoundException {
        Map<String, String> map = new HashMap<>();
        
        try (BufferedReader r =new BufferedReader(new FileReader(file))) {
            for (String line : r.lines().collect(Collectors.toList())) {
                String[] s = line.split(" ");
                map.put(s[0], line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
