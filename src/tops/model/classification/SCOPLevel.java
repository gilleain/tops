package tops.model.classification;

import java.util.ArrayList;

import java.io.PrintStream;

public class SCOPLevel {

    /** ROOT */
    public static final int ROOT = -1;

    /** Class */
    public static final int CL = 0;

    /** Fold */
    public static final int CF = 1;

    /** Superfamily */
    public static final int SF = 2;

    /** Family */
    public static final int FA = 3;

    /** Protein */
    public static final int DM = 4;

    /** Species */
    public static final int SP = 5;

    /** Domain */
    public static final int PX = 6;

    /** The actual strings for the level names */
    public static final String[] fullNames = new String[] { "Class", "Fold",
            "Superfamily", "Family", "Protein", "Species", "Domain" };

    /** The level name (Class, Fold, Superfamily, Family, etc) for this level */
    private int name;

    /** The level name for the children */
    private int childLevelName;

    /** The sunid for this level - equivalent of 'code' in CATHLevel */
    private int sunid;

    /** The domain ID of the representative of this level */
    private String repName;

    /** A list of children - either sublevels or domainids */
    private ArrayList children;

    /**
     * Create a new level.
     * 
     * @param name
     *            the flag for this level type (C, A, T, H, etc)
     * @param sunid
     *            the numerical id for this level (53931, 103233, etc)
     * @param repName
     *            the domain id of the level representative
     */

    public SCOPLevel(int name, int sunid, String repName) {
        this.name = name;
        this.sunid = sunid;
        this.repName = repName;
        this.children = new ArrayList();

        // determine the name of the next level : -1 indicates the leaves
        this.childLevelName = -1;
        if (this.name != SCOPLevel.PX) {
            this.childLevelName = this.name + 1;
        }
    }

    /**
     * Recursively add the data from the scopNumber into this level and its
     * children.
     * 
     * @param scopNumber
     *            the data to be added
     */

    public void addSCOPNumber(SCOPNumber scopNumber) {

        // adding the domain name is the last step, stop recursing
        if (this.childLevelName == -1) {
            this.children.add(scopNumber.getDomainID());
            return;
        }

        // try to find a child level with the appropriate sunid
        int childLevelSunid = scopNumber.getSunidForName(this.childLevelName);
        SCOPLevel childLevel = this.getChildLevel(childLevelSunid);

        // create new levels as appropriate, making this scopNumber the rep
        // (assumes an ordering on inputs)
        if (childLevel == null) {
            childLevel = new SCOPLevel(this.childLevelName, childLevelSunid,
                    scopNumber.getDomainID());
            this.children.add(childLevel);
        }

        // now continue to traverse the tree
        childLevel.addSCOPNumber(scopNumber);
    }

    /**
     * Run through the children, trying to find one with the specified sunid.
     * 
     * @param sunid
     *            the desired sunid.
     * @return a level, or null if none is found with a sunid like
     *         <code>sunid</code>.
     */

    public SCOPLevel getChildLevel(int sunid) {
        for (int i = 0; i < this.children.size(); i++) {
            SCOPLevel child = (SCOPLevel) this.children.get(i);
            if (child.hasSunid(sunid)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Check this level's sunid against the supplied sunid
     * 
     * @param sunid
     *            a sunid to check
     * @return true if the sunid matches this sunid
     */

    public boolean hasSunid(int sunid) {
        return this.sunid == sunid;
    }

    /**
     * Lookup a domain in the level's children to find out its rep status.
     * 
     * @param scopNumber
     *            the domain we are searching for.
     */

    public int getHighestRep(SCOPNumber scopNumber) {
        // the first level we reach with this domainID as a rep, we return
        if (this.name != SCOPLevel.ROOT
                && this.repName.equals(scopNumber.getDomainID())) {
            return this.name;
        }

        // no more reps
        if (this.childLevelName == -1) {
            return 8;
        }

        // we are making the dangerous assumption that the tree actually
        // contains the domain we are searching for!
        int childLevelCode = scopNumber.getSunidForName(this.childLevelName);
        SCOPLevel childLevel = this.getChildLevel(childLevelCode);

        // continue to search
        if (childLevel == null) {
            System.err.println(scopNumber + " " + this);
            return -1;
        } else {
            return childLevel.getHighestRep(scopNumber);
        }
    }

    /**
     * Lookup a domain in the level's children to find out its rep status.
     * 
     * @param scopNumber
     *            the domain we are searching for.
     * @param repInts
     *            the list of Integers (the level name constants) we are
     *            assembling.
     */

    public void getReps(SCOPNumber scopNumber, ArrayList repInts) {
        // add a rep to the list if necessary
        if (this.name != SCOPLevel.ROOT
                && this.repName.equals(scopNumber.getDomainID())) {
            repInts.add(new Integer(this.name));
        }

        // no more reps
        if (this.childLevelName == -1) {
            return;
        }

        // we are making the dangerous assumption that the tree actually
        // contains the domain we are searching for!
        int childLevelSunid = scopNumber.getSunidForName(this.childLevelName);
        SCOPLevel childLevel = this.getChildLevel(childLevelSunid);

        // continue to search
        childLevel.getReps(scopNumber, repInts);
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
                ((SCOPLevel) this.children.get(i)).printToStream(out);
            }
        } else {
            out.println(this + " " + this.children);
        }
    }

    @Override
    public String toString() {
        if (this.name != SCOPLevel.ROOT) {
            return SCOPLevel.fullNames[this.name] + " " + this.sunid
                    + " rep : " + this.repName;
        }
        return "ROOT";
    }
}
