package tops.translation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import tops.translation.Geometer;

public class Sheet implements Iterable<BackboneSegment> {

    private int number;

    // The strand map has as the keys all the strands in the map;
    // the values are the other strands in the sheet that the key is attached to.
    // In theory, a nice pure sheet would only have one value per key. In theory...
    private TreeMap<BackboneSegment, List<BackboneSegment>> strandMap;
    
    private Axis axis;

    public Sheet(int number) {
        this.number = number;
        this.strandMap = new TreeMap<BackboneSegment, List<BackboneSegment>>();
        this.axis = null;
    }

    public Sheet(int number, BackboneSegment first, BackboneSegment second) {
        this(number);
        this.addPair(first, second);
    }

    public void addPair(BackboneSegment first, BackboneSegment second) {
        if (first.compareTo(second) < 0) {
            this.map(first, second);
        } else {
            this.map(second, first);
        }
    }

    public void map(BackboneSegment keyStrand, BackboneSegment partner) {
        List<BackboneSegment> values;
        if (this.strandMap.containsKey(keyStrand)) {
            values = this.strandMap.get(keyStrand);
        } else {
            values = new ArrayList<BackboneSegment>();
            this.strandMap.put(keyStrand, values);
        }
        values.add(partner);
    }

    public List<BackboneSegment> getPartners(BackboneSegment key) {
        return this.strandMap.get(key);
    }

    public Iterator<BackboneSegment> getPartnerIterator(BackboneSegment key) {
        return this.getPartners(key).iterator();
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
        List<Point3d> centers = new ArrayList<Point3d>();
        for (BackboneSegment strand : this.strandMap.keySet()) {
            centers.add(strand.getAxis().getCentroid());
        }
        return Geometer.averagePoints(centers);
    }

    public int size() {
        int size = 0;
        for (BackboneSegment strand : this.strandMap.keySet()) {
            size++;
            size += this.strandMap.get(strand).size();
        }
        return size;
    }
    
    public void extend(Sheet other) {
        Iterator<BackboneSegment> keyIterator = other.iterator();
        while (keyIterator.hasNext()) {
            BackboneSegment key = (BackboneSegment) keyIterator.next();
            List<BackboneSegment> otherValues = other.getPartners(key);

            if (this.strandMap.containsKey(key)) {
                List<BackboneSegment> thisValues = this.getPartners(key);
                thisValues.addAll(otherValues);
            } else {
                this.strandMap.put(key, otherValues);
            }
        }
    }

    public Iterator<BackboneSegment> iterator() {
        return this.strandMap.keySet().iterator();
    }

    public Iterator<BackboneSegment> chainOrderIterator() {
        List<BackboneSegment> chainOrder = new ArrayList<BackboneSegment>();
        chainOrder.addAll(this.strandMap.keySet());
        Iterator<BackboneSegment> iterator = this.iterator();
        while (iterator.hasNext()) {
            List<BackboneSegment> partners = this.strandMap.get(iterator.next());
            for (int i = 0; i < partners.size(); i++) {
                BackboneSegment partner = (BackboneSegment) partners.get(i);
                if (!chainOrder.contains(partner)) {
                    chainOrder.add(partner);
                }
            }
        }
        Collections.sort(chainOrder);
        return chainOrder.iterator();
    }

    public boolean contains(BackboneSegment strand) {
        if (this.strandMap.containsKey(strand)) {
            return true;
        } else {
            Iterator<BackboneSegment> keyIterator = this.iterator();
            while (keyIterator.hasNext()) {
                if (this.strandMap.get(keyIterator.next()).contains(strand)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public List<List<BackboneSegment>> getSheetPaths() {
        List<List<BackboneSegment>> paths = new ArrayList<List<BackboneSegment>>();
        Iterator<BackboneSegment> iterator = this.iterator();
        while (iterator.hasNext()) {
            BackboneSegment key = (BackboneSegment) iterator.next();
            paths.add(this.traverseSheetPath(key, new ArrayList<BackboneSegment>()));
        }
        return paths;
    }

    public List<BackboneSegment> traverseSheetPath(BackboneSegment currentStrand, List<BackboneSegment> path) {
        List<BackboneSegment> partners = this.strandMap.get(currentStrand);
        for (int i = 0; i < partners.size(); i++) {
            BackboneSegment partner = partners.get(i);
            path.add(partner);
            if (this.strandMap.containsKey(partner)) {
                this.traverseSheetPath(partner, path);            
            } else {
                return path;
            }
        } 
        return path;
    }

    public void reverse() {
//        Collections.reverse(this.strands);
    }

    public void closeBarrel(BackboneSegment first, BackboneSegment second) {
//        BackboneSegment firstInSheet = this.strands.get(0);
//        BackboneSegment lastInSheet = this.strands.get(this.strands.size() - 1);
//
//        // so [1 - 2 - 3] + [1, 3] => [1 - 2 - 3 - 1]
//        if (firstInSheet == first || lastInSheet == second) {
//            this.strands.add(first);
//            // so [4 - 1 - 2 - 3] + [3, 4] => [4 - 1 - 2 - 3 - 4]
//        } else if (firstInSheet == second || lastInSheet == first) {
//            this.strands.add(second);
//        } else {
            // we have a problem, both strands are in the sheet, but the
            // connection is bifurcated
            // System.err.println("Possible bifurcation in " + this + "
            // connections : " + first + ", " + second);
//        }
    }

    public int indexOf(BackboneSegment strand) {
//        return this.strands.indexOf(strand);
        return -1;  /// XXX TODO
    }

    public boolean strandInMiddle(BackboneSegment strand) {
//        BackboneSegment firstInSheet = this.strands.get(0);
//        BackboneSegment lastInSheet = this.strands.get(this.size() - 1);
//        return firstInSheet != strand && lastInSheet != strand;
        return false;   // XXX TODO
    }

    public void insert(BackboneSegment existingStrand, BackboneSegment newStrand) {
//        int indexOfExistingStrand = this.indexOf(existingStrand);
//
//        if (indexOfExistingStrand == 0) {
//            this.strands.add(0, newStrand);
//        } else if (indexOfExistingStrand == this.size() - 1) {
//            this.strands.add(newStrand);
//        } else {
//            // can this happen? - apparently it can...
//            // System.err.println("WARNING Adding : " + newStrand + " to
//            // position " + indexOfExistingStrand + " in " + this);
//            this.strands.add(indexOfExistingStrand, newStrand);
//        }
//        this.reorient();
    }

    public void reorient() {
//        BackboneSegment firstInSheet = this.strands.get(0);
//        BackboneSegment lastInSheet = this.strands.get(this.size() - 1);
//        if (firstInSheet.compareTo(lastInSheet) == 1) {
//            this.reverse();
//        }
    }

    public void assignOrientationsToStrands() {

        // while we're at it, we might as well calculate the sheet axis
        Vector3d sheetVector = new Vector3d();

        // reset the iterator
        Iterator<BackboneSegment> iterator = this.iterator();
        while (iterator.hasNext()) {
            BackboneSegment strand = iterator.next();
            if (strand.getOrientation().equals("None")) {
                strand.setOrientation("UP");
            }

            Iterator<BackboneSegment> partnerIterator = this.getPartnerIterator(strand);
            while (partnerIterator.hasNext()) {
                BackboneSegment partner = partnerIterator.next();
                this.assignOrientations(strand, partner);

                // add to the average vector, or subtract if DOWN
                if (partner.getOrientation().equals("UP")) {
                    sheetVector.add(partner.getAxis().getAxisVector());
                } else {
                    sheetVector.sub(partner.getAxis().getAxisVector());
                }
                Logger.getLogger("translation.FoldAnalyser").info("orientation " + strand + " -> " + partner);
            }
        }

        sheetVector.normalize();
        this.setAxis(sheetVector);
    }

    private void assignOrientations(BackboneSegment strand, BackboneSegment partner) {
        char relativeOrientation = strand.getRelativeOrientation(partner);
        if (relativeOrientation == 'P') {
            String strandOrientation = strand.getOrientation();
            if (strandOrientation.equals("None")) {
                String partnerOrientation = partner.getOrientation();
                if (partnerOrientation.equals("None")) {
                    Logger.getLogger("translation.FoldAnalyser").info("No orientation known for " + strand + " and " + partner);
                } else {
                    Logger.getLogger("translation.FoldAnalyser").info("Assigning orientation : " + partnerOrientation + " to " + strand);
                    strand.setOrientation(partnerOrientation);
                }
            } else {
                Logger.getLogger("translation.FoldAnalyser").info("Assigning orientation : " + strandOrientation + " to " + partner);
                partner.setOrientation(strandOrientation);
            }
        } else {
            String strandOrientation = strand.getOrientation();
            if (strandOrientation.equals("None")) {
                String partnerOrientation = partner.getOrientation();
                if (partnerOrientation.equals("None")) {
                    Logger.getLogger("translation.FoldAnalyser").info("No orientation known for " + strand + " and " + partner);
                } else {
                    Logger.getLogger("translation.FoldAnalyser").info("Assigning orientation : " + partnerOrientation + " to " + strand);
                    strand.setOrientation(partnerOrientation);
                }
            } else if (strandOrientation.equals("UP")) {
                Logger.getLogger("translation.FoldAnalyser").info("Assigning orientation : UP to " + partner);
                partner.setOrientation("DOWN");
            } else if (strandOrientation.equals("DOWN")) {
                Logger.getLogger("translation.FoldAnalyser").info("Assigning orientation : DOWN to " + partner);
                partner.setOrientation("UP");
            }
        }
    }

    public List<Edge> toTopsEdges(Domain domain) {
        List<Edge> edges = new ArrayList<Edge>();

        for (BackboneSegment strand : this.strandMap.keySet()) {
            if (!domain.contains(strand)) {
                continue;
            }

            Iterator<BackboneSegment> partnerIterator = this.getPartnerIterator(strand);
            while (partnerIterator.hasNext()) {
                BackboneSegment partner = (BackboneSegment) partnerIterator.next();
                if (!domain.contains(partner)) {
                    continue;
                }

                char relativeOrientation = strand.getRelativeOrientation(partner); 
                Edge edge;
                if (strand.compareTo(partner) < 0) {
                    edge = new Edge(strand, partner, relativeOrientation);
                } else {
                    edge = new Edge(partner, strand, relativeOrientation);
                }
                //System.err.println("Made edge : " + edge);

                // since the mapping is symmetric, we have to discard half the edges we make!
                if (!edges.contains(edge)) {
                    edges.add(edge);
                }
            }
        } 

        return edges;
    }

    @Override
    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        returnValue.append("Sheet (" + this.number + ") [");

        Iterator<BackboneSegment> iterator = this.strandMap.keySet().iterator();

        while (iterator.hasNext()) {
            BackboneSegment strand = (BackboneSegment) iterator.next();
            returnValue.append(strand);

            Iterator<BackboneSegment> partnerIterator = this.getPartnerIterator(strand);
            while (partnerIterator.hasNext()) {
                BackboneSegment partner = (BackboneSegment) partnerIterator.next();
                returnValue.append(" -> ").append(partner); 
            }
            returnValue.append("\n");
        }
        returnValue.append("]");
        return returnValue.toString();
    }

}
