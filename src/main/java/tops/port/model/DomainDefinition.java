package tops.port.model;

import static tops.port.model.DomainDefinition.DomainType.CHAIN_SET;
import static tops.port.model.DomainDefinition.DomainType.SEGMENT_SET;

import java.util.ArrayList;
import java.util.List;

import tops.port.DomainCalculator.DomDefError;
import tops.port.DomainCalculator.ErrorType;

public class DomainDefinition {
    
    public enum DomainType {
        SEGMENT_SET,
        CHAIN_SET
    }
    
    private DomainType domainType;
    private String domainCATHCode = "";
    private List<Segment> segments;
    
    public DomainDefinition(String code, DomainType domainType) {
        this.segments = new ArrayList<Segment>();
        this.domainCATHCode = code;
        this.domainType = domainType;
    }
    
    public List<Segment> getSegments() {
        return this.segments;
    }

    public void addSegment(char startChainId, int startIndex, char endChainId, int endIndex) {
        segments.add(new Segment(startChainId, startIndex, endChainId, endIndex));
    }
    
    public boolean hasResidues(Protein protein, DomDefError ddep) {
        for (int j = 0; j < getNumberOfSegments(); j++) {
            for (int k = 0; k < 2; k++) {
                int segmentIndex = (k == 0)? getStartSegmentIndex(j) : getEndSegmentIndex(j);
                char segmentChain = (k == 0)? getStartSegmentChain(j) : getEndSegmentChain(j);
                if (protein.getSequenceNumber(segmentIndex, segmentChain) < 0) {

                    ddep.ErrorType = ErrorType.DOMAIN_RESIDUE_ERROR;
                    ddep.ErrorString += 
                            String.format(
                                    "Residue %c %d for domain definition not found in protein",
                                    segmentIndex, segmentChain);
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean hasChain(List<Chain> chains) {
        char domainChain = ' ';
        for (int segmentIndex = 0; segmentIndex < this.getNumberOfSegments(); segmentIndex++) {
            for (int k = 0; k < 2; k++) {
                domainChain = (k == 0)? getStartSegmentChain(segmentIndex) : getEndSegmentChain(segmentIndex);
                for (Chain chain : chains) {
                    if (domainChain == chain.getName()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public String getCode() {
        return this.domainCATHCode;
    }
    
    public boolean is(DomainType type) {
        return this.domainType == type;
    }
    
    public char getStartSegmentChain(int index) {
        return segments.get(index).startChain;
    }
    
    public char getEndSegmentChain(int index) {
        return segments.get(index).endChain;
    }
    
    public int getStartSegmentIndex(int index) {
        return segments.get(index).startIndex;
    }
    
    public int getEndSegmentIndex(int index) {
        return segments.get(index).endIndex;
    }
    
    
    public int getNumberOfSegments() {
        return segments.size(); 
    }
    

    public boolean resIsInDomain(int pdbRes, char pdbChain) {

        for (int i = 0; i < getNumberOfSegments(); i++) {

            if (is(SEGMENT_SET)) {
                if (pdbChain == getStartSegmentChain(i) && pdbChain == getEndSegmentChain(i)) {
                    if (pdbRes >= getStartSegmentIndex(i) && pdbRes <= getEndSegmentIndex(i))
                        return true;
                } else if (pdbChain == getStartSegmentChain(i)) {
                    if (pdbRes >= getStartSegmentIndex(i))
                        return true;
                } else if (pdbChain == getEndSegmentChain(i)) {
                    if (pdbRes <= getEndSegmentIndex(i))
                        return true;
                }
            } else if (is(CHAIN_SET)) {
                if (pdbChain == getStartSegmentChain(i)|| pdbChain == getEndSegmentChain(i))
                    return true;
            }
        }
        return false;
    }

}
