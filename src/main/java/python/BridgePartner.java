package python;

class BridgePartner {
    
    public enum Side { LEFT, RIGHT, UNKNOWN }
    
    public SSE partner;
    
    public Side side;
    
    public BridgeType bridgeType;
    
    public int rangeMin;
    
    public int rangeMax;
    
    public int NumberBridgePartners;
    
    public BridgePartner(SSE partner, int residueNumber, BridgeType bridgeType, Side side) {
        this(partner, residueNumber, residueNumber, bridgeType, side);
    }
    
    public BridgePartner(SSE partner, int rangeMin, int rangeMax, BridgeType bridgeType, Side side) {
        this.partner = partner;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.bridgeType = bridgeType;
        this.side = (side == null)? Side.UNKNOWN : side;
        this.NumberBridgePartners = 1;
    }
    
    public void update(int residueNumber) {
        if (residueNumber < rangeMin) rangeMin = residueNumber;
        if (residueNumber > rangeMax) rangeMax = residueNumber;
        NumberBridgePartners++;
    }
}
