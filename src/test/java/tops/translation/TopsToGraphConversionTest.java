package tops.translation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import org.junit.Test;

public class TopsToGraphConversionTest {
	
	
	
	@Test
	public void convertExamplesDir() throws IOException {
		URL inputURL = this.getClass().getResource("/examples/tops");
		File inputDir = new File(inputURL.getFile());
		Writer outputWriter = new FileWriter(new File("src/test/resources/examples/graphs.txt"));
//		Writer outputWriter = new OutputStreamWriter(System.out);
		Tops2String t2s = new Tops2String(inputDir);
		for (String filename : inputDir.list()) {
			try {
				convert(filename, t2s, outputWriter);
			} catch (Exception e) {
				System.err.println("Error " + filename);
				e.printStackTrace();
			}
		}
		outputWriter.close();
	}
	
	@Test
	public void convertSingleExample() throws IOException {
		URL inputURL = this.getClass().getResource("/examples/tops");
		File inputDir = new File(inputURL.getFile());
		Writer outputWriter = new OutputStreamWriter(System.out);
		Tops2String t2s = new Tops2String(inputDir);
		convert("1a2p.tops", t2s, outputWriter);
		outputWriter.flush();
		outputWriter.close();
	}

	private void convert(String filename, Tops2String t2s, Writer outputWriter) throws IOException {
		for (String topsString : t2s.convert(filename, "", "CATH")) {
			outputWriter.write(topsString);
			outputWriter.write('\n');
		}
	}
}
