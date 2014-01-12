package tops.db.generation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import tops.engine.PlainFormatter;
import tops.engine.Vertex;

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
    public int findOtherEnd(int left, TreeMap<Integer, String> vertices) {
        String type = vertices.get(left);
        SortedMap<Integer, String> range = vertices.subMap(left + 1, left + 10);
        for(Integer i : range.keySet()) {
            String otherType = range.get(i);
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
    public void mapNumbers(TreeMap<Integer, Integer> numMap, HashMap<String, TreeMap<Integer, String>> domMap, int number, String id) {
        if (numMap.isEmpty()) {
            numMap.put(number, 1);
        } else {
            Integer lastEndNumber = numMap.lastKey();
            String lastId = this.getDomain(lastEndNumber, domMap);
            if (id.equals(lastId)) {
                // if the domains are the same
                Integer mappedLast = numMap.get(lastEndNumber);
                int lastVal = mappedLast.intValue();
                int mappedNew = lastVal + 1;
                numMap.put(number, mappedNew);
            } else {
                // crossing domain boundary
                lastEndNumber = this.getLastEndNumber(id, number, domMap);
                if (lastEndNumber.equals(-1)) {
                    // first SSE in domain
                    numMap.put(number, 1);
                } else {
                    Integer mappedLast = numMap.get(lastEndNumber);
                    int mappedNew = mappedLast.intValue() + 1;
                    numMap.put(number, mappedNew);
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
    public String lookupType(int num, ArrayList<Secondary_Structure_Element> list) {
        for (int i = 0; i < list.size(); i++) {
            Secondary_Structure_Element s = list
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
    public String getDomain(Integer number, HashMap<String, TreeMap<Integer, String>> map) {
        // System.err.println("getting domain for number : " + number);
        for (String dom : map.keySet()) {
            TreeMap<Integer, String> vertices = map.get(dom);
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
    public Integer getLastEndNumber(String domid, int num, HashMap<String, TreeMap<Integer, String>> map) {
        TreeMap<Integer, String> vertices = map.get(domid);
        SortedMap<Integer, String> heads = vertices.headMap(num);
        if (heads.isEmpty()) {
            return new Integer(-1);
        }
        // nothing smaller than num!
        else {
            return heads.lastKey();
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
    public String convertClassToType(int classid, ArrayList<HPP_Class> types) {
        classid /= 10;
        // !!ARRGH!
        for (int i = 0; i < types.size(); i++) {
            HPP_Class hpc = types.get(i);
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
        ArrayList<Chain> chains = new ArrayList<Chain>();
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
            Chain nextChain = chains.get(i);
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

        ArrayList<Hydrogen_Bond> hbonds;
        ArrayList<Helix_Packing_Pair> hpacks;
        ArrayList<Chiral_Connection> chirals;
        ArrayList<Secondary_Structure_Element> sses;
        ArrayList<SSE_DOM> ssedoms;

        HashMap<String, TreeMap<Integer, String>> domMap = new HashMap<String, TreeMap<Integer, String>>();
        HashMap<String, ArrayList<Edge>> bondMap = new HashMap<String, ArrayList<Edge>>();
        TreeMap<Integer, Integer> numMap = new TreeMap<Integer, Integer>();
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
        ArrayList<HPP_Class> hppdata = new ArrayList<HPP_Class>();
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
            hbonds = new ArrayList<Hydrogen_Bond>();
            hpacks = new ArrayList<Helix_Packing_Pair>();
            chirals = new ArrayList<Chiral_Connection>();
            sses = new ArrayList<Secondary_Structure_Element>();
            ssedoms = new ArrayList<SSE_DOM>();

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
                SSE_DOM ssd = ssedoms.get(j);
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
                    TreeMap<Integer, String> sseMap;
                    if (domMap.containsKey(id)) {
                        sseMap = domMap.get(id);
                    } else {
                        sseMap = new TreeMap<Integer, String>();
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
                Hydrogen_Bond hb = hbonds.get(k);
                int left = hb.getSSE_No();
                Integer mapped_left = numMap.get(new Integer(left));
                int right = hb.getSSE_NoC();
                Integer mapped_right = numMap.get(new Integer(right));
                String type = hb.getType();
                String id_left = this.getDomain(new Integer(left), domMap);
                String id_right = this.getDomain(new Integer(right), domMap);
                if ((id_left == null) || (id_right == null)) {
                    StringFactory.logger.log(Level.INFO, "hbond null pointer!" + left + "("
                            + id_left + "):" + right + "(" + id_right + ")"
                            + type);
                } else {
                    if (id_left.equals(id_right)) {
                        ArrayList<Edge> blist;
                        if (bondMap.containsKey(id_left)) {
                            blist = bondMap.get(id_left);
                        } else {
                            blist = new ArrayList<Edge>();
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
                Helix_Packing_Pair hpp = hpacks.get(l);
                int left = hpp.getSSE_No();
                Integer mapped_left = numMap.get(new Integer(left));
                int right = hpp.getSSE_NoC();
                Integer mapped_right = numMap.get(new Integer(right));
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
                        ArrayList<Edge> blist;
                        if (bondMap.containsKey(id_left)) {
                            blist = bondMap.get(id_left);
                        } else {
                            blist = new ArrayList<Edge>();
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
                Chiral_Connection cc = chirals.get(m);
                String type = cc.getHand();

                int left = cc.getSSE_No();
                String leftDom = this.getDomain(new Integer(left), domMap);
                Integer mapped_left = numMap.get(new Integer(left));

                int right = this.findOtherEnd(left, domMap.get(leftDom));
                Integer mapped_right = numMap.get(new Integer(right));

                ArrayList<Edge> blist;

                if (bondMap.containsKey(leftDom)) {
                    blist = bondMap.get(leftDom);
                } else {
                    blist = new ArrayList<Edge>();
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
                        Edge hbond = blist.get(j);
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
            sses = new ArrayList<Secondary_Structure_Element>();
            ssedoms = new ArrayList<SSE_DOM>();
            hbonds = new ArrayList<Hydrogen_Bond>();
            hpacks = new ArrayList<Helix_Packing_Pair>();
            chirals = new ArrayList<Chiral_Connection>();
        }

        // go through the domains, getting the bits and pieces
        Set<String> domains = domMap.keySet();
        Iterator<String> itr = domains.iterator();
        String[] domainStrings = new String[domains.size()];
        int d = 0;
        while (itr.hasNext()) {
            StringBuffer buffer = new StringBuffer();

            String domid = itr.next();
            // System.err.println("got domain : " + domid);
            buffer.append(domid).append(" ").append("N");

            TreeMap<Integer, String> sseMap = domMap.get(domid);
            for (Integer sse_num : sseMap.keySet()) {
//                Integer m_sse_num = (Integer) numMap.get(sse_num);
                String type = (String) sseMap.get(sse_num);
                // System.err.print(m_sse_num + "-" + type + ",");
                buffer.append(type);
            }

            buffer.append("C ");

            ArrayList<Edge> bonds = bondMap.get(domid);
            if (bonds != null) {
//                Collections.sort(bonds);	// TODO !
                for (Edge bond : bonds) {
                    buffer.append(bond);
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
        HashMap<String, String> dom_ids = new HashMap<String, String>();
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

        ArrayList<String> domains = new ArrayList<String>();
        Iterator<String> itr = dom_ids.keySet().iterator();
        while (itr.hasNext()) {
            String dom_id = itr.next();
            String domain = this.getDomain(source, dom_id);
            if (domain != null && !domain.equals("")) {
                String classification = dom_ids.get(dom_id);
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
            BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(args[2]));
	            try {
	                String line;
	                ArrayList<String> resultList = new ArrayList<String>();
	                while ((line = reader.readLine()) != null) {
	                    String domain = stringFactory.getDomain(source, line);
	                    if (domain != null && !domain.equals("")) {
	                        resultList.add(domain);
	                    }
	                }
	                results = resultList.toArray(new String[0]);
	            } catch (java.io.IOException ioe) {
	                System.err.println(ioe);
	            } finally {
	            	try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	            }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

        } else {
            System.exit(0);
        }

        for (int i = 0; i < results.length; i++) {
            System.out.println(results[i]);
        }
    }

}
