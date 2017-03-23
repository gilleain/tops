package tops.cli.translation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.dw.io.TopsFileReader;
import tops.dw.protein.Protein;
import tops.translation.CompressedFileHandler;
import tops.translation.PDBToCartoon;
import tops.translation.PDBToGraph;
import tops.translation.Tops2String;


/**
 * @author maclean
 *
 */
public class DsspTopsRunner implements PDBToGraph, PDBToCartoon, Command {
    
    private RunDssp dssp;
    
    private RunTops tops;
    
    private Tops2String tops2String;
    
    private CompressedFileHandler compressedFileHandler;
    
    private String scratchDirectory;
    
    public DsspTopsRunner(String scratchDirectory) {
        this("./", scratchDirectory);
    }
    
    public DsspTopsRunner(String executableDir, String scratchDirectory) {
    	this.scratchDirectory = scratchDirectory;
    	String dsspPath = new File(executableDir, "dssp").toString();
    	String topsPath = new File(executableDir, "Tops").toString();
        this.dssp = new RunDssp(dsspPath, scratchDirectory, scratchDirectory, "./");
        this.tops = new RunTops(topsPath, scratchDirectory, scratchDirectory, "./");
        this.tops2String = new Tops2String(scratchDirectory);
        this.compressedFileHandler = new CompressedFileHandler(scratchDirectory);
    }

    @Override
    public String getDescription() {
        return "Run dssp then tops";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        String path = args[0];
        String scratchDirectory = args[1];
        DsspTopsRunner runner = new DsspTopsRunner(scratchDirectory);
        
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            String[] files = pathFile.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdb");
                }
            });
            for (int i = 0; i < files.length; i++) {
                try {
                    runner.convertAndPrint(files[i]);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } else if (pathFile.isFile()) {
            try {
                runner.convertAndPrint(pathFile.getName());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            System.out.println("Path is not a file or directory " + path);
        }
    }
    
    private File toTopsFile(String pdbFilename, String fileType, String fourLetterCode) throws IOException {
    	String decompressedFileName = 
    			this.compressedFileHandler.attemptDecompressionOfFile(pdbFilename, fileType);
        
        // if the compressedFileHandler has successfully decompressed the file, it will have a new name
        if (!decompressedFileName.equals("")) {
            pdbFilename = decompressedFileName;
        }
       
        String dsspFilename = fourLetterCode + ".dssp";
        String topsFilename = fourLetterCode + ".tops";
        this.dssp.convert(pdbFilename, dsspFilename);
        this.tops.convert(fourLetterCode, "", topsFilename, "");
        return new File(scratchDirectory, topsFilename);
    }

    @Override
    public String[] convertToGraphs(String pdbFilename, String fileType, String fourLetterCode) throws IOException {
    	File topsFile = toTopsFile(pdbFilename, fileType, fourLetterCode);
        return this.tops2String.convert(topsFile.toString(), "", "CATH");
    }
    
    @Override
	public Protein convertToCartoon(
			String pdbFilename, String fileType, String fourLetterCode) throws IOException {
    	File topsFile = toTopsFile(pdbFilename, fileType, fourLetterCode);
    	TopsFileReader topsFileReader = new TopsFileReader();
		return topsFileReader.readTopsFile(topsFile);
	}

	public void convertAndPrint(String pdbFilename) throws IOException {
        String fourLetterCode = "";
        if (pdbFilename.substring(0, 3).equals("pdb")) {
            fourLetterCode = pdbFilename.substring(3, 7);
        } else {
            fourLetterCode = pdbFilename.substring(0, 4);
        }
        String[] topsStrings = this.convertToGraphs(pdbFilename, "", fourLetterCode);
        for (int j = 0; j < topsStrings.length; j++) {
            // XXX topsStrings have a space in front!
            System.out.println(topsStrings[j]); 
        }
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }
}
