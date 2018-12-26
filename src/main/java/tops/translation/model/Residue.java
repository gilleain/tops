package tops.translation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import tops.translation.Geometer;

public class Residue implements Comparable<Residue> {
    private Map<String, Point3d> atoms;
    private int absoluteNumber;
    private int pdbNumber;
    private String type;
    private String polymerType;
    private String environment;
    private List<HBond> hBonds;
    private double phi;
    private double psi;

    public Residue() {
        atoms = new HashMap<>();
        hBonds = new ArrayList<>();
        this.phi = 0;
        this.psi = 0;
        this.type = "None";
        this.polymerType = "None";
        this.environment = "None";
    }

    public Residue(int absoluteNumber, int pdbNumber) {
        this();
        this.absoluteNumber = absoluteNumber;
        this.pdbNumber = pdbNumber;
    }

    public Residue(int absoluteNumber, int pdbNumber, String type) {
        this(absoluteNumber, pdbNumber);
        this.type = type.trim();
        if (this.isBase()) {
            this.polymerType = "DNA";
        } else {
            this.polymerType = "Protein";
        }
    }

    public int compareTo(Residue other) {
        return Integer.valueOf(this.absoluteNumber).compareTo(Integer.valueOf(other.absoluteNumber));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + absoluteNumber;
        result = prime * result + ((atoms == null) ? 0 : atoms.hashCode());
        result = prime * result + ((environment == null) ? 0 : environment.hashCode());
//        result = prime * result + ((hBonds == null) ? 0 : hBonds.hashCode()); // TODO - recursive hashcode!
        result = prime * result + pdbNumber;
        long temp;
        temp = Double.doubleToLongBits(phi);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((polymerType == null) ? 0 : polymerType.hashCode());
        temp = Double.doubleToLongBits(psi);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Residue other = (Residue) obj;
        if (absoluteNumber != other.absoluteNumber)
            return false;
        if (atoms == null) {
            if (other.atoms != null)
                return false;
        } else if (!atoms.equals(other.atoms))
            return false;
        if (environment == null) {
            if (other.environment != null)
                return false;
        } else if (!environment.equals(other.environment))
            return false;
        if (hBonds == null) {
            if (other.hBonds != null)
                return false;
        } else if (!hBonds.equals(other.hBonds))
            return false;
        if (pdbNumber != other.pdbNumber)
            return false;
        if (Double.doubleToLongBits(phi) != Double.doubleToLongBits(other.phi))
            return false;
        if (polymerType == null) {
            if (other.polymerType != null)
                return false;
        } else if (!polymerType.equals(other.polymerType))
            return false;
        if (Double.doubleToLongBits(psi) != Double.doubleToLongBits(other.psi))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    public boolean isPro() {
        return this.type.equals("PRO");
    }

    public boolean isBase() {
        return this.type.equals("A") || this.type.equals("C") || this.type.equals("G") || this.type.equals("T");
    }

    public boolean isStandardAminoAcid() {
        return  this.type.equals("ALA") ||
                this.type.equals("ARG") ||
                this.type.equals("ASP") ||
                this.type.equals("ASN") ||
                this.type.equals("CYS") ||
                this.type.equals("GLN") ||
                this.type.equals("GLU") ||
                this.type.equals("GLY") ||
                this.type.equals("HIS") ||
                this.type.equals("ILE") ||
                this.type.equals("LEU") ||
                this.type.equals("LYS") ||
                this.type.equals("MET") ||
                this.type.equals("PHE") ||
                this.type.equals("PRO") ||
                this.type.equals("SER") ||
                this.type.equals("THR") ||
                this.type.equals("TRP") ||
                this.type.equals("TYR") ||
                this.type.equals("VAL");
    }

    public boolean isDNA() {
        return this.polymerType == "DNA";
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public void addHBond(HBond hbond) {
        this.hBonds.add(hbond);
    }

    public Iterator<HBond> getHBondIterator() {
        return this.hBonds.iterator();
    }

    public List<HBond> getNTerminalHBonds() {
    	List<HBond> nTerminalHBonds = new ArrayList<>();
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hBond = this.hBonds.get(i);
            if (hBond.residueIsDonor(this)) {
                nTerminalHBonds.add(hBond);
            }
        }
        return nTerminalHBonds;
    }

    public List<HBond> getCTerminalHBonds() {
    	List<HBond> cTerminalHBonds = new ArrayList<>();
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hBond = this.hBonds.get(i);
            if (hBond.residueIsAcceptor(this)) {
                cTerminalHBonds.add(hBond);
            }
        }
        return cTerminalHBonds;
    }

    public int[] getHBondPartners() {
        int[] partners = new int[this.hBonds.size()];
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hbond = hBonds.get(i);
            partners[i] = hbond.getPartner(this).getAbsoluteNumber();
        }
        return partners;
    }

    public boolean bondedTo(Residue other) {
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hbond = this.hBonds.get(i);
            if (hbond == null) { System.err.println("hbond null"); continue; }
            if (hbond.contains(other)) {
                return true;
            }
        }
        return false;
    }

    public void setPhi(double phi) {
        this.phi = phi;
    }

    public void setPsi(double psi) {
        this.psi = psi;
    }

    public double getPhi() {
        return this.phi;
    }

    public double getPsi() {
        return this.psi;
    }

    public void setAtom(String atomType, Point3d coordinates) {
        atoms.put(atomType, coordinates);
    }

    public void setAtom(String atomType, String xyz) {
        Point3d coordinates = this.parseXYZ(xyz);
        this.setAtom(atomType, coordinates);
    }

    public Point3d parseXYZ(String xyz) {
        String[] bits = xyz.split("\\s+");
        double[] coordinates = new double[3];
        try {
            for (int i = 0; i < 3; i++) {
                coordinates[i] = Double.parseDouble(bits[i]);
            }
        } catch (ArrayIndexOutOfBoundsException a) {
            System.err.println("index out of bounds in parseXYZ in residue : " + this.getPDBNumber());
            return new Point3d();
        } catch (NumberFormatException n) {
            System.err.println("number format exception in parseXYZ in residue : " + this.getPDBNumber() + " xyz string = " + xyz);
            return new Point3d();
        }
        return new Point3d(coordinates);
    }

    public Point3d getCenter() {
        if (this.atoms.containsKey("CA")) {
            return this.getCoordinates("CA");
        } else {
            return this.calculateCenterOfMass();
        }
    }

    public Point3d calculateCenterOfMass() {
        return Geometer.averagePoints(this.atoms.values());
    }

    public Point3d getCoordinates(String atomType) {
        return this.atoms.get(atomType);
    }

    public int getAbsoluteNumber() {
        return this.absoluteNumber;
    }

    public int getPDBNumber() {
        return this.pdbNumber;
    }

    public String getType() {
        return this.type;
    }

    public String hBondString() {
        StringBuilder strbuf = new StringBuilder();
        for (HBond hbond : this.hBonds) {
            strbuf.append(hbond).append(" ");
        }
        return strbuf.toString();
    }

    public String toFullString() {
        //return String.format("%s-%-3d %-3d %-19s [%6.2f, %6.2f] %s", this.type, this.pdbNumber, this.absoluteNumber, this.environment, this.phi, this.psi, this.hBondString());
        return "";
    }

    public String toString() {
        //return String.format("%s-%d ", this.type, this.pdbNumber);
        return this.type + " " + this.pdbNumber;
    }
}
