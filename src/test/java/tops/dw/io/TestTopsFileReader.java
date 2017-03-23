package tops.dw.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import tops.dw.protein.Protein;
import tops.dw.protein.TopsFileFormatException;

public class TestTopsFileReader {

    @Test
    public void testRead() throws FileNotFoundException, TopsFileFormatException, IOException {
        String filename ="/2bopA.tops";
        InputStream in = TestTopsFileReader.class.getResourceAsStream(filename);
        Assert.assertNotNull(in);
        TopsFileReader topsFileReader = new TopsFileReader();
        
        Protein protein = topsFileReader.readTopsFile(in);
        Assert.assertNotNull(protein);
    }
}
