package tops.model.classification;

/**
 * Simple container for scop data.
 */

public class SCOPNumber {

    private int cl; // class

    private int cf; // fold

    private int sf; // superfamily

    private int fa; // family

    private int dm; // tops.dw.protein ... er .. because "dm" and
                    // tops.dw.protein are so similar...

    private int sp; // species

    private int px; // domain .. see tops.dw.protein

    /**
     * The scss is defined in the SCOP paper NAR (2002) V 30 No 1 as a "compact
     * representation of a SCOP domain classification" : cl/cf/sf/fa only.
     */
    private String scss;

    private String domainID;

    public SCOPNumber(int cl, int cf, int sf, int fa, int dm, int sp, int px,
            String scss, String domainID) {
        this.cl = cl;
        this.cf = cf;
        this.sf = sf;
        this.fa = fa;
        this.dm = dm;
        this.sp = sp;
        this.px = px;
        this.scss = scss;
        this.domainID = domainID;
    }

    /**
     * Get the sunid of this scop number at the specified level
     * <code>name</code>.
     * 
     * @param name
     *            the level (class, fold, superfamily, family...) at which to
     *            return a sunid.
     * @return the sunid at level <code>name</code>.
     */

    public int getSunidForName(int name) {
        switch (name) {
            case 0:
                return this.cl;
            case 1:
                return this.cf;
            case 2:
                return this.sf;
            case 3:
                return this.fa;
            case 4:
                return this.dm;
            case 5:
                return this.sp;
            case 6:
                return this.px;
            default:
                return -1;
        }
    }

    /**
     * Provide the domain ID associated with this cathnumber
     * 
     * @return the domain ID
     */
    public String getDomainID() {
        return this.domainID;
    }

    public String getSCSS() {
        return this.scss;
    }

    /**
     * Return just the scss and the domain id .
     * 
     * @return a String
     */

    @Override
    public String toString() {
        return this.scss + " " + this.domainID;
    }
}
