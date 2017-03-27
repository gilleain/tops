package tops.web.display.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

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
    	// XXX - for now, a duplicate of doPost! FIXME
    	HttpSession session = request.getSession();
    	Protein protein = (Protein) session.getAttribute("protein");
    	
    	String contextPath = request.getContextPath();
        this.viewPath = contextPath + "/view"; // "/tops/view";
        
        String group = "";
        displayDomains(protein, group, response);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	// XXX - for now, a duplicate of doGet! FIXME
    	HttpSession session = request.getSession();
    	Protein protein = (Protein) session.getAttribute("protein");
    	
    	String contextPath = request.getContextPath();
        this.viewPath = contextPath + "/view"; // "/tops/view";
        
        String group = "session";	// pseudo-group indicating the data is in the session :/
        displayDomains(protein, group, response);
    }

    private void displayDomains(Protein protein, String group, HttpServletResponse response) {
        List<DomainDefinition> domainNames = protein.getDomainDefs();
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
            this.log("displayDomains : ", ioe);
        }

        String proteinName = protein.getName();
        pout.println("<html><head><title>Domains for : " + proteinName + "</title></head><body><table align=\"center\">");
        for (DomainDefinition dd : domainNames) {
            String name = dd.toString();
            String filename = name + ".gif"; // eg '2bopA0' + '.' +  'gif' = '2bopA0.gif'
            String url = this.viewPath + "/" + group + "/" + filename;
            pout.println("<tr><td>");
            pout.println("<a href=\"" + url + "\">");
            pout.println("<img src=\"" + url + "\"/>");
            pout.println("</a></td></tr>");
        }
        pout.println("</table></body></html>");
    }

}
