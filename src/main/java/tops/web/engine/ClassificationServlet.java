package tops.web.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tops.engine.TParser;
import tops.engine.TopsStringFormatException;
import tops.engine.drg.Comparer;
import tops.engine.drg.Pattern;
import tops.engine.drg.Utilities;
import tops.model.classification.CATHLevel;
import tops.model.classification.SCOPLevel;

/**
 * Browser for the strings database, accepting a classification "stub" and
 * displaying reps for that group as linear diagrams and cartoons.
 * 
 */

public class ClassificationServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5144597700148212412L;

	private static final String cartoonURL = "/tops/view";

    private static final String diagramURL = "/tops/diagram";

    private static final String classificationURL = "http://localhost:8080/tops/classification.html";

    private static final String cathNameURL = "http://www.biochem.ucl.ac.uk/tops.dw.cgi-bin/cath/SearchPdb.pl?type=PDB&query=";

//    private static final String scopNameURL = "http://www.biochem.ucl.ac.uk/tops.dw.cgi-bin/cath/SearchPdb.pl?type=PDB&query="; // FIXME

    private static final String cartoonSize = "100x100";

    public HashMap<String, String> getInstances(String classificationSchemeName,
            String classificationStub, int repLevel) {

        String query = "SELECT dom_id, vertex_string, edge_string, class FROM TOPS_instance_nr JOIN TOPS_nr ";
        query += "WHERE classification = \"" + classificationSchemeName
                + "\" AND gr = group_id AND ";

        /**
         * Here is the subtle part - we ask for rows with a subclass value LESS
         * THAN OR EQUAL to the desired level The reason for this is that we
         * only store the /highest/ (closest to the root) level at which a
         * domain is a rep So, if it is an Architecture rep, it is ALSO a
         * Topology, Homology, etc rep.
         */
        query += "subclass <= " + repLevel + " and class like";

        // check to see if there is a dot at the end!
        if (classificationStub.charAt(classificationStub.length() - 1) == '.') {
            classificationStub += "%";
        } else {
            classificationStub += ".%";
        }

        try {
            Connection connection = DataSourceWrapper.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query + " '"
                    + classificationStub + ".%' ORDER BY class;");
            HashMap<String, String> in = new HashMap<String, String>();
            while (rs.next()) {
                String nextInstance = new String();
                nextInstance += rs.getString("dom_id") + " ";
                nextInstance += rs.getString("vertex_string") + " ";
                nextInstance += rs.getString("edge_string") + " ";
                String classification = rs.getString("class");
                in.put(classification, nextInstance);
            }
            connection.close();
            statement.close();
            return in;
        } catch (SQLException squeel) {
            this.log("getInstances! :", squeel);
            return null;
        }
    }

    private void warning(String problem, HttpServletResponse response) {
        response.setContentType("text/html");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IOException ioe) {
        }

        pout.println("<html><head><title>Warning</title></head>");
        pout.println("<body>Something is wrong : <p>" + problem);
        pout.println("</body></html>");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        String from = request.getContextPath();
//        ServletContext context = getServletContext();
        String classificationStub = request.getParameter("code");
        String classificationSchemeName = request
                .getParameter("scheme");

        int levelIndex;
        int subLevelIndex;

        String levelName;
        String subLevelName;

        // depending on how long the stub we are given, set the level index
        if (classificationStub == null) {
            levelIndex = 0;
        } else {

            // remove any dots at the end, for consistency
            int endPos = classificationStub.length() - 1;
            if (classificationStub.charAt(endPos) == '.') {
                classificationStub = classificationStub.substring(0, endPos);
            }

            // now, determine the level
            String[] bits = classificationStub.split("\\.");
            levelIndex = bits.length - 1;
        }

        // not surprisingly, the next level is one down
        subLevelIndex = levelIndex + 1;

        // now get the names of the levels we are looking at ... if we can
        try {
            if (classificationSchemeName.equals("CATH")) {
                levelName = CATHLevel.fullNames[levelIndex];
                subLevelName = CATHLevel.fullNames[subLevelIndex];
            } else if (classificationSchemeName.equals("SCOP")) {
                levelName = SCOPLevel.fullNames[levelIndex];
                subLevelName = SCOPLevel.fullNames[subLevelIndex];
            } else {
                this.warning("Unknown classification scheme : " + classificationSchemeName, response);
                return;
            }
        } catch (ArrayIndexOutOfBoundsException a) {
            this.warning("No more levels in " + classificationSchemeName, response);
            return;
        }

        // get the TOPSStrings the database, along with their classifications as
        // the keys to a map
        HashMap<String, String> instances = 
        		this.getInstances(classificationSchemeName, classificationStub, subLevelIndex);
        if (instances == null || instances.size() == 0) {
            this.warning("no reps for this level!", response);
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><meta http-equiv=\"Pragma\" content=\"no-cache\">");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"tops.css\">");
        out.println("<title>" + levelName + " Group Pattern: "
                + classificationStub + "</title></head><body>");
        out.println("<a href=\"" + ClassificationServlet.classificationURL + "\">Another level?</a>");

        // calculate a common pattern
        Comparer ex = new Comparer();
        Pattern result = null;
        float compression = 1.0f;
        try {
            List<String> instanceValues = new ArrayList<String>(instances.values());
            result = ex.findPattern(instanceValues);
            result.rename(classificationStub);
            compression = 1 - Utilities.doCompression(instanceValues, result);
        } catch (TopsStringFormatException tsfe) {
            this.log(tsfe.toString());
            return;
        } catch (NullPointerException npe) {
            this.log("null pointer in classification servlet!");
            return;
        }

        String patternDiagramURL = result.getWholeVertexString() + "/"
                + result.getEdgeString() + "/" + result.getName();

        // display the patterns generated
        out.println("<h1>Pattern for CATH " + levelName + " Group "
                + classificationStub + " with " + instances.size() + " "
                + subLevelName + " sublevels</h1>");
        out.println("<img align=\"center\" src=\"" + ClassificationServlet.diagramURL
                + "/200/100/none/" + patternDiagramURL + ".gif\"/>");
        out.println("<p>Compression = " + compression + "</p>");
        out.println("<hr>");

        // now, start on the actual representatives of the subgroups
        out.println("<p><b>Diagrams of the representative examples for each sublevel of this group : </b></p><table class=\"results\">");

        Iterator<String> itr = instances.keySet().iterator();
        TParser parser = new TParser();

        while (itr.hasNext()) {
            // lookup the string by its classification
            String classification = (String) itr.next();
            String diagram = (String) instances.get(classification);

            // split it into bits again...
            parser.load(diagram);
            String name = parser.getName();
            patternDiagramURL = parser.getVertexString() + "/"
                    + parser.getEdgeString() + "/" + parser.getName();

            // trim the classification to make a new stub
            String[] bits = classification.split("\\.");
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < subLevelIndex; i++) {
                stringBuffer.append(bits[i]).append(".");
            }
            stringBuffer.append(bits[subLevelIndex]);
            String newClassificationStub = stringBuffer.toString();

            // provide a link to the cath or scop page and to the level below
            // and an pair of images
            out.println("<tr>");
            out.println("<td><a href=\"" + ClassificationServlet.cathNameURL + name + "\">" + name
                    + "</a></td>");
            out.println("<td><a href=\"class?scheme="
                    + classificationSchemeName + "&code="
                    + newClassificationStub + "\">" + newClassificationStub
                    + "</a></td>");
            out.println("<td><img align=\"center\" src=\"" + ClassificationServlet.diagramURL
                    + "/200/100/none/" + patternDiagramURL + ".gif\"/></td>");
            out.println("<td><img align=\"center\" src=\"" + ClassificationServlet.cartoonURL + "/"
                    + classificationSchemeName.toLowerCase() + "/"
                    + ClassificationServlet.cartoonSize + "/all/" + name + ".gif\"/></a></td>");
            out.println("</tr>");
        }

        // finish up
        out.println("</table>");
        out.println("</body></html>");
    }

}
