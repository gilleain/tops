package tops.dw.protein;

import java.util.*;

/**
 * class representing the definition of a tops.dw.protein domain
 * 
 * @author David Westhead
 * @version 1.00 21 Apr. 1997
 */
public class DomainDefinition {

    CATHcode CathCode = new CATHcode("1xxx00");

    Vector<IntegerInterval> SequenceFragments = new Vector<IntegerInterval>();

    Vector<Integer> FragmentIndices = new Vector<Integer>();

    /* START constructors */
    public DomainDefinition(CATHcode c) {
        this.CathCode = c;
    }

    public DomainDefinition(char chain) {
        this.CathCode = new CATHcode("1xxx" + chain + "0");
    }

    /* END constructors */

    /* START get/set/add methods */

    public CATHcode getCATHcode() {
        return this.CathCode;
    }

    public void addSequenceFragment(IntegerInterval Frag, int StartIndex) {
        this.SequenceFragments.addElement(Frag);
        this.FragmentIndices.addElement(new Integer(StartIndex));
    }

    public Enumeration<IntegerInterval> getSequenceFragments() {
        return this.SequenceFragments.elements();
    }

    public Enumeration<Integer> getFragmentIndices() {
        return this.FragmentIndices.elements();
    }

    public char getChain() {
        char c = '\0';
        if (this.CathCode != null)
            c = this.CathCode.getChain();
        return c;
    }
    
    public int getDomainID() {
        return this.CathCode.getDomain();
    }

    @Override
    public String toString() {
        String s = null;
        if (this.CathCode != null)
            s = this.CathCode.toString();
        return s;
    }

}
