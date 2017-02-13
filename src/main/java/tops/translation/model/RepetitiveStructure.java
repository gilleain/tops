package tops.translation.model;


public abstract class RepetitiveStructure extends BackboneSegment {

    public RepetitiveStructure() {
        super();
    }

    public RepetitiveStructure(Residue residue) {
        super(residue);
    }

    public static boolean torsionsMatch(Residue residue, int phiMin, int phiMax, int psiMin, int psiMax) {
        double phi = residue.getPhi();
        double psi = residue.getPsi();
        boolean phiMatches = phi < phiMax && phi > phiMin;
        boolean psiMatches = psi < psiMax && psi > psiMin;
        return phiMatches && psiMatches;
    }

}
