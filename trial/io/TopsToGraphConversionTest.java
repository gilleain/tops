package io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;

import org.junit.Test;

import tops.translation.Tops2String;

public class TopsToGraphConversionTest {
	
    private static final String GOLDEN_TOPS_DIR = "/home/gilleain/Data/topsfiles";
	
    @Test
    public void convertGolden() throws IOException {
        File dir = new File(GOLDEN_TOPS_DIR);
        Writer out = new PrintWriter(System.out);
        convertDir(dir, out);
    }

    
	@Test
	public void convertExamplesDir() throws IOException {
		URL inputURL = this.getClass().getResource("/examples/tops");
		File inputDir = new File(inputURL.getFile());
		Writer outputWriter = new FileWriter(new File("src/test/resources/examples/graphs.txt"));
		convertDir(inputDir, outputWriter);
	}
	
	private void convertDir(File inputDir, Writer outputWriter) throws IOException {
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
	
	@Test
	public void convertAnotherSingleExample() throws IOException {
	    File inputDir  = new File("/home/gilleain/Data/topsfiles");
	    Writer outputWriter = new OutputStreamWriter(System.out);
	    Tops2String t2s = new Tops2String(inputDir);
	    convert("9wga", t2s, outputWriter);
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
