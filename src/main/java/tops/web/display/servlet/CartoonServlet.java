package tops.web.display.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;
import tops.view.tops2D.cartoon.CartoonDrawer;

public class CartoonServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8579686840885253792L;

	static {
        System.setProperty("java.awt.headless", "true");
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
        
        ServletConfig config = this.getServletConfig();
        CartoonDataSource source = new URICartoonDataSource(path, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        Map<String, String> params = source.getParams();
        String pathToFiles = config.getInitParameter(params.get("group"));
        
        Protein protein;
        try {
        	protein = source.getCartoon(pathToFiles);
        } catch (IOException ioe) {
        	this.error(ioe.getMessage(), response);
        	return;
        }

        // quickly check if we only want the text of the file
        if (params.get("fileType").equals("tops")) { // TOPS! file format (basically, old-style tops file!)
            response.setContentType("text/plain");
            ServletOutputStream out = response.getOutputStream();
            protein.WriteTopsFile(out);
            return;
        }

        SecStrucElement root = protein.getDomain(0);	// for now...
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
