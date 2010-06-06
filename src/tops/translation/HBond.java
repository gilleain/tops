package tops.translation;

public class HBond implements Comparable {

    private Residue donor;

    private Residue acceptor;

    private double distance;

    private double nhoAngle;

    private double hocAngle;

    public HBond(Residue donor, Residue acceptor, double distance,
            double nhoAngle, double hocAngle) {
        this.donor = donor;
        this.acceptor = acceptor;
        this.distance = distance;
        this.nhoAngle = nhoAngle;
        this.hocAngle = hocAngle;
    }

    // sort by donor, then acceptor (well, why not, eh?)
    public int compareTo(Object o) {
        HBond other = (HBond) o;
        int c = Integer.valueOf(this.donor.getAbsoluteNumber()).compareTo(
                Integer.valueOf(other.donor.getAbsoluteNumber()));
        if (c == 0) {
            return Integer
                    .valueOf(this.acceptor.getAbsoluteNumber())
                    .compareTo(
                            Integer.valueOf(other.acceptor.getAbsoluteNumber()));
        } else {
            return c;
        }
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
        return Math.abs(this.donor.getAbsoluteNumber()
                - this.acceptor.getAbsoluteNumber());
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

    @Override
    public String toString() {
        return String.format("%3s - %3s (%4.2f, %6.2f, %6.2f)",
                this.donor, this.acceptor, this.distance, this.nhoAngle,
                this.hocAngle);
    }
}
