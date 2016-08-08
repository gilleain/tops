package tops.port;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import tops.port.calculate.Configure;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class RunAll {
    
    private static final String DIR = "/Users/maclean/data/dssp/reps";
    
    @Test
    public void run() {
        Logger.getLogger("").setLevel(Level.OFF);
        File dirFile = new File(DIR);
        DsspReader dsspReader = new DsspReader();
        Configure configure = new Configure();
        for (File file : dirFile.listFiles()) {
            if (!file.getName().endsWith(".dssp")) continue;
            try {
                Protein protein = dsspReader.readDsspFile(file.getAbsolutePath());
                Chain chain = protein.getChains().get(0);
                configure.configure(chain);
                System.out.println(protein.getProteinCode() + chain.toString());
            } catch (Exception e) {
                System.out.println("Failed " + file.getName());
            }
        }
    }

}
