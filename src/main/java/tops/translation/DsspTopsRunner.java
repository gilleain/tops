package tops.translation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import tops.dw.protein.Protein;


/**
 * @author maclean
 *
 */
public class DsspTopsRunner implements PDBToGraph, PDBToCartoon {
    
    private RunDssp dssp;
    
    private RunTops tops;
    
    private Tops2String tops2String;
    
    private CompressedFileHandler compressedFileHandler;
    
    public DsspTopsRunner(String scratchDirectory) {
        this("./", scratchDirectory);
    }
    
    public DsspTopsRunner(String executableDir, String scratchDirectory) {
    	String dsspPath = new File(executableDir, "dssp").toString();
    	String topsPath = new File(executableDir, "Tops").toString();
        this.dssp = new RunDssp(dsspPath, scratchDirectory, scratchDirectory, "./");
        this.tops = new RunTops(topsPath, scratchDirectory, scratchDirectory, "./");
        this.tops2String = new Tops2String(scratchDirectory);
        this.compressedFileHandler = new CompressedFileHandler(scratchDirectory);
    }
    
    private String toTopsFile(String pdbFilename, String fileType, String fourLetterCode) throws IOException {
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
        return topsFilename;
    }

    @Override
    public String[] convertToGraphs(String pdbFilename, String fileType, String fourLetterCode) throws IOException {
    	String topsFilename = toTopsFile(pdbFilename, fileType, fourLetterCode);
        return this.tops2String.convert(topsFilename, "", "CATH");
    }
    
    @Override
	public Protein convertToCartoon(
			String pdbFilename, String fileType, String fourLetterCode) throws IOException {
    	String topsFilename = toTopsFile(pdbFilename, fileType, fourLetterCode);
		return new tops.dw.protein.Protein(topsFilename);
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
    
    public static void main(String[] args) {
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

}
