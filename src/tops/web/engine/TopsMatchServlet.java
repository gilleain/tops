package tops.web.engine;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.ArrayList;

import java.sql.SQLException;
import java.sql.ResultSet;

import tops.engine.TopsStringFormatException;
import tops.engine.drg.Matcher;

public class TopsMatchServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestPath = request.getContextPath();
//        ServletContext context = getServletContext();
        String target = (String) request.getAttribute("target");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Match Results</title>");
        out.println("<style type=\"text/css\">");
        out.println("<!--");
        out.println(".oddrow  { background-color: #cccc99; }");
        out.println(".evenrow { background-color: #fffafa; }");
        out.println("-->");
        out.println("</style></head><body>");

        if (target == null) {
            out.println("<hr><b>!NO DIAGRAMS IN REQUEST!</b><hr>");
            out.println("</body></html>");
            return;
        }

        out
                .println("<p><b>Patterns that match Target : " + target
                        + "</b></p>");
        out.println("<p>OR : compare to all in db :");
        out.println("<form action=\"" + requestPath
                + "/pattern/compare?target=" + target + "\" method=\"POST\">");
        out.println("<input type=\"submit\" name=\"compare\"></form>");

        String[] patterns = null;

        String query = "SELECT pattern_id, pattern_vertices, pattern_edges, note  FROM TOPS_pattern ORDER BY compression";

        try {
            ResultSet rs = DataSourceWrapper.executeQuery(query);
            ArrayList p = new ArrayList();
            while (rs.next()) {
                String nextPattern = new String();
                nextPattern += rs.getString("pattern_id") + " ";
                nextPattern += rs.getString("pattern_vertices") + " ";
                nextPattern += rs.getString("pattern_edges") + " ";
                nextPattern += rs.getString("note");
                p.add(nextPattern);
            }
            patterns = (String[]) p.toArray(new String[0]);

        } catch (SQLException sqle) {
            out.println("sql exception!");
        }

        if (patterns != null) {
            try {
                Matcher m = new Matcher();
                String[] results = m.run(patterns, target);
                out.println("<p><table border=\"1\" align=\"center\">");
                out
                        .println("<thead><th>Pattern</th><th>Correspondance</th><th>Inserts</th><th>Group</th></thead>");
                out.println("<tbody align=\"center\">");
                for (int i = 0; i < results.length; i++) {
                    String[] bits = results[i].split("\t");
                    if (i % 2 == 0)
                        out.println("<tr class=\"evenrow\">");
                    else
                        out.println("<tr class=\"oddrow\">");
                    for (int j = 0; j < bits.length; j++) {
                        out.println("<td>" + bits[j] + "</td>");
                    }
                    out.println("</tr>");
                }
                out.println("</tbody></table></p>");

            } catch (StringIndexOutOfBoundsException sioob) {
                out.println("string index out of bounds!");
            } catch (TopsStringFormatException tsfe) {
                this.log(tsfe.toString());
            }
        }

        out.println("</body></html>");
    }
}
