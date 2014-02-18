package tops.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Sheet {

    private int number;

    private List<BackboneSegment> strands;

    private Axis axis;

    public Sheet(int number) {
        this.number = number;
        this.strands = new ArrayList<BackboneSegment>();
        this.axis = null;
    }

    public Sheet(int number, BackboneSegment first, BackboneSegment second) {
        this(number);
        this.strands.add(first);
        this.strands.add(second);
    }

    public int getNumber() {
        return this.number;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    public void setAxis(Vector3d axisVector) {
        this.setAxis(new Axis(this.calculateCentroid(), axisVector));
    }

    public Axis getAxis() {
        return this.axis;
    }

    public Point3d calculateCentroid() {
        ArrayList<Point3d> centers = new ArrayList<Point3d>();
        Iterator<BackboneSegment> iterator = this.strands.iterator();
        while (iterator.hasNext()) {
            BackboneSegment strand = (BackboneSegment) iterator.next();
            centers.add(strand.getAxis().getCentroid());
        }
        return Geometer.averagePoints(centers);
    }

    public int size() {
        return this.strands.size();
    }

    public void reverse() {
        Collections.reverse(this.strands);
    }

    public void extend(Sheet other) {
        this.strands.addAll(other.strands);
    }

    public void closeBarrel(BackboneSegment first, BackboneSegment second) {
        BackboneSegment firstInSheet = (BackboneSegment) this.strands.get(0);
        BackboneSegment lastInSheet = (BackboneSegment) this.strands
                .get(this.strands.size() - 1);

        // so [1 - 2 - 3] + [1, 3] => [1 - 2 - 3 - 1]
        if (firstInSheet == first || lastInSheet == second) {
            this.strands.add(first);
            // so [4 - 1 - 2 - 3] + [3, 4] => [4 - 1 - 2 - 3 - 4]
        } else if (firstInSheet == second || lastInSheet == first) {
            this.strands.add(second);
        } else {
            // we have a problem, both strands are in the sheet, but the
            // connection is bifurcated
            // System.err.println("Possible bifurcation in " + this + "
            // connections : " + first + ", " + second);
        }
    }

    public Iterator<BackboneSegment> iterator() {
        return this.strands.iterator();
    }

    public boolean contains(BackboneSegment strand) {
        return this.strands.contains(strand);
    }

    public int indexOf(BackboneSegment strand) {
        return this.strands.indexOf(strand);
    }

    public boolean strandInMiddle(BackboneSegment strand) {
        BackboneSegment firstInSheet = (BackboneSegment) this.strands.get(0);
        BackboneSegment lastInSheet = (BackboneSegment) this.strands.get(this
                .size() - 1);
        return firstInSheet != strand && lastInSheet != strand;
    }

    public void insert(BackboneSegment existingStrand, BackboneSegment newStrand) {
        int indexOfExistingStrand = this.indexOf(existingStrand);

        if (indexOfExistingStrand == 0) {
            this.strands.add(0, newStrand);
        } else if (indexOfExistingStrand == this.size() - 1) {
            this.strands.add(newStrand);
        } else {
            // can this happen? - apparently it can...
            // System.err.println("WARNING Adding : " + newStrand + " to
            // position " + indexOfExistingStrand + " in " + this);
            this.strands.add(indexOfExistingStrand, newStrand);
        }
        this.reorient();
    }

    public void reorient() {
        BackboneSegment firstInSheet = (BackboneSegment) this.strands.get(0);
        BackboneSegment lastInSheet = (BackboneSegment) this.strands.get(this
                .size() - 1);
        if (firstInSheet.compareTo(lastInSheet) == 1) {
            this.reverse();
        }
    }

    public void assignOrientationsToStrands() {
        // arbitrarily assign the first strand as UP
        BackboneSegment firstStrand = (BackboneSegment) this.strands.get(0);
        firstStrand.setOrientation("UP");

        // while we're at it, we might as well calculate the sheet axis
        Vector3d sheetVector = new Vector3d();
        sheetVector.set(firstStrand.getAxis().getAxisVector());

        for (int i = 0; i < this.strands.size() - 1; i++) {
            BackboneSegment strand = (BackboneSegment) this.strands.get(i);
            BackboneSegment partner = (BackboneSegment) this.strands.get(i + 1);
            // System.out.println(strand + " -> " + partner);

            this.assignOrientations(strand, partner);

            // add to the average vector, or subtract if DOWN
            if (partner.getOrientation().equals("UP")) {
                sheetVector.add(partner.getAxis().getAxisVector());
            } else {
                sheetVector.sub(partner.getAxis().getAxisVector());
            }
        }

        sheetVector.normalize();
        this.setAxis(sheetVector);
    }

    private void assignOrientations(BackboneSegment strand,
            BackboneSegment partner) {
        char relativeOrientation = strand.getRelativeOrientation(partner);
        if (relativeOrientation == 'P') {
            String strandOrientation = strand.getOrientation();
            if (strandOrientation.equals("None")) {
                String partnerOrientation = partner.getOrientation();
                if (partnerOrientation.equals("None")) {
                    // System.out.println("No orientation known for " + strand +
                    // " and " + partner);
                } else {
                    // System.out.println("Assigning orientation : " +
                    // partnerOrientation + " to " + strand);
                    strand.setOrientation(partnerOrientation);
                }
            } else {
                // System.out.println("Assigning orientation : " +
                // strandOrientation + " to " + partner);
                partner.setOrientation(strandOrientation);
            }
        } else {
            String strandOrientation = strand.getOrientation();
            if (strandOrientation.equals("None")) {
                String partnerOrientation = partner.getOrientation();
                if (partnerOrientation.equals("None")) {
                    // System.out.println("No orientation known for " + strand +
                    // " and " + partner);
                } else {
                    // System.out.println("Assigning orientation : " +
                    // partnerOrientation + " to " + strand);
                    strand.setOrientation(partnerOrientation);
                }
            } else if (strandOrientation.equals("UP")) {
                // System.out.println("Assigning orientation : UP to " +
                // partner);
                partner.setOrientation("DOWN");
            } else if (strandOrientation.equals("DOWN")) {
                // System.out.println("Assigning orientation : DOWN to " +
                // partner);
                partner.setOrientation("UP");
            }
        }
    }

    public List<Edge> toTopsEdges(Domain domain) {
        List<Edge> edges = new ArrayList<Edge>();
        for (int i = 0; i < this.size() - 1; i++) {
            BackboneSegment strand = (BackboneSegment) this.strands.get(i);
            if (!domain.isEmpty() && !domain.contains(strand)) {
                continue;
            }

            BackboneSegment partner = (BackboneSegment) this.strands.get(i + 1);
            if (!domain.isEmpty() && !domain.contains(partner)) {
                continue;
            }

            char relativeOrientation = strand.getRelativeOrientation(partner);
            if (strand.compareTo(partner) < 0) {
                edges.add(new Edge(strand, partner, relativeOrientation));
            } else {
                edges.add(new Edge(partner, strand, relativeOrientation));
            }
        }
        return edges;
    }

    @Override
    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append("Sheet (" + this.number + ") [");

        Iterator<BackboneSegment> iterator = this.strands.iterator();
        returnValue.append(iterator.next());
        while (iterator.hasNext()) {
            BackboneSegment strand = (BackboneSegment) iterator.next();
            returnValue.append(" - " + strand);
        }
        returnValue.append("]");
        return returnValue.toString();
    }

}
