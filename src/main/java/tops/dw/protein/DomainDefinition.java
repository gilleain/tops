package tops.dw.protein;

import java.util.*;

/**
 * class representing the definition of a tops.dw.protein domain
 * 
 * @author David Westhead
 * @version 1.00 21 Apr. 1997
 */
public class DomainDefinition {

    private CATHcode cathCode = new CATHcode("1xxx00");

    private Vector<IntegerInterval> sequenceFragments = new Vector<IntegerInterval>();

    public DomainDefinition(CATHcode c) {
        this.cathCode = c;
    }

    public DomainDefinition(char chain) {
        this.cathCode = new CATHcode("1xxx" + chain + "0");
    }

    public CATHcode getCATHcode() {
        return this.cathCode;
    }

    public void addSequenceFragment(IntegerInterval frag) {
        this.sequenceFragments.addElement(frag);
    }

    public Enumeration<IntegerInterval> getSequenceFragments() {
        return this.sequenceFragments.elements();
    }

    public char getChain() {
        char c = '\0';
        if (this.cathCode != null)
            c = this.cathCode.getChain();
        return c;
    }
    
    public int getDomainID() {
        return this.cathCode.getDomain();
    }

    @Override
    public String toString() {
        String s = null;
        if (this.cathCode != null)
            s = this.cathCode.toString();
        return s;
    }

}
