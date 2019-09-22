package port;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import tops.engine.TopsStringFormatException;
import tops.engine.drg.Pattern;
import tops.engine.drg.Utilities;

public class CompareOutputs {
    
    @Test
    public void compareGoldenToPort() throws IOException {
        String trialLog = "port_comparison.txt";
        String baseDir = "/home/gilleain/Data/topsstrings/";
        File goldenFile = new File(baseDir, "golden.txt");
        File portFile = new File(baseDir, "port.txt");
        
        Map<String, String> gMap = toMap(goldenFile);
        Map<String, String> pMap = toMap(portFile);
         
        int identityCount = 0;
        Histogram h = new Histogram(10);
        for (String key : gMap.keySet()) {
            if (pMap.containsKey(key)) {
                String gString = gMap.get(key);
                String pString = pMap.get(key);
                
                System.out.println(gString);
                System.out.println(pString);
                double c = compression(gString, pString);
                System.out.println(c);
                h.add(key, c);
                if (gString.equals(pString)) {
                    identityCount++;
                }
            }
        }
        System.out.println(identityCount + " identical");
        System.out.println(h.toShortString());
        writeToFile(h, trialLog);
    }
    
    private void writeToFile(Histogram histogram, String filename) throws IOException {
        File file = new File(filename);
        boolean isNew = file.createNewFile();
        if (isNew) {
            System.out.println("Making new file " + filename);
        }
        FileWriter writer = new FileWriter(file);
        writer.append(histogram.toShortString()).append('\n');
        writer.close();
    }
    
    private double compression(String gString, String pString) {
        try {
            Pattern p = new Pattern(gString);
            Pattern t = new Pattern(pString);
            // hmmm, api needs improving!
            Pattern[] instances ;
            if (gString.length() < pString.length()) {
                instances = new Pattern[] {p, t} ;
                return Utilities.doDrgCompression(instances, p);
            } else { 
                instances = new Pattern[] {p, t} ;
                return Utilities.doDrgCompression(instances, t);
            }
        } catch (TopsStringFormatException e) {
            // this should be impossible, given that we are generating them...
            e.printStackTrace();
            return 0;
        }
        
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
