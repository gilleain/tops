package tops.port.model;

public class DomainDefinition {
    private int MAX_SEGMENTS = 50;
    
    public enum DomainType {
        SEGMENT_SET,
        CHAIN_SET
    }
    
    private DomainType domainType;
    private String domainCATHCode = "";
    private int[][] segmentIndices = new int[2][MAX_SEGMENTS];
    private char[][] segmentChains = new char[2][MAX_SEGMENTS];
    
    private int index;
    
    public DomainDefinition(String code, DomainType domainType) {
        this.domainCATHCode = code;
        this.domainType = domainType;
        this.index = 0;
    }
    
    public String getCode() {
        return this.domainCATHCode;
    }
    
    public boolean is(DomainType type) {
        return this.domainType == type;
    }
    
    public char getStartSegmentChain(int index) {
        return segmentChains[0][index];
    }
    
    public char getEndSegmentChain(int index) {
        return segmentChains[1][index];
    }
    
    public int getStartSegmentIndex(int index) {
        return segmentIndices[0][index];
    }
    
    public int getEndSegmentIndex(int index) {
        return segmentIndices[1][index];
    }
    
    public void addSegment(char startChainId, int startIndex, char endChainId, int endIndex) {
        segmentChains[0][index] = startChainId;
        segmentChains[1][index] = endChainId;
        segmentIndices[0][index] = startIndex;
        segmentIndices[1][index] = endIndex;
        index++;
    }
    
    public int getNumberOfSegments() {
        return index;
    }

}
