package tops.translation;

import java.io.IOException;

/**
 * Convert a pdb file (identified by its filename) to a .tops file.
 * 
 * @author maclean
 *
 */
public interface PDBToTopsConverter {

	public String[] convert(String pdbFilename, String fileType, String fourLetterCode) throws IOException;
}
