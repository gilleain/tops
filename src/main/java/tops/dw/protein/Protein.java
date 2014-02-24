package tops.dw.protein;

import java.util.*;
import java.io.*;
import java.awt.*;


public class Protein {

    private String Name;

    private Vector<SecStrucElement> TopsLinkedLists;

    private Vector<DomainDefinition> DomainDefs;

    public Protein() {
        this.DomainDefs = new Vector<DomainDefinition>();
        this.TopsLinkedLists = new Vector<SecStrucElement>();
        this.Name = "Unknown";
    }
    
    public Protein(String filename) throws FileNotFoundException, IOException,
    	TopsFileFormatException {
    	this(new File(filename));
    }

    public Protein(File TopsFile) throws FileNotFoundException, IOException,
            TopsFileFormatException {

        this();

        FileInputStream fis = new FileInputStream(TopsFile);
        this.ReadTopsFile(fis);

        StringTokenizer st = new StringTokenizer(TopsFile.getName(), ".");

        if (st.hasMoreTokens())
            this.Name = st.nextToken();

    }

    public Protein(BufferedReader br) throws IOException,
            TopsFileFormatException {
        this();
        this.ReadTopsFile(br);
    }

    public Protein(InputStream InStream) throws IOException,
            TopsFileFormatException {
        this();
        this.ReadTopsFile(InStream);
    }

    public Protein(InputStream InStream, String name) throws IOException,
            TopsFileFormatException {
        this(InStream);
        this.Name = name;
    }
    
    public String getName() {
    	return this.Name;
    }
    
    public void setName(String name) {
    	this.Name = name;
    }

    public void AddTopsLinkedList(SecStrucElement s, DomainDefinition d) {

        if (this.TopsLinkedLists == null) {
            this.TopsLinkedLists = new Vector<SecStrucElement>();
            this.DomainDefs = new Vector<DomainDefinition>();
        }
        this.TopsLinkedLists.addElement(s);
        this.DomainDefs.addElement(d);

    }

    public int GetDomainIndex(CATHcode cc) {

        int i;
        int ind = -1;
        Enumeration<DomainDefinition> e = this.DomainDefs.elements();
        CATHcode CompareCathCode;

        for (i = 0; e.hasMoreElements(); i++) {
            CompareCathCode = ((DomainDefinition) e.nextElement()).getCATHcode();
            if (cc.equals(CompareCathCode)) {
                ind = i;
                break;
            }
        }

        return (ind);

    }
    
    public SecStrucElement getRootSSE(String domainName) {
    	for (int i = 0; i < this.DomainDefs.size(); i++) {
    		CATHcode code = ((DomainDefinition) this.DomainDefs.get(i)).getCATHcode();
    		if (code.toString().equals(domainName)) {
    			return (SecStrucElement) this.TopsLinkedLists.get(i);
    		}
    	}
    	return null;
    }

    public Vector<SecStrucElement> GetLinkedLists() {
        return this.TopsLinkedLists;
    }

    public int NumberDomains() {
        return this.DomainDefs.size();
    }

    public Vector<DomainDefinition> GetDomainDefs() {
        return this.DomainDefs;
    }
    
    public SecStrucElement getDomain(int i) {
    	return (SecStrucElement) this.TopsLinkedLists.get(i);
    }

    @Override
    public String toString() {
        return this.Name;
    }

    public void WriteTopsFile(OutputStream os) {

        if (os == null)
            return;

        PrintWriter pw = new PrintWriter(os, true);

        this.WriteTopsHeader(pw);

        if ((this.DomainDefs != null) && (this.TopsLinkedLists != null)) {

            Enumeration<DomainDefinition> ddefs = this.DomainDefs.elements();
            Enumeration<SecStrucElement> lls = this.TopsLinkedLists.elements();

            int i;
            for (i = 0; ddefs.hasMoreElements(); i++) {

                DomainDefinition dd = (DomainDefinition) ddefs.nextElement();
                pw.print("DOMAIN_NUMBER " + i + " " + dd.CathCode);
                Enumeration<IntegerInterval> sfs = dd.getSequenceFragments();
                Enumeration<Integer> fis = dd.getFragmentIndices();
                while (sfs.hasMoreElements()) {
                    int fi = fis.nextElement();
                    IntegerInterval sf = sfs.nextElement();
                    pw.print(" " + fi + " " + sf.getLower() + " " + sf.getUpper());
                }
                pw.print("\n\n");

                SecStrucElement s;
                for (s = (SecStrucElement) lls.nextElement(); s != null; s = s
                        .GetTo()) {
                    s.PrintAsText(pw);
                    pw.print("\n");
                }

            }
        }

    }

    private void WriteTopsHeader(PrintWriter pw) {

        pw.println("##");
        pw.println("## TOPS: tops.dw.protein topology information file");
        pw.println("##");
        pw.println("## Protein code " + this.Name);

        pw.println("## Number of domains " + this.NumberDomains());
        pw.println("##");
        pw.print("\n");

    }

    private void ReadTopsFile(InputStream InStream) throws IOException,
            TopsFileFormatException {
        this.ReadTopsFile(new BufferedReader(new InputStreamReader(InStream)));
    }

    private void ReadTopsFile(BufferedReader br) throws IOException,
            TopsFileFormatException {

        String line = null, FirstToken;
        StringTokenizer st;
        DomainDefinition ddef = null;
        CATHcode ccode = null;

        int[] tmp_int = new int[3];

        int countss = 0;
        int nTokens;
        int x, y, r;
        int j;

        SecStrucElement LastSS = null, CurrentSS = null;

        int i;
        for (i = 1; i < 5; i++) {
            line = br.readLine();
            if (line == null)
                throw new TopsFileFormatException();
            /*
            if (i == 2) {
                if (!line.equals("## TOPS: tops.dw.protein topology information file"))
                    throw new TopsFileFormatException();
            }
            */
        }

        while (line != null) {

            st = new StringTokenizer(line);
            nTokens = st.countTokens();

            if (nTokens > 0) {

                FirstToken = st.nextToken();

                if (FirstToken.equals("DOMAIN_NUMBER")) {
                    if (nTokens < 3)
                        throw new TopsFileFormatException();
                    st.nextToken();
                    ccode = new CATHcode(st.nextToken());
                    ddef = new DomainDefinition(ccode);
                    while (st.hasMoreTokens()) {
                        for (j = 0; j < 3; j++) {
                            if (st.hasMoreTokens()) {
                                try {
                                    tmp_int[j] = Integer.parseInt(st.nextToken());
                                } catch (NumberFormatException e) {
                                    throw new TopsFileFormatException();
                                }
                            } else {
                                throw new TopsFileFormatException();
                            }
                        }
                        ddef.addSequenceFragment(new IntegerInterval(tmp_int[1], tmp_int[2]), tmp_int[0]);
                    }

                    countss = 0;
                    LastSS = null;

                } else if (FirstToken.equals("SecondaryStructureType")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    countss++;
                    CurrentSS = new SecStrucElement();

                    if (countss == 1) {
                        this.AddTopsLinkedList(CurrentSS, ddef);
                    }

                    CurrentSS.SetFrom(LastSS);
                    if (LastSS != null)
                        LastSS.SetTo(CurrentSS);
                    LastSS = CurrentSS;

                    CurrentSS.Type = st.nextToken();
                    CurrentSS.SymbolNumber = countss - 1;

                } else if (FirstToken.equals("Direction")) {
                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    CurrentSS.Direction = st.nextToken();
                } else if (FirstToken.equals("Label")) {
                    if (nTokens > 1) {
                        CurrentSS.Label = st.nextToken();
                    } else {
                        CurrentSS.Label = null;
                    }
                } else if (FirstToken.equals("Colour")) {
                    for (j = 0; j < 3; j++) {
                        tmp_int[j] = 255;
                        if (st.hasMoreTokens()) {
                            try {
                                tmp_int[j] = Integer.parseInt(st.nextToken());
                            } catch (NumberFormatException e) {
                            }
                        }
                        if (tmp_int[j] < 0)
                            tmp_int[j] = 0;
                        if (tmp_int[j] > 255)
                            tmp_int[j] = 255;
                    }
                    CurrentSS.Colour = new Color(tmp_int[0], tmp_int[1], tmp_int[2]);
                } else if (FirstToken.equals("Fixed")) {
                    int fs;
                    if (nTokens > 1) {
                        fs = -1;
                        try {
                            fs = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException e) {
                            throw new TopsFileFormatException();
                        }
                    } else {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetFixedIndex(fs);
                } else if (FirstToken.equals("Next")) {
                    int ns;
                    if (nTokens > 1) {
                        ns = -1;
                        try {
                            ns = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException e) {
                            throw new TopsFileFormatException();
                        }
                    } else {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetNextIndex(ns);
                } else if (FirstToken.equals("FixedType")) {
                    if (nTokens > 1) {
                        CurrentSS.SetFixedType(st.nextToken());
                    } else {
                        CurrentSS.SetFixedType("UNKNOWN");
                    }
                } else if (FirstToken.equals("BridgePartner")) {
                    int bp;
                    while (st.hasMoreTokens()) {
                        try {
                            bp = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                        CurrentSS.AddBridgePartner(bp);
                    }
                } else if (FirstToken.equals("BridgePartnerSide")) {
                    while (st.hasMoreTokens()) {
                        CurrentSS.AddBridgePartnerSide(st.nextToken());
                    }
                } else if (FirstToken.equals("BridgePartnerType")) {
                    while (st.hasMoreTokens()) {
                        CurrentSS.AddBridgePartnerType(st.nextToken());
                    }
                } else if (FirstToken.equals("Neighbour")) {
                    int nb;
                    while (st.hasMoreTokens()) {
                        try {
                            nb = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                        CurrentSS.AddNeighbour(nb);
                    }
                } else if (FirstToken.equals("SeqStartResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    int res;
                    try {
                        res = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetSeqStartResidue(res);

                } else if (FirstToken.equals("SeqFinishResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    int res;
                    try {
                        res = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetSeqFinishResidue(res);

                } else if (FirstToken.equals("Chirality")) {

                    int chi = 0;
                    if (nTokens > 1) {
                        try {
                            chi = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetChirality(chi);

                } else if (FirstToken.equals("AxesStartPoint")) {
                    if (nTokens != 4)
                        throw new TopsFileFormatException();
                    float sp[] = new float[3];
                    int ct;
                    for (ct = 0; st.hasMoreTokens() && ct < 3; ct++) {
                        sp[ct] = 0.0f;
                        try {
                            sp[ct] = Float.valueOf(st.nextToken()).floatValue();
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetAxesStartPoint(sp[0], sp[1], sp[2]);
                } else if (FirstToken.equals("AxesFinishPoint")) {
                    if (nTokens != 4)
                        throw new TopsFileFormatException();
                    float fp[] = new float[3];
                    int ct;
                    for (ct = 0; st.hasMoreTokens() && ct < 3; ct++) {
                        fp[ct] = 0.0f;
                        try {
                            fp[ct] = Float.valueOf(st.nextToken()).floatValue();
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetAxesFinishPoint(fp[0], fp[1], fp[2]);
                } else if (FirstToken.equals("AxisLength")) {
                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    float al;
                    try {
                        al = Float.valueOf(st.nextToken()).floatValue();
                    } catch (NumberFormatException nfe) {
                        throw new TopsFileFormatException();
                    }
                    CurrentSS.SetAxisLength(al);
                } else if (FirstToken.equals("CartoonX")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    x = Integer.parseInt(st.nextToken());
                    CurrentSS.PlaceElementX(x);

                } else if (FirstToken.equals("CartoonY")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    y = Integer.parseInt(st.nextToken());
                    CurrentSS.PlaceElementY(y);

                } else if (FirstToken.equals("SymbolRadius")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();

                    r = Integer.parseInt(st.nextToken());
                    CurrentSS.SetSymbolRadius(r);

                } else if (FirstToken.equals("ConnectionTo")) {

                    if ((nTokens % 2) != 1)
                        throw new TopsFileFormatException();

                    while (st.hasMoreTokens()) {
                        x = Float.valueOf(st.nextToken()).intValue();
                        y = Float.valueOf(st.nextToken()).intValue();
                        CurrentSS.AddConnectionTo(x, y);
                    }

                } else if (FirstToken.equals("PDBStartResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    CurrentSS.PDBStartResidue = Integer
                            .parseInt(st.nextToken());

                } else if (FirstToken.equals("PDBFinishResidue")) {

                    if (nTokens != 2)
                        throw new TopsFileFormatException();
                    CurrentSS.PDBFinishResidue = Integer.parseInt(st
                            .nextToken());

                } else if (FirstToken.equals("Chain")) {
                    if (st.hasMoreTokens()) {
                        CurrentSS.Chain = st.nextToken();
                    } else {
                        CurrentSS.Chain = " ";
                    }
                } else if (FirstToken.equals("Fill")) {

                    int f = 0;
                    if (nTokens > 1) {
                        try {
                            f = Integer.parseInt(st.nextToken());
                        } catch (NumberFormatException nfe) {
                            throw new TopsFileFormatException();
                        }
                    }
                    CurrentSS.SetFill(f);

                }

            }

            line = br.readLine();

        }

        Enumeration<SecStrucElement> lls = this.GetLinkedLists().elements();
        while (lls.hasMoreElements()) {
            SecStrucElement ll = (SecStrucElement) lls.nextElement();
            this.FixedFromFixedIndex(ll);
            this.NextFromNextIndex(ll);
        }

    }

    private void FixedFromFixedIndex(SecStrucElement root) {

        if (!root.IsRoot())
            return;

        SecStrucElement s;
        for (s = root; s != null; s = s.GetTo()) {
            s.SetFixed(this.getListElement(root, s.GetFixedIndex()));
        }

    }

    private void NextFromNextIndex(SecStrucElement root) {

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
