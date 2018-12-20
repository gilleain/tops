package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.drg.Comparer;

public class TopsComparisonServlet extends HttpServlet {

	private static final String PAGESIZE_ATTR = "pagesize";

    private static final String TOPNUM_ATTR = "topnum";

    private static final String TARGET_SERVICE_ATTR = "targetService";

    private static final long serialVersionUID = -7416381583234400690L;

	private String compareStartPageURL;

    private String advancedStartPageURL;

    private String matchStartPageURL;

    private String servletURL;

    private String cartoonURL;

    private String diagramURL;

    private String cathNumberURL;

    private String cathNameURL;

    private String scopURL;

    @Override
    public void init() throws ServletException {
        this.compareStartPageURL = this.getInitParameter("compareStartPageURL");
        this.advancedStartPageURL = this.getInitParameter("advancedStartPageURL");
        this.matchStartPageURL = this.getInitParameter("matchStartPageURL");
        this.servletURL = this.getInitParameter("servletURL");
        this.cartoonURL = this.getInitParameter("cartoonURL");
        this.diagramURL = this.getInitParameter("diagramURL");
        this.cathNumberURL = this.getInitParameter("cathNumberURL");
        this.cathNameURL = this.getInitParameter("cathNameURL");
        this.scopURL = this.getInitParameter("scopURL");
    }

    public void displayIndex(int page, int startPage, int endPage,
            int totalPages, String name, PrintWriter out) {
        out.println("<p><table align=\"center\">");
        out.println("<colgroup width=\"15\"/><tr>");
        if (page > 5) {
            int prev = page - 5;
            cellWithLink(out, servletURL, "page=" + prev + "&name=" + name, "&lt;&lt;");
        }

        for (int j = startPage; j < endPage; j++) {
            out.println("<td>");
            if (j == page)
                out.println("<b>");
            cellWithLink(out, servletURL, "page=" + j + "&name=" + name, "&lt;&lt;");
            if (j == page)
                out.println("</b>");
            out.println("</td>");
        }

        if (page < (totalPages - 5)) {
            int next = page + 5;
            cellWithLink(out, servletURL, "page=" + next + "&name=" + name, "&gt;&gt;");
        }
        out.println("</tr></table></p>");
    }
    
    private void cellWithLink(PrintWriter out, String url, String queryParams, String cellContents) {
        out.println("<td><a href=\"" + url + "?" + queryParams + "\">" + cellContents + "</a></td>");
    }
    
    private void imageCell(PrintWriter out, String imgSrc) {
        out.println("<td><img src=\"" + imgSrc + "\"></img></td>");
    }

    public void displayPage(int page, int pageSize, List<Result> results,
            PrintWriter out, String targetName, String target,
            Map<Integer, List<String>> idToNameMap, String targetService, String classification) {
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
            out.println("<a href=\"" + compareStartPageURL + "\">Compare again?</a><p>");
            out.println("<p><b>Pairwise comparison of " + classification
                    + " with target: " + targetName + "</b>");
        } else if (targetService.equals("advanced-compare")) {
            out.println("<a href=\"" + advancedStartPageURL + "\">Advanced Compare again?</a><p>");
            out.println("<p><b>Pairwise comparison of " + classification
                    + " with target: " + targetName + "</b>");
        } else if (targetService.equals("advanced-match")) {
            out.println("<a href=\"" + advancedStartPageURL + "\">Advanced Match again?</a><p>");
            out.println("<p><b>" + results.size() + " Matches for pattern: "
                    + targetName + " to " + classification + "</b>");
            if (results.isEmpty()) {
            	// probably from pattern invention
                out.println("Sorry, no matches for this pattern!"); 
                return;
            }
        } else if (targetService.equals("insert-match")) {
            out.println("<a href=\"" + advancedStartPageURL + "\">Insert Match again?</a><p>");
            out.println("<p><b>" + results.size() + " Matches for pattern: "
                    + targetName + " to " + classification + "</b>");
            if (results.isEmpty()) {
            	// probably from pattern invention
                out.println("Sorry, no matches for this pattern!"); 
                return;
            }
        } else {
            out.println("<a href=\"" + matchStartPageURL + "\">Classic Match again?</a><p>");
            out.println("<p><b>" + results.size() + " Matches for pattern: "
                    + targetName + " to " + classification + "</b>");
            if (results.isEmpty()) {
            	// probably from pattern invention
                out.println("Sorry, no matches for this pattern!"); 
                return;
            }
        }

        String vertexString = "";
        String edgeString = "";
        String nameString = "";

        if (targetService.equals("insert-match")) {
            tops.engine.inserts.TParser parser = new tops.engine.inserts.TParser(target);
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
        String patternDiagramURL = "/" + vertexString + "/" + edgeString + "/" + nameString + ".gif";
        out.println("<img src=\"" + diagramURL + "/300/100/none" + patternDiagramURL + "\"/></p><hr>");

        if (pageSize > results.size())
            pageSize = results.size(); // no funny business!

        int startIndex = (page - 1) * pageSize; // page-1 because array index is
                                                // from 0!
        int endIndex = startIndex + pageSize;
        int totalPages = results.size() / pageSize;

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

        // run through the groups, printing out rows for each identical member of that group
        boolean evenrow = true; // a flag to enable altering the class of alternate group 'blocks'
        float compression = 0;

        String nameUrl = "";
        String numUrl = "";
        if (classification.equals("scop")) {
            nameUrl = scopURL; // scop treats names and numbers the same - as 'keys'
            numUrl = scopURL;
        } else {
            nameUrl = cathNameURL;
            numUrl = cathNumberURL;
        }
        String body = null;
        String tail = null;

        tops.engine.TParser resultParser = new tops.engine.TParser();

        for (int i = startIndex; i < endIndex; i++) {
            Result result = results.get(i);
            Integer id = new Integer(result.getID());
            List<String> nameList = idToNameMap.get(id);
            String data = result.getData();
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
                compression = result.getCompression();
            }

            for (int j = 0; j < nameList.size(); j++) {
                String nameAndClass = nameList.get(j);
                int tabindex = nameAndClass.indexOf('\t');
                String name = nameAndClass.substring(0, tabindex);
                String classif = nameAndClass.substring(tabindex + 1);
                if (evenrow) {
                    out.println("<tr class=\"evenrow\">");
                } else {
                    out.println("<tr class=\"oddrow\">");
                }
                if (comparisonStyle) {
                    String cartoonImageURL = cartoonURL + "/" + classification
                            + "/100x100/" + name + ".gif";
                    String diagramImageURL = diagramURL + "/200/100/none/"
                            + body + "/" + tail + "/" + name + ".gif";

                    imageCell(out, cartoonImageURL);
                    imageCell(out, diagramImageURL);
                    out.println("<td>" + compression + "</td>");
                } else {
                    String cartoonImageHighlightedURL = cartoonURL + "/"
                            + classification + "/100x100/" + matchString + "/"
                            + name + ".gif";

                    imageCell(out, cartoonImageHighlightedURL);
                }
                out.println("<td><a href=\"" + nameUrl + name + "\">" + name + "</a></td>");
                out.println("<td><a href=\"" + numUrl + classif + "\">" + classif + "</a></td>");
                out.println("</tr>");
            }
            evenrow = !evenrow;
        }
        out.println("</tbody></table></p>");
    }

    private void mapList(Map<Integer, List<String>> map, Integer key, String value) {
        List<String> list;

        if (map.containsKey(key)) {
            list = map.get(key);
            list.add(value); // ADD VALUE! :)
        } else {
            list = new ArrayList<>(); // make a new list, starting with the current key
            list.add(value); // look lively!
        }
        map.put(key, list);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doGet(request, response);
    }

    @SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pageS = request.getParameter("page");

        int page = 1;
        int pageSize = 10;

        HttpSession session = request.getSession();
        List<Result> results = (List<Result>) session.getAttribute("results");
        
        // this should have been passed in by the caller
        String newSubmission = (String) request.getAttribute("newSubmission"); 

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // all got /either/ from session /or/request.
        String topResultS;
        String pageSizeS;
        String target;
        String targetService;
        
        String[] subclasses; // parsed from a 'sub' string from input
        String targetName = "NONAME"; // get the target from request and parse to get head
        Map<Integer, List<String>> idToNameMap = null;

        if (newSubmission == null) {
            if (results == null) { // the results have been removed from the
                                    // cache
                out.println("RESULTS LOST!");
                return;
            }

            targetService = (String) session.getAttribute(TARGET_SERVICE_ATTR);
            topResultS = (String) session.getAttribute(TOPNUM_ATTR);
            pageSizeS = (String) session.getAttribute(PAGESIZE_ATTR);
            targetName = (String) session.getAttribute("targetName");
            target = (String) session.getAttribute("target");
            idToNameMap = (Map<Integer, List<String>>) session.getAttribute("idToNameMap");
            subclasses = (String[]) session.getAttribute("subclasses");

            try {
                if (pageS != null)
                    page = Integer.parseInt(pageS);
                if (pageSizeS != null)
                    pageSize = Integer.parseInt(pageSizeS);
    
                this.displayPage(page, pageSize, results, out, targetName, target,
                        idToNameMap, targetService, subclasses[0]);
            } catch (NumberFormatException n) {
                this.log("Number format exception " + n.getMessage());
            }

        } else {

            targetService = (String) request.getAttribute(TARGET_SERVICE_ATTR);
            topResultS = (String) request.getAttribute(TOPNUM_ATTR);
            pageSizeS = (String) request.getAttribute(PAGESIZE_ATTR);
            targetName = (String) request.getAttribute("targetName");
            target = (String) request.getAttribute("target");
            int firstSpace = target.indexOf(' ');
            targetName = target.substring(0, firstSpace);

            String sub = (String) request.getAttribute("sub"); // CATH, nreps, superfamily, etc
            subclasses = sub.split(","); // split the subclass list into bits

            try {
                if (pageS != null)
                    page = Integer.parseInt(pageS);
                if (pageSizeS != null)
                    pageSize = Integer.parseInt(pageSizeS);
            } catch (NumberFormatException n) {
                log("Number format exception for " + pageS + " or " + pageSizeS + " " + n.getMessage());
            }
            this.log("targetName = " + targetName + " target = " + target
                    + " pagesize = " + pageSizeS + " maxresults = "
                    + topResultS);

            // subclasses[0] should be the classification (CATH/SCOP)
            // subclasses[1] should be the largest group of the list or 'all'

            String queryA = "SELECT TOPS_nr.* FROM TOPS_instance_nr, TOPS_nr ";
            String queryB = "SELECT dom_id, gr, class FROM TOPS_instance_nr ";
            StringBuilder whereExpression = new StringBuilder("WHERE classification = '" + subclasses[0] + "'");

            if (!subclasses[1].equals("all")) {
                whereExpression.append(" AND subclass IN (");
                for (int k = 1; k < subclasses.length; k++) { // starts from 1!, because subclasses[0] is CATH/SCOP
                    whereExpression.append("'" + subclasses[k] + "'");
                    if (k < subclasses.length - 1)
                        whereExpression.append(" , ");
                }
                whereExpression.append(")");
            }

            queryB += whereExpression + ";";

            if (targetService.equals("match") || targetService.equals("advanced-match")) {
                tops.engine.TParser parser = new tops.engine.TParser(target);
                String vertexstring = parser.getVertexString();
                whereExpression.append(" AND length(vertex_string) >= " + vertexstring.length());
            } else if (targetService.equals("insert-match")) {
                tops.engine.inserts.TParser parser = new tops.engine.inserts.TParser(target);
                char[] vertices = parser.getVerticesWithInserts();
                whereExpression.append(" AND length(vertex_string) >= "  + vertices.length);
            }

            queryA += whereExpression + " AND gr = group_id GROUP BY group_id;";

            this.log("A = " + queryA + " B = " + queryB);

            List<String> instances = new ArrayList<>();

            try (
                Connection connection = DataSourceWrapper.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(queryA)) {
                while (rs.next()) {
                    StringBuilder nextInstance = new StringBuilder();
                    nextInstance.append(rs.getString("group_id") + " ");
                    nextInstance.append(rs.getString("vertex_string") + " ");
                    nextInstance.append(rs.getString("edge_string"));
                    instances.add(nextInstance.toString());
                }

                // now concatenate the names together into a csv list, put lists
                // into a map
                ResultSet rs2 = statement.executeQuery(queryB);
                idToNameMap = new HashMap<>();

                while (rs2.next()) {
                    Integer gr = rs2.getInt("gr");

                    String domId = rs2.getString("dom_id");
                    String cl = rs2.getString("class");
                    String data = domId + "\t" + cl;

                    this.mapList(idToNameMap, gr, data); // map the groupid to the domain name
                }
                rs2.close();
            } catch (SQLException squeel) {
                this.log("sql exception!", squeel);
            }

            if (instances != null) {
                try {
                    List<Result> tmp = null;
                    if (targetService.equals("compare") || targetService.equals("advanced-compare")) {
                        Comparer ex = new Comparer();
                        tmp = ex.compare(target, instances);
                    } else if (targetService.equals("insert-match")) {
                        tops.engine.inserts.Matcher m = new tops.engine.inserts.Matcher(instances);
                        tmp = m.runResults(new tops.engine.inserts.Pattern(target));
                    } else {
                        tops.engine.drg.Matcher m = new tops.engine.drg.Matcher(instances);
                        tmp = m.runResults(new tops.engine.drg.Pattern(target));
                    }

                    if (topResultS.equals("all")) {
                        results = tmp;
                    } else {
                        int max = Integer.parseInt(topResultS);
                        if (max > tmp.size()) {
                            max = tmp.size();
                        }
                        results = tmp.subList(0, max);
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

        session.setAttribute(TOPNUM_ATTR, topResultS);
        session.setAttribute(PAGESIZE_ATTR, pageSizeS);
        session.setAttribute("targetName", targetName);
        session.setAttribute("target", target);
        session.setAttribute("results", results);
        session.setAttribute("idToNameMap", idToNameMap);
        session.setAttribute(TARGET_SERVICE_ATTR, targetService);
        session.setAttribute("subclasses", subclasses);
        // session.setAttribute("idToClassMap", idToClassMap);

        out.println("</body></html>");
    }
}
