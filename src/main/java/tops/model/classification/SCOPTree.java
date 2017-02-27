package tops.model.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCOPTree implements ClassificationTree {

    /** The root of the hierarchy */
    private SCOPLevel root;

    /** A map of domainIDs to SCOPNumbers */
    private HashMap<String, SCOPNumber> domainSCOPNumberMap;

    /**
     * Create the scop tree with a single root level.
     */

    public SCOPTree() {
        this.root = new SCOPLevel(SCOPLevel.ROOT, 0, null);
        this.domainSCOPNumberMap = new HashMap<String, SCOPNumber>();
    }

    /**
     * Add a new SCOP number to the tree, determining if it is a rep along the
     * way.
     * 
     * @param scopNumber
     *            the SCOPNumber object to get data from
     */

    public void addSCOPNumber(SCOPNumber scopNumber) {
        this.root.addSCOPNumber(scopNumber);
        this.domainSCOPNumberMap.put(scopNumber.getDomainID(), scopNumber);
    }

    /**
     * An optimisation tactic for filling the db.
     * 
     * @param domainID
     *            a String id for the domain we will remove
     */
    public void removeDomainID(String domainID) {
        this.domainSCOPNumberMap.remove(domainID);
    }

    /**
     * Convenience method for converting a level name flag into a string.
     * 
     * @param name
     *            the SCOPLevel static int representing a level
     * @return a String for the name of the level
     */

    public String translate(int name) {
        return SCOPLevel.fullNames[name];
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
            SCOPNumber scopNumber = (SCOPNumber) this.domainSCOPNumberMap
                    .get(domainID);
            return scopNumber.getSCSS();
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
        return this.domainSCOPNumberMap.containsKey(domainID);
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
        SCOPNumber scopNumber = (SCOPNumber) this.domainSCOPNumberMap
                .get(domainID);
        return this.getHighestRep(scopNumber);
    }

    /**
     * Lookup a domain (in the form of a SCOPNumber) in the tree to find out its
     * highest rep status. The reason for using a SCOPNumber object is so that
     * we don't have to DFS the tree.
     * 
     * @param scopNumber
     *            a domain in the form of a SCOPNumber.
     * @return a single int - the level closest to the root that this domid is a
     *         rep for.
     */

    public int getHighestRep(SCOPNumber scopNumber) {
        return this.root.getHighestRep(scopNumber);
    }

    /**
     * Lookup a domain (in the form of a domainID string) in the tree to find
     * out its rep status.
     * 
     * @param domainID
     *            a domain ID String.
     * @return a list of ints - one for each level that this domain is a rep of.
     */

    public ArrayList<Integer> getReps(String domainID) {
        SCOPNumber scopNumber = (SCOPNumber) this.domainSCOPNumberMap
                .get(domainID);
        return this.getReps(scopNumber);
    }

    /**
     * Lookup a domain (in the form of a SCOPNumber) in the tree to find out its
     * rep status. The reason for using a SCOPNumber object is so that we don't
     * have to DFS the tree.
     * 
     * @param scopNumber
     *            a domain in the form of a SCOPNumber.
     * @return a list of ints - one for each level that this domain is a rep of.
     */

    public ArrayList<Integer> getReps(SCOPNumber scopNumber) {
        ArrayList<Integer> repInts = new ArrayList<Integer>();
        this.root.getReps(scopNumber, repInts);
        return repInts;
    }

    /**
     * Print out the whole tree to the stream out.
     * 
     * @param out
     *            the PrintStream to write to (eg: System.out)
     */

    public void printToStream(PrintStream out) {
        out.println("SCOP Tree:");
        this.root.printToStream(out);

        Iterator<String> domainIterator = this.domainSCOPNumberMap.keySet().iterator();
        while (domainIterator.hasNext()) {
            String domainID = (String) domainIterator.next();
            ArrayList<Integer> repList = this.getReps(domainID);
            out.println(domainID + " " + repList);
        }
    }

    /**
     * Classmethod to create trees from a file of scopnumbers - assumes a
     * certain format.
     * 
     * @param filepath
     *            a File assumed to be the path to a dir.cla.scop file.
     * @return a new SCOPTree.
     * 
     * @throws IOException
     *             if the path is wrong, file missing, etc
     */

    public static SCOPTree fromFile(File filepath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));

        String line;
        Pattern linePattern = Pattern
                .compile("^(.+?)\\t+(?:.+?)\\t+(?:.+?)\\t+(.+?)\\t+(?:.+?)\\t+cl=(\\d+),cf=(\\d+),sf=(\\d+),fa=(\\d+),dm=(\\d+),sp=(\\d+),px=(\\d+)$");

        SCOPTree tree = new SCOPTree();
        while ((line = bufferedReader.readLine()) != null) {
            Matcher m = linePattern.matcher(line);
            if (m.matches()) {
                SCOPNumber scopNumber = new SCOPNumber(Integer.parseInt(m
                        .group(3)), // Class
                        Integer.parseInt(m.group(4)), // Fold
                        Integer.parseInt(m.group(5)), // Superfamily
                        Integer.parseInt(m.group(6)), // Family
                        Integer.parseInt(m.group(7)), // Protein
                        Integer.parseInt(m.group(8)), // Species
                        Integer.parseInt(m.group(9)), // Domain
                        m.group(2), // scss
                        m.group(1) // Domain ID
                );
                // System.out.println("Adding " + scopNumber + " to tree");
                tree.addSCOPNumber(scopNumber);
            }
        }
        bufferedReader.close();

        return tree;
    }
}
