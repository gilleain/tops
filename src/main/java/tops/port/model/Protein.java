package tops.port.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.DomainDefinition.DomainType;

public class Protein {

    public enum ErrorType {
        NO_DOMAIN_ERRORS, DOMAIN_CHAIN_ERROR, DOMAIN_RESIDUE_ERROR, DOMAIN_RANGE_OVERLAP_ERROR
    }
    
    public class DomDefError {
        public String ErrorString;
        public ErrorType ErrorType;
    }
    
    private List<Chain> chains;
    private String proteinName;
    public String proteinCode;
    public int numberOfDomains;
    public List<DomainDefinition> domains;
    
    public Protein(String ProteinCode) {
        this.proteinName = "";
        this.proteinCode = ProteinCode;
        this.numberOfDomains = 1;

        this.chains = new ArrayList<Chain>();
        this.domains = new ArrayList<DomainDefinition>();
    }
    
    public void setName(String name) {
        this.proteinName = name;
    }
    
    public DomainDefinition getDomain(int i) {
        return domains.get(i);
    }
    
    public int numberOfDomains() {
        return this.domains.size();
    }
    
    public void addChain(Chain chain) {
        this.chains.add(chain);
    }
    
    public List<Chain> getChains() {
        return this.chains;
    }

    public static Protein createFromTopsFile(String filename) throws IOException {
        File topsFile = new File(filename);
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(topsFile));
        String lineBuffer = reader.readLine();
        while (lineBuffer != null) {
            lines.add(lineBuffer);
            lineBuffer = reader.readLine();
        }
        reader.close();

        String pdbid = filename.substring(0, 4);
        Protein protein = new Protein(pdbid);
        SSE sse = null;
        Chain chain = null;
        for (String line : lines) {
            if (line.charAt(0) == '#' || line.length() < 2) continue;
            String[] bits = line.split("\\s+"); 
            String keyword= bits[0];
            List<String> values = new ArrayList<String>();
            for (int i = 1; i < bits.length; i++) { values.add(bits[i]); }
            // print "keyword = [%s], value(s) = [%s]" % (keyword, values)
            if (values.isEmpty()) values = null;
            if (keyword.equals("DOMAIN_NUMBER")) {
                String cathcode = values.get(1);
                protein.proteinCode = cathcode.substring(0, 4);
                char chain_id = cathcode.charAt(4);
                char domain_id = cathcode.charAt(5);
                chain = new Chain(chain_id);
                protein.chains.add(chain);
            } else if (keyword.equals("SecondaryStructureType")) {
                if (sse != null) chain.addSSE(sse);
                sse = new SSE(values.get(0).charAt(0));
            } else if (keyword.equals("FixedType") && values.size() > 0) {
                sse.setFixedType(values.get(0));
                //print "setting fixed type: %s to %i" % (values[0], sse.FixedType)
            } else if (keyword.equals("AxisStartPoint") || keyword.equals("AxisFinishPoint")) {
//                if (values == null) { 
//                    sse.__dict__[keyword] = null
//                    continue;
//                }
//                sse.__dict__[keyword] = [float(v) for v in values]
            } else {
                if (values != null && values.size() == 1 && !keyword.contains("BridgePartner") && !keyword.equals("Neighbour")) {
//                    value = values[0];
//                    if (value.isalnum() && !value.isdigit()) {
//                        sse.__dict__[keyword] = value;
//                    } else {
//                        try {
//                            sse.__dict__[keyword] = int(value);
//                        } catch (ValueError v) {
//                            sse.__dict__[keyword] = float(value);
//                        }
//                    }
                } else {
//                    sse.__dict__[keyword] = values;
                }
            }
        }
        chain.addSSE(sse);    // add the last one
        return protein;
    }

    public String topsHeader() {
        String header = "##\n## TOPS: protein topology information file\n##\n";
        header += String.format("## Protein code %s\n", this.proteinCode);
        header += String.format("## Number of domains %d\n##\n", this.numberOfDomains);
        return header;
    }

    public String toTopsFile() {
        StringBuffer chainStringList = new StringBuffer(this.topsHeader());
        for (Chain chain : this.chains) {
            chainStringList.append(chain.topsHeader(this.proteinCode));
            chainStringList.append(chain.toTopsFile());
            chainStringList.append("\n\n");
        }
        return chainStringList.toString();
    }
        
    public String toString() {
        StringBuffer sb = new StringBuffer(this.proteinCode);
        sb.append("\n");
        for (Chain chain : this.chains) {
            sb.append(chain.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    
    ///////////////////////
    
    

    /*
      This function forces consistency between domain definitions and secondary structure information 
      in the protein data structure.
      Bridge partner relationships which cross domain boundaries are removed 
      ss elements which cross domain boundaries are limited to the domain they are mostly in
    */
    public void ForceConsistent( Protein p ) {

        for (Chain chain : this.chains) {
            for (int i = 0; i < chain.sequenceLength(); i++) {
                if ( chain.isExtended(i) ) {
    
                    int Dom = ResidueDomain(i);
    
                    int bpl = chain.getLeftBridgePartner(i);
                    int bpr = chain.getRightBridgePartner(i);
    
                    if ( (ResidueDomain(bpl) != Dom)||(Dom<0) ) {
                        chain.removeLeftBridge(i);
                    }
    
                    if ( (ResidueDomain(bpr) != Dom)||(Dom<0) ) {
                        chain.removeRightBridge(i);
                    }
                }
            }
        }

        for (Chain chain : this.chains) {
            chain.forceConsistent(p);
        }
    }


    public boolean segmentsOverlap(char[] Seg1Chains, int[] Seg1Range, char[] Seg2Chains, int[] Seg2Range) {

        if (Seg1Chains[0] == Seg1Chains[1]) {

            if (Seg2Chains[0] == Seg2Chains[1]) {
                if (Seg2Chains[0] == Seg1Chains[0]) {

                    if ((Seg2Range[0] >= Seg1Range[0]) && (Seg2Range[0] <= Seg1Range[1]))
                        return true;
                    if ((Seg2Range[1] >= Seg1Range[0]) && (Seg2Range[1] <= Seg1Range[1]))
                        return true;
                } else {

                    if (Seg2Chains[0] == Seg1Chains[0]) {
                        if (Seg2Range[0] <= Seg1Range[1])
                            return true;
                    } else if ((Seg2Chains[1] == Seg1Chains[0])) {
                        if (Seg2Range[1] >= Seg1Range[0])
                            return true;
                    }
                }

            }
        } else {

            if (Seg2Chains[0] == Seg2Chains[1]) {
                if (Seg1Chains[0] == Seg2Chains[0]) {
                    if (Seg1Range[0] <= Seg2Range[1])
                        return true;
                }

            } else if ((Seg1Chains[1] == Seg2Chains[0])) {
                if (Seg1Range[1] >= Seg2Range[0])
                    return true;
            } else {

                if (Seg1Chains[0] == Seg2Chains[0])
                    return true;
                if (Seg1Chains[1] == Seg2Chains[1])
                    return true;
                if (Seg1Chains[1] == Seg2Chains[0]) {
                    if (Seg1Range[1] >= Seg2Range[0])
                        return true;
                }
                if (Seg1Chains[0] == Seg2Chains[1]) {
                    if (Seg1Range[0] <= Seg2Range[1])
                        return true;
                }
            }
        }
        return false;
    }
    
  
    public DomDefError checkDomainDefs() {

        char dm_chain = ' ';

        char[] Seg1Chains = new char[2];
        char[] Seg2Chains = new char[2];
        int[] Seg1Range = new int[2];
        int[] Seg2Range = new int[2];

        DomDefError ddep = new DomDefError();

        ddep.ErrorType = ErrorType.NO_DOMAIN_ERRORS;
        boolean found = false;
        if (numberOfDomains > 0) {

            /* check each domain in turn for chains not in protein */
            for (int i = 0; i < numberOfDomains; i++) {

                DomainDefinition dm = domains.get(i);
                for (int j = 0; j < dm.numberOfSegments; j++) {
                    for (int k = 0; k < 2; k++) {
                        dm_chain = dm.segmentChains[k][j];
                        found = false;
                        for (int l = 0; l < chains.size(); l++) {
                            if (dm_chain == chains.get(l).getName()) {
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if (!found) {
                    ddep.ErrorType = ErrorType.DOMAIN_CHAIN_ERROR;
                    System.out.println(ddep.ErrorString + String.format(
                            "Chain %c for domain definition not found in protein",
                            dm_chain));
                    return ddep;
                }
            }
        }

        /*
         * check each domain in turn for segments including residues not in the
         * protein
         */
        for (int i = 0; i < numberOfDomains; i++) {

            DomainDefinition dm = domains.get(i);
            if (dm.domainType == DomainType.SEGMENT_SET) {

                for (int j = 0; j < dm.numberOfSegments; j++) {

                    for (int k = 0; k < 2; k++) {

                        if (GetSequenceNumber(dm.segmentIndices[k][j], dm.segmentChains[k][j]) < 0) {

                            ddep.ErrorType = ErrorType.DOMAIN_RESIDUE_ERROR;
                            System.out.println(ddep.ErrorString + 
                                    String.format("Residue %c %d for domain definition not found in protein",
                                    dm.segmentChains[k][j],
                                    dm.segmentIndices[k][j]));
                            return ddep;
                        }
                    }
                }
            }

            /* cross check segments for overlaps */

            for (i = 0; i < numberOfDomains; i++) {

                DomainDefinition dm1 = domains.get(i);
                if (dm1.domainType == DomainType.SEGMENT_SET) {

                    for (int j = i; j < numberOfDomains; j++) {

                        DomainDefinition dm2 = domains.get(j);
                        if (dm2.domainType == DomainType.SEGMENT_SET) {

                            for (int l = 0; l < dm1.numberOfSegments; l++) {

                                for (int k = 0; k < 2; k++)
                                    Seg1Chains[k] = dm1.segmentChains[k][l];
                                for (int k = 0; k < 2; k++)
                                    Seg1Range[k] = dm1.segmentIndices[k][l];

                                for (int m = 0; m < dm2.numberOfSegments; m++) {

                                    for (int k = 0; k < 2; k++)
                                        Seg2Chains[k] = dm2.segmentChains[k][m];
                                    for (int k = 0; k < 2; k++)
                                        Seg2Range[k] = dm2.segmentIndices[k][m];

                                    if ((dm1 == dm2) && (l == m))
                                        continue;

                                    if (segmentsOverlap(Seg1Chains, Seg1Range,
                                            Seg2Chains, Seg2Range)) {

                                        ddep.ErrorType = ErrorType.DOMAIN_RANGE_OVERLAP_ERROR;
                                        System.out.println(ddep.ErrorString
                                                + " Overlaps were found in residue ranges specifying domains");
                                        return ddep;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ddep;
    }

    public boolean ResIsInDomain( int PDBRes, char PDBChain, DomainDefinition Domain) {
        if ( Domain == null) return false;

        for ( int i=0 ; i<Domain.numberOfSegments ; i++) {

            if ( Domain.domainType == DomainType.SEGMENT_SET ) {
                if ( (PDBChain==Domain.segmentChains[0][i])&&(PDBChain==Domain.segmentChains[1][i]) ) {
                    if ( (PDBRes>=(Domain.segmentIndices[0][i]))&&(PDBRes<=(Domain.segmentIndices[1][i])) ) return true;
                } else if ( PDBChain==Domain.segmentChains[0][i] ) {
                    if ( PDBRes>=(Domain.segmentIndices[0][i]) ) return true;
                } else if ( PDBChain==Domain.segmentChains[1][i] ) {
                    if ( PDBRes<=(Domain.segmentIndices[1][i]) ) return true;
                }
            } else if ( Domain.domainType == DomainType.CHAIN_SET ) {
                if ( (PDBChain==Domain.segmentChains[0][i])||(PDBChain==Domain.segmentChains[1][i]) ) return true;
            }
        }
        return false;
    }


    public int ResidueDomain(int Residue) {
        for (Chain chain : chains) {
            if ((Residue >= chain.sequenceLength()) || (Residue < 0)) {
                return -1;
            }
    
            int PDBRes = chain.getPDBIndex(Residue);
            char PDBChain = chain.getName();
    
            for (int i = 0; i < numberOfDomains; i++) {
                if (ResIsInDomain(PDBRes, PDBChain, domains.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    public boolean isChainRepresented(char chain) {
    
      for (int i=0 ; i<numberOfDomains ; i++ ) {
          if (chain == domains.get(i).domainCATHCode.charAt(4)) {
              return true;
          }
      }
    
      return false;
     }
     
    public int GetSequenceNumber(int PDBIndex, char Chain) {
//        for (int  i=0 ;i<sequenceLength ;i++) {
//            if ( (PDBIndices[i]==PDBIndex) && (GetChainIdentifier(i)==Chain) ) {
//                return i;
//            }
//        }
        return -1;  // XXX TODO
    }
    

    public DomainId SSIsInDomain(SSE p, DomainDefinition Domain) {
        for (Chain chain : this.chains) {
            DomainId domainId = SSIsInDomain(chain, p, Domain);
            if (domainId.segment > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }

    public DomainId SSIsInDomain(Chain chain, SSE p, DomainDefinition Domain) {
        int ExclusionType = -1;
        int Segment = -1;

        /* First check the element passed in */
        Segment = SingleSSIsInDomain(p, Domain);
        if (Segment > 0) {
            ExclusionType = 0;
            return new DomainId(Segment, -1, ExclusionType);
        }

        /* Now check the whole associated fixed list */
        for (p = chain.findFixedStart(p); p != null; p = p.getFixed()) {
            Segment = SingleSSIsInDomain(p, Domain);
            if (Segment > 0) {
                ExclusionType = 1;
                return new DomainId(Segment, -1, ExclusionType);
            }
        }
        return new DomainId(Segment, -1, ExclusionType);
    }
    

    public int SingleSSIsInDomain(SSE p, DomainDefinition Domain) {

        int i;
        char chain = p.Chain;
        int PDBStart = p.sseData.PDBStartResidue;
        int PDBFinish = p.sseData.PDBFinishResidue;

        for (i = 0; i < Domain.numberOfSegments; i++) {

            if (Domain.domainType == DomainDefinition.DomainType.SEGMENT_SET) {
                if ((chain == Domain.segmentChains[0][i])
                        && (chain == Domain.segmentChains[1][i])) {
                    if ((PDBStart >= (Domain.segmentIndices[0][i]))
                            && (PDBFinish <= (Domain.segmentIndices[1][i]))) {
                        return i;
                    }
                } else if (chain == Domain.segmentChains[0][i]) {
                    if (PDBStart >= (Domain.segmentIndices[0][i])) {
                        return i;
                    }
                } else if (chain == Domain.segmentChains[1][i]) {
                    if (PDBFinish <= (Domain.segmentIndices[1][i])) {
                        return i;
                    }
                }
            } else
                if (Domain.domainType == DomainDefinition.DomainType.CHAIN_SET) {
                if (chain == Domain.segmentChains[0][i] || chain == Domain.segmentChains[1][i]) {
                    return i;
                }
            }

        }
        return -1;
    }

    /** 
     * Function to find the domain associated with a SS element
     */
    public DomainId FindDomain(SSE p) {
        for (Chain chain : this.chains) {
            DomainId domainId = FindDomain(chain, p);
            if (domainId.segment > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }
    
    public DomainId FindDomain(Chain chain, SSE p) {
        for (int i = 0; i < domains.size(); i++) {
            DomainId result = SSIsInDomain(chain, p, domains.get(i));
            if (result.segment > -1) {
                return result;
            }
        }
        return new DomainId(-1, -1, -1);
    }
    
    public class DomainId {
        public int segment;
        public int domain;
        public int exclusionType;
        
        public DomainId(int segment, int domain, int exclusionType) {
            this.segment = segment;
            this.domain = domain;
            this.exclusionType = exclusionType;
        }
    }
    

    public void DefaultDomains(char ChainToPlot) {
        /*
         * In the case of default domains being used we cannot tolerate separate
         * chains with the same chain identifier
         */
        /* This routine removes the problem */
        // RemoveDuplicateChains(protein); XXX what to do in this case?

        if (ChainToPlot == '*') { // XXX used to be 'ALL'

            DomainDefinition domain = new DomainDefinition(
                    DomainType.CHAIN_SET);

            domain.domainCATHCode = this.proteinCode;
            domain.numberOfSegments = this.getChains().size();

            int i = 0;
            for (Chain chain : this.chains) {
                char chainId = chain.getName();
                domain.segmentChains[0][i] = chainId;
                domain.segmentChains[1][i] = chainId;
                domain.segmentIndices[0][i] = chain.getStartIndex();
                domain.segmentIndices[1][i] = chain.getFinishIndex();
                i++;
            }
            domains.add(domain);
        } else {
            for (Chain chain : this.chains) {
                char chainId = chain.getName();
                if (!isChainRepresented(chainId)) {
                    DomainDefinition domain = new DomainDefinition(
                            DomainType.CHAIN_SET);

                    domain.domainCATHCode = this.proteinCode.substring(0, 4) + chainId + '0';
                    domain.numberOfSegments = 1;
                    domain.segmentChains[0][0] = chainId;
                    domain.segmentChains[1][0] = chainId;
                    domain.segmentIndices[0][0] = chain.getStartIndex();
                    domain.segmentIndices[1][0] = chain.getFinishIndex();
                    domains.add(domain);
                }
            }
        }
    }

    public void InitPlotFragInfo(PlotFragInformation pfi) {
        pfi.NFrags = 0;
        for (int i = 0; i < PlotFragInformation.MAX_PLOT_FRAGS; i++) {
            pfi.FragDomain[i] = -1;
            for (int j = 0; j < 2; j++) {
                pfi.FragChainLims[i][j] = '\0';
                pfi.FragResLims[i][j] = -1;
            }
        }
    }

    public char GetChain(String cathCode) {
        return cathCode.length() < 4 ? 0 : cathCode.charAt(3);
    }

    public char GetDomainNumber(String cathCode) {
        return cathCode.length() < 6 ? 0 : cathCode.charAt(5);
    }

    public List<Integer> FixDomainsToPlot(char ChainToPlot, int DomainToPlot) {

        int ndp;

        /*
         * if ChainToPlot==ALL or ( ChainToPlot = NULL and DomainToPlot=0 ) then
         * all domains set up by by ReadDomBoundaryFile or DefaultDomains should
         * be plotted
         */
        List<Integer> DomainsToPlot = new ArrayList<Integer>();
        if ((ChainToPlot == 0 && DomainToPlot == 0) || ChainToPlot == '*') {
            ndp = domains.size();
            for (int i = 0; i < ndp; i++) {
                DomainsToPlot.add(i);
            }
        } else {
            for (int i = 0; i < domains.size(); i++) {
                DomainDefinition domain = domains.get(i);
                if (ChainToPlot == 0
                        || ChainToPlot == GetChain(domain.domainCATHCode)) {
                    if (DomainToPlot == 0 
                            || DomainToPlot == GetDomainNumber(domain.domainCATHCode)) {
                        DomainsToPlot.add(i);
                    }
                }
            }
        }

        if (DomainsToPlot.isEmpty()) {
            System.out.println("Tops Error: no domains found to plot\n");
            System.err.println("Tops Error: no domains found to plot\n");
        }

        return DomainsToPlot;
    }

    void MoveNextToDom(Protein protein, SSE q, DomainDefinition Domain) {
        for (SSE p = q.Next; p != null; p = p.Next) {
            DomainId result = protein.SSIsInDomain(p, Domain);
            if (result.segment > 0) {
                q.Next = p;
                return;
            }
        }
        q.Next = null;
    }

    /*
     * Function to set up a new linked list corresponding to a given domain XXX
     * now returns the new root
     */
    public Cartoon SetDomain(SSE OriginalRoot, DomainDefinition domain) {

        int numberStructures;
        int domBreakNum = 0;

        Map<SSE, SSE> copyTable = new HashMap<SSE, SSE>();

        SSE lastNextListMem, newRoot, s;

        /* this loop decides which structures to copy and copies attributes */
        boolean FIRST = true;
        lastNextListMem = null;
        numberStructures = -1;
        newRoot = null;
        SSE p = null;
        for (SSE r = OriginalRoot.To; r != null; r = r.To) {

            DomainId domainId = SSIsInDomain(r, domain);
            if (domainId.segment != -1) {

                /*
                 * add an N terminus if we're starting a new piece of continuous
                 * peptide chain
                 */
                if ((r.domainBreakType == DomainBreakType.N_DOM_BREAK)
                        || (r.domainBreakType == DomainBreakType.NC_DOM_BREAK)) {

                    domBreakNum = r.DomainBreakNumber;

                    numberStructures++;
                    SSE q = new SSE('N');

                    if (FIRST) {
                        newRoot = q;
                        FIRST = false;
                    }
                    q.setSymbolPlaced(true);
                    q.setSymbolNumber(numberStructures);

                    q.setLabel("N" + domBreakNum);

                    if (lastNextListMem != null) {
                        lastNextListMem.Next = q;
                    }
                    lastNextListMem = q;
                    q.From = p;
                    if (p != null) {
                        p.To = q;
                    }
                    p = q;
                }

                /* add an ss element equivalent of r */
                numberStructures++;
                SSE q = new SSE(r);
                q.setSymbolNumber(numberStructures);
                copyTable.put(q, r);

                /* special action for the Next pointer */
                if (r.Next != null) {
                    lastNextListMem.Next = q;
                    lastNextListMem = q;
                }

                /* sort out from and to */
                q.From = p;
                p.To = q;
                p = q;

                /*
                 * cancel any chirality of an element separated from its chiral
                 * partner by a domain break
                 */
                for (Chain chain : getChains()) {
                    SSE cp = ChiralityCalculator.topsChiralPartner(chain, r);
                    if ((r.Chirality != Hand._no_hand) && (cp != null)) {
                        for (s = r; s != cp; s = s.To) {
                            if ((s.domainBreakType == DomainBreakType.C_DOM_BREAK)
                                    || (s.domainBreakType == DomainBreakType.NC_DOM_BREAK)) {
                                q.Chirality = Hand._no_hand;
                                break;
                            }
                        }
                    }
                }

                /*
                 * add a C terminus if we're ending a piece of continuous
                 * peptide chain
                 */
                if ((r.domainBreakType == DomainBreakType.C_DOM_BREAK)
                        || (r.domainBreakType == DomainBreakType.NC_DOM_BREAK)) {
                    numberStructures++;
                    domBreakNum++;
                    q = new SSE('C');
                    q.setSymbolPlaced(true);
                    q.setSymbolNumber(numberStructures);
                    q.setLabel("C" + domBreakNum);

                    /* special action for Next pointer */
                    lastNextListMem.Next = q;
                    lastNextListMem = q;

                    q.From = p;
                    q.To = null;
                    p.To = q;
                    p = q;
                }
            }
        }

        /*
         * if there are any elements on the new list some pointer type structure
         * members in the new list refer the old list - use CopyTable to convert
         * to equivalent in new list
         */
        if (newRoot != null) {
            for (SSE q = newRoot; q != null; q = q.To) {
                if (q.hasFixed())
                    q.setFixed(copyTable.get(q.getFixed()));
                for (BridgePartner bridgePartner : q.getBridgePartners()) {
                    bridgePartner.partner = copyTable.get(bridgePartner.partner);
                }
                // for (SSE q.Neighbours)
                // q.Neighbour[i] = GetFromCopyTab(q.Neighbour[i],CopyTable);
                // }
            }
        }

        /* Now re-set the root */
        return new Cartoon(newRoot);
    }

    /*
     * Function to set up DomainBreakNumbers in the master linked list
     */
    public void setDomBreaks(SSE Root, PlotFragInformation PlotFragInf) {

        int Count = 0;
        int nf = 0;
        int Dom, LastDom;
        int LastSegment;
        SSE sse, LastCTerm;

        for (sse = Root; sse != null; sse = sse.To)
            sse.DomainBreakNumber = 0;
        for (sse = Root; sse != null; sse = sse.To)
            sse.domainBreakType = DomainBreakType.NOT_DOM_BREAK;

        InitPlotFragInfo(PlotFragInf);

        /* advance to the first ss element in a real domain */
        int segment = -1;
        for (sse = Root.To; sse != null && segment < 0; sse = sse.To) {
            segment = FindDomain(sse).segment;
        }

        if (sse == null)
            return;

        Count++;
        sse.DomainBreakNumber = Count;
        sse.domainBreakType = DomainBreakType.N_DOM_BREAK;

        DomainId result = FindDomain(sse);
        LastDom = -1;
        LastSegment = -1;
        if (result.segment > -1) {
            LastDom = result.domain;
            LastSegment = result.segment;
        }

        nf = 1;
        if (nf <= PlotFragInformation.MAX_PLOT_FRAGS) {
            PlotFragInf.NFrags = nf;
            PlotFragInf.FragChainLims[nf - 1][0] = sse.Chain;
            PlotFragInf.FragResLims[nf - 1][0] = sse.sseData.PDBStartResidue;
            PlotFragInf.FragDomain[nf - 1] = LastDom + 1;
            /*
             * this will be done later in a Protein.c func. it is easier here
             * for now
             */
            domains.get(LastDom).segmentStartIndex[LastSegment] = nf;
        }

        LastCTerm = sse;

        while (sse != null) {
            sse = sse.To;
            DomainId result2 = FindDomain(sse);
            Dom = result2.domain;

            if ((Dom != LastDom) || (segment != LastSegment)) {

                DomainId did = SSIsInDomain(sse, domains.get(LastDom));

                if ((did.exclusionType == 0) || (Dom == LastDom)) {

                    Count++;

                    if (LastCTerm.DomainBreakNumber == 0) {
                        LastCTerm.DomainBreakNumber = Count;
                        LastCTerm.domainBreakType = DomainBreakType.C_DOM_BREAK;
                    } else {
                        LastCTerm.domainBreakType = DomainBreakType.NC_DOM_BREAK;
                    }

                    PlotFragInf.FragChainLims[nf - 1][1] = LastCTerm.Chain;
                    PlotFragInf.FragResLims[nf - 1][1] = LastCTerm.sseData.PDBFinishResidue;

                    Dom = -1;
                    while (sse != null && Dom < 0) {
                        sse = sse.To;
                        Dom = FindDomain(sse).domain;
                    }
                    if (sse != null) {
                        sse.DomainBreakNumber = Count;
                        sse.domainBreakType = DomainBreakType.N_DOM_BREAK;
                        LastCTerm = sse;

                        nf++;
                        if (nf <= PlotFragInformation.MAX_PLOT_FRAGS) {
                            PlotFragInf.NFrags = nf;
                            PlotFragInf.FragChainLims[nf - 1][0] = sse.Chain;
                            PlotFragInf.FragResLims[nf - 1][0] = sse.sseData.PDBStartResidue;
                            PlotFragInf.FragDomain[nf - 1] = Dom + 1;
                            domains.get(Dom).segmentStartIndex[segment] = nf;
                        }
                    }
                    LastDom = Dom;
                    LastSegment = segment;
                }
            } else {
                LastCTerm = sse;
            }
        }
    }

    public void BridgePartFromHBonds() {
        for (Chain chain : chains) {
            chain.bridgePartFromHBonds();
        }
    }

    public String getProteinCode() {
        return this.proteinCode;
    }

    public void addDomain(DomainDefinition domain) {
        this.domains.add(domain);
    }

}
