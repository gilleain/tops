package tops.model.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CATHTree implements ClassificationTree {

    /** The tree root */
    private CATHLevel root;

    /** A map of domainIDs to CATHNumbers */
    private Map<String, CATHNumber> domainCATHNumberMap;

    /**
     * Create the cath tree with default 4 top level classes (without reps).
     */

    public CATHTree() {
        this.root = new CATHLevel(CATHLevel.ROOT, 0, null);
        this.domainCATHNumberMap = new HashMap<String, CATHNumber>();
    }

    /**
     * Add a new CATH number to the tree, determining if it is a rep along the
     * way.
     * 
     * @param cathNumber
     *            the CATHNumber object to get data from
     */

    public void addCATHNumber(CATHNumber cathNumber) {
        this.root.addCATHNumber(cathNumber);
        this.domainCATHNumberMap.put(cathNumber.getDomainID(), cathNumber);
    }

    /**
     * An optimisation tactic for filling the db.
     * 
     * @param domainID
     *            a String id for the domain we will remove
     */
    public void removeDomainID(String domainID) {
        this.domainCATHNumberMap.remove(domainID);
    }

    /**
     * Convenience method for converting a level name flag into a string.
     * 
     * @param name
     *            the CATHLevel static int representing a level
     * @return a String for the name of the level
     */

    public String translate(int name) {
        return CATHLevel.FULL_NAMES[name];
    }

    /**
     * Get the number for this domainID. Note that it returns an empty string if
     * this domainID is not in the tree.
     * 
     * @param domainID
     *            the domain we are looking up.
     * @return a String classification identifier.
     */

    public String getNumberForDomainID(String domainID) {
        try {
            CATHNumber cathNumber = this.domainCATHNumberMap.get(domainID);
            return cathNumber.getFullCode();
        } catch (NullPointerException npe) {
            return "";
        }
    }

    /**
     * Check that a domainID is actually in the tree!
     * 
     * @param domainID
     *            the domain we want to know about.
     * @return true or false
     */

    public boolean isDomainIDInTree(String domainID) {
        return this.domainCATHNumberMap.containsKey(domainID);
    }

    /**
     * Lookup a domain (in the form of a domainID string) in the tree to find
     * out its highest rep status.
     * 
     * @param domainID
     *            a domain ID String.
     * @return a single int - the level closest to the root that this domid is a
     *         rep for.
     */

    public int getHighestRep(String domainID) {
        CATHNumber cathNumber = this.domainCATHNumberMap.get(domainID);
        return this.getHighestRep(cathNumber);
    }

    /**
     * Lookup a domain (in the form of a CATHNumber) in the tree to find out its
     * highest rep status. The reason for using a CATHNumber object is so that
     * we don't have to DFS the tree.
     * 
     * @param scopNumber
     *            a domain in the form of a CATHNumber.
     * @return a single int - the level closest to the root that this domid is a
     *         rep for.
     */

    public int getHighestRep(CATHNumber cathNumber) {
        return this.root.getHighestRep(cathNumber);
    }

    /**
     * Lookup a domain (in the form of a domainID string) in the tree to find
     * out its rep status.
     * 
     * @param domainID
     *            a domain ID String.
     * @return a list of ints - one for each level that this domain is a rep of.
     */

    public List<Integer> getReps(String domainID) {
        CATHNumber cathNumber = this.domainCATHNumberMap.get(domainID);
        return this.getReps(cathNumber);
    }

    /**
     * Lookup a domain (in the form of a CATHNumber) in the tree to find out its
     * rep status. The reason for using a CATHNumber object is so that we don't
     * have to DFS the tree.
     * 
     * @param cathNumber
     *            a domain in the form of a CATHNumber.
     * @return a list of ints - one for each level that this domain is a rep of.
     */

    public List<Integer> getReps(CATHNumber cathNumber) {
        List<Integer> repInts = new ArrayList<Integer>();

        this.root.getReps(cathNumber, repInts);
        return repInts;
    }

    /**
     * Print out the whole tree to the stream out.
     * 
     * @param out
     *            the PrintStream to write to (eg: System.out)
     */

    public void printToStream(PrintStream out) {
        out.println("CATH Tree:");
        this.root.printToStream(out);

        for (String domainId : this.domainCATHNumberMap.keySet()) {
            List<Integer> repList = this.getReps(domainId);
            int highestRep = this.getHighestRep(domainId);
            out.println(highestRep + " " + domainId + " " + repList);
        }
    }

    /**
     * Classmethod to create trees from a file of cathnumbers - assumes a
     * certain format.
     * 
     * @param filepath
     *            a File assumed to be the path to a CathDomainList
     * @return a new CATHTree
     * 
     * @throws IOException
     *             if the path is wrong, file missing, etc
     */

    public static CATHTree fromFile(File filepath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));

        String line;
        Pattern linePattern = Pattern
                .compile("^(.{6})\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d).+$");

        CATHTree tree = new CATHTree();
        while ((line = bufferedReader.readLine()) != null) {
            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                CATHNumber cathNumber = new CATHNumber(
                        Integer.parseInt(m.group(2)), // Class
                        Integer.parseInt(m.group(3)), // Architecture
                        Integer.parseInt(m.group(4)), // Topology
                        Integer.parseInt(m.group(5)), // Homology
                        Integer.parseInt(m.group(6)), // Sequence 35 Family
                        Integer.parseInt(m.group(7)), // Sequence 95 Family
                        Integer.parseInt(m.group(8)), // Sequence 100 Family
                        m.group(1) // Domain ID
                );
                // System.out.println("Adding " + cathNumber + " to tree");
                tree.addCATHNumber(cathNumber);
            }
        }
        bufferedReader.close();

        return tree;
    }
}
