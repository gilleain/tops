package python;

public class DomainDefinition {
    int MAX_SEGMENTS = 50;
    int CATH_CODE_LEN = 7;
    
    public enum DomainType {
        SEGMENT_SET,
        CHAIN_SET
    }
    
    public DomainType domainType;
    public String domainCATHCode = "";
    public int numberOfSegments;
    public int[][] segmentIndices = new int[2][MAX_SEGMENTS];
    public char[][] segmentChains = new char[2][MAX_SEGMENTS];
    public int[] segmentStartIndex = new int[MAX_SEGMENTS];
    
    public DomainDefinition(DomainType domainType) {
        this.domainType = domainType;
    }

}
