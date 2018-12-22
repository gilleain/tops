package tops.model.classification;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CATHLevel {

    /** ROOT */
    public static final int ROOT = -1;

    /** Class */
    public static final int C = 0;

    /** Architecture */
    public static final int A = 1;

    /** Topology */
    public static final int T = 2;

    /** Homology */
    public static final int H = 3;

    /** Sequence 35 family */
    public static final int S35 = 4;

    /** Sequence 95 family */
    public static final int S95 = 5;

    /** Sequence 100 family */
    public static final int S100 = 6;

    /** The actual strings for the level names */
    public static final String[] FULL_NAMES = new String[] { "Class",
            "Architecture", "Topology", "Homology", "S35 Family", "S95 Family",
            "S100 Family" };

    /** The level name (C, A, T, H, etc) for this level */
    private int name;

    /** The level name for the children */
    private int childLevelName;

    /** The code for this level - the code for the T level for 1.10.20 is 20 */
    private int code;

    /** The domain ID of the representative of this level */
    private String repName;

    /** A list of children - either sublevels or domainids */
    private List<Object> children;	// TODO : FIXME - use design pattern for trees!

    /**
     * Create a new level.
     * 
     * @param name
     *            the flag for this level type (C, A, T, H, etc)
     * @param code
     *            the numerical id for this level (10, 100, 530, etc)
     * @param repName
     *            the domain id of the level representative
     */

    public CATHLevel(int name, int code, String repName) {
        this.name = name;
        this.code = code;
        this.repName = repName;
        this.children = new ArrayList<>();

        // determine the name of the next level : -1 indicates the leaves
        this.childLevelName = -1;
        if (this.name != CATHLevel.S100) {
            this.childLevelName = this.name + 1;
        }
    }

    /**
     * Recursively add the data from the cathNumber into this level and its
     * children.
     * 
     * @param cathNumber
     *            the data to be added
     */

    public void addCATHNumber(CATHNumber cathNumber) {

        // adding the domain name is the last step, stop recursing
        if (this.childLevelName == -1) {
            this.children.add(cathNumber.getDomainID());
            return;
        }

        // try to find a child level with the appropriate code
        int childLevelCode = cathNumber.getCodeForName(this.childLevelName);
        CATHLevel childLevel = this.getChildLevel(childLevelCode);

        // create new levels as appropriate, making this cathNumber the rep
        // (assumes an ordering on inputs)
        if (childLevel == null) {
            childLevel = new CATHLevel(this.childLevelName, childLevelCode,
                    cathNumber.getDomainID());
            this.children.add(childLevel);
        }

        // now continue to traverse the tree
        childLevel.addCATHNumber(cathNumber);
    }

    /**
     * Run through the children, trying to find one with the specified code.
     * 
     * @param code
     *            the desired code.
     * @return a level, or null if none is found with a code like
     *         <code>code</code>.
     */

    public CATHLevel getChildLevel(int code) {
        for (int i = 0; i < this.children.size(); i++) {
            CATHLevel child = (CATHLevel) this.children.get(i);
            if (child.hasCode(code)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Check this level's code against the supplied code
     * 
     * @param code
     *            a code to check
     * @return true if the code matches this code
     */

    public boolean hasCode(int code) {
        return this.code == code;
    }

    /**
     * Lookup a domain in the level's children to find out its rep status.
     * 
     * @param cathNumber
     *            the domain we are searching for.
     */

    public int getHighestRep(CATHNumber cathNumber) {
        // the first level we reach with this domainID as a rep, we return
        if (this.name != CATHLevel.ROOT
                && this.repName.equals(cathNumber.getDomainID())) {
            return this.name;
        }

        // no more reps
        if (this.childLevelName == -1) {
            return 8;
        }

        // we are making the dangerous assumption that the tree actually
        // contains the domain we are searching for!
        int childLevelCode = cathNumber.getCodeForName(this.childLevelName);
        CATHLevel childLevel = this.getChildLevel(childLevelCode);

        // continue to search
        if (childLevel == null) {
            System.err.println(cathNumber + " " + this);
            return -1;
        } else {
            return childLevel.getHighestRep(cathNumber);
        }
    }

    /**
     * Lookup a domain in the level's children to find out its rep status.
     * 
     * @param cathNumber
     *            the domain we are searching for.
     * @param repInts
     *            the list of ints we are assembling.
     */

    public void getReps(CATHNumber cathNumber, List<Integer> repInts) {
        // add a rep to the list if necessary
        if (this.name != CATHLevel.ROOT
                && this.repName.equals(cathNumber.getDomainID())) {
            repInts.add(this.name);
        }

        // no more reps
        if (this.childLevelName == -1) {
            return;
        }

        // we are making the dangerous assumption that the tree actually
        // contains the domain we are searching for!
        int childLevelCode = cathNumber.getCodeForName(this.childLevelName);
        CATHLevel childLevel = this.getChildLevel(childLevelCode);

        // continue to search
        if (childLevel == null) {
            System.err.println(cathNumber + " " + this);
        } else {
            childLevel.getReps(cathNumber, repInts);
        }
    }

    /**
     * Print out the level and its children to the stream <code>out</code>.
     * 
     * @param out
     *            the PrintStream to write to (eg: System.out)
     */

    public void printToStream(PrintStream out) {
        // pretty-print the level by tabbing
        for (int l = 0; l < this.name; l++) {
            out.print("\t");
        }

        if (this.childLevelName != -1) {
            out.println(this);
            for (int i = 0; i < this.children.size(); i++) {
                ((CATHLevel) this.children.get(i)).printToStream(out);
            }
        } else {
            out.println(this + " " + this.children);
        }
    }

    @Override
    public String toString() {
        if (this.name != CATHLevel.ROOT) {
            return CATHLevel.FULL_NAMES[this.name] + " " + this.code + " rep : "
                    + this.repName;
        }
        return "ROOT";
    }
}
