package tops.web.engine;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.StringTokenizer;

import tops.engine.TopsStringFormatException;
import tops.engine.TParser;

import tops.engine.drg.Comparer;
import tops.engine.drg.Pattern;

public class TopsGroupServlet extends javax.servlet.http.HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6078417380709102405L;
	private static final String diagramURL = "/tops/diagram";

    public String[] getInstances(String names) {

        String query = "SELECT dom_id, vertex_string, edge_string FROM TOPS_instance_nr JOIN TOPS_nr WHERE gr = group_id AND dom_id in";
        String nameList = names.replaceAll(",", "','");

        try {
            Connection connection = DataSourceWrapper.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query + " ('" + nameList
                    + "');");
            ArrayList<String> in = new ArrayList<String>();
            while (rs.next()) {
                String nextInstance = new String();
                nextInstance += rs.getString("dom_id") + " ";
                nextInstance += rs.getString("vertex_string") + " ";
                nextInstance += rs.getString("edge_string");
                in.add(nextInstance);
            }
            rs.close();
            statement.close();
            connection.close();
            this.getServletContext().log(
                    "Instances! :" + in.toString() + " " + nameList);
            return (String[]) in.toArray(new String[0]);
        } catch (SQLException squeel) {
            this.getServletContext().log("getInstances! :", squeel);
            return null;
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        String from = request.getContextPath();
//        ServletContext context = getServletContext();
        String names = request.getParameter("names");

        // if there are no name sin the request, try getting an array of results
        // from pipeline
        // odd way to switch between alternative services - might be a better
        // way
        String[] instances = new String[0];

        if (names == null) {
            instances = (String[]) request.getAttribute("results");
        } else {
            ArrayList<String> instanceList = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(names, ".");
            while (st.hasMoreElements()) {
                String n = (String) st.nextElement();
                // log(n);
                instanceList.add(n);
            }
            instances = (String[]) instanceList.toArray(instances);
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Group Pattern</title></head><body>");

        if (instances != null) {
            Comparer ex = new Comparer();
            // String result = null;
            Pattern result = null;
            try {
                result = ex.findPattern(instances);
            } catch (TopsStringFormatException tsfe) {
                this.log(tsfe.toString());
                return;
            }
            out.println("<b>" + result + "</b><hr>");
            out.println("<p><b>Common Pattern for group : </b></p><table>");
            TParser parser = new TParser();
            for (int i = 0; i < instances.length; i++) {
                parser.load(instances[i]);
                out.println("<tr><td>" + instances[i] + "</td><td>");
                String patternDiagramURL = "/" + parser.getVertexString() + "/"
                        + parser.getEdgeString() + "/" + parser.getName();
                out.println("<img src=\"" + TopsGroupServlet.diagramURL + "/200/100/none"
                        + patternDiagramURL + ".gif\"/></p>");
                out.println("</td></tr>");
            }

            out.println("</ul>");
        }
        out.println("</body></html>");
    }
}
