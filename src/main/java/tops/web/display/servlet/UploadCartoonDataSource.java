package tops.web.display.servlet;

import java.io.IOException;
import java.util.Map;

import tops.dw.protein.Protein;
import tops.translation.DsspTopsRunner;
import tops.translation.PDBToCartoon;

/**
 * Source of cartoon data - from a converted, uploaded PDB file.
 * 
 * @author maclean
 *
 */
public class UploadCartoonDataSource implements CartoonDataSource {
	
	private PDBToCartoon cartoonConverter;
	
	private Map<String, String> filenameMap;
	
	public UploadCartoonDataSource(Map<String, String> filenameMap, String pathToScratch) {
		this.filenameMap = filenameMap;
		this.cartoonConverter = new DsspTopsRunner(pathToScratch);
	}

	@Override
	public Protein getCartoon(String directory) {
		if (filenameMap.size() > 1)	return null;		// XXX -can't currently handle multiple files!
		
		String pdbFilename = filenameMap.keySet().toArray(new String[]{})[0];
		String fileType = filenameMap.get(pdbFilename);
		String fourLetterCode = this.randomName();
		try {
			return this.cartoonConverter.convertToCartoon(pdbFilename, fileType, fourLetterCode);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String randomName() {
		StringBuffer name = new StringBuffer();
		name.append(this.d10());
		name.append(this.d10());
		name.append(this.d10());
		name.append(this.d10());
		return name.toString();
	}

	private int d10() {
		return (int) (Math.random() * 9) + 1;
	}

	@Override
	public Map<String, String> getParams() {
		return filenameMap;
	}

}
