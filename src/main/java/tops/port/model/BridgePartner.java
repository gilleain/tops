package tops.port.model;

public class BridgePartner implements Comparable<BridgePartner> {
    
    public enum Side { LEFT, RIGHT, UNKNOWN }
    
    private SSE partner;
    
    private int partnerResidue;
    
    private Side side;
    
    private BridgeType bridgeType;
    
    private int rangeMin;
    
    private int rangeMax;
    
    private int numberBridgePartners;
    
    private double energy;
    
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
        this.numberBridgePartners = 1;
    }
    
    public SSE getPartner() {
        return partner;
    }

    public void setPartner(SSE partner) {
        this.partner = partner;
    }

    public int getPartnerResidue() {
        return partnerResidue;
    }

    public void setPartnerResidue(int partnerResidue) {
        this.partnerResidue = partnerResidue;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public BridgeType getBridgeType() {
        return bridgeType;
    }

    public void setBridgeType(BridgeType bridgeType) {
        this.bridgeType = bridgeType;
    }

    public boolean isLeft() {
        return this.getSide() == Side.LEFT;
    }
    
    public void setUnknown() {
        this.setSide(Side.UNKNOWN);
    }
    
    public void setLeft() {
        this.setSide(Side.LEFT);
    }
    
    public void setRight() {
        this.setSide(Side.RIGHT);
    }
    
    public boolean isUnknownSide() {
        return this.getSide() == Side.UNKNOWN;
    }
    
    public int getBridgeLength() {
        return Math.abs(rangeMax - rangeMin);
    }

    public int getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(int rangeMin) {
        this.rangeMin = rangeMin;
    }

    public int getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(int rangeMax) {
        this.rangeMax = rangeMax;
    }

    public int getNumberBridgePartners() {
        return numberBridgePartners;
    }

    public void setNumberBridgePartners(int numberBridgePartners) {
        this.numberBridgePartners = numberBridgePartners;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }
    
    public void update(int residueNumber) {
        if (residueNumber < rangeMin) rangeMin = residueNumber;
        if (residueNumber > rangeMax) rangeMax = residueNumber;
        numberBridgePartners++;
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

    public int compareTo(BridgePartner other) {
      return Integer.compare(this.numberBridgePartners, other.numberBridgePartners);
    }
    
    public int hashCode() {
        return getPartnerResidue();
    }
    
    public String toString() {
        return String.format("%s %s %s %s %s", 
                partner == null? "{}" : partner.getSymbolNumber(),
                side, bridgeType, rangeMin, rangeMax);
    }

}
