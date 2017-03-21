package tops.port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.BridgePartner;
import tops.port.model.Cartoon;
import tops.port.model.Chain;
import tops.port.model.DomainBreakType;
import tops.port.model.DomainDefinition;
import tops.port.model.DomainDefinition.DomainType;
import tops.port.model.Hand;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.SSE.SSEType;

/**
 * Handle domain information in proteins.
 * 
 * @author maclean
 *
 */
public class DomainCalculator {
    
    private class DomainId {
        public int segment;
        public int domain;
        public int exclusionType;
        
        public DomainId(int segment, int domain, int exclusionType) {
            this.segment = segment;
            this.domain = domain;
            this.exclusionType = exclusionType;
        }
    }

    public enum ErrorType {
        NO_DOMAIN_ERRORS, DOMAIN_CHAIN_ERROR, DOMAIN_RESIDUE_ERROR, DOMAIN_RANGE_OVERLAP_ERROR
    }
    
    public class DomDefError {
        public String ErrorString;
        public ErrorType ErrorType;
    }

    public List<DomainDefinition> defaultDomains(Protein protein, char chainToPlot) {
        
        List<DomainDefinition> domains = new ArrayList<DomainDefinition>();
        /*
         * In the case of default domains being used we cannot tolerate separate
         * chains with the same chain identifier
         */
        /* This routine removes the problem */
        // RemoveDuplicateChains(protein); XXX what to do in this case?

        if (chainToPlot == '*') { // XXX used to be 'ALL'

            DomainDefinition domain = new DomainDefinition(DomainType.CHAIN_SET);

            domain.domainCATHCode = protein.getProteinCode();
            domain.numberOfSegments = protein.getChains().size();

            int i = 0;
            for (Chain chain : protein.getChains()) {
                char chainId = chain.getName();
                domain.segmentChains[0][i] = chainId;
                domain.segmentChains[1][i] = chainId;
                domain.segmentIndices[0][i] = chain.getStartIndex();
                domain.segmentIndices[1][i] = chain.getFinishIndex();
                i++;
            }
            domains.add(domain);
        } else {
            for (Chain chain : protein.getChains()) {
                char chainId = chain.getName();
                if (!isChainRepresented(domains, chainId)) {
                    DomainDefinition domain = new DomainDefinition(DomainType.CHAIN_SET);

                    domain.domainCATHCode = protein.getProteinCode().substring(0, 4) + chainId + '0';
                    domain.numberOfSegments = 1;
                    domain.segmentChains[0][0] = chainId;
                    domain.segmentChains[1][0] = chainId;
                    domain.segmentIndices[0][0] = chain.getStartIndex();
                    domain.segmentIndices[1][0] = chain.getFinishIndex();
                    domains.add(domain);
                }
            }
        }
        return domains;
    }
    
    public DomDefError checkDomainDefs(List<DomainDefinition> domains, Protein protein) {

        char dm_chain = ' ';
        int numberOfDomains = domains.size();

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
                        for (int l = 0; l < protein.getChains().size(); l++) {
                            if (dm_chain == protein.getChains().get(l).getName()) {
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

                        if (protein.getSequenceNumber(dm.segmentIndices[k][j], dm.segmentChains[k][j]) < 0) {

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

                                    if (segmentsOverlap(Seg1Chains, Seg1Range, Seg2Chains, Seg2Range)) {

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
    
    
    /*
     * Function to set up DomainBreakNumbers in the master linked list
     */
    public PlotFragInformation setDomBreaks(List<DomainDefinition> domains, Protein protein, SSE Root) {
        PlotFragInformation plotFragmentInformation = new PlotFragInformation();
        
        int Count = 0;
        int nf = 0;
        int Dom, LastDom;
        int LastSegment;
        SSE sse, LastCTerm;
    
        for (sse = Root; sse != null; sse = sse.To)
            sse.DomainBreakNumber = 0;
        for (sse = Root; sse != null; sse = sse.To)
            sse.domainBreakType = DomainBreakType.NOT_DOM_BREAK;
    
        /* advance to the first ss element in a real domain */
        int segment = -1;
        for (sse = Root.To; sse != null && segment < 0; sse = sse.To) {
            segment = findDomain(domains, protein, sse).segment;
        }
    
        if (sse == null)
            return plotFragmentInformation;
    
        Count++;
        sse.DomainBreakNumber = Count;
        sse.domainBreakType = DomainBreakType.N_DOM_BREAK;
    
        DomainId result = findDomain(domains, protein, sse);
        LastDom = -1;
        LastSegment = -1;
        if (result.segment > -1) {
            LastDom = result.domain;
            LastSegment = result.segment;
        }
    
        nf = 1;
        if (nf <= PlotFragInformation.MAX_PLOT_FRAGS) {
            plotFragmentInformation.setNumberOfFragments(nf);
            plotFragmentInformation.setChainLim0(nf - 1, sse.getChain());
            plotFragmentInformation.setResLim0(nf - 1, sse.sseData.PDBStartResidue);
            plotFragmentInformation.setFragDomain(nf - 1, LastDom + 1);
            /*
             * this will be done later in a Protein.c func. it is easier here
             * for now
             */
            domains.get(LastDom).segmentStartIndex[LastSegment] = nf;
        }
    
        LastCTerm = sse;
    
        while (sse != null) {
            sse = sse.To;
            DomainId result2 = findDomain(domains, protein, sse);
            Dom = result2.domain;
    
            if ((Dom != LastDom) || (segment != LastSegment)) {
    
                DomainId did = ssIsInDomain(protein, sse, domains.get(LastDom));
    
                if ((did.exclusionType == 0) || (Dom == LastDom)) {
    
                    Count++;
    
                    if (LastCTerm.DomainBreakNumber == 0) {
                        LastCTerm.DomainBreakNumber = Count;
                        LastCTerm.domainBreakType = DomainBreakType.C_DOM_BREAK;
                    } else {
                        LastCTerm.domainBreakType = DomainBreakType.NC_DOM_BREAK;
                    }
    
                    plotFragmentInformation.setChainLim1(nf - 1, LastCTerm.getChain());
                    plotFragmentInformation.setResLim1(nf - 1, LastCTerm.sseData.PDBFinishResidue);
    
                    Dom = -1;
                    while (sse != null && Dom < 0) {
                        sse = sse.To;
                        Dom = findDomain(domains, protein, sse).domain;
                    }
                    if (sse != null) {
                        sse.DomainBreakNumber = Count;
                        sse.domainBreakType = DomainBreakType.N_DOM_BREAK;
                        LastCTerm = sse;
    
                        nf++;
                        if (nf <= PlotFragInformation.MAX_PLOT_FRAGS) {
                            plotFragmentInformation.setNumberOfFragments(nf);
                            plotFragmentInformation.setChainLim0(nf - 1, sse.getChain());
                            plotFragmentInformation.setResLim0(nf - 1, sse.sseData.PDBStartResidue);
                            plotFragmentInformation.setFragDomain(nf - 1, Dom + 1);
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
        
        return plotFragmentInformation;
    }

    public List<Integer> fixDomainsToPlot(List<DomainDefinition> domains, char ChainToPlot, int DomainToPlot) {
    
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
                        || ChainToPlot == getChain(domain.domainCATHCode)) {
                    if (DomainToPlot == 0 
                            || DomainToPlot == getDomainNumber(domain.domainCATHCode)) {
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

    /*
     * Function to set up a new linked list corresponding to a given domain XXX
     * now returns the new root
     */
    public Cartoon setDomain(SSE OriginalRoot, Protein protein, DomainDefinition domain) {

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

            DomainId domainId = ssIsInDomain(protein, r, domain);
            if (domainId.segment != -1) {

                /*
                 * add an N terminus if we're starting a new piece of continuous
                 * peptide chain
                 */
                if ((r.domainBreakType == DomainBreakType.N_DOM_BREAK)
                        || (r.domainBreakType == DomainBreakType.NC_DOM_BREAK)) {

                    domBreakNum = r.DomainBreakNumber;

                    numberStructures++;
                    SSE q = new SSE(SSEType.NTERMINUS);

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
                for (Chain chain : protein.getChains()) {
                    SSE cp = ChiralityCalculator.topsChiralPartner(chain, r);
                    if ((r.Chirality != Hand.NONE) && (cp != null)) {
                        for (s = r; s != cp; s = s.To) {
                            if ((s.domainBreakType == DomainBreakType.C_DOM_BREAK)
                                    || (s.domainBreakType == DomainBreakType.NC_DOM_BREAK)) {
                                q.Chirality = Hand.NONE;
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
                    q = new SSE(SSEType.CTERMINUS);
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
                //                for (BridgePartner bridgePartner : q.getBridgePartners()) {
                //                    bridgePartner.partner = copyTable.get(bridgePartner.partner);
                //                }
                // for (SSE q.Neighbours)
                // q.Neighbour[i] = GetFromCopyTab(q.Neighbour[i],CopyTable);
                // }
            }
        }

        /* Now re-set the root */
        return new Cartoon(newRoot);
    }
    
    /*
    This function forces consistency between domain definitions and secondary structure information 
    in the protein data structure.
    Bridge partner relationships which cross domain boundaries are removed 
    ss elements which cross domain boundaries are limited to the domain they are mostly in
     */
    public void forceConsistent(Protein p, List<DomainDefinition> domains) {

        for (Chain chain : p.getChains()) {
            for (int i = 0; i < chain.sequenceLength(); i++) {
                if ( chain.isExtended(i) ) {

                    int Dom = residueDomain(p, i, domains);

                    // TODO
                    BridgePartner bpl = chain.getBridgePartner(i).get(0);
                    BridgePartner bpr = chain.getBridgePartner(i).get(1);

                    if ( residueDomain(p, bpl.partnerResidue, domains) != Dom || Dom<0  ) {
//                        chain.removeLeftBridge(i);
                    }

                    if ( residueDomain(p, bpr.partnerResidue, domains) != Dom||Dom<0 ) {
//                        chain.removeRightBridge(i);
                    }
                }
            }
        }

        for (Chain chain : p.getChains()) {
            forceConsistent(p, chain, domains);
        }
    }

    private void forceConsistent(Protein protein, Chain chain, List<DomainDefinition> domains) {
        for (int i = 0; i < chain.sequenceLength(); i++) {

            if (chain.isSSelement(i) ) {
                int start = i;
                SSEType thissstype = chain.getSSEType(i);
                int Dom = residueDomain(protein, i, domains);
                int DBreak = -1;
                int LastDom = -1;
                while (chain.isSSelement(i) && chain.getSSEType(i) == thissstype ) {
                    i++;
                    LastDom = Dom;
                    Dom = residueDomain(protein, i, domains);
                    if ( Dom != LastDom ) DBreak = i;
                }

                int finish = --i;

                if ( DBreak > -1 ) {

                    if ( (DBreak-start) > (finish-DBreak) ) {
                        for (int j=DBreak ; j<=finish ; j++) chain.setSSEType(i, SSEType.COIL);
                    } else {
                        for (int j=start ; j<DBreak ; j++) chain.setSSEType(j, SSEType.COIL);
                    }
                }
            }
        }
    }

    private int residueDomain(Protein protein, int Residue, List<DomainDefinition> domains) {
        for (Chain chain : protein.getChains()) {
            if ((Residue >= chain.sequenceLength()) || (Residue < 0)) {
                return -1;
            }
    
            int PDBRes = chain.getPDBIndex(Residue);
            char PDBChain = chain.getName();
    
            for (int i = 0; i < domains.size(); i++) {
                if (resIsInDomain(PDBRes, PDBChain, domains.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean resIsInDomain( int PDBRes, char PDBChain, DomainDefinition Domain) {
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

    private boolean segmentsOverlap(char[] Seg1Chains, int[] Seg1Range, char[] Seg2Chains, int[] Seg2Range) {

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


    private boolean isChainRepresented(List<DomainDefinition> domains, char chain) {
        for (int i = 0; i < domains.size(); i++) {
            if (chain == domains.get(i).domainCATHCode.charAt(4)) {
                return true;
            }
        }
        return false;
    }

    private char getChain(String cathCode) {
        return cathCode.length() < 4 ? 0 : cathCode.charAt(3);
    }

    private char getDomainNumber(String cathCode) {
        return cathCode.length() < 6 ? 0 : cathCode.charAt(5);
    }
    
    /** 
     * Function to find the domain associated with a SS element
     */
    private DomainId findDomain(List<DomainDefinition> domains, Protein protein, SSE p) {
        for (Chain chain : protein.getChains()) {
            DomainId domainId = findDomain(domains, chain, p);
            if (domainId.segment > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }
    
    private DomainId findDomain(List<DomainDefinition> domains, Chain chain, SSE p) {
        for (int i = 0; i < domains.size(); i++) {
            DomainId result = ssIsInDomain(chain, p, domains.get(i));
            if (result.segment > -1) {
                return result;
            }
        }
        return new DomainId(-1, -1, -1);
    }


    private DomainId ssIsInDomain(Chain chain, SSE p, DomainDefinition domain) {
        int ExclusionType = -1;
        int Segment = -1;

        /* First check the element passed in */
        Segment = singleSSIsInDomain(p, domain);
        if (Segment > 0) {
            ExclusionType = 0;
            return new DomainId(Segment, -1, ExclusionType);
        }

        /* Now check the whole associated fixed list */
        for (p = chain.findFixedStart(p); p != null; p = p.getFixed()) {
            Segment = singleSSIsInDomain(p, domain);
            if (Segment > 0) {
                ExclusionType = 1;
                return new DomainId(Segment, -1, ExclusionType);
            }
        }
        return new DomainId(Segment, -1, ExclusionType);
    }

    private DomainId ssIsInDomain(Protein protein, SSE p, DomainDefinition domain) {
        for (Chain chain : protein.getChains()) {
            DomainId domainId = ssIsInDomain(chain, p, domain);
            if (domainId.segment > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }

    private int singleSSIsInDomain(SSE p, DomainDefinition Domain) {

        int i;
        char chain = p.getChain();
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
}
