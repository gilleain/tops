package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import tops.engine.drg.Explorer;

public class TopsPatternGroupServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4051601334759040331L;

	public String getPattern(String pattern_id) {
        String query = "SELECT * FROM TOPS_pattern WHERE pattern_id = "
                + pattern_id + ";";
        try {
            ResultSet rs = DataSourceWrapper.executeQuery(query);
            // ArrayList in = new ArrayList();
            String pattern = new String();
            while (rs.next()) {
                pattern += rs.getString("pattern_id") + " ";
                pattern += rs.getString("pattern_vertices") + " ";
                pattern += rs.getString("pattern_edges") + " ";
                pattern += rs.getString("compression") + " ";
                pattern += rs.getString("note");
            }
            return pattern;

        } catch (SQLException sqle) {
            this.getServletContext().log("getPattern! :", sqle);
            return null;
        }

    }

    public String[] getInstances(String pattern_id) {
        String query = "SELECT instance_id,vertex_string,edge_string "
                + "FROM TOPS_pattern JOIN TOPS_pattern_instance JOIN TOPS_instance_nr JOIN TOPS_nr "
                + "WHERE TOPS_pattern.pattern_id = TOPS_pattern_instance.pattern_id "
                + "AND instance_id = dom_id AND gr = group_id AND TOPS_pattern.pattern_id = ";

        try {
            ResultSet rs = DataSourceWrapper.executeQuery(query + pattern_id
                    + ";");
            ArrayList<String> in = new ArrayList<String>();
            while (rs.next()) {
                String nextInstance = new String();
                nextInstance += rs.getString("instance_id") + " ";
                nextInstance += rs.getString("vertex_string") + " ";
                nextInstance += rs.getString("edge_string");
                in.add(nextInstance);
            }
            return (String[]) in.toArray(new String[0]);

        } catch (SQLException sqle) {
            this.getServletContext().log("getInstances! :", sqle);
            return null;
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // String from = request.getContextPath();
        // ServletContext context = getServletContext();
        String pattern_id = request.getParameter("pid");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Instances for Pattern " + pattern_id
                + "</title></head><body>");

        String pattern = this.getPattern(pattern_id);
        String[] instances = this.getInstances(pattern_id);

        out.println("<p><b>Members of pattern group : </b></p>");
        out.println("<p>" + pattern + "</p>");
        out.println("<form action=\"/tops/group\" method=\"POST\">");
        for (int i = 0; i < instances.length; i++) {
            out.println("<input type=\"checkbox\" name=\"" + instances[i]
                    + "\">" + instances[i] + "</input><br>");
        }
        out
                .println("<input type=\"submit\" value=\"find pattern for subset\">");
        out.println("</form>");

        /*
         * if (instances != null) {
         * 
         * Explorer ex = new Explorer(); String result =
         * ex.findPattern(instances); out.println("<b>" + result + "</b><hr>");
         * 
         * out.println("<p><b>Common Pattern for group : </b></p><ul>");
         * for (int i = 0; i < instances.length; i++) { out.println("<li>" +
         * instances[i] + "</li>"); } out.println("</ul>"); }
         */

        out.println("</body></html>");
    }

}
