package tops.dw.protein;

import java.util.Enumeration;
import java.util.Vector;


public class Protein {

    private String name;

    private Vector<SecStrucElement> topsLinkedLists;

    private Vector<DomainDefinition> domainDefs;

    public Protein() {
        this.domainDefs = new Vector<DomainDefinition>();
        this.topsLinkedLists = new Vector<SecStrucElement>();
        this.name = "Unknown";
    }
    
    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    public void addTopsLinkedList(SecStrucElement s, DomainDefinition d) {

        if (this.topsLinkedLists == null) {
            this.topsLinkedLists = new Vector<SecStrucElement>();
            this.domainDefs = new Vector<DomainDefinition>();
        }
        this.topsLinkedLists.addElement(s);
        this.domainDefs.addElement(d);

    }

    public int getDomainIndex(CATHcode cc) {

        int i;
        int ind = -1;
        Enumeration<DomainDefinition> e = this.domainDefs.elements();
        CATHcode compareCathCode;

        for (i = 0; e.hasMoreElements(); i++) {
            compareCathCode = e.nextElement().getCATHcode();
            if (cc.equals(compareCathCode)) {
                ind = i;
                break;
            }
        }

        return (ind);

    }
    
    public SecStrucElement getRootSSE(String domainName) {
    	for (int i = 0; i < this.domainDefs.size(); i++) {
    		CATHcode code = this.domainDefs.get(i).getCATHcode();
    		if (code.toString().equals(domainName)) {
    			return this.topsLinkedLists.get(i);
    		}
    	}
    	return null;
    }

    public Vector<SecStrucElement> getLinkedLists() {
        return this.topsLinkedLists;
    }

    public int numberDomains() {
        return this.domainDefs.size();
    }

    public Vector<DomainDefinition> getDomainDefs() {
        return this.domainDefs;
    }
    
    public SecStrucElement getDomain(int i) {
    	return this.topsLinkedLists.get(i);
    }

    @Override
    public String toString() {
        return this.name;
    }

   

    public void fixedFromFixedIndex(SecStrucElement root) {

        if (!root.IsRoot())
            return;

        SecStrucElement s;
        for (s = root; s != null; s = s.GetTo()) {
            s.SetFixed(this.getListElement(root, s.GetFixedIndex()));
        }

    }

    public void nextFromNextIndex(SecStrucElement root) {

        if (!root.IsRoot())
            return;

        SecStrucElement s;
        for (s = root; s != null; s = s.GetTo()) {
            s.SetNext(this.getListElement(root, s.GetNextIndex()));
        }

    }

    private SecStrucElement getListElement(SecStrucElement root, int index) {

        if (!root.IsRoot())
            return null;

        SecStrucElement s;
        int i;
        for (s = root, i = 0; (s != null) && (i < index); s = s.GetTo(), i++)
            ;

        if (i != index)
            return null;

        return s;

    }

}
