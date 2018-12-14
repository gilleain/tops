package tops.port;

import static tops.port.model.DomainBreakType.C_DOM_BREAK;
import static tops.port.model.DomainBreakType.NC_DOM_BREAK;
import static tops.port.model.DomainBreakType.N_DOM_BREAK;
import static tops.port.model.DomainDefinition.DomainType.CHAIN_SET;
import static tops.port.model.DomainDefinition.DomainType.SEGMENT_SET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tops.dw.protein.CathCode;
import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.BridgePartner;
import tops.port.model.Cartoon;
import tops.port.model.Chain;
import tops.port.model.DomDefError;
import tops.port.model.DomainBreakType;
import tops.port.model.DomainDefinition;
import tops.port.model.ErrorType;
import tops.port.model.Hand;
import tops.port.model.PlotFragInformation;
import tops.port.model.Protein;
import tops.port.model.SSE;
import tops.port.model.SSEType;
import tops.port.model.Segment;

/**
 * Handle domain information in proteins.
 * 
 * @author maclean
 *
 */
public class DomainCalculator {
    
    private Logger log = Logger.getLogger(DomainCalculator.class.getName());
    
    private class DomainId {
        private final int segment;
        private final int domain;
        private final int exclusionType;
        
        public DomainId(int segment, int domain, int exclusionType) {
            this.segment = segment;
            this.domain = domain;
            this.exclusionType = exclusionType;
        }

        public int getSegment() {
            return segment;
        }

        public int getDomain() {
            return domain;
        }

        public int getExclusionType() {
            return exclusionType;
        }
    }

    public List<DomainDefinition> defaultDomains(Protein protein, char chainToPlot) {
        
        List<DomainDefinition> domains = new ArrayList<>();
        /*
         * In the case of default domains being used we cannot tolerate separate
         * chains with the same chain identifier
         */
        /* This routine removes the problem */
        // RemoveDuplicateChains(protein); XXX what to do in this case?

        if (chainToPlot == '*') { // XXX used to be 'ALL'
            DomainDefinition domain = new DomainDefinition(new CathCode(protein.getProteinCode()), CHAIN_SET);
            for (Chain chain : protein.getChains()) {
                char chainId = chain.getName();
                domain.addSegment(chainId, chain.getStartIndex(), chainId, chain.getFinishIndex());
            }
            domains.add(domain);
        } else {
            for (Chain chain : protein.getChains()) {
                char chainId = chain.getName();
                if (!isChainRepresented(domains, chainId)) {
                    String code = protein.getProteinCode().substring(0, 4) + chainId + '0';
                    DomainDefinition domain = new DomainDefinition(new CathCode(code), CHAIN_SET);
                    domain.addSegment(chainId, chain.getStartIndex(), chainId, chain.getFinishIndex());
                    domains.add(domain);
                }
            }
        }
        return domains;
    }
    
    public DomDefError checkDomainDefs(List<DomainDefinition> domains, Protein protein) {

        DomDefError ddep = new DomDefError("", ErrorType.NO_DOMAIN_ERRORS);
        
        int numberOfDomains = domains.size();
      
        /* check each domain in turn for chains not in protein */
        for (DomainDefinition dm : domains) {
            if (!dm.hasChain(protein.getChains())) {
                return new DomDefError(
                        String.format("Chain not found for domain definition %s ", dm), 
                        ErrorType.DOMAIN_CHAIN_ERROR);
            }
        }

        /*
         * check each domain in turn for segments including residues not in the
         * protein
         */
        for (int i = 0; i < numberOfDomains; i++) {
            DomainDefinition dm = domains.get(i);
            if (dm.is(SEGMENT_SET)) {
                DomDefError error = dm.hasResidues(protein, ddep);
                if (error.errorType != ErrorType.NO_DOMAIN_ERRORS) {
                    return error;
                }
            }
        }

        /* cross check segments for overlaps */
        for (int i = 0; i < numberOfDomains; i++) {
            DomainDefinition dm1 = domains.get(i);
            if (dm1.is(SEGMENT_SET)) {
                for (int j = i + 1; j < numberOfDomains; j++) {
                    DomainDefinition dm2 = domains.get(j);
                    if (dm2.is(SEGMENT_SET)) {
                        for (Segment segment1 : dm1.getSegments()) {
                            for (Segment segment2 : dm2.getSegments()) {
                                if (segment1.overlaps(segment2)) {
                                    return new DomDefError(
                                            "Overlaps were found in residue ranges specifying domains", 
                                            ErrorType.DOMAIN_RANGE_OVERLAP_ERROR);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new DomDefError("", ErrorType.NO_DOMAIN_ERRORS);
    }
    
    
    /*
     * Function to set up DomainBreakNumbers in the master linked list
     */
    public PlotFragInformation setDomBreaks(List<DomainDefinition> domains, Protein protein, List<SSE> sses) {
        PlotFragInformation plotFragmentInformation = new PlotFragInformation();
        
        int count = 0;
        int nf = 0;
        int domain;
        int lastDomain;
        int lastSegment;
        SSE lastCTerm;
    
        for (SSE sse : sses) {
            sse.domainBreakNumber = 0;
            sse.domainBreakType = DomainBreakType.NOT_DOM_BREAK;
        }
    
        /* advance to the first ss element in a real domain */
        int segment = -1;
        SSE sse = null;
        for (int index = 1; index < sses.size() && segment < 0; index++) {
            sse = sses.get(index);
            segment = findDomain(domains, protein, sse).getSegment();
        }
    
        if (sse == null) {
            return plotFragmentInformation;
        }
    
        count++;
        sse.domainBreakNumber = count;
        sse.domainBreakType = DomainBreakType.N_DOM_BREAK;
    
        DomainId result = findDomain(domains, protein, sse);
        lastDomain = -1;
        lastSegment = -1;
        if (result.getSegment() > -1) {
            lastDomain = result.getDomain();
            lastSegment = result.getSegment();
        }
    
        nf = 1;
        if (nf <= PlotFragInformation.MAX_PLOT_FRAGS) {
            plotFragmentInformation.setNumberOfFragments(nf);
            plotFragmentInformation.setStartChainLim(nf - 1, sse.getChain());
            plotFragmentInformation.setStartResLim(nf - 1, sse.sseData.pdbStartResidue);
            plotFragmentInformation.setFragDomain(nf - 1, lastDomain + 1);
        }
    
        lastCTerm = sse;
    
        for (SSE sseA : sses) {
            DomainId result2 = findDomain(domains, protein, sseA);
            domain = result2.getDomain();
    
            if (domain != lastDomain || segment != lastSegment) {
    
                DomainId did = ssIsInDomain(protein, sseA, domains.get(lastDomain));
    
                if (did.getExclusionType() == 0 || domain == lastDomain) {
    
                    count++;
    
                    if (lastCTerm.domainBreakNumber == 0) {
                        lastCTerm.domainBreakNumber = count;
                        lastCTerm.domainBreakType = DomainBreakType.C_DOM_BREAK;
                    } else {
                        lastCTerm.domainBreakType = DomainBreakType.NC_DOM_BREAK;
                    }
    
                    plotFragmentInformation.setEndChainLim(nf - 1, lastCTerm.getChain());
                    plotFragmentInformation.setEndResLim(nf - 1, lastCTerm.sseData.pdbFinishResidue);
    
                    domain = -1;
                    while (sse != null && domain < 0) {
//                        sse = sse.To; TODO
                        domain = findDomain(domains, protein, sse).getDomain();
                    }
                    if (sse != null) {
                        sse.domainBreakNumber = count;
                        sse.domainBreakType = DomainBreakType.N_DOM_BREAK;
                        lastCTerm = sse;
    
                        nf++;
                        if (nf <= PlotFragInformation.MAX_PLOT_FRAGS) {
                            plotFragmentInformation.setNumberOfFragments(nf);
                            plotFragmentInformation.setStartChainLim(nf - 1, sse.getChain());
                            plotFragmentInformation.setStartResLim(nf - 1, sse.sseData.pdbStartResidue);
                            plotFragmentInformation.setFragDomain(nf - 1, domain + 1);
                        }
                    }
                    lastDomain = domain;
                    lastSegment = segment;
                }
            } else {
                lastCTerm = sse;
            }
        }
        
        return plotFragmentInformation;
    }

    public List<DomainDefinition> selectDomainsToPlot(List<DomainDefinition> domains, char chainToPlot, int domainToPlot) {
    
        /*
         * if ChainToPlot==ALL or ( ChainToPlot = NULL and DomainToPlot=0 ) then
         * all domains set up by by ReadDomBoundaryFile or DefaultDomains should
         * be plotted
         */
        List<DomainDefinition> domainsToPlot = new ArrayList<>();
        if ((chainToPlot == 0 && domainToPlot == 0) || chainToPlot == '*') {
            domainsToPlot = domains;
        } else {
            for (int i = 0; i < domains.size(); i++) {
                DomainDefinition domain = domains.get(i);
                if (chainToPlot == 0 || chainToPlot == getChain(domain.getCode())
                  && domainToPlot == 0 || domainToPlot == getDomainNumber(domain.getCode())) {
                    domainsToPlot.add(domain);
                }
            }
        }
    
        if (domainsToPlot.isEmpty()) {
            log.warning("Tops Error: no domains found to plot\n");
            log.warning("Tops Error: no domains found to plot\n");
        }
    
        return domainsToPlot;
    }

    /*
     * Function to set up a new linked list corresponding to a given domain XXX
     * now returns the new root
     */
    public Cartoon setDomain(List<SSE> originalSSEList, Protein protein, DomainDefinition domain) {

        int numberStructures;
        int domBreakNum = 0;

        Map<SSE, SSE> copyTable = new HashMap<>();

        /* this loop decides which structures to copy and copies attributes */
        numberStructures = -1;
        List<SSE> sses = new ArrayList<>();
        for (SSE r : originalSSEList) {

            DomainId domainId = ssIsInDomain(protein, r, domain);
            if (domainId.getSegment() != -1) {

                /*
                 * add an N terminus if we're starting a new piece of continuous
                 * peptide chain
                 */
                if (r.domainBreakType == N_DOM_BREAK || r.domainBreakType == NC_DOM_BREAK) {

                    domBreakNum = r.domainBreakNumber;

                    numberStructures++;
                    SSE q = new SSE(SSEType.NTERMINUS);
                    sses.add(q);
                    q.setSymbolPlaced(true);
                    q.setSymbolNumber(numberStructures);
                    q.setLabel("N" + domBreakNum);
                }

                /* add an ss element equivalent of r */
                numberStructures++;
                SSE q = new SSE(r);
                q.setSymbolNumber(numberStructures);
                copyTable.put(q, r);

                /*
                 * cancel any chirality of an element separated from its chiral
                 * partner by a domain break
                 */
                for (Chain chain : protein.getChains()) {
                    SSE cp = ChiralityCalculator.topsChiralPartner(chain, r);
                    if ((r.chirality != Hand.NONE) && (cp != null)) {
//                        for (s = r; s != cp; s = s.To) {
                        for (SSE s : originalSSEList) {    // TODO - incorrect: should be range(r, cp)?
                            if (s.domainBreakType == C_DOM_BREAK || s.domainBreakType == NC_DOM_BREAK) {
                                q.chirality = Hand.NONE;
                                break;
                            }
                        }
                    }
                }

                /*
                 * add a C terminus if we're ending a piece of continuous
                 * peptide chain
                 */
                if ((r.domainBreakType == C_DOM_BREAK) || (r.domainBreakType == NC_DOM_BREAK)) {
                    numberStructures++;
                    domBreakNum++;
                    q = new SSE(SSEType.CTERMINUS);
                    q.setSymbolPlaced(true);
                    q.setSymbolNumber(numberStructures);
                    q.setLabel("C" + domBreakNum);
                }
            }
        }

        /*
         * if there are any elements on the new list some pointer type structure
         * members in the new list refer the old list - use CopyTable to convert
         * to equivalent in new list
         */

        for (SSE q : sses) {
            if (q.hasFixed())
                q.setFixed(copyTable.get(q.getFixed()));
            //                for (BridgePartner bridgePartner : q.getBridgePartners()) {
            //                    bridgePartner.partner = copyTable.get(bridgePartner.partner);
            //                }
            // for (SSE q.Neighbours)
            // q.Neighbour[i] = GetFromCopyTab(q.Neighbour[i],CopyTable);
            // }
        }

        /* Now re-set the root */
        return new Cartoon(sses);
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

                    int dom = residueDomain(chain, i, domains);

                    // TODO
                    BridgePartner bpl = chain.getBridgePartner(i).get(0);
                    BridgePartner bpr = chain.getBridgePartner(i).get(1);

                    if ( residueDomain(chain, bpl.getPartnerResidue(), domains) != dom || dom<0  ) {
//                        chain.removeLeftBridge(i);
                    }

                    if ( residueDomain(chain, bpr.getPartnerResidue(), domains) != dom||dom<0 ) {
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
                int dom = residueDomain(chain, i, domains);
                int dBreak = -1;
                int lastDom = -1;
                while (chain.isSSelement(i) && chain.getSSEType(i) == thissstype ) {
                    i++;
                    lastDom = dom;
                    dom = residueDomain(chain, i, domains);
                    if ( dom != lastDom ) dBreak = i;
                }

                int finish = --i;

                if ( dBreak > -1 ) {

                    if ( (dBreak-start) > (finish-dBreak) ) {
                        for (int j=dBreak ; j<=finish ; j++) chain.setSSEType(i, SSEType.COIL);
                    } else {
                        for (int j=start ; j<dBreak ; j++) chain.setSSEType(j, SSEType.COIL);
                    }
                }
            }
        }
    }

    private int residueDomain(Chain chain, int residue, List<DomainDefinition> domains) {
        if (residue >= chain.sequenceLength() || residue < 0) {
            return -1;
        }

        int pdbRes = chain.getPDBIndex(residue);
        char pdbChain = chain.getName();

        for (int i = 0; i < domains.size(); i++) {
            DomainDefinition domain = domains.get(i);
            if (domain.resIsInDomain(pdbRes, pdbChain)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isChainRepresented(List<DomainDefinition> domains, char chain) {
        for (int i = 0; i < domains.size(); i++) {
            if (chain == domains.get(i).getCode().charAt(4)) {
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
            if (domainId.getSegment() > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }
    
    private DomainId findDomain(List<DomainDefinition> domains, Chain chain, SSE p) {
        for (int i = 0; i < domains.size(); i++) {
            DomainId result = ssIsInDomain(chain, p, domains.get(i));
            if (result.getSegment() > -1) {
                return result;
            }
        }
        return new DomainId(-1, -1, -1);
    }


    private DomainId ssIsInDomain(Chain chain, SSE p, DomainDefinition domain) {
        int exclusionType = -1;
        int segment = -1;

        /* First check the element passed in */
        segment = domain.getSegmentForSSE(chain, p);
        if (segment > 0) {
            exclusionType = 0;
            return new DomainId(segment, -1, exclusionType);
        }

        /* Now check the whole associated fixed list */
        for (p = chain.findFixedStart(p); p != null; p = p.getFixed()) {
            segment = domain.getSegmentForSSE(chain, p);
            if (segment > 0) {
                exclusionType = 1;
                return new DomainId(segment, -1, exclusionType);
            }
        }
        return new DomainId(segment, -1, exclusionType);
    }

    private DomainId ssIsInDomain(Protein protein, SSE p, DomainDefinition domain) {
        for (Chain chain : protein.getChains()) {
            DomainId domainId = ssIsInDomain(chain, p, domain);
            if (domainId.getSegment() > -1) {
                return domainId;
            }
        }
        return new DomainId(-1, -1, -1);
    }
    
}
