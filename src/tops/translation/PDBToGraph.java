package tops.translation;

import java.io.IOException;

/**
 * Convert a PDB file to a list of tops graphs.
 * 
 * @author maclean
 *
 */
public interface PDBToGraph {

	public String[] convertToGraphs(String pdbFilename, String fileType, String fourLetterCode) throws IOException;
}
