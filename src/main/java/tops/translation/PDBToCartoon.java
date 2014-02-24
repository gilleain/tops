package tops.translation;

import java.io.IOException;

/**
 * Convert a PDB file to a 'Cartoon' object, which is a 2D layout of a protein.
 * 
 * XXX : note that it currently creates an 'old-style' object...
 *  
 * @author maclean
 *
 */
public interface PDBToCartoon {
	
	public tops.dw.protein.Protein convertToCartoon(
			String pdbFilename, String fileType, String fourLetterCode) throws IOException;

}
