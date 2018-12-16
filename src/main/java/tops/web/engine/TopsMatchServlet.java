package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.data.datasource.ArrayGraphDatasource;
import tops.data.datasource.GraphSetDatasource;
import tops.engine.TopsStringFormatException;
import tops.engine.drg.Matcher;

public class TopsMatchServlet extends HttpServlet {

	private static final long serialVersionUID = 4954322689423567290L;
	
	private GraphSetDatasource patternSource;
	
	public void init() {
		String patternSourceType = getInitParameter("patternSourceType");
		if (patternSourceType.equals("DB")) {
			// TODO
		} else if (patternSourceType.equals("MEMORY")){
			Map<String, String[]> patterns = new HashMap<>();
			patterns.put("PATTERNS", new String[] {
					"greek_key NEeEeEC 1:4A2:3A2:5A3:4A",
			});
			patternSource = new ArrayGraphDatasource(patterns);
		}
	}

	@Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestPath = request.getContextPath();
        String target = request.getParameter("target");

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

        out.println("<p><b>Patterns that match Target : " + target + "</b></p>");
        out.println("<p>OR : compare to all in db :");
        out.println("<form action=\"" + requestPath + "/pattern/compare?target=" + target + "\" method=\"POST\">");
        out.println("<input type=\"submit\" name=\"compare\"></form>");

        List<String> patterns = patternSource.getGraphSet("PATTERNS");

        // TODO : move to db graph source...
//        String query = "SELECT pattern_id, pattern_vertices, pattern_edges, note  FROM TOPS_pattern ORDER BY compression";

//        try {
//            ResultSet rs = DataSourceWrapper.executeQuery(query);
//            List<String> p = new ArrayList<String>();
//            while (rs.next()) {
//                String nextPattern = new String();
//                nextPattern += rs.getString("pattern_id") + " ";
//                nextPattern += rs.getString("pattern_vertices") + " ";
//                nextPattern += rs.getString("pattern_edges") + " ";
//                nextPattern += rs.getString("note");
//                p.add(nextPattern);
//            }
//            patterns = (String[]) p.toArray(new String[0]);
//
//        } catch (SQLException sqle) {
//            out.println("sql exception!");
//        }
        // TODO : /move to db graph source

        if (patterns.isEmpty()) {
            try {
                Matcher m = new Matcher();
                List<String> results = m.run(patterns, target);
                out.println("<p><table border=\"1\" align=\"center\">");
                out.println("<thead><th>Pattern</th><th>Correspondance</th><th>Inserts</th><th>Group</th></thead>");
                out.println("<tbody align=\"center\">");
                for (int i = 0; i < results.size(); i++) {
                    String[] bits = results.get(i).split("\t");
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
