package tops.web.display.servlet;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.dw.protein.CATHcode;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.view.tops2D.cartoon.CartoonDrawer;

public class CartoonServlet extends HttpServlet {

    static {
        System.setProperty("java.awt.headless", "true");
        // System.setProperty ("awt.toolkit", "com.eteks.awt.PJAToolkit");
        // System.setProperty("java.awt.graphicsenv",
        // "com.eteks.java2d.PJAGraphicsEnvironment");
    }

    private static final int DEFAULT_WIDTH = 200;

    private static final int DEFAULT_HEIGHT = 200;

    /*
     * horrible hack to ensure images have correct filenames. uses extra path
     * info to the servlet as the parameters so a request for
     * '/tops/path-servlet-name/group/chaindomain.ext' gets chain.tops (or db equiv.) 
     * from whatever directory 'group' maps to and renders it as an ".ext" file. 'nice'
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo(); // eg /cath/1.2/2bopA0.gif OR /cath/2bopA0.gif
        PathParser pathParser = new PathParser(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        Map<String, String> params = pathParser.parsePath(path);
        
        ServletConfig config = this.getServletConfig();
        String pathToFiles = config.getInitParameter(params.get("group"));
        this.log("path = " + pathToFiles 
        		+ " group = " + params.get("group") 
        		+ " fileType = " + params.get("fileType") 
        		+ " domain = " + params.get("domain") );

        // first, try and get the data from the given source
        File f = new File(pathToFiles, params.get("filename"));
        if (!f.canRead()) {
            this.error("File not found " + params.get("filename"), response);
            return;
        }

        // quickly check if we only want the text of the file
        if (params.get("fileType").equals("tops")) { // TOPS! file format (basically, old-style tops file!)
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            String line;
            BufferedReader buf = null;
            try {
                buf = new BufferedReader(new FileReader(f));
                while ((line = buf.readLine()) != null) {
                    out.println(line);
                }
            } finally {
                if (buf != null) {
                    buf.close();
                }
            }
            return;
        }

        // assuming the tops.dw.protein can be created, determine what to return
        SecStrucElement root = this.getRoot(params.get("domain"), f);
        
        // do highlights
        String highlight = params.get("highlight");
        if (highlight != null) {
            this.highlight(root, highlight);
        }

        String domain = params.get("domain");
        if (root == null) {
            this.error("Domain not found : " + domain, response);
            return;
        }

        String fileType = params.get("fileType");
        int width = Integer.parseInt(params.get("width"));
        int height = Integer.parseInt(params.get("height"));
        CartoonDrawer drawer = new CartoonDrawer();
        if (fileType.equals("gif")) { // Image - could be several types?

            response.setContentType("image/gif");
            OutputStream out = response.getOutputStream();

            drawer.draw(domain, "IMG", width, height, root, out);

        } else if (fileType.equals("pdf")) { // Portable Document Format, using iText library

            response.setContentType("application/pdf");
            OutputStream out = response.getOutputStream();

            drawer.draw(domain, "PDF", width, height, root, out);

        } else if (fileType.equals("svg")) { // Support Vector Graphics - basically XML

            response.setContentType("image/svg+xml");
            PrintWriter writer = response.getWriter();

            drawer.draw(domain, "SVG", root, writer);

        } else if (fileType.equals("ps")) { // Postscript - basically text

            response.setContentType("application/ps");
            PrintWriter writer = response.getWriter();

            drawer.draw(domain, "PS", root, writer);

        } else {
            this.error("filetype is not supported", response);
            return;
        }

    }

    private void highlight(SecStrucElement root, String highlight) {
        Color strandColor = Color.yellow;
        Color helixColor = Color.red;
        Color otherColor = Color.blue;

        // check for special cases - "none" and "all"
        if (highlight.equals("none")) {
            return;
        } else if (highlight.equals("all")) {
            for (SecStrucElement s = root; s != null; s = s.GetTo()) {
                if (s.Type.equals("E")) {
                    s.setColour(strandColor);
                } else if (s.Type.equals("H")) {
                    s.setColour(helixColor);
                } else {
                    s.setColour(otherColor);
                }
            }
            return;
        }

        String[] bits = highlight.split("\\.");
        for (int i = 0; i < bits.length; i++) {
            int index = Integer.parseInt(bits[i]);
            SecStrucElement s = root.GetSSEByNumber(index);
            if (s.Type.equals("E")) {
                s.setColour(strandColor);
            } else if (s.Type.equals("H")) {
                s.setColour(helixColor);
            } else {
                s.setColour(otherColor);
            }
        }
    }

    private SecStrucElement getRoot(String domid, File f) throws IOException {
        Protein p = new Protein(f);

        Vector<SecStrucElement> doms = p.GetLinkedLists();
        int domainIndex = p.GetDomainIndex(new CATHcode(domid));
        if (domainIndex == -1) {
            return null;
        }

        return doms.elementAt(domainIndex);
    }

    private void error(String problem, HttpServletResponse response) {
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
        }

        pout.println("<html><head><title>ERROR</title></head>");
        pout.println("<body>Sorry, could not draw this cartoon. Reason : <p>" + problem);
        pout.println("</body></html>");
    }

}
