package tops.port.model;

public class BridgePartner {
    
    public enum Side { LEFT, RIGHT, UNKNOWN }
    
    public SSE partner;
    
    public int partnerResidue;
    
    public Side side;
    
    public BridgeType bridgeType;
    
    public int rangeMin;
    
    public int rangeMax;
    
    public int NumberBridgePartners;
    
    public double energy;
    
    public BridgePartner() {
        // TODO
    }
    
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
    
    public boolean isLeft() {
        return this.side == Side.LEFT;
    }
    
    public void setUnknown() {
        this.side = Side.UNKNOWN;
    }
    
    public void setLeft() {
        this.side = Side.LEFT;
    }
    
    public void setRight() {
        this.side = Side.RIGHT;
    }
    
    public boolean isUnknownSide() {
        return this.side == Side.UNKNOWN;
    }
    
    public int getBridgeLength() {
        return Math.abs(rangeMax - rangeMin);
    }
    
    public void update(int residueNumber) {
        if (residueNumber < rangeMin) rangeMin = residueNumber;
        if (residueNumber > rangeMax) rangeMax = residueNumber;
        NumberBridgePartners++;
    }
    
    public boolean equals(Object other) {
        if (other instanceof BridgePartner) {
            BridgePartner oBridgePartner = (BridgePartner) other;
            // TODO
            return this.bridgeType == oBridgePartner.bridgeType
                    && this.partner.equals(oBridgePartner.partner);
        }
        return false;
    }
    
    public int hashCode() {
        return partnerResidue;
    }
    
    public String toString() {
        return String.format("%s %s %s %s %s", 
                partner == null? "{}" : partner.getSymbolNumber(),
                side, bridgeType, rangeMin, rangeMax);
    }
}
