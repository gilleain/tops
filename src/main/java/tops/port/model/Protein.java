package tops.port.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tops.port.calculate.chirality.ChiralityCalculator;
import tops.port.model.DomainDefinition.DomainType;
import tops.port.model.SSE.SSEType;

public class Protein {

    
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

    public int getSequenceNumber(int PDBIndex, char Chain) {
//        for (int i = 0; i < sequenceLength; i++) {
//            if ((PDBIndices[i] == PDBIndex)
//                    && (GetChainIdentifier(i) == Chain)) {
//                return i;
//            }
//        }
        return -1; // XXX TODO
    }
    

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


    public String getProteinCode() {
        return this.proteinCode;
    }

    public void addDomain(DomainDefinition domain) {
        this.domains.add(domain);
    }

}
