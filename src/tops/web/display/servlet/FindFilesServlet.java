package tops.web.display.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import tops.dw.protein.*;

public class FindFilesServlet extends javax.servlet.http.HttpServlet {

    private TopsFileManager tfm;

    private String contextPath;

    private String viewPath;

    private String fullPath;

    @Override
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        try {
            Enumeration<?> e = config.getInitParameterNames();
            this.tfm = new TopsFileManager();
            while (e.hasMoreElements()) {
                String nextClass = (String) e.nextElement();
                String nextPath = config.getInitParameter(nextClass);
                this.tfm.addPathMapping(nextClass, nextPath);
                this.getServletContext().log(
                        "mapping classname : " + nextClass + " to path "
                                + nextPath);
            }
        } catch (Exception e) {
            this.getServletContext().log("path problems", e);
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pid = request.getParameter("pcode");
        String cid = request.getParameter("chain");
        String did = request.getParameter("domid");
        String cla = request.getParameter("classf");
        String typ = request.getParameter("filetype");
        this.contextPath = request.getContextPath();
        this.fullPath = this.contextPath + request.getServletPath(); // "/tops/find";
        this.viewPath = this.contextPath + "/view"; // "/tops/view";

        if (pid.equals("")) {
            this.rtfm(response);
        } // sanity check

        if (((cid != null) && (!cid.equals("")))
                && ((did != null) && (!did.equals("")))) {// all parameters
                                                            // supplied
//            String path = tfm.getPathMapping(cla);
            String filename = pid + cid + did + "." + typ;
            // String name = pid + cid + did;
            // String url = "/view?path=" + path + "&filename=" + filename +
            // "&domid=" + name; OLD STYLE
            String url = "/view/" + cla + "/" + filename;
            RequestDispatcher dispatcher = request.getRequestDispatcher(url);
            dispatcher.forward(request, response);
        }

        if ((cid != null) && (cid.equals(""))) { // no chain, search for
                                                    // chain files
            String[] names = null;
            try {
                // get the file names from the directory
                names = this.tfm.getNames(cla, pid, cid);

            } catch (FileNotFoundException fnfe) {
                this.fileNotFound(fnfe, response);
                return;
            }

            this.listChains(names, cla, pid, typ, response);

        } else { // one of chain or domain may be blank

            Protein p = null;
//            String filename = pid + cid + did + "." + typ;
            String topsfile = pid + cid + ".tops";

            try {
                InputStream in = this.tfm.getStreamFromDir(cla, topsfile);
                p = new Protein(in);
                if (did.equals("")) {
                    // String path = tfm.getPathMapping(cla); UNNECCESSARY

                    this.displayDomains(p, pid, cid, typ, cla, response); 
                    return;
                }

            } catch (FileNotFoundException fnfe) {
                this.fileNotFound(fnfe, response);
                return;
            }

        }
    }

    private void listChains(String[] names, String cl, String pi, String tp,
            HttpServletResponse response) {
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
            this.log("listChains : ", ioe);
        }

        pout.println("<html><head><title>Chains found for : " + pi
                + "</title></head><body><table align=\"center\">");
        for (int i = 0; i < names.length; i++) {
            String ch = names[i].substring(4, 5);
            String url = this.fullPath + "?pcode=" + pi + "&chain=" + ch
                    + "&domid=&classf=" + cl + "&filetype=" + tp;
            pout.println("<tr><td>");
            pout.println("<a href=\"" + url + "\">" + names[i]);
            pout.println("</a></td></tr>");
        }
        pout.println("</table></body></html>");
    }

    private void displayDomains(Protein p, String pi, String c,
            String filetype, String path, HttpServletResponse response) {
        Vector<DomainDefinition> domainNames = p.GetDomainDefs();
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
            this.log("displayDomains : ", ioe);
        }

        pout.println("<html><head><title>Domains for : " + pi + " Chain " + c
                + "</title></head><body><table align=\"center\">");
        Enumeration<DomainDefinition> e = domainNames.elements();
        while (e.hasMoreElements()) {
            DomainDefinition dd = e.nextElement();
            String name = dd.toString();
            // String url = viewPath + "?path=" + path + "&filename=" + filename
            // + "&domid=" + name; OLD STYLE
            String filename = name + "." + filetype; // eg '2bopA0' + '.' +
                                                        // 'gif' = '2bopA0.gif'
            String url = this.viewPath + "/" + path + "/" + filename;
            pout.println("<tr><td>");
            pout.println("<a href=\"" + url + "\">");
            pout.println("<img src=\"" + url + "\"></img>");
            pout.println("</a></td></tr>");
        }
        pout.println("</table></body></html>");
    }

    private void fileNotFound(FileNotFoundException fnfe,
            HttpServletResponse response) {
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
        }

        pout
                .println("<html><head><title>ERROR 403 : TOPS CARTOON NOT FOUND</title></head>");
        pout.println("<body>Sorry, could not find this cartoon. Reason : <p>"
                + fnfe);
        pout.println("</body></html>");
    }

    private void rtfm(HttpServletResponse response) {
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
        }

        pout.println("<html><head><title>RTFM</title></head>");
        pout.println("<body><b>PDB ID MUST BE SPECIFIED</b></body></html>");
    }

} // EOC
