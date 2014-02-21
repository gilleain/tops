package tops.translation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class ConversionTest {
	
	public void convert(File file, File outputDir) throws IOException {
		FoldAnalyser foldAnalyser = new FoldAnalyser();
		Protein protein = PDBReader.read(file);
		foldAnalyser.analyse(protein);
		tops.dw.protein.Protein oldStyleProtein = ProteinConverter.convert(protein);
		File outputFile = new File(outputDir, protein.getID() + ".tops");
		oldStyleProtein.WriteTopsFile(new FileOutputStream(outputFile));
	}

	@Test
	public void convertExamplesDir() throws IOException {
		URL inputURL = this.getClass().getResource("/examples/pdb");
		URL outputURL = this.getClass().getResource("/examples/tops");
		File inputDir = new File(inputURL.getFile());
		File outputDir = new File(outputURL.getFile());
		for (String filename : inputDir.list()) {
			try {
				convert(new File(inputDir, filename), outputDir);
			} catch (Exception e) {
				System.err.println("Error " + filename);
			}
		}
	}
}
