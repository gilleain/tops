package tops.web.display.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;

public class FindFilesServlet extends HttpServlet {

	private static final long serialVersionUID = -8699565050755398690L;

	private TopsFileManager tfm;

    @Override
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        try {
            Enumeration<?> e = config.getInitParameterNames();
            this.tfm = new TopsFileManager();
            while (e.hasMoreElements()) {
                String nextClass = (String) e.nextElement();
                String nextPath = config.getInitParameter(nextClass);
                String realPath = config.getServletContext().getRealPath(nextPath);
                this.tfm.addPathMapping(nextClass, realPath);
                this.getServletContext().log(
                        "mapping classname : " + nextClass + " to path " + nextPath + " actually at " + realPath);
            }
        } catch (Exception e) {
            this.getServletContext().log("path problems", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pid = request.getParameter("pcode");
        String cid = request.getParameter("chain");
        String did = request.getParameter("domid");
        String cla = request.getParameter("classf");
        String action = request.getParameter("action");
        
        System.out.println("pid [" + pid + "] chain [" + cid + "] domid [" + did + "] classf [" + cla + "]");
        

        // sanity check
        if (pid.equals("")) {
        	this.printError("<html><head><title>Error</title></head>" +
        					"<body><b>PDB ID MUST BE SPECIFIED</b></body></html>",
        					response);
        	return;
        } 

        try {
	        if ((cid != null) && (cid.equals(""))) { // no chain, search for chain files
	        	// get the file names from the directory
	        	String[] names = this.tfm.getNames(cla, pid, cid);
	        	
	        	// for each file name, read in the file, and add to a single protein object
	        	Protein mergedProtein = new Protein();
	        	for (String name : names) {
	        		Protein chain = new Protein(this.tfm.getStreamFromDir(cla, name));
	        		for (DomainDefinition dd : chain.getDomainDefs()) {
	        			SecStrucElement root = chain.getDomain(chain.getDomainIndex(dd.getCATHcode()));
	        			mergedProtein.addTopsLinkedList(root, dd);
	        		}
	        	}
	        	handle(mergedProtein, action, request, response);

	        } else if ((cid != null && !cid.equals("")) && (did != null && !did.equals(""))) { // all parameters supplied
	        	String topsfile = pid + cid + ".tops";	// Uhm, then what happens here if either are blank??
	        	Protein protein = new Protein(this.tfm.getStreamFromDir(cla, topsfile));
	        	handle(protein, action, request, response);
	        } else {
	        	// TODO - no chain or domain?
	        }
        } catch (FileNotFoundException fnfe) {
        	this.printError(
        			"<html><head><title>ERROR 403 : TOPS CARTOON NOT FOUND</title></head>" +
        					"<body>Sorry, could not find this cartoon. Reason : <p>" + fnfe +
        					"</body></html>",
        					response
        			);
        	return;
        }
    }
    
    private void handle(Protein protein, String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	session.setAttribute("protein", protein);
    	String url = "/" + action;
    	RequestDispatcher dispatcher = request.getRequestDispatcher(url);
        dispatcher.forward(request, response);
    }
    
    private void printError(String message, HttpServletResponse response) {
    	response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
        }
        pout.write(message);
        pout.flush();
    }

}
