package tops.dw.protein;

/**
 * This class implement an object representing a range of integer values ( to be
 * really object oriented it ought to be possible to implement this class for
 * more general numbers (not just integers) but I can't see how to do it in java )
 * 
 * @author David Westhead
 * @version 1.00 21 Apr. 1997
 */
public class IntegerInterval {

    int lower = Integer.MIN_VALUE, upper = Integer.MAX_VALUE;

    /* START constructors */
    /**
     * constructor which takes lower and upper limits of the interval
     * (re-ordered if l>u)
     * 
     * @param l -
     *            the lower limit of the interval
     * @param u -
     *            the upper limit of the interval
     */
    public IntegerInterval(int l, int u) {
        if (l <= u) {
            this.lower = l;
            this.upper = u;
        } else {
            this.lower = u;
            this.upper = l;
        }
    }

    /* END constructors */

    /* START get and set methods */

    public void setLimits(int l, int u) {
        if (l <= u) {
            this.lower = l;
            this.upper = u;
        } else {
            this.lower = u;
            this.upper = l;
        }
    }

    public int getLower() {
        return this.lower;
    }

    public int getUpper() {
        return this.upper;
    }

    /* END get and set methods */

    /* START general methods */
    /**
     * decide whether a number is in the interval ie. lower <= i < upper
     */
    public boolean IsInside(int i) {
        boolean in = false;

        if ((i >= this.lower) && (i < this.upper))
            in = true;

        return in;

    }

    /* END general methods */

}
