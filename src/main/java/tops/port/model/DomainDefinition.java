package tops.port.model;

import static tops.port.model.DomainDefinition.DomainType.CHAIN_SET;
import static tops.port.model.DomainDefinition.DomainType.SEGMENT_SET;

import java.util.ArrayList;
import java.util.List;

import tops.dw.protein.CATHcode;
import tops.port.DomainCalculator.DomDefError;
import tops.port.DomainCalculator.ErrorType;

public class DomainDefinition {
    
    public enum DomainType {
        SEGMENT_SET,
        CHAIN_SET
    }
    
    private DomainType domainType;
    private CATHcode domainCATHCode = new CATHcode("");
    private List<Segment> segments;
    
    public DomainDefinition(CATHcode code, DomainType domainType) {
        this.segments = new ArrayList<Segment>();
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
    
    public boolean hasResidues(Protein protein, DomDefError ddep) {
        for (Segment segment : segments) {
            if (!check(protein, segment.startIndex, segment.startChain, ddep)) {
                return false;
            }
            
            if (!check(protein, segment.endIndex, segment.endChain, ddep)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean check(Protein protein, int segmentIndex, char segmentChain, DomDefError ddep) {
        if (protein.getSequenceNumber(segmentIndex, segmentChain) < 0) {
            ddep.ErrorType = ErrorType.DOMAIN_RESIDUE_ERROR;
            ddep.ErrorString += 
                    String.format(
                            "Residue %c %d for domain definition not found in protein",
                            segmentIndex, segmentChain);
            return false;
        }
        return true;
    }
    
    public boolean hasChain(List<Chain> chains) {
        for (Segment segment : segments) {
            if (hasChain(segment.startChain, chains)) {
                return true;
            } 
            if (hasChain(segment.endChain, chains)) {
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

        for (Segment segment : segments) {

            if (is(SEGMENT_SET)) {
                if (pdbChain == segment.startChain && pdbChain == segment.endChain) {
                    if (pdbRes >= segment.startIndex && pdbRes <= segment.endIndex) {
                        return true;
                    }
                } else if (pdbChain == segment.startChain) {
                    if (pdbRes >= segment.startIndex) {
                        return true;
                    }
                } else if (pdbChain == segment.endChain) {
                    if (pdbRes <= segment.endIndex) {
                        return true;
                    }
                }
            } else if (is(CHAIN_SET)) {
                if (pdbChain == segment.startChain || pdbChain == segment.endChain)
                    return true;
            }
        }
        return false;
    }

    public CATHcode getCATHcode() {
        return domainCATHCode;
    }

}
