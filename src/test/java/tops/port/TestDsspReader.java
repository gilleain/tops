package tops.port;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.Test;

import tops.port.io.TopsFileWriter;
import tops.port.model.Chain;
import tops.port.model.DsspReader;
import tops.port.model.Protein;
import tops.port.model.SSE;

public class TestDsspReader {
    
    private static final String DIR = "/Users/maclean/data/dssp/reps";

    @Test
    public void test1NOT() throws IOException {
        Protein protein = new DsspReader().readDsspFile("1NOT", read("1not.dssp"));
        assertEquals(1, protein.getChains().size());
        new TopsFileWriter().writeTOPSFile(System.out, protein);
    }
    
    @Test
    public void test1TGX() throws IOException {
        Protein protein = new DsspReader().readDsspFile("1TGX", read("1tgx.dssp"));
//        System.out.println(protein.toTopsFile());
        System.out.print(protein.toString());
        for (Chain chain : protein.getChains()) {
            for (SSE sse : chain.getSSEs()) {
                System.out.println(sse.getSummary());
            }
        }
    }
    
    @Test
    public void test2igd() throws IOException {
        Protein protein = new DsspReader().readDsspFile("2IGD", read("2igd.dssp"));
//        System.out.println(protein.toTopsFile());
        for (Chain chain : protein.getChains()) {
            for (SSE sse : chain.getSSEs()) {
                System.out.println(sse.getSummary());
            }
        }
    }
    
    private Reader read(String filename) throws FileNotFoundException {
//        return new InputStreamReader(getClass().getResourceAsStream(DIR + "/" + filename));
        return new FileReader(new File(DIR + "/" + filename));
    }
    
  }
