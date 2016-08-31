package tops.port.model;

public class HBond {
    
    private int donorIndex;
    
    private int acceptorIndex;
    
    private double energy;
    
    public HBond(int donorIndex, int acceptorIndex, double energy) {
        this.donorIndex = donorIndex;
        this.acceptorIndex = acceptorIndex;
        this.energy = energy;
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
        return String.format("%s->%s %2.2f", donorIndex, acceptorIndex, energy);
    }

}
