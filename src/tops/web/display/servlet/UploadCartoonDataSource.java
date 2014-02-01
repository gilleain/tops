package tops.web.display.servlet;

import java.io.IOException;
import java.util.Map;

import tops.dw.protein.Protein;
import tops.translation.DsspTopsRunner;
import tops.translation.PDBToCartoon;

public class UploadCartoonDataSource implements CartoonDataSource {
	
	private PDBToCartoon cartoonConverter;
	
	private String pathToScratch;
	
	private Map<String, String> filenameMap;
	
	public UploadCartoonDataSource(Map<String, String> filenameMap, String pathToScratch) {
		this.pathToScratch = pathToScratch;
		this.filenameMap = filenameMap;
		this.cartoonConverter = new DsspTopsRunner(pathToScratch);
	}

	@Override
	public Protein getCartoon(String directory) {
		String pdbFilename = "";
		String fileType = "";
		String fourLetterCode = "";
		try {
			return this.cartoonConverter.convertToCartoon(pdbFilename, fileType, fourLetterCode);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map<String, String> getParams() {
		return filenameMap;
	}

}
