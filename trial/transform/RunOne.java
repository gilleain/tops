package transform;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import tops.port.calculate.Configure;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class RunOne {
    
    private static final String PATH = "/home/gilleain/Code/eclipse/ctops/Debug/dsspfiles";
    
    @Test
    public void test() {
        String result = run("1fxd");
        assertEquals("0 NEhEeHeC 1:6A3:4A", result);
    }
    
    private String run(String pdbcode) {
        DsspReader dsspReader = new DsspReader();
        Configure configure = new Configure();
        File file = new File(PATH, pdbcode + ".dssp");
        try {
            Protein protein = dsspReader.readDsspFile(file.getAbsolutePath());
            Chain chain = protein.getChains().get(0);
            configure.configure(chain);
            return chain.toString();
        } catch (Exception e) {
            System.out.println("Failed " + file.getName() + " " + e.getStackTrace()[0]);
        }
        return "";
    }

}
