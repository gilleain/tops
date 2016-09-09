package tops.port.model;

public class HBond implements Comparable<HBond> {
    
    private int donorIndex;
    
    private int acceptorIndex;
    
    private double energy;
    
    public HBond(int donorIndex, int acceptorIndex, double energy) {
        this.donorIndex = donorIndex;
        this.acceptorIndex = acceptorIndex;
        this.energy = energy;
    }
    
    @Override
    public int compareTo(HBond other) {
        if (this.equals(other)) {
            return 0;
        } else {
            if (this.greaterThan(other)) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    public boolean equals(Object other) {
        if (other instanceof HBond) {
            HBond otherHBond = (HBond) other;
            return donorIndex == otherHBond.donorIndex && 
                   acceptorIndex == otherHBond.acceptorIndex;
        }
        return false;
    }
    
    public int hashCode() {
        return donorIndex * acceptorIndex;
    }
    
    public boolean greaterThan(HBond other) {
        // TODO : less function calls...
        int min = Math.min(donorIndex, acceptorIndex);
        int max = Math.max(donorIndex, acceptorIndex);
        int otherMin = Math.min(other.donorIndex, other.acceptorIndex);
        int otherMax = Math.max(other.donorIndex, other.acceptorIndex);
        return min > otherMin || (min == otherMin && max > otherMax);
    }

    public int getDonorIndex() {
        return donorIndex;
    }

    public void setDonorIndex(int donorIndex) {
        this.donorIndex = donorIndex;
    }

    public int getAcceptorIndex() {
        return acceptorIndex;
    }

    public void setAcceptorIndex(int acceptorIndex) {
        this.acceptorIndex = acceptorIndex;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }
    
    public String toString() {
        if (donorIndex < acceptorIndex) {
            return String.format("%s->%s %2.2f", donorIndex, acceptorIndex, energy);
        } else {
            return String.format("%s<-%s %2.2f", acceptorIndex, donorIndex, energy);
        }
    }

}
