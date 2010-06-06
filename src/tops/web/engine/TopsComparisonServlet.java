package tops.web.engine;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.HashMap;

import tops.engine.Result;
//import tops.engine.TParser;
import tops.engine.TopsStringFormatException;

import tops.engine.drg.Comparer;

//import tops.engine.inserts.Matcher;
//import tops.engine.inserts.Pattern;
//import tops.engine.inserts.TParser;

public class TopsComparisonServlet extends javax.servlet.http.HttpServlet {

    private static String compareStartPageURL;

    private static String advancedStartPageURL;

    private static String matchStartPageURL;

    private static String servletURL;

    private static String cartoonURL;

    private static String diagramURL;

    private static String cathNumberURL;

    private static String cathNameURL;

    private static String scopURL;

    public void init() throws ServletException {
        TopsComparisonServlet.compareStartPageURL = this.getInitParameter("compareStartPageURL");
        TopsComparisonServlet.advancedStartPageURL = this.getInitParameter("advancedStartPageURL");
        TopsComparisonServlet.matchStartPageURL = this.getInitParameter("matchStartPageURL");
        TopsComparisonServlet.servletURL = this.getInitParameter("servletURL");
        TopsComparisonServlet.cartoonURL = this.getInitParameter("cartoonURL");
        TopsComparisonServlet.diagramURL = this.getInitParameter("diagramURL");
        TopsComparisonServlet.cathNumberURL = this.getInitParameter("cathNumberURL");
        TopsComparisonServlet.cathNameURL = this.getInitParameter("cathNameURL");
        TopsComparisonServlet.scopURL = this.getInitParameter("scopURL");
    }

    public void displayIndex(int page, int startPage, int endPage,
            int totalPages, String name, PrintWriter out) {
        out.println("<p><table align=\"center\">");
        out.println("<colgroup width=\"15\"/><tr>");
        if (page > 5) {
            int prev = page - 5;
            out.println("<td><a href=\"" + TopsComparisonServlet.servletURL + "?page=" + prev
                    + "&name=" + name + "\">&lt;&lt;</a></td>");
        }

        for (int j = startPage; j < endPage; j++) {
            out.println("<td>");
            if (j == page)
                out.println("<b>");
            out.println("<a href=\"" + TopsComparisonServlet.servletURL + "?page=" + j + "&name="
                    + name + "\">" + j + "</a>");
            if (j == page)
                out.println("</b>");
            out.println("</td>");
        }

        if (page < (totalPages - 5)) {
            int next = page + 5;
            out.println("<td><a href=\"" + TopsComparisonServlet.servletURL + "?page=" + next
                    + "&name=" + name + "\">&gt;&gt;</a></td>");
        }
        out.println("</tr></table></p>");
    }

    public void displayPage(int page, int pageSize, Result[] results,
            PrintWriter out, String targetName, String target,
            HashMap idToNameMap, String targetService, String classification) {
        out
                .println("<html><head><meta http-equiv=\"Pragma\" content=\"no-cache\">");
        if (targetService.equals("compare")
                || targetService.equals("advanced-compare")) {
            out.println("<title>Comparison Results. Page : " + page
                    + "</title>");
        } else {
            out.println("<title>Match Results. Page : " + page + "</title>");
        }
        out.println("<style type=\"text/css\">");
        out.println("<!--");
        out.println(".oddrow  { background-color: #cccc99; }");
        out.println(".evenrow { background-color: #fffafa; }");
        out.println("-->");
        out.println("</style></head><body>");

        if (targetService.equals("compare")) {
            out.println("<a href=\"" + TopsComparisonServlet.compareStartPageURL
                    + "\">Compare again?</a><p>");
            out.println("<p><b>Pairwise comparison of " + classification
                    + " with target: " + targetName + "</b>");
        } else if (targetService.equals("advanced-compare")) {
            out.println("<a href=\"" + TopsComparisonServlet.advancedStartPageURL
                    + "\">Advanced Compare again?</a><p>");
            out.println("<p><b>Pairwise comparison of " + classification
                    + " with target: " + targetName + "</b>");
        } else if (targetService.equals("advanced-match")) {
            out.println("<a href=\"" + TopsComparisonServlet.advancedStartPageURL
                    + "\">Advanced Match again?</a><p>");
            out.println("<p><b>" + results.length + " Matches for pattern: "
                    + targetName + " to " + classification + "</b>");
            if (results.length == 0) {
                out.println("Sorry, no matches for this pattern!"); // probably
                                                                    // from
                                                                    // pattern
                                                                    // invention
                return;
            }
        } else if (targetService.equals("insert-match")) {
            out.println("<a href=\"" + TopsComparisonServlet.advancedStartPageURL
                    + "\">Insert Match again?</a><p>");
            out.println("<p><b>" + results.length + " Matches for pattern: "
                    + targetName + " to " + classification + "</b>");
            if (results.length == 0) {
                out.println("Sorry, no matches for this pattern!"); // probably
                                                                    // from
                                                                    // pattern
                                                                    // invention
                return;
            }
        } else {
            out.println("<a href=\"" + TopsComparisonServlet.matchStartPageURL
                    + "\">Classic Match again?</a><p>");
            out.println("<p><b>" + results.length + " Matches for pattern: "
                    + targetName + " to " + classification + "</b>");
            if (results.length == 0) {
                out.println("Sorry, no matches for this pattern!"); // probably
                                                                    // from
                                                                    // pattern
                                                                    // invention
                return;
            }
        }

        String vertexString = "";
        String edgeString = "";
        String nameString = "";

        if (targetService.equals("insert-match")) {
            tops.engine.inserts.TParser parser = new tops.engine.inserts.TParser(
                    target);
            vertexString = new String(parser.getVerticesWithInserts());
            edgeString = parser.getEdgeString();
            nameString = parser.getName();
        } else {
            tops.engine.TParser parser = new tops.engine.TParser(target);
            vertexString = parser.getVertexString();
            edgeString = parser.getEdgeString();
            nameString = parser.getName();
        }

        if (edgeString.equals("")) {
            edgeString = "none";
        }
        String patternDiagramURL = "/" + vertexString + "/" + edgeString + "/"
                + nameString + ".gif";
        out.println("<img src=\"" + TopsComparisonServlet.diagramURL + "/300/100/none"
                + patternDiagramURL + "\"/></p><hr>");

        if (pageSize > results.length)
            pageSize = results.length; // no funny business!

        int startIndex = (page - 1) * pageSize; // page-1 becuase array index is
                                                // from 0!
        int endIndex = startIndex + pageSize;
        int totalPages = Math.round(results.length / pageSize);

        int startPage = (page > 5) ? page - 5 : 1;
        int endPage = (page < (totalPages - 5)) ? page + 5 : totalPages;

        this.displayIndex(page, startPage, endPage, totalPages, targetName, out);

        boolean comparisonStyle = false; // only print out the compression
                                            // for comparisons!
        if (targetService.equals("compare")
                || targetService.equals("advanced-compare")) {
            comparisonStyle = true;
        }

        out.println("<p><table border=\"1\" align=\"center\">");
        if (comparisonStyle) {
            out.println("<thead>");
            out.println("<th>Cartoon</th>");
            out.println("<th>Diagram</th>");
            out.println("<th>Compression</th>");
        } else {
            out.println("<thead><th>Image</th>");
        }
        out.println("<th>Name</th>");

        out.println("<th>Classification</th></thead>");
        out.println("<tbody>");

        // run through the groups, printing out rows for each identical member
        // of that group
        boolean evenrow = true; // a flag to enable altering the class of
                                // alternate group 'blocks'
        float compression = 0;

        String nameUrl = new String();
        String numUrl = new String();
        if (classification.equals("scop")) {
            nameUrl = TopsComparisonServlet.scopURL; // scop treats names and numbers the same - as
                                // 'keys'
            numUrl = TopsComparisonServlet.scopURL;
        } else {
            nameUrl = TopsComparisonServlet.cathNameURL;
            numUrl = TopsComparisonServlet.cathNumberURL;
        }
        String body = null;
        String tail = null;

        tops.engine.TParser resultParser = new tops.engine.TParser();

        for (int i = startIndex; i < endIndex; i++) {
            Integer id = new Integer(results[i].getID());
            ArrayList nameList = (ArrayList) idToNameMap.get(id);
            String data = results[i].getData();
//            String insertString = null; // TODO sort this all out!
            String matchString = null;

            if (!comparisonStyle) {
                int tab = data.indexOf('\t');
//                insertString = data.substring(0, tab);
                matchString = data.substring(tab + 1);
            }

            if (comparisonStyle) {
                resultParser.load(data);
                body = resultParser.getVertexString();
                tail = resultParser.getEdgeString();
                if (tail.equals(""))
                    tail = "none";
                compression = results[i].getCompression();
                // compression = 1 - compression;
            }

            for (int j = 0; j < nameList.size(); j++) {
                String nameAndClass = (String) nameList.get(j);
                int tabindex = nameAndClass.indexOf('\t');
                String name = nameAndClass.substring(0, tabindex);
                String classif = nameAndClass.substring(tabindex + 1);
                if (evenrow) {
                    out.println("<tr class=\"evenrow\">");
                } else {
                    out.println("<tr class=\"oddrow\">");
                }
                if (comparisonStyle) {
                    String cartoonImageURL = TopsComparisonServlet.cartoonURL + "/" + classification
                            + "/100x100/" + name + ".gif";
                    String diagramImageURL = TopsComparisonServlet.diagramURL + "/200/100/none/"
                            + body + "/" + tail + "/" + name + ".gif";
                    // String diagramImageURL = diagramURL + "?data=" + data +
                    // "&width=100&height=50";

                    out.println("<td><img src=\"" + cartoonImageURL
                            + "\"></img></td>");
                    out.println("<td><img src=\"" + diagramImageURL
                            + "\"></img></td>");
                    out.println("<td>" + compression + "</td>");
                } else {
                    String cartoonImageHighlightedURL = TopsComparisonServlet.cartoonURL + "/"
                            + classification + "/100x100/" + matchString + "/"
                            + name + ".gif";
                    // String diagramImageHighlightedURL = diagramURL + "?data="
                    // + data + "&matches=" + matchString +
                    // "&width=100&height=50";

                    out.println("<td><img src=\"" + cartoonImageHighlightedURL
                            + "\"></img></td>");
                    // out.println("<td><img src=\"" +
                    // diagramImageHighlightedURL + "\"></img></td>");
                }
                out.println("<td><a href=\"" + nameUrl + name + "\">" + name
                        + "</a></td>");
                out.println("<td><a href=\"" + numUrl + classif + "\">"
                        + classif + "</a></td>");
                out.println("</tr>");
            }
            evenrow = !evenrow;
        }
        out.println("</tbody></table></p>");
    }

    private void mapList(HashMap map, Integer key, String value) {
        ArrayList list;

        if (map.containsKey(key)) {
            list = (ArrayList) map.get(key);
            list.add(value); // ADD VALUE! :)
        } else {
            list = new ArrayList(); // make a new list, starting with the
                                    // current key
            list.add(value); // look lively!
        }
        map.put(key, list);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pageS = request.getParameter("page");

        int page = 1;
        int pageSize = 10;

        HttpSession session = request.getSession();
        Result[] results = (Result[]) session.getAttribute("results");
        String newSubmission = (String) request.getAttribute("newSubmission"); // this
                                                                                // should
                                                                                // have
                                                                                // been
                                                                                // passed
                                                                                // in
                                                                                // by
                                                                                // the
                                                                                // caller

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String topResultS, pageSizeS, target, targetService; // all got
                                                                // /either/ from
                                                                // session /or/
                                                                // request.
        String[] subclasses; // parsed from a 'sub' string from input
        String targetName = "NONAME"; // get the target from request and parse
                                        // to get head
        HashMap idToNameMap = null;

        if (newSubmission == null) {
            if (results == null) { // the results have been removed from the
                                    // cache
                out.println("RESULTS LOST!");
                return;
            }

            targetService = (String) session.getAttribute("targetService");
            topResultS = (String) session.getAttribute("topnum");
            pageSizeS = (String) session.getAttribute("pagesize");
            targetName = (String) session.getAttribute("targetName");
            target = (String) session.getAttribute("target");
            idToNameMap = (HashMap) session.getAttribute("idToNameMap");
            subclasses = (String[]) session.getAttribute("subclasses");

            if (pageS != null)
                page = Integer.parseInt(pageS);
            if (pageSizeS != null)
                pageSize = Integer.parseInt(pageSizeS);

            this.displayPage(page, pageSize, results, out, targetName, target,
                    idToNameMap, targetService, subclasses[0]);

        } else {

            targetService = (String) request.getAttribute("targetService");
            topResultS = (String) request.getAttribute("topnum");
            pageSizeS = (String) request.getAttribute("pagesize");
            targetName = (String) request.getAttribute("targetName");
            target = (String) request.getAttribute("target");
            int firstSpace = target.indexOf(" ");
            targetName = target.substring(0, firstSpace);

            String sub = (String) request.getAttribute("sub"); // CATH, nreps,
                                                                // superfamily,
                                                                // etc
            subclasses = sub.split(","); // split the subclass list into bits

            if (pageS != null)
                page = Integer.parseInt(pageS);
            if (pageSizeS != null)
                pageSize = Integer.parseInt(pageSizeS);
            this.log("targetName = " + targetName + " target = " + target
                    + " pagesize = " + pageSizeS + " maxresults = "
                    + topResultS);

            // subclasses[0] should be the classification (CATH/SCOP)
            // subclasses[1] should be the largest group of the list or 'all'

            String query_a = "SELECT TOPS_nr.* FROM TOPS_instance_nr, TOPS_nr ";
            String query_b = "SELECT dom_id, gr, class FROM TOPS_instance_nr ";
            String where_expression = "WHERE classification = '"
                    + subclasses[0] + "'";

            if (!subclasses[1].equals("all")) {
                where_expression += " AND subclass IN (";
                for (int k = 1; k < subclasses.length; k++) { // starts from
                                                                // 1!, becuase
                                                                // subclasses[0]
                                                                // is CATH/SCOP
                    where_expression += "'" + subclasses[k] + "'";
                    if (k < subclasses.length - 1)
                        where_expression += " , ";
                }
                where_expression += ")";
            }

            query_b += where_expression + ";";

            if (targetService.equals("match")
                    || targetService.equals("advanced-match")) {
                tops.engine.TParser parser = new tops.engine.TParser(target);
                String vertexstring = parser.getVertexString();
                where_expression += " AND length(vertex_string) >= "
                        + vertexstring.length();
            } else if (targetService.equals("insert-match")) {
                tops.engine.inserts.TParser parser = new tops.engine.inserts.TParser(
                        target);
                char[] vertices = parser.getVerticesWithInserts();
                where_expression += " AND length(vertex_string) >= "
                        + vertices.length;
            }

            query_a += where_expression
                    + " AND gr = group_id GROUP BY group_id;";

            this.log("A = " + query_a + " B = " + query_b);

            String[] instances = null;

            try {
                Connection connection = DataSourceWrapper.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(query_a);
                ArrayList in = new ArrayList();
                while (rs.next()) {
                    String nextInstance = new String();
                    nextInstance += rs.getString("group_id") + " ";
                    nextInstance += rs.getString("vertex_string") + " ";
                    nextInstance += rs.getString("edge_string");
                    // log(nextInstance); !!
                    in.add(nextInstance);
                }
                rs.close();
                instances = (String[]) in.toArray(new String[0]);

                // now concatenate the names together into a csv list, put lists
                // into a map
                ResultSet rs2 = statement.executeQuery(query_b);
                idToNameMap = new HashMap();
                // idToClassMap = new HashMap();

                while (rs2.next()) {
                    Integer gr = new Integer(rs2.getInt("gr"));

                    String dom_id = rs2.getString("dom_id");
                    String cl = rs2.getString("class");
                    String data = dom_id + "\t" + cl;

                    this.mapList(idToNameMap, gr, data); // map the groupid to the
                                                    // domain name
                    // mapList(idToClassMap, gr, cl); //map the groupid to the
                    // class //ERRR...just use the one map
                }
                rs2.close();
                statement.close();
                connection.close();
            } catch (SQLException squeel) {
                this.log("sql exception!", squeel);
            }

            if (instances != null) {
                try {
                    Result[] tmp = null;
                    if (targetService.equals("compare")
                            || targetService.equals("advanced-compare")) {
                        Comparer ex = new Comparer();
                        tmp = ex.compare(target, instances);
                    } else if (targetService.equals("insert-match")) {
                        tops.engine.inserts.Matcher m = new tops.engine.inserts.Matcher(
                                instances);
                        tmp = m.runResults(new tops.engine.inserts.Pattern(target));
                    } else {
                        tops.engine.drg.Matcher m = new tops.engine.drg.Matcher(
                                instances);
                        tmp = m.runResults(new tops.engine.drg.Pattern(target));
                    }

                    if (topResultS.equals("all")) {
                        results = tmp;
                    } else {
                        int max = Integer.parseInt(topResultS);
                        if (max > tmp.length)
                            max = tmp.length;
                        results = new Result[max];
                        System.arraycopy(tmp, 0, results, 0, max);
                    }

                    this.displayPage(1, pageSize, results, out, targetName, target,
                            idToNameMap, targetService, subclasses[0]);

                } catch (TopsStringFormatException tsfe) {
                    this.log("execution error : " + tsfe.getMessage()
                            + " in comparisonservlet");
                } catch (Exception e) {
                    this.log("execution error : ", e);
                }
            }
        }

        session.setAttribute("topnum", topResultS);
        session.setAttribute("pagesize", pageSizeS);
        session.setAttribute("targetName", targetName);
        session.setAttribute("target", target);
        session.setAttribute("results", results);
        session.setAttribute("idToNameMap", idToNameMap);
        session.setAttribute("targetService", targetService);
        session.setAttribute("subclasses", subclasses);
        // session.setAttribute("idToClassMap", idToClassMap);

        out.println("</body></html>");
    }
}
