package tops.port.model;

import static tops.port.model.DomainDefinition.DomainType.CHAIN_SET;
import static tops.port.model.DomainDefinition.DomainType.SEGMENT_SET;

import java.util.ArrayList;
import java.util.List;

import tops.dw.protein.CathCode;

public class DomainDefinition {
    
    public enum DomainType {
        SEGMENT_SET,
        CHAIN_SET
    }
    
    private DomainType domainType;
    private CathCode domainCATHCode = new CathCode("");
    private List<Segment> segments;
    
    public DomainDefinition(CathCode code, DomainType domainType) {
        this.segments = new ArrayList<>();
        this.domainCATHCode = code;
        this.domainType = domainType;
    }
    
    public List<Segment> getSegments() {
        return this.segments;
    }

    public void addSegment(char chainId, int startIndex, int endIndex) {
        addSegment(chainId, startIndex, chainId, endIndex);
    }
    
    public void addSegment(char startChainId, int startIndex, char endChainId, int endIndex) {
        segments.add(new Segment(startChainId, startIndex, endChainId, endIndex));
    }
    
    public int getSegmentForSSE(Chain chain, SSE sse) {
        char chainName = chain.getName();
        int pdbStart = sse.sseData.pdbStartResidue;
        int pdbFinish = sse.sseData.pdbFinishResidue;
        
        int segmentIndex = 0;
        for (Segment segment : segments) {
            if (is(SEGMENT_SET)) {
                if (chainName == segment.getStartChain() && chainName == segment.getEndChain()) {
                    if (pdbStart >= segment.getStartIndex() && pdbFinish <= segment.getEndIndex()) {
                        return segmentIndex;
                    }
                } else if (chainName == segment.getStartChain() && pdbStart >= segment.getStartIndex()) {
                    return segmentIndex;
                } else if (chainName == segment.getEndChain() && pdbFinish <= segment.getEndIndex()) {
                    return segmentIndex;
                }
            } else if (is(CHAIN_SET) && (chainName == segment.getStartChain() || chainName == segment.getEndChain())) {
                return segmentIndex;
            }
        }
        return -1;
    }
    
    public DomDefError hasResidues(Protein protein, DomDefError ddep) {
        for (Segment segment : segments) {
            DomDefError error;
            
            error = check(protein, segment.getStartIndex(), segment.getStartChain(), ddep);
            if (!error.isOk()) {
                return error;
            }
            
            error = check(protein, segment.getEndIndex(), segment.getEndChain(), ddep);
            if (!error.isOk()) {
                return error;
            }
        }
        return new DomDefError("", ErrorType.NO_DOMAIN_ERRORS);
    }
    
    private DomDefError check(Protein protein, int segmentIndex, char segmentChain, DomDefError ddep) {
        if (protein.getSequenceNumber(segmentIndex, segmentChain) < 0) {
            return new DomDefError(
                    String.format(
                            "Residue %c %d for domain definition not found in protein",
                            segmentIndex, segmentChain), ErrorType.DOMAIN_RESIDUE_ERROR);
        }
        return new DomDefError("", ErrorType.NO_DOMAIN_ERRORS);
    }
    
    public boolean hasChain(List<Chain> chains) {
        for (Segment segment : segments) {
            if (hasChain(segment.getStartChain(), chains)) {
                return true;
            } 
            if (hasChain(segment.getEndChain(), chains)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasChain(char domainChain, List<Chain> chains) {
        for (Chain chain : chains) {
            if (domainChain == chain.getName()) {
                return true;
            }
        }
        return false;
    }
    
    public String getCode() {
        return this.domainCATHCode.toString();
    }
    
    public boolean is(DomainType type) {
        return this.domainType == type;
    }
    
    public int getNumberOfSegments() {
        return segments.size(); 
    }

    public boolean resIsInDomain(int pdbRes, char pdbChain) {

        for (Segment segment : segments) {

            if (is(SEGMENT_SET)) {
                if (pdbChain == segment.getStartChain() && pdbChain == segment.getEndChain()) {
                    if (pdbRes >= segment.getStartIndex() && pdbRes <= segment.getEndIndex()) {
                        return true;
                    }
                } else if (pdbChain == segment.getStartChain()) {
                    if (pdbRes >= segment.getStartIndex()) {
                        return true;
                    }
                } else if (pdbChain == segment.getEndChain() && pdbRes <= segment.getEndIndex()) {
                    return true;
                }
            } else if (is(CHAIN_SET) && (pdbChain == segment.getStartChain() || pdbChain == segment.getEndChain())) {
                return true;
            }
        }
        return false;
    }

    public CathCode getCATHcode() {
        return domainCATHCode;
    }

}
