package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;

import com.oreilly.servlet.MultipartRequest; //accessory class from writer of oreilly 'java servlet programming' book

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.translation.PDBFileConverter;

public class TranslationServlet extends HttpServlet {

    private PDBFileConverter pdbFileConverter;

    private String path_to_scratch;

    @Override
    public void init() throws ServletException {
        String home = this.getInitParameter("home.dir");
        this.log("home directory = " + home);

        this.path_to_scratch = home + "/scratch/";
        this.pdbFileConverter = new PDBFileConverter(this.path_to_scratch);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HashMap filenames = this.uploadPDBFiles(request);
        ArrayList results = new ArrayList();

        if (filenames.size() > 0) {

            Iterator filenameIterator = filenames.keySet().iterator();
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
                    for (int i = 0; i < domains.length; i++) {
                        results.add(domains[i]);
                    }
                }
            }

            if (results.isEmpty()) {
                this.log("no results!");
                this.faliure(response, "conversion process failed!");
                return;
            }

        } else {
            this.log("no filenames!");
            this.faliure(response, "no filenames!");
            return;
        }

        // assuming the process hasn't failed so far, reply with the domain
        // strings
        StringBuffer xmlBuffer = new StringBuffer();
        xmlBuffer.append("<status>conversion success</status>");
        for (int j = 0; j < results.size(); j++) {
            String domainString = (String) results.get(j);
            xmlBuffer.append("<structure>");
            xmlBuffer.append("<domainString>").append(domainString).append(
                    "</domainString>");
            xmlBuffer.append("</structure>");
        }
        this.sendResponse(response, xmlBuffer);

    }

    public void faliure(HttpServletResponse response, String message)
            throws IOException {
        StringBuffer xmlBuffer = new StringBuffer();
        xmlBuffer.append("<status>").append(message).append("</status>");
        this.sendResponse(response, xmlBuffer);
    }

    // wrap the response in result tags and print to stream
    public void sendResponse(HttpServletResponse response,
            StringBuffer xmlBuffer) throws IOException {
        xmlBuffer.insert(0,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<result>");
        xmlBuffer.append("</result>");

        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        out.write(xmlBuffer.toString());
        out.close();
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

    public HashMap uploadPDBFiles(HttpServletRequest request) {
        HashMap filenames = new HashMap();

        try {
            // save to scratch directory, 5MB limit
            MultipartRequest multi = new MultipartRequest(request,
                    this.path_to_scratch, 5 * 1024 * 1024);
            Enumeration files = multi.getFileNames();

            while (files.hasMoreElements()) {
                String name = (String) files.nextElement();
                filenames.put(multi.getFilesystemName(name), multi
                        .getContentType(name));
            }

        } catch (Exception e) {
            this.log("upload problem: ", e);
        }

        return filenames;
    }

}// EOC
