package python;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import tops.port.model.DsspReader;
import tops.port.model.Protein;

public class TestDsspReader {

    @Test
    public void test1NOT() throws IOException {
        Protein protein = new DsspReader().readDsspFile("1NOT", read("data/1not.dssp"));
        assertEquals(1, protein.getChains().size());
        System.out.println(protein.toTopsFile());
    }
    
    @Test
    public void test1TGX() throws IOException {
        Protein protein = new DsspReader().readDsspFile("1TGX", read("data/1tgx.dssp"));
        System.out.println(protein.toTopsFile());
    }
    
    private Reader read(String filename) throws FileNotFoundException {
        return new InputStreamReader(getClass().getResourceAsStream(filename));
    }
    
  }
