package tops.translation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class PDBToTopsConversionTest {
	
	public void convert(File file, File outputDir) throws IOException {
		FoldAnalyser foldAnalyser = new FoldAnalyser();
		Protein protein = PDBReader.read(file);
		foldAnalyser.analyse(protein);
		String id = file.getName().substring(0, file.getName().indexOf("."));
		protein.setID(id);
		tops.dw.protein.Protein oldStyleProtein = ProteinConverter.convert(protein);
		File outputFile = new File(outputDir, id + ".tops");
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		oldStyleProtein.WriteTopsFile(outputStream);
		outputStream.flush();
		outputStream.close();
	}

	@Test
	public void convertExamplesDir() throws IOException {
		URL inputURL = this.getClass().getResource("/examples/pdb");
		File inputDir = new File(inputURL.getFile());
		File outputDir = new File("src/test/resources/examples/tops");
		for (String filename : inputDir.list()) {
			try {
				convert(new File(inputDir, filename), outputDir);
			} catch (Exception e) {
				System.err.println("Error " + filename);
			}
		}
	}
}
