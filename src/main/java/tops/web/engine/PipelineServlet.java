package tops.web.engine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import tops.translation.PDBFileConverter;
import tops.translation.PDBToGraph;

public class PipelineServlet extends HttpServlet {

	private static final long serialVersionUID = 3578846986797503235L;

	private PDBToGraph pdbFileConverter;

    private String pathToScratch;

    @Override
    public void init() throws ServletException {
        String home = this.getInitParameter("home.dir");
        this.pathToScratch = home + "/scratch/";
        this.log("home directory = " + home);
        this.pdbFileConverter = new PDBFileConverter(this.pathToScratch);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> filenameTypeMap = this.uploadPDBFiles(request);

        if (filenameTypeMap.size() > 0) {
        	int i = 0;
            String[] results = new String[filenameTypeMap.size()];
            for (Entry<String, String> entry : filenameTypeMap.entrySet()) {
                String fileName = entry.getKey();
                String fileType = entry.getValue();
                String fourCharId = this.randomName();
                this.log("submitted file : " + fileName + " type : " + fileType
                        + " four_char_id : " + fourCharId);
                String[] domains = null;
                try {
                    domains = this.pdbFileConverter.convertToGraphs(fileName, fileType, fourCharId);
                    for (int j = 0; j < domains.length; j++) {
                        this.log("made domain string : " + domains[j] + " for job : " + fourCharId);
                    }
                } catch (IOException ioe) {
                    this.log("tops file io exception! : " + fourCharId + ".tops " + ioe);
                }
                if (domains == null) {
                    this.log("domains null, no results for four_char_id : " + fourCharId);
                } else {
                    results[i++] = domains[0];
                }
            }

            if (results[0] == null) {
                this.log("no results!");
                this.failure(response, "Conversion process failed!");
                return;
            }

            String next = null;
            String targetService = (String) request.getAttribute("targetService");
            if (targetService.equals("match")) {
                request.setAttribute("target", results[0]); // Note the this only uses the first file!
                next = "/pattern/match";
            } else if (targetService.equals("compare")) {
            	// doesn't matter what this is, so long as it's set!
                request.setAttribute("newSubmission", "true");
                
                // Note the this only uses the first file!
                request.setAttribute("target", results[0]); 
                next = "/pattern/compare";
            } else if (targetService.equals("group")) {
                request.setAttribute("results", results);
                next = "/pattern/group";
            } else {
                this.log("unknown service : " + targetService);
                this.failure(response, "unknown service : " + targetService);
                return;
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher(next);
            dispatcher.forward(request, response);

        } else {
            this.log("no filenames!");
            this.failure(response, "No filenames!");
        }

    }

    public void failure(HttpServletResponse response, String message) {
        response.setContentType("text/html");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException ioe) {
            this.log(ioe.toString());
        }
        out.println("<html><body>");
        out.println("FAILED! : " + message);
        out.println("</body></html>");
    }

    private String randomName() {
        StringBuilder name = new StringBuilder();
        name.append(this.d10());
        name.append(this.d10());
        name.append(this.d10());
        name.append(this.d10());
        return name.toString();
    }

    private int d10() {
        return new Random().nextInt(10) + 1;
    }

    public Map<String, String> uploadPDBFiles(HttpServletRequest request) {
        Map<String, String> filenames = new HashMap<>();

        File repository = new File(pathToScratch);
        
        // Create a factory for disk-based file items
        int sizeThreshold = 5 * 1024 * 1024;	// lower limit below which file is in-memory
        FileItemFactory factory = new DiskFileItemFactory(sizeThreshold, repository);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request
        try {
			for (FileItem item : upload.parseRequest(request)) {
				if (item.isFormField()) {
					String fieldName = item.getFieldName();
					if (fieldName.equals("topnum")) {
						request.setAttribute("topnum", item.getString());
					} else if (fieldName.equals("pagesize")) {
						request.setAttribute("pagesize", item.getString());
					} else if (fieldName.equals("targetService")) {
						request.setAttribute("targetService", item.getString());
					} else if (fieldName.equals("subclasses")) {
						// cath, scop, nreps, superfamilies, etc
						request.setAttribute("sub", item.getString());
					}
				} else {
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
