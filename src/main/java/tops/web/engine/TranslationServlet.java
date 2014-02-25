package tops.web.engine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import tops.dw.protein.Protein;
import tops.web.display.servlet.CartoonDataSource;
import tops.web.display.servlet.UploadCartoonDataSource;

public class TranslationServlet extends HttpServlet {

	private static final long serialVersionUID = -7794798388203061901L;

    private String pathToScratch;
    
    private String executablePath;

    @Override
    public void init() throws ServletException {
        String scratchDir = this.getInitParameter("scratch.dir");
        String exePath = this.getInitParameter("executable.path");
        
        this.pathToScratch = getServletContext().getRealPath(scratchDir);
        this.executablePath = getServletContext().getRealPath(exePath);
        
        this.log("scratch directory = " + scratchDir + " -> " + pathToScratch);
        this.log("exe path = " + exePath + " -> " + executablePath);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HashMap<String, String> filenameMap = this.uploadPDBFiles(request);
        CartoonDataSource cartoonDataSource = 
        		new UploadCartoonDataSource(filenameMap, executablePath, pathToScratch);
        Protein protein = cartoonDataSource.getCartoon(pathToScratch);
        HttpSession session = request.getSession();
        session.setAttribute("protein", protein);
    }

    public HashMap<String, String> uploadPDBFiles(HttpServletRequest request) {
        HashMap<String, String> filenames = new HashMap<String, String>();

        File repository = new File(pathToScratch);
        
        // Create a factory for disk-based file items
        int sizeThreshold = 5 * 1024 * 1024;	// lower limit below which file is in-memory
        FileItemFactory factory = new DiskFileItemFactory(sizeThreshold, repository);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        try {
			for (FileItem item : upload.parseRequest(request)) {
				if (!item.isFormField()) {
					String name = item.getName();
					String contentType = item.getContentType();
					filenames.put(name, contentType);
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
			log("File upload problem");
		}

        return filenames;
    }

}
