package tops.translation;

import java.io.File;
import java.io.IOException;

import tops.translation.model.Protein;

public class PDBFileConverter implements PDBToGraph, PDBToCartoon {

    private CompressedFileHandler compressedFileHandler;
    
    private String pathToScratch;

    public PDBFileConverter(String pathToScratch) {
        this.compressedFileHandler = new CompressedFileHandler(pathToScratch);
        this.pathToScratch = pathToScratch;
    }
    
    public String getNameFromFilename(String fileName) {
        // check for spaces, and replace with underscores
        if (fileName.indexOf(' ') != -1) {
            fileName.replace(' ', '_');
        }
        int dotPosition = fileName.indexOf('.');
        if (dotPosition == -1) {
            return fileName;
        }
        return fileName.substring(0, dotPosition);
    }
    
    private Protein toProtein(String fileName, String fileType, String fourCharId) throws IOException {
    	String decompressedFileName = this.compressedFileHandler.attemptDecompressionOfFile(fileName, fileType);
        
        // if the compressedFileHandler has successfully decompressed the file, it will have a new name
        if (!decompressedFileName.equals("")) {
            fileName = decompressedFileName;
        }

        FoldAnalyser foldAnalyser = new FoldAnalyser();
        Protein protein = PDBReader.read(new File(this.pathToScratch, decompressedFileName).toString());
        foldAnalyser.analyse(protein);
        return protein;
    }
    
    public tops.dw.protein.Protein convertToCartoon(
    		String fileName, String fileType, String fourCharId) throws IOException {
    	Protein protein = toProtein(fileName, fileType, fourCharId);
    	return ProteinConverter.convert(protein);
    }

    
    /**
     * 
     * @param fileName The name of the file to convert.
     * @param fileType The file's compressed type.
     * @param fourCharId The pdb id.
     * @return
     * @throws IOException
     */
    public String[] convertToGraphs(String fileName, String fileType, String fourCharId) throws IOException {
    	Protein protein = toProtein(fileName, fileType, fourCharId);
    	
        // XXX TODO : why is fourCharId not used here?
        String[] chainStrings = protein.toTopsChainStringArray();
        String forename = this.getNameFromFilename(fileName);
        for (int i = 0; i < chainStrings.length; i++) {
            chainStrings[i] = forename + chainStrings[i];
        }

        return chainStrings;
    }
}
