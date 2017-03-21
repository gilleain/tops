package tops.port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tops.port.calculate.chirality.ChiralityCalculator;
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
 * Bad name, but parses out domains.
 * 
 * @author maclean
 *
 */
public class DomainCalculator {
    
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
    
    private boolean isChainRepresented(List<DomainDefinition> domains, char chain) {
        for (int i = 0; i < domains.size(); i++) {
            if (chain == domains.get(i).domainCATHCode.charAt(4)) {
                return true;
            }
        }
        return false;
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

    private char getChain(String cathCode) {
        return cathCode.length() < 4 ? 0 : cathCode.charAt(3);
    }

    private char getDomainNumber(String cathCode) {
        return cathCode.length() < 6 ? 0 : cathCode.charAt(5);
    }
    
    public void moveNextToDom(Protein protein, SSE q, DomainDefinition Domain) {
        for (SSE p = q.Next; p != null; p = p.Next) {
            DomainId result = ssIsInDomain(protein, p, Domain);
            if (result.segment > 0) {
                q.Next = p;
                return;
            }
        }
        q.Next = null;
    }
    
    /*
     * Function to set up DomainBreakNumbers in the master linked list
     */
    public void setDomBreaks(List<DomainDefinition> domains, Protein protein, SSE Root, PlotFragInformation plotFragInf) {

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
            return;

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
            plotFragInf.setNumberOfFragments(nf);
            plotFragInf.setChainLim0(nf - 1, sse.getChain());
            plotFragInf.setResLim0(nf - 1, sse.sseData.PDBStartResidue);
            plotFragInf.setFragDomain(nf - 1, LastDom + 1);
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

                    plotFragInf.setChainLim1(nf - 1, LastCTerm.getChain());
                    plotFragInf.setResLim1(nf - 1, LastCTerm.sseData.PDBFinishResidue);

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
                            plotFragInf.setNumberOfFragments(nf);
                            plotFragInf.setChainLim0(nf - 1, sse.getChain());
                            plotFragInf.setResLim0(nf - 1, sse.sseData.PDBStartResidue);
                            plotFragInf.setFragDomain(nf - 1, Dom + 1);
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
    
    /** 
     * Function to find the domain associated with a SS element
     */
    public DomainId findDomain(List<DomainDefinition> domains, Protein protein, SSE p) {
        for (Chain chain : protein.getChains()) {
            DomainId domainId = findDomain(domains, chain, p);
            if (domainId.segment > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }
    
    public DomainId findDomain(List<DomainDefinition> domains, Chain chain, SSE p) {
        for (int i = 0; i < domains.size(); i++) {
            DomainId result = ssIsInDomain(chain, p, domains.get(i));
            if (result.segment > -1) {
                return result;
            }
        }
        return new DomainId(-1, -1, -1);
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
    
    public DomainId ssIsInDomain(Chain chain, SSE p, DomainDefinition domain) {
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

    public DomainId ssIsInDomain(Protein protein, SSE p, DomainDefinition domain) {
        for (Chain chain : protein.getChains()) {
            DomainId domainId = ssIsInDomain(chain, p, domain);
            if (domainId.segment > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }

    public int singleSSIsInDomain(SSE p, DomainDefinition Domain) {

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
