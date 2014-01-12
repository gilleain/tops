package tops.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.vecmath.Point3d;

public class Residue implements Comparable<Object> {

    private HashMap<String, Point3d> atoms;

    private int absoluteNumber;

    private int pdbNumber;

    private String type;

    private String polymerType;

    private String environment;

    private ArrayList<HBond> hBonds;

    private double phi;

    private double psi;

    public Residue() {
        this.atoms = new HashMap<String, Point3d>();
        this.hBonds = new ArrayList<HBond>();
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

    public int compareTo(Object o) {
        Residue other = (Residue) o;
        return Integer.valueOf(this.absoluteNumber).compareTo(
                Integer.valueOf(other.absoluteNumber));
    }

    public boolean isBase() {
        return this.type.equals("A") || this.type.equals("C")
                || this.type.equals("G") || this.type.equals("T");
    }

    public boolean isStandardAminoAcid() {
        return this.type.equals("ALA") || this.type.equals("ARG")
                || this.type.equals("ASP") || this.type.equals("ASN")
                || this.type.equals("CYS") || this.type.equals("GLN")
                || this.type.equals("GLU") || this.type.equals("GLY")
                || this.type.equals("HIS") || this.type.equals("ILE")
                || this.type.equals("LEU") || this.type.equals("LYS")
                || this.type.equals("MET") || this.type.equals("PHE")
                || this.type.equals("PRO") || this.type.equals("SER")
                || this.type.equals("THR") || this.type.equals("TRP")
                || this.type.equals("TYR") || this.type.equals("VAL");
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

    public ArrayList<HBond> getNTerminalHBonds() {
        ArrayList<HBond> nTerminalHBonds = new ArrayList<HBond>();
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hBond = (HBond) this.hBonds.get(i);
            if (hBond.residueIsDonor(this)) {
                // System.out.println("N : " + hBond + " for " + this);
                nTerminalHBonds.add(hBond);
            }
        }
        return nTerminalHBonds;
    }

    public ArrayList<HBond> getCTerminalHBonds() {
        ArrayList<HBond> cTerminalHBonds = new ArrayList<HBond>();
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hBond = (HBond) this.hBonds.get(i);
            if (hBond.residueIsAcceptor(this)) {
                // System.out.println("C : " + hBond + " for " + this);
                cTerminalHBonds.add(hBond);
            }
        }
        return cTerminalHBonds;
    }

    public int[] getHBondPartners() {
        int[] partners = new int[this.hBonds.size()];
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hbond = (HBond) this.hBonds.get(i);
            partners[i] = hbond.getPartner(this).getAbsoluteNumber();
        }
        return partners;
    }

    public boolean bondedTo(Residue other) {
        for (int i = 0; i < this.hBonds.size(); i++) {
            HBond hbond = (HBond) this.hBonds.get(i);
            if (hbond == null) {
                System.err.println("hbond null");
                continue;
            }
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

    public void setAtom(String atomType, String xyz) {
        Point3d coordinates = this.parseXYZ(xyz);
        this.atoms.put(atomType, coordinates);
    }

    public void setAtom(String atomType, Point3d coordinates) {
        this.atoms.put(atomType, coordinates);
    }

    public Point3d parseXYZ(String xyz) {
        String[] bits = xyz.split("\\s+");
        double[] coordinates = new double[3];
        try {
            for (int i = 0; i < 3; i++) {
                coordinates[i] = Double.parseDouble(bits[i]);
            }
        } catch (ArrayIndexOutOfBoundsException a) {
            System.err.println("index out of bounds in parseXYZ in residue : "
                    + this.getPDBNumber());
            return new Point3d();
        } catch (NumberFormatException n) {
            System.err
                    .println("number format exception in parseXYZ in residue : "
                            + this.getPDBNumber() + " xyz string = " + xyz);
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
        return (Point3d) this.atoms.get(atomType);
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
        StringBuffer strbuf = new StringBuffer();
        Iterator<HBond> itr = this.hBonds.iterator();
        while (itr.hasNext()) {
            HBond hbond = (HBond) itr.next();
            strbuf.append(hbond).append(" ");
        }
        return strbuf.toString();
    }

    public String toFullString() {
        return String.format("%s-%-3d %-3d %-19s [%6.2f, %6.2f] %s", this.type,
                this.pdbNumber, this.absoluteNumber, this.environment,
                this.phi, this.psi, this.hBondString());
    }

    @Override
    public String toString() {
        return String.format("%s-%d ", this.type, this.pdbNumber);
    }

    public static void main(String[] args) {
        Residue r = new Residue(1, 1, "TYR");
        r.setAtom(args[0], args[1]);
        System.out.println(r.toString());
    }
}
