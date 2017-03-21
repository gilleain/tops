package tops.port.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.DomainDefinition.DomainType;
import tops.port.model.SSE.SSEType;

public class Protein {

    public enum ErrorType {
        NO_DOMAIN_ERRORS, DOMAIN_CHAIN_ERROR, DOMAIN_RESIDUE_ERROR, DOMAIN_RANGE_OVERLAP_ERROR
    }
    
    public class DomDefError {
        public String ErrorString;
        public ErrorType ErrorType;
    }
    
    private List<Chain> chains;
    private String proteinCode;
    public int numberOfDomains;
    public List<DomainDefinition> domains;
    
    public Protein(String proteinCode) {
        this.proteinCode = proteinCode;
        this.numberOfDomains = 1;

        this.chains = new ArrayList<Chain>();
        this.domains = new ArrayList<DomainDefinition>();
    }
    
    public void setCode(String name) {
        this.proteinCode = name;
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
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        for (Chain chain : this.chains) {
            sb.append(this.proteinCode + chain.toString());
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
    public void forceConsistent( Protein p ) {

        for (Chain chain : this.chains) {
            for (int i = 0; i < chain.sequenceLength(); i++) {
                if ( chain.isExtended(i) ) {
    
                    int Dom = residueDomain(i);
    
                    // TODO
//                    int bpl = chain.getLeftBridgePartner(i);
//                    int bpr = chain.getRightBridgePartner(i);
//    
//                    if ( (ResidueDomain(bpl) != Dom)||(Dom<0) ) {
//                        chain.removeLeftBridge(i);
//                    }
//    
//                    if ( (ResidueDomain(bpr) != Dom)||(Dom<0) ) {
//                        chain.removeRightBridge(i);
//                    }
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

                        if (getSequenceNumber(dm.segmentIndices[k][j], dm.segmentChains[k][j]) < 0) {

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

    public boolean resIsInDomain( int PDBRes, char PDBChain, DomainDefinition Domain) {
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


    public int residueDomain(int Residue) {
        for (Chain chain : chains) {
            if ((Residue >= chain.sequenceLength()) || (Residue < 0)) {
                return -1;
            }
    
            int PDBRes = chain.getPDBIndex(Residue);
            char PDBChain = chain.getName();
    
            for (int i = 0; i < numberOfDomains; i++) {
                if (resIsInDomain(PDBRes, PDBChain, domains.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    public int getSequenceNumber(int PDBIndex, char Chain) {
//        for (int  i=0 ;i<sequenceLength ;i++) {
//            if ( (PDBIndices[i]==PDBIndex) && (GetChainIdentifier(i)==Chain) ) {
//                return i;
//            }
//        }
        return -1;  // XXX TODO
    }

    public String getProteinCode() {
        return this.proteinCode;
    }

    public void addDomain(DomainDefinition domain) {
        this.domains.add(domain);
    }

}
