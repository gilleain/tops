package tops.web.display.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

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

	private Map<String, String> filenameMap;
	
	private PDBToCartoon cartoonConverter;
	
	public UploadCartoonDataSource(HttpServletRequest request, 
								   String executablePath, String pathToScratch) {
		try {
			this.filenameMap = uploadPDBFiles(request, pathToScratch);
		} catch (Exception e) {
			// TODO - really commons-upload, exception?
			e.printStackTrace();
		}
		this.cartoonConverter = new DsspTopsRunner(executablePath, pathToScratch);
	}

	@Override
	public Protein getCartoon(String directory) {
		// XXX - can't currently handle multiple files!
		if (filenameMap.size() > 1)	return null;		

		String pdbFilename = filenameMap.keySet().toArray(new String[]{})[0];
		String fileType = filenameMap.get(pdbFilename);
		
		try {
			String fourLetterCode = pdbFilename.substring(0, 4);
			return this.cartoonConverter.convertToCartoon(pdbFilename, fileType, fourLetterCode);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public HashMap<String, String> uploadPDBFiles(HttpServletRequest request, String pathToScratch) throws Exception {
		HashMap<String, String> filenames = new HashMap<String, String>();

		File repository = new File(pathToScratch);

		// Create a factory for disk-based file items
		//	        int sizeThreshold = 5 * 1024 * 1024;	// lower limit below which file is in-memory
		int sizeThreshold = 100;	// force to write to disk?
		FileItemFactory factory = new DiskFileItemFactory(sizeThreshold, repository);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			for (FileItem item : upload.parseRequest(request)) {
				if (!item.isFormField()) {
//					String name = item.getName();
					String contentType = item.getContentType();
					String randomName = this.makeRandomName() + ".pdb";
					File uploadedFile = new File(pathToScratch, randomName);
					item.write(uploadedFile);
					filenames.put(randomName, contentType);
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
//			log("File upload problem");	// TODO
		}

		return filenames;
	}

	private String makeRandomName() {
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
