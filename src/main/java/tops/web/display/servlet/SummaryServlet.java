package tops.web.display.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;

/**
 * Display an HTML page, summarizing a protein.
 * 
 * @author maclean
 *
 */
public class SummaryServlet extends HttpServlet {

	private static final long serialVersionUID = 1404081477291934173L;
	
    private String viewPath;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	Protein protein = (Protein) session.getAttribute("protein");
    	
    	String contextPath = request.getContextPath();
        this.viewPath = contextPath + "/view"; // "/tops/view";
        
        String path = "";
        displayDomains(protein, path, response);
    }

    private void displayDomains(Protein protein, String path, HttpServletResponse response) {
        Vector<DomainDefinition> domainNames = protein.GetDomainDefs();
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
            this.log("displayDomains : ", ioe);
        }

        String proteinName = protein.getName();
        pout.println("<html><head><title>Domains for : " + proteinName + "</title></head><body><table align=\"center\">");
        Enumeration<DomainDefinition> e = domainNames.elements();
        while (e.hasMoreElements()) {
            DomainDefinition dd = e.nextElement();
            String name = dd.toString();
            String filename = name + ".gif"; // eg '2bopA0' + '.' +  'gif' = '2bopA0.gif'
            String url = this.viewPath + "/" + path + "/" + filename;
            pout.println("<tr><td>");
            pout.println("<a href=\"" + url + "\">");
            pout.println("<img src=\"" + url + "\"></img>");
            pout.println("</a></td></tr>");
        }
        pout.println("</table></body></html>");
    }

}