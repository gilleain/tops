package tops.port;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.Test;

import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class TestDsspReader {
    
    private static final String DIR = "/Users/maclean/data/dssp/reps";

    @Test
    public void test1NOT() throws IOException {
        Protein protein = new DsspReader().readDsspFile("1NOT", read("resources/1not.dssp"));
        assertEquals(1, protein.getChains().size());
        System.out.println(protein.toTopsFile());
    }
    
    @Test
    public void test1TGX() throws IOException {
        Protein protein = new DsspReader().readDsspFile("1TGX", read("1tgx.dssp"));
//        System.out.println(protein.toTopsFile());
        System.out.print(protein.toString());
    }
    
    private Reader read(String filename) throws FileNotFoundException {
//        return new InputStreamReader(getClass().getResourceAsStream(DIR + "/" + filename));
        return new FileReader(new File(DIR + "/" + filename));
    }
    
  }
