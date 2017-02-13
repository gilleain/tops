package tops.translation.model;

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

    public char getTypeChar() {
        if (this.orientation.equals("UP")) {
            return 'E';
        } else {
            return 'e';
        }
    }

    public static boolean torsionsMatch(Residue r) {
        return RepetitiveStructure.torsionsMatch(r, Strand.phiMin, Strand.phiMax, Strand.psiMin, Strand.psiMax);
    }

    public static boolean hbondsMatch(Residue r) {
        Iterator<HBond> iterator = r.getHBondIterator();
        while (iterator.hasNext()) {
            HBond hbond = iterator.next();
            if (hbond.hasSheetResidueSeparation()) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "Strand : " + this.firstResidue() + " - " + this.lastResidue();
    }

    public String toFullString() {
        return "Strand : " + this.orientation + " " + this.firstResidue() + " - " + this.lastResidue() + " " + this.axis;
    }
}
