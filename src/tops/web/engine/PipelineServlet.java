package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import com.oreilly.servlet.MultipartRequest; //accessory class from writer of oreilly 'java servlet programming' book

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.translation.PDBFileConverter;

public class PipelineServlet extends HttpServlet {

    private PDBFileConverter pdbFileConverter;

    private String path_to_scratch;

    @Override
    public void init() throws ServletException {
        String home = this.getInitParameter("home.dir");
        this.path_to_scratch = home + "/scratch/";
//        String dssp_executable = home + "/dssp";
//        String tops_executable = home + "/tops";
        this.log("home directory = " + home);
        this.pdbFileConverter = new PDBFileConverter(this.path_to_scratch);
    }

    // following to be set in the upload method
    private String topNumber;

    private String targetService; // the name of the servlet service that will
                                    // receive this data

    private String pagesize;

    private String sub;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HashMap<String, String> filenames = this.uploadPDBFiles(request);

        if (filenames.size() > 0) {

            String[] results = new String[filenames.size()];
            Iterator<String> filenameIterator = filenames.keySet().iterator();
            int i = 0;

            while (filenameIterator.hasNext()) {
                String file_name = (String) filenameIterator.next();
                String file_type = (String) filenames.get(file_name);
                String four_char_id = this.randomName();
                this.log("submitted file : " + file_name + " type : " + file_type
                        + " four_char_id : " + four_char_id);
                String[] domains = null;
                try {
                    domains = this.pdbFileConverter.convertFileToTopsStrings(
                            file_name, file_type, four_char_id,
                            this.path_to_scratch);
                    for (int j = 0; j < domains.length; j++) {
                        this.log("made domain string : " + domains[j]
                                + " for job : " + four_char_id);
                    }
                } catch (IOException ioe) {
                    this.log("tops file io exception! : " + four_char_id + ".tops "
                            + ioe);
                }
                if (domains == null) {
                    this.log("domains null, no results for four_char_id : "
                            + four_char_id);
                } else {
                    results[i++] = domains[0];
                }
            }

            if (results[0] == null) {
                this.log("no results!");
                this.faliure(response, "Conversion process failed!");
                return;
            }

            String next = null;
            if (this.targetService.equals("match")) {
                request.setAttribute("target", results[0]); // Note the this
                                                            // only uses the
                                                            // first file!
                next = "/pattern/match";
            } else if (this.targetService.equals("compare")) {
                request.setAttribute("newSubmission", "true"); // doesn't
                                                                // matter what
                                                                // this is, so
                                                                // long as it's
                                                                // set!
                request.setAttribute("topnum", this.topNumber); // !!
                request.setAttribute("targetService", this.targetService); // !!
                request.setAttribute("pagesize", this.pagesize); // !!
                request.setAttribute("target", results[0]); // Note the this
                                                            // only uses the
                                                            // first file!
                request.setAttribute("sub", this.sub); // !!
                next = "/pattern/compare";
            } else if (this.targetService.equals("group")) {
                request.setAttribute("results", results);
                next = "/pattern/group";
            } else {
                this.log("unknown service : " + this.targetService);
                this.faliure(response, "unknown service : " + this.targetService);
                return;
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher(next);
            dispatcher.forward(request, response);

        } else {
            this.log("no filenames!");
            this.faliure(response, "No filenames!");
            return;
        }

    }

    public void faliure(HttpServletResponse response, String message) {
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

    public HashMap<String, String> uploadPDBFiles(HttpServletRequest request) {
        HashMap<String, String> filenames = new HashMap<String, String>();

        try {
            // save to scratch directory, 5MB limit
            MultipartRequest multi = new MultipartRequest(request,
                    this.path_to_scratch, 5 * 1024 * 1024);
            Enumeration<?> files = multi.getFileNames();

            while (files.hasMoreElements()) {
                String name = (String) files.nextElement();
                filenames.put(multi.getFilesystemName(name), multi
                        .getContentType(name));
            }

            this.topNumber = multi.getParameter("topnum"); // number of results
                                                            // to return
            this.pagesize = multi.getParameter("pagesize"); // page size
            this.targetService = multi.getParameter("targetService"); // where
                                                                        // is
                                                                        // this
                                                                        // going?
            this.sub = multi.getParameter("subclasses"); // cath, scop,
                                                            // nreps,
                                                            // superfamilies,
                                                            // etc

        } catch (Exception e) {
            this.log("upload problem: ", e);
        }

        return filenames;
    }

}// EOC
