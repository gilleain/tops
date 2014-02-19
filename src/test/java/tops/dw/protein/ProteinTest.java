package tops.dw.protein;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ProteinTest {
	
	@Test
	public void read() throws FileNotFoundException, TopsFileFormatException, IOException {
		String filename ="2bopA.tops";
		Protein protein = new Protein(ProteinTest.class.getResourceAsStream(filename));
		Assert.assertNotNull(protein);
	}

}
