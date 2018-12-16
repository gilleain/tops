package tops.translation.model;


public class HBond implements Comparable<HBond> {
    private Residue donor;
    private Residue acceptor;
    private double distance;
    private double nhoAngle;
    private double hocAngle;
    
    // XXX - this is odd, we have two different ways to define an hbond - 
    // different classes?
    private double energy;

    /**
     * Construct an HBond from geometric parameters.
     */
    public HBond(Residue donor, Residue acceptor, double distance, double nhoAngle, double hocAngle) {
        this.donor = donor;
        this.acceptor = acceptor;
        this.distance = distance;
        this.nhoAngle = nhoAngle;
        this.hocAngle = hocAngle;
    }
    
    /**
     * Construct an HBond with just an energy.
     */
    public HBond(Residue donor, Residue acceptor, double energy) {
        this.donor = donor;
        this.acceptor = acceptor;
        this.energy = energy;
    }

    //sort by donor, then acceptor (well, why not, eh?)
    public int compareTo(HBond other) {
        int c = Integer.valueOf(this.donor.getAbsoluteNumber()).compareTo(Integer.valueOf(other.donor.getAbsoluteNumber()));
        if (c == 0) {
            return Integer.valueOf(this.acceptor.getAbsoluteNumber()).compareTo(Integer.valueOf(other.acceptor.getAbsoluteNumber()));
        } else {
            return c;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((acceptor == null) ? 0 : acceptor.hashCode());
        long temp;
        temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((donor == null) ? 0 : donor.hashCode());
        temp = Double.doubleToLongBits(energy);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(hocAngle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nhoAngle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HBond other = (HBond) obj;
        if (acceptor == null) {
            if (other.acceptor != null)
                return false;
        } else if (!acceptor.equals(other.acceptor))
            return false;
        if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
            return false;
        if (donor == null) {
            if (other.donor != null)
                return false;
        } else if (!donor.equals(other.donor))
            return false;
        if (Double.doubleToLongBits(energy) != Double.doubleToLongBits(other.energy))
            return false;
        if (Double.doubleToLongBits(hocAngle) != Double.doubleToLongBits(other.hocAngle))
            return false;
        if (Double.doubleToLongBits(nhoAngle) != Double.doubleToLongBits(other.nhoAngle))
            return false;
        return true;
    }

    public Residue getPartner(Residue residue) {
        if (residue == this.donor) {
            return this.acceptor;
        } else {
            return this.donor;
        }
    }

    public boolean contains(Residue residue) {
        return this.donor == residue || this.acceptor == residue;
    }

    public int getResidueSeparation() {
        return Math.abs(this.donor.getAbsoluteNumber() - this.acceptor.getAbsoluteNumber());
    }

    public boolean hasHelixResidueSeparation() {
        return this.getResidueSeparation() == 4;
    }

    public boolean hasSheetResidueSeparation() {
        return this.getResidueSeparation() > 4;
    }

    public Residue acceptor() {
        return this.acceptor;
    }

    public boolean residueIsAcceptor(Residue residue) {
        return this.acceptor == residue;
    }

    public Residue donor() {
        return this.donor;
    }

    public boolean residueIsDonor(Residue residue) {
        return this.donor == residue;
    }

    public String toFullString() {
        return String.format("%3s - %3s (%4.2f, %6.2f, %6.2f) [%2.2f]", this.donor, this.acceptor, this.distance, this.nhoAngle, this.hocAngle, this.energy);        
    }
    
    public String toString() {
        return "";
    }
}
