package tops.translation.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import javax.vecmath.Point3d;

public abstract class BackboneSegment implements Comparable<BackboneSegment>, Iterable<Residue> {

    protected int number;

    protected TreeSet<Residue> residues;

    protected Axis axis;

    protected String orientation;

    public BackboneSegment() {
        this.residues = new TreeSet<>();
        this.axis = null;
        this.orientation = "None";
    }

    public BackboneSegment(Residue first) {
        this();
        this.residues.add(first);
    }

    public abstract String toFullString();

    public abstract char getTypeChar();

    public int compareTo(BackboneSegment other) {
        try {
            return this.firstResidue().compareTo(other.firstResidue());
        } catch (NoSuchElementException n) {
            return 0;
        }
    }
    
    public boolean equals(Object o) {
        if (o instanceof BackboneSegment) {
            BackboneSegment other = (BackboneSegment) o;
            if (residues.isEmpty()) {
                return other.residues.isEmpty();
            } else {
                if (other.residues.isEmpty()) {
                    return false;
                } else {
                    return firstResidue().equals(other.firstResidue());
                }
            }
        }
        return false;
    }
    
    public int hashCode() {
        return residues.hashCode();
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    public int length() {
        return this.residues.size();
    }

    public int firstPDB() {
        try {
            return this.firstResidue().getPDBNumber();
        } catch (NoSuchElementException n) {
            return -1;
        }
    }

    public int lastPDB() {
        try {
            return this.lastResidue().getPDBNumber();
        } catch (NoSuchElementException n) {
            return -1;
        }
    }

    public Residue firstResidue() {
        return this.residues.first();
    }

    public Residue lastResidue() {
        return this.residues.last();
    }

    public Iterator<Residue> iterator() {
        return this.residues.iterator();
    }
    
    public Residue getResidueByAbsoluteNumber(int number) {
        for (Residue residue : this.residues) {
            if (residue.getAbsoluteNumber() == number) {
                return residue;
            }
        }
        return null;
    }

    public boolean bondedTo(Residue otherResidue) {
        for (Residue residue : this) {
            if (residue.bondedTo(otherResidue)) {
                return true;
            }
        }
        return false;
    }

    public boolean continuousWith(BackboneSegment other) {
        if (this.getClass() == other.getClass()) {
            return this.getAxis().approximatelyLinearTo(other.getAxis());
        } else {
            return false;
        }
    }

    public void mergeWith(BackboneSegment other) {
        this.residues.addAll(other.residues);
        this.axis = null;
    }

    public boolean overlaps(BackboneSegment other) {
        try {
            int oS = other.firstResidue().getAbsoluteNumber();
            int oE = other.lastResidue().getAbsoluteNumber();
            return this.containsAbsoluteNumber(oS)
                    || this.containsAbsoluteNumber(oE);
        } catch (NoSuchElementException n) {
            return false;
        }
    }

    public List<Point3d> getCAlphaCoordinates() {
        List<Point3d> cAlphas = new ArrayList<>();
        Iterator<Residue> itr = this.residues.iterator();
        while (itr.hasNext()) {
            Residue nextResidue = itr.next();
            cAlphas.add(nextResidue.getCoordinates("CA"));
        }
        return cAlphas;
    }

    public char getTopsSymbol() {
        if (this.getOrientation().equals("UP")) {
            return Character.toUpperCase(this.getTypeChar());
        } else {
            return Character.toLowerCase(this.getTypeChar());
        }
    }

    public String getOrientation() {
        return this.orientation;
    }

    public void expandBy(Residue r) {
        this.residues.add(r);
    }

    public boolean containedInPDBNumberRange(int pdbResidueNumberStart,
            int pdbResidueNumberEnd) {
        try {
            return this.firstPDB() >= pdbResidueNumberStart
                    && this.lastPDB() <= pdbResidueNumberEnd;
        } catch (NoSuchElementException n) {
            return false;
        }
    }
    
    public boolean overlapsPDBNumberRange(int pdbResidueNumberStart, int pdbResidueNumberEnd) {
        try {
            int first = this.firstPDB();
            int last = this.lastPDB();
            return (first >= pdbResidueNumberStart && first <= pdbResidueNumberEnd) ||
                    (last >= pdbResidueNumberStart && last <= pdbResidueNumberEnd);
        } catch (NoSuchElementException n) {
            return false;
        }
    }

    public boolean containsPDBNumber(int pdbResidueNumber) {
        for (Residue r : this) {
            if (r.getPDBNumber() == pdbResidueNumber) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAbsoluteNumber(int absoluteResidueNumber) {
        try {
            Residue first = this.firstResidue();
            Residue last = this.lastResidue();
            return first.getAbsoluteNumber() <= absoluteResidueNumber
                    && last.getAbsoluteNumber() >= absoluteResidueNumber;
        } catch (NoSuchElementException n) {
            return false;
        }
    }

    public boolean contains(Residue r) {
        return this.containsAbsoluteNumber(r.getAbsoluteNumber());
    }

    public Axis getAxis() {
        if (this.axis == null) {
            this.calculateAxis();
        }
        return this.axis;
    }

    public void calculateAxis() {
        // if this segment is only one or no residues, make a zero axis
        if (this.residues.size() < 2) {
            this.axis = new Axis();
            return;
        }

        // otherwise, diff the centers of the first and last residues
        try {
            Point3d start = this.firstResidue().getCenter();
            Point3d end = this.lastResidue().getCenter();
            this.axis = new Axis(start, end);
            this.axis.setStart(start);
            this.axis.setEnd(end);
        } catch (NoSuchElementException n) {
            // do nothing?
            System.out.println("NoSuchElementException for " + this);
        }
    }

    // the axis we pass into the function is considered to be "UP"
    public void determineOrientation(Axis axis) {
        double angle = this.getAxis().angle(axis);
        if (angle > 90) {
            this.orientation = "DOWN";
        } else {
            this.orientation = "UP";
        }
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public char getRelativeOrientation(BackboneSegment other) {
        return this.getRelativeOrientation(other.getAxis());
    }

    public char getRelativeOrientation(Axis other) {
        double angle = this.getAxis().angle(other);
        if (angle > 90) {
            return 'A';
        } else {
            return 'P';
        }
    }

    public List<Residue> getResidues() {
        List<Residue> residueList = new ArrayList<Residue>();
        for (Residue residue : this.residues) {
            residueList.add(residue);
        }
        return residueList;
    }
}
