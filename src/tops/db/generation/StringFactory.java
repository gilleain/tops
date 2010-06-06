package tops.db.generation;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import tops.beans.Chain;
import tops.beans.Chiral_Connection;
import tops.beans.HPP_Class;
import tops.beans.Helix_Packing_Pair;
import tops.beans.Hydrogen_Bond;
import tops.beans.SSE_DOM;
import tops.beans.Secondary_Structure_Element;

import tops.engine.Edge;
import tops.engine.Vertex;
import tops.engine.PlainFormatter;

/**
 * Creates string format TOPS graphs from a database query.
 * 
 * @author maclean
 */
public class StringFactory extends TopsFactory {

    private static Logger logger = Logger.getLogger("tops.db.StringFactory");

    /**
     * No-argument, null constructor.
     */
    public StringFactory() {
    }

    /**
     * Look up a chiral partner in the Map for a vertex.
     * 
     * @param left
     *            the index of the vertex we are searhing for
     * @param vertices
     *            a TreeMap of vertices and types
     * @return the index of the chiral partner
     */
    public int findOtherEnd(int left, TreeMap vertices) {
        String type = (String) vertices.get(new Integer(left));
        SortedMap range = vertices.subMap(new Integer(left + 1), new Integer(
                left + 10));
        Set keys = range.keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            Integer i = (Integer) itr.next();
            String otherType = (String) range.get(i);
            if (type.equals(otherType)) {
                return i.intValue();
            }
        }
        return -1;
    }

    /**
     * Renumber vertices from chain to domain number. This is necessary because
     * the database stores SSEs as chains, with a domain mapping.
     * 
     * @param numMap
     *            filled with mappings of numbers
     * @param domMap
     *            passed to getDomain to get a domain id
     * @param number
     *            the index of the vertex to map
     * @param id
     *            a domain id
     */
    public void mapNumbers(TreeMap numMap, HashMap domMap, int number, String id) {
        if (numMap.isEmpty()) {
            numMap.put(new Integer(number), new Integer(1));
        } else {
            Integer lastEndNumber = (Integer) numMap.lastKey();
            String lastId = this.getDomain(lastEndNumber, domMap);
            if (id.equals(lastId)) {
                // if the domains are the same
                Integer mappedLast = (Integer) numMap.get(lastEndNumber);
                int lastVal = mappedLast.intValue();
                int mappedNew = lastVal + 1;
                numMap.put(new Integer(number), new Integer(mappedNew));
            } else {
                // crossing domain boundary
                lastEndNumber = this.getLastEndNumber(id, number, domMap);
                if (lastEndNumber.equals(new Integer(-1))) {
                    // first SSE in domain
                    numMap.put(new Integer(number), new Integer(1));
                } else {
                    Integer mappedLast = (Integer) numMap.get(lastEndNumber);
                    int mappedNew = mappedLast.intValue() + 1;
                    numMap.put(new Integer(number), new Integer(mappedNew));
                }
            }
        }
    }

    /**
     * Lookup the type of a vertex in a list of SSEs
     * 
     * @param num
     *            the index of the sse
     * @param list
     *            a list of Secondary_Structure_Elements
     * @return the String type of the SSE
     */
    public String lookupType(int num, ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            Secondary_Structure_Element s = (Secondary_Structure_Element) list
                    .get(i);
            int n = s.getSSE_No();
            if (num == n) {
                return s.getType();
            }
        }
        return "U";
    }

    /**
     * Lookup the domain String using an Integer vertex index
     * 
     * @param number
     *            the vertex index
     * @param map
     *            an Integer-String map
     * @return the domain String
     */
    public String getDomain(Integer number, HashMap map) {
        // System.err.println("getting domain for number : " + number);
        Set keys = map.keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            String dom = (String) itr.next();
            TreeMap vertices = (TreeMap) map.get(dom);
            if (vertices.containsKey(number)) {
                return dom;
            }
        }
        return null;
        // no domain found!
    }

    /**
     * Find the first number in the domain Map that has this domain id AND is
     * less than num
     * 
     * @param domid
     *            domain id String
     * @param num
     *            vertex index
     * @param map
     *            Integer-String map
     * @return the vertex index
     */
    public Integer getLastEndNumber(String domid, int num, HashMap map) {
        TreeMap vertices = (TreeMap) map.get(domid);
        SortedMap heads = vertices.headMap(new Integer(num));
        if (heads.isEmpty()) {
            return new Integer(-1);
        }
        // nothing smaller than num!
        else {
            return (Integer) heads.lastKey();
        }
    }

    /**
     * Convert the class (number from 0-35) into a relative orientation A or P.
     * 
     * @param classid
     *            Description of the Parameter
     * @param types
     *            list of HPP_Class objects
     * @return a String representing the direction
     */
    public String convertClassToType(int classid, ArrayList types) {
        classid /= 10;
        // !!ARRGH!
        for (int i = 0; i < types.size(); i++) {
            HPP_Class hpc = (HPP_Class) types.get(i);
            int cl = hpc.getClass_ID();
            if (classid == cl) {
                return hpc.getRelOrientation();
            }
        }
        return null;
    }

    /**
     * Returns a String array of domain Strings by calling a combination of
     * getDomains and getChains.
     * 
     * @param source
     *            the classification source (CATH, SCOP)
     * @param pdbid
     *            PDB tops.dw.protein 4 character id
     * @return an array of TOPS strings
     */
    public String[] getProtein(String source, String pdbid) {
        return this.getDomains(source, this.getChains(source, pdbid));
    }

    /**
     * List the chain names for a particular PDB id.
     * 
     * @param source
     *            the classification source (CATH, SCOP)
     * @param pdbid
     *            PDB tops.dw.protein 4 character id
     * @return an array of chain ids
     */
    public String[] getChains(String source, String pdbid) {
        String Chain_Q = "SELECT Chain_ID FROM Chain WHERE PDB_ID = '" + pdbid
                + "'";
        ArrayList chains = new ArrayList();
        // get the chains for this tops.dw.protein
        ResultSet cr = this.doQuery(Chain_Q);
        try {
            while (cr.next()) {
                Chain c = new Chain();
                c.setChain_ID(cr.getString("Chain_ID"));
                chains.add(c);
            }
        } catch (SQLException squeel) {
            System.err.println(squeel);
        }

        String[] chain_ids = new String[chains.size()];
        for (int i = 0; i < chains.size(); i++) {
            Chain nextChain = (Chain) chains.get(i);
            StringFactory.logger
                    .log(Level.INFO, "getting chain : "
                            + nextChain.getChain_ID());
            chain_ids[i] = nextChain.getChain_ID();
        }
        return chain_ids;
    }

    /**
     * Make tops strings for every domain of each chain in the chains input
     * array.
     * 
     * @param source
     *            the classification source (CATH, SCOP)
     * @param chains
     *            an array of chain id strings
     * @return an array of tops strings
     */
    public String[] getDomains(String source, String[] chains) {

        ArrayList hbonds;
        ArrayList hpacks;
        ArrayList chirals;
        ArrayList sses;
        ArrayList ssedoms;

        HashMap domMap = new HashMap();
        HashMap bondMap = new HashMap();
        TreeMap numMap = new TreeMap();
        // mappings of the database numbering to domain numbering WHY TreeMap? -
        // because it is ordered!

        String SSE_DOM_QStub = "SELECT DOM_ID,SSE_No,Direction FROM SSE_DOM WHERE Source = '"
                + source + "' AND Chain_ID = '";
        String SSE_QStub = "SELECT SSE_No,Type FROM Secondary_Structure_Element WHERE Chain_ID = '";
        String HBond_QStub = "SELECT SSE_No,SSE_NoC,Type FROM Hydrogen_Bond WHERE Chain_ID = '";
        String Helix_QStub = "SELECT SSE_No,SSE_NoC,Class_ID FROM Helix_Packing_Pair WHERE Chain_ID = '";
        // String Helix_QStub = "SELECT SSE_No,SSE_NoC,Class_ID FROM
        // Helix_Packing_Pair WHERE (ContResNum + ContResNumC) > 7 AND Chain_ID
        // = '";

        String HPP_Q = "SELECT Class_ID,RelOrientation FROM HPP_Class";
        String Chiral_QStub = "SELECT SSE_No,Hand FROM Chiral_Connection WHERE Chain_ID = '";

        // get the HPP data (this might never change!)
        ArrayList hppdata = new ArrayList();
        ResultSet hppr = this.doQuery(HPP_Q);
        try {
            while (hppr.next()) {
                HPP_Class hppc = new HPP_Class();
                hppc.setClass_ID(hppr.getInt("Class_ID"));
                hppc.setRelOrientation(hppr.getString("RelOrientation"));
                hppdata.add(hppc);
            }
        } catch (SQLException squeel) {
            System.err.println(squeel);
        }

        // step through the chains, getting data for each
        for (int i = 0; i < chains.length; i++) {
            hbonds = new ArrayList();
            hpacks = new ArrayList();
            chirals = new ArrayList();
            sses = new ArrayList();
            ssedoms = new ArrayList();

            String cid = chains[i];

            try {
                ResultSet ssedr = this.doQuery(SSE_DOM_QStub + cid + "'");
                while (ssedr.next()) {
                    SSE_DOM sd = new SSE_DOM();
                    sd.setDOM_ID(ssedr.getString("DOM_ID"));
                    sd.setSSE_No(ssedr.getInt("SSE_No"));
                    sd.setDirection(ssedr.getString("Direction"));
                    ssedoms.add(sd);
                }

                ResultSet sser = this.doQuery(SSE_QStub + cid + "'");
                while (sser.next()) {
                    Secondary_Structure_Element sse = new Secondary_Structure_Element();
                    sse.setSSE_No(sser.getInt("SSE_No"));
                    sse.setType(sser.getString("Type"));
                    sses.add(sse);
                }

                ResultSet hbr = this.doQuery(HBond_QStub + cid + "'");
                while (hbr.next()) {
                    Hydrogen_Bond h = new Hydrogen_Bond();
                    h.setSSE_No(hbr.getInt("SSE_No"));
                    h.setSSE_NoC(hbr.getInt("SSE_NoC"));
                    h.setType(hbr.getString("Type"));
                    hbonds.add(h);
                }

                ResultSet hpr = this.doQuery(Helix_QStub + cid + "'");
                while (hpr.next()) {
                    Helix_Packing_Pair p = new Helix_Packing_Pair();
                    p.setSSE_No(hpr.getInt("SSE_No"));
                    p.setSSE_NoC(hpr.getInt("SSE_NoC"));
                    p.setClass_ID(hpr.getInt("Class_ID"));
                    hpacks.add(p);
                }

                ResultSet chr = this.doQuery(Chiral_QStub + cid + "'");
                while (chr.next()) {
                    Chiral_Connection c = new Chiral_Connection();
                    c.setSSE_No(chr.getInt("SSE_No"));
                    c.setHand(chr.getString("Hand"));
                    chirals.add(c);
                }

            } catch (SQLException squeel) {
                System.err.println(squeel);
            }

            // catch the 'no data in database' exception!
            if (ssedoms.size() == 0) {
                StringFactory.logger.log(Level.INFO, "No SSE_DOM data for " + cid);
            }

            // map the sses to a particular domain for this chain
            for (int j = 0; j < ssedoms.size(); j++) {
                SSE_DOM ssd = (SSE_DOM) ssedoms.get(j);
                String id = ssd.getDOM_ID();
                int number = ssd.getSSE_No();

                String type = this.lookupType(number, sses);
                // System.err.println("SSE : " + id + ", " + number + ", " +
                // type);
                if (!type.equals("U")) {
                    String direction = ssd.getDirection();
                    if ((direction != null) && (direction.equals("D"))) {
                        type = type.toLowerCase();
                    }
                    TreeMap sseMap;
                    if (domMap.containsKey(id)) {
                        sseMap = (TreeMap) domMap.get(id);
                    } else {
                        sseMap = new TreeMap();
                        domMap.put(id, sseMap);
                    }
                    // System.err.println("putting number " + number + " into
                    // domain " + id);
                    sseMap.put(new Integer(number), type);
                    this.mapNumbers(numMap, domMap, number, id);
                }
            }

            // do the same for the hydrogen bonds, throwing away those that
            // cross domains
            for (int k = 0; k < hbonds.size(); k++) {
                Hydrogen_Bond hb = (Hydrogen_Bond) hbonds.get(k);
                int left = hb.getSSE_No();
                Integer mapped_left = (Integer) numMap.get(new Integer(left));
                int right = hb.getSSE_NoC();
                Integer mapped_right = (Integer) numMap.get(new Integer(right));
                String type = hb.getType();
                String id_left = this.getDomain(new Integer(left), domMap);
                String id_right = this.getDomain(new Integer(right), domMap);
                if ((id_left == null) || (id_right == null)) {
                    StringFactory.logger.log(Level.INFO, "hbond null pointer!" + left + "("
                            + id_left + "):" + right + "(" + id_right + ")"
                            + type);
                } else {
                    if (id_left.equals(id_right)) {
                        ArrayList blist;
                        if (bondMap.containsKey(id_left)) {
                            blist = (ArrayList) bondMap.get(id_left);
                        } else {
                            blist = new ArrayList();
                            bondMap.put(id_left, blist);
                        }
                        // blist.add(new String(mapped_left + ":" + mapped_right
                        // + type));
                        blist.add(new Edge(new Vertex('E', mapped_left
                                .intValue()), new Vertex('E', mapped_right
                                .intValue()), type.charAt(0)));
                    } else {
                        StringFactory.logger.log(Level.INFO, "cross-domain H bond " + left
                                + ":" + right + type);
                    }
                }
            }
            // and for helix-packing
            for (int l = 0; l < hpacks.size(); l++) {
                Helix_Packing_Pair hpp = (Helix_Packing_Pair) hpacks.get(l);
                int left = hpp.getSSE_No();
                Integer mapped_left = (Integer) numMap.get(new Integer(left));
                int right = hpp.getSSE_NoC();
                Integer mapped_right = (Integer) numMap.get(new Integer(right));
                int classid = hpp.getClass_ID();
                String type = this.convertClassToType(classid, hppdata);
                String id_left = this.getDomain(new Integer(left), domMap);
                String id_right = this.getDomain(new Integer(right), domMap);
                if ((id_left == null) || (id_right == null)) {
                    StringFactory.logger.log(Level.INFO, "helix null pointer!" + left + "("
                            + id_left + "):" + right + "(" + id_right + ")"
                            + type);
                } else {
                    if (id_left.equals(id_right)) {
                        ArrayList blist;
                        if (bondMap.containsKey(id_left)) {
                            blist = (ArrayList) bondMap.get(id_left);
                        } else {
                            blist = new ArrayList();
                            bondMap.put(id_left, blist);
                        }
                        // blist.add(new String(mapped_left + ":" + mapped_right
                        // + type));
                        blist.add(new Edge(new Vertex('E', mapped_left
                                .intValue()), new Vertex('E', mapped_right
                                .intValue()), type.charAt(0)));
                    } else {
                        StringFactory.logger.log(Level.INFO, "cross-domain Packing " + left
                                + ":" + right + type);
                    }
                }
            }

            // and for chiralities
            for (int m = 0; m < chirals.size(); m++) {
                Chiral_Connection cc = (Chiral_Connection) chirals.get(m);
                String type = cc.getHand();

                int left = cc.getSSE_No();
                String leftDom = this.getDomain(new Integer(left), domMap);
                Integer mapped_left = (Integer) numMap.get(new Integer(left));

                int right = this.findOtherEnd(left, (TreeMap) domMap.get(leftDom));
                Integer mapped_right = (Integer) numMap.get(new Integer(right));

                ArrayList blist;

                if (bondMap.containsKey(leftDom)) {
                    blist = (ArrayList) bondMap.get(leftDom);
                } else {
                    blist = new ArrayList();
                    bondMap.put(leftDom, blist);
                }
                // blist.add(new String(mapped_left + ":" + mapped_right +
                // type));
                if ((mapped_left != null) && (mapped_right != null)
                        && (type != null)) {
                    Vertex v1 = new Vertex('E', mapped_left.intValue());
                    Vertex v2 = new Vertex('E', mapped_right.intValue());
                    char chiral_type = type.charAt(0);
                    Edge e = new Edge(v1, v2, chiral_type);
                    // try to find any hbonds in the same position
                    for (int j = 0; j < blist.size(); j++) {
                        Edge hbond = (Edge) blist.get(j);
                        if (hbond.equals(e)) {
                            StringFactory.logger.log(Level.INFO, "mixed type!");
                            char hbond_type = hbond.getType();
                            if (hbond_type == 'A') {
                                // this shouldn't happen!
                            } else {
                                if (type.equals("R")) {
                                    chiral_type = 'Z';
                                } else {
                                    chiral_type = 'X';
                                }
                            }
                            e.setType(chiral_type);
                            blist.remove(hbond);
                        }
                    }
                    blist.add(e);
                }
            }
            sses = new ArrayList();
            ssedoms = new ArrayList();
            hbonds = new ArrayList();
            hpacks = new ArrayList();
            chirals = new ArrayList();
        }

        // go through the domains, getting the bits and pieces
        Set domains = domMap.keySet();
        Iterator itr = domains.iterator();
        String[] domainStrings = new String[domains.size()];
        int d = 0;
        while (itr.hasNext()) {
            StringBuffer buffer = new StringBuffer();

            String domid = (String) itr.next();
            // System.err.println("got domain : " + domid);
            buffer.append(domid).append(" ").append("N");

            TreeMap sseMap = (TreeMap) domMap.get(domid);
            Set numbers = sseMap.keySet();
            Iterator itr2 = numbers.iterator();

            while (itr2.hasNext()) {
                Integer sse_num = (Integer) itr2.next();
//                Integer m_sse_num = (Integer) numMap.get(sse_num);
                String type = (String) sseMap.get(sse_num);
                // System.err.print(m_sse_num + "-" + type + ",");
                buffer.append(type);
            }

            buffer.append("C ");

            ArrayList bonds = (ArrayList) bondMap.get(domid);
            if (bonds != null) {
                Collections.sort(bonds);
                Iterator itr3 = bonds.iterator();
                while (itr3.hasNext()) {
                    buffer.append(itr3.next());
                }
            }
            domainStrings[d++] = buffer.toString();
        }
        return domainStrings;
    }

    /**
     * Given a code prefix (from cath or scop), returns all the domains that
     * have a code matching that prefix. So "2.20.30" will return all 2.20.30s -
     * eg 2.20.30.1, 2.20.30.2, etc
     * 
     * @param source
     *            cath, scop, etc
     * @param code
     *            a classification code prefix
     * @param rep
     *            a rep level to select at
     * @return an array of domain strings
     */
    public String[] getGroup(String source, String code, String rep) {
        String GROUP_Q = "SELECT Domain.DOM_ID, Code FROM Domain, Reps WHERE Domain.DOM_ID = Reps.DOM_ID AND Reps.Source = '";
        GROUP_Q += source + "' AND Rep = '" + rep + "' AND Code LIKE '" + code
                + "%';";
        HashMap dom_ids = new HashMap();
        ResultSet group = this.doQuery(GROUP_Q);
        try {
            while (group.next()) {
                String DOM_ID = group.getString("DOM_ID");
                String fullcode = group.getString("Code");
                dom_ids.put(DOM_ID, fullcode);
            }
        } catch (SQLException squeel) {
            System.err.println(squeel);
        }

        ArrayList domains = new ArrayList();
        Iterator itr = dom_ids.keySet().iterator();
        while (itr.hasNext()) {
            String dom_id = (String) itr.next();
            String domain = this.getDomain(source, dom_id);
            if (domain != null && !domain.equals("")) {
                String classification = (String) dom_ids.get(dom_id);
                domains.add(domain + " " + classification);
            }
        }

        return (String[]) domains.toArray(new String[0]);
    }

    /**
     * Finds all the domains for the chain that this domain is in, and returns
     * only the tops string for this domain. This is obviously a bit wasteful,
     * but there is no easier way to do this.
     * 
     * @param source
     *            cath, scop, etc
     * @param dom_id
     *            domain id string
     * @return a tops string
     */
    public String getDomain(String source, String dom_id) {
        String chain = dom_id.substring(0, 5);
        String[] allDomains = null;
        try {
            allDomains = this.getDomains(source, chain);
        } catch (NullPointerException npe) {
            StringFactory.logger.log(Level.INFO, "NULL POINTER FOR " + chain + "!");
            return "";
        }
        for (int i = 0; i < allDomains.length; i++) {
            String other_dom_id = allDomains[i].substring(0, 6);
            if (other_dom_id.equals(dom_id)) {
                return allDomains[i];
            }
        }
        return null;
    }

    /**
     * Retrieves all the domains for this chain.
     * 
     * @param source
     *            cath. scop, etc
     * @param chain
     *            the chain id string
     * @return an array of domain strings
     */
    public String[] getDomains(String source, String chain) {
        String[] chains = { chain };
        return this.getDomains(source, chains);
    }

    /**
     * The main program for the StringFactory class. Run it with one of:
     * <p>
     * <ul>
     * <li>tops.db.StringFactory &lt;source&gt; "-g" &lt;code&gt; &lt;rep&gt;</li>
     * <li>tops.db.StringFactory &lt;source&gt; "-p" &lt;name&gt;</li>
     * <li>tops.db.StringFactory &lt;source&gt; "-c" &lt;chain&gt;</li>
     * <li>tops.db.StringFactory &lt;source&gt; "-d" &lt;domain&gt;</li>
     * <li>tops.db.StringFactory &lt;source&gt; "-f" &lt;filename&gt;</li>
     * </ul>
     * 
     * Where 'source' is, for example, 'CATH2.4' or 'SCOP1.61'. And 'rep' is a
     * rep level, for example, 'Hreps' or 'Treps'..
     * 
     * @param args
     *            source, flag, and argument.
     */
    public static void main(String[] args) {
        StringFactory.logger.addHandler(new StreamHandler(System.err, new PlainFormatter()));
        StringFactory.logger.setUseParentHandlers(false);
        StringFactory.logger.setLevel(Level.ALL);

        // System.err.println(logger.isLoggable(Level.INFO));

        String source = args[0];
        String flag = args[1];
        StringFactory stringFactory = new StringFactory();
        String[] results = null;

        if (flag.equals("-g")) {
            // group
            String code = args[2];
            String rep = args[3];
            results = stringFactory.getGroup(source, code, rep);
        } else if (flag.equals("-p")) {
            // tops.dw.protein
            String name = args[2];
            results = stringFactory.getProtein(source, name);
        } else if (flag.equals("-c")) {
            // chain
            String chain = args[2];
            results = stringFactory.getDomains(source, chain);
        } else if (flag.equals("-d")) {
            // domain
            String domid = args[2];
            String result = stringFactory.getDomain(source, domid);
            results = new String[1];
            results[0] = result;
        } else if (flag.equals("-f")) {
            // names from file
            String filename = args[2];
            System.err.println("reading file " + filename);
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.FileReader(args[2]));
                String line;
                ArrayList resultList = new ArrayList();
                while ((line = reader.readLine()) != null) {
                    String domain = stringFactory.getDomain(source, line);
                    if (domain != null && !domain.equals("")) {
                        resultList.add(domain);
                    }
                }
                results = (String[]) resultList.toArray(new String[0]);
            } catch (java.io.IOException ioe) {
                System.err.println(ioe);
            }

        } else {
            System.exit(0);
        }

        for (int i = 0; i < results.length; i++) {
            System.out.println(results[i]);
        }
    }

}
