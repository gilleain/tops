package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClassicMatchServlet extends HttpServlet {

    private static final String servletName = "classicmatch"; // for the form
                                                                // action

    private static final String imageDir = "/tops/images/"; // where the pattern
                                                            // images are

    @SuppressWarnings("unchecked")
	@Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String generateForm = request.getParameter("generate");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        HashMap<String, String> pattern_map = new HashMap<String, String>();

        if (generateForm == null) {
            pattern_map = this.generateForm(out); // do a query to get the data
                                                // for the map
            session.setAttribute("pattern_map", pattern_map); // store the
                                                                // pattern data
                                                                // in the
                                                                // session
        } else {
            pattern_map = (HashMap<String, String>) session.getAttribute("pattern_map"); // get
                                                                            // the
                                                                            // pattern
                                                                            // definitions
            String pattern_id = request.getParameter("pattern_id");
            this.log("pattern id : " + pattern_id);
            String pattern = (String) pattern_map.get(pattern_id);

            request.setAttribute("targetService", "match");
            request.setAttribute("topnum", "all");
            request.setAttribute("pagesize", "100");
            request.setAttribute("target", pattern); // how much does this
                                                        // not make sense!
            request.setAttribute("newSubmission", "true");
            request.setAttribute("sub", request
                    .getParameter("subclasses"));

            String next = "/pattern/compare";
            RequestDispatcher dispatcher = request.getRequestDispatcher(next);
            dispatcher.forward(request, response);
        }
    }

    // this method gets the data from the database, prints out some of it and
    // returns the rest.
    public HashMap<String, String> generateForm(PrintWriter out) {
        String query = "SELECT pattern_id, pattern_vertices, pattern_edges, note, picfile FROM TOPS_classic_pattern";

        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> pics = new ArrayList<String>();
        HashMap<String, String> pattern_map = new HashMap<String, String>();

        try {
            Connection connection = DataSourceWrapper.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                String pattern_id = rs.getString("pattern_id");
                ids.add(pattern_id);
                String name = rs.getString("note");
                names.add(name);
                pics.add(rs.getString("picfile"));
                String pattern = name;
                pattern += " ";
                pattern += rs.getString("pattern_vertices");
                pattern += " ";
                pattern += rs.getString("pattern_edges");
                pattern_map.put(pattern_id, pattern);
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (SQLException squeel) {
            this.log("generateform! :", squeel);
        }

        out.println("<html>");
        out.println("<head>");
        out
                .println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/tops/classic.css\">");
        out
                .println("<script type=\"text/javascript\" src=\"/tops/classic.js\"></script>");
        out
                .println("<title>Classic Patterns</title></head><body onload=\"highlightFirst()\">");
        out
                .println("<h1 align=\"center\">Find Matches For Classic Protein Structure Patterns</h1><p>");
        out.println("<table><tr>");

        // print out the pictures

        int images_per_cell = 2;
        for (int j = 0; j < pics.size(); j++) {
            if (j % images_per_cell == 0)
                out.println("<td>");
            String filename = (String) pics.get(j);
            filename += "_thumb.png"; // start by printing the thumbnails
            String imgid = "img" + j;
            out.println("<img id=\"" + imgid + "\" src=\"" + ClassicMatchServlet.imageDir
                    + filename + "\" ");
            out.println("onclick=\"selectName(" + j + ", this.id)\">");
            if (j % images_per_cell == (images_per_cell - 1))
                out.println("</td>");
        }
        out.println("<td><form id=\"theForm\" action=\"" + ClassicMatchServlet.servletName
                + "\" method=\"POST\">");

        // print out the patterns
        int numberOfPatterns = names.size();
        out.println("<select name=\"pattern_id\" size=\"" + numberOfPatterns
                + "\">");
        for (int i = 0; i < numberOfPatterns; i++) {
            String optid = "opt" + i;
            String imgtarget = "img" + i;
            out.println("<option id=\"" + optid + "\" value=\"" + ids.get(i)
                    + "\" ");
            out.println("onclick=\"selectImage(\'" + imgtarget + "\')\"");
            if (i == 0)
                out.println("selected>");
            else
                out.println(">");
            out.println(names.get(i) + "</option>");
        }
        out.println("</select>");
        out
                .println("<input type=\"hidden\" name=\"generate\" value=\"false\">");
        out.println("</td></tr><tr>");

        out.println("<td>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"cath,crep,arep,trep\" checked>CATH treps</input><br/>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"cath,crep,arep,trep,hrep\">CATH hreps</input><br/>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"cath,crep,arep,trep,hrep,nrep\">CATH nreps</input><br/>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"cath,all\">CATH all</input><br/>");
        out.println("</td>");
        out.println("<td>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"scop,class,fold\">SCOP folds</input><br/>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"scop,class,fold,superfamily\">SCOP superfamilies</input><br/>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"scop,class,fold,superfamily,family\">SCOP families</input><br/>");
        out
                .println("<input type=\"radio\" name=\"subclasses\" value=\"scop,all\">SCOP all</input><br/>");
        out.println("</td>");
        out.println("<td><input type=\"submit\" value=\"match\"></td>");
        out.println("</form></tr>");
        out.println("</table>"); // finish outer table
        out.println("</body></html>");

        return pattern_map;
    }
}
