package tops.translation;

import java.util.Iterator;

public class Strand extends RepetitiveStructure {

    private static int phiMin = -170;

    private static int phiMax = -60;

    private static int psiMin = 110;

    private static int psiMax = 180;

    public Strand() {
        super();
    }

    public Strand(Residue first) {
        super(first);
    }

    @Override
    public char getTypeChar() {
        if (this.orientation.equals("UP")) {
            return 'E';
        } else {
            return 'e';
        }
    }

    public static boolean torsionsMatch(Residue r) {
        return RepetitiveStructure.torsionsMatch(r, Strand.phiMin,
                Strand.phiMax, Strand.psiMin, Strand.psiMax);
    }

    public static boolean hbondsMatch(Residue r) {
        Iterator<HBond> iterator = r.getHBondIterator();
        while (iterator.hasNext()) {
            HBond hbond = (HBond) iterator.next();
            if (hbond.hasSheetResidueSeparation()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Strand : " + this.firstResidue() + " - " + this.lastResidue();
    }

    @Override
    public String toFullString() {
        return "Strand : " + this.orientation + " " + this.firstResidue()
                + " - " + this.lastResidue() + " " + this.axis;
    }
}
