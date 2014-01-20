package tops.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.vecmath.Point3d;

public class Chain implements Iterable<BackboneSegment> {

    private String label;

//    private String type;

//    private Point3d center;

    private List<Residue> residues;

    private List<HBond> hbonds;

    private List<Sheet> sheets;

    private List<BackboneSegment> backboneSegments;

    private List<Edge> chiralities;

//    private ArrayList domains;

    public Chain() {
        this.residues = new ArrayList<Residue>();
        this.hbonds = new ArrayList<HBond>();
        this.sheets = new ArrayList<Sheet>();
        this.backboneSegments = new ArrayList<BackboneSegment>();
        this.chiralities = new ArrayList<Edge>();
//        this.domains = new ArrayList();
//        this.center = null;
    }

    public Chain(String label) {
        this();
        this.label = label;
    }

    public int length() {
        return this.residues.size();
    }

    public boolean isDNA() {
        return ((Residue) this.residues.get(0)).isDNA();
    }

    public Residue createResidue(int pdbNumber, String residueType) {
        Residue r = new Residue(this.residues.size(), pdbNumber, residueType);
        this.residues.add(r);
        return r;
    }

    public void createHelix(int helixStartIndex, int helixEndIndex) {
        BackboneSegment helix = new Helix(
        		this.getResidueByAbsoluteNumbering(helixStartIndex));
        for (int i = helixStartIndex + 1; i < helixEndIndex + 1; i++) {
            helix.expandBy(this.getResidueByAbsoluteNumbering(i));
        }
        // System.err.println("Created " + helix + " from " + helixStartIndex +
        // " " + helixEndIndex);
        this.backboneSegments.add(helix);
    }

    public void createStrand(int strandStartIndex, int strandEndIndex) {
        BackboneSegment strand = new Strand(this
                .getResidueByAbsoluteNumbering(strandStartIndex));
        for (int i = strandStartIndex + 1; i < strandEndIndex + 1; i++) {
            strand.expandBy(this.getResidueByAbsoluteNumbering(i));
        }
        // System.err.println("Created " + strand + " from " + strandStartIndex
        // + " " + strandEndIndex);
        this.backboneSegments.add(strand);
    }

    public void createLoop(int startIndex, int endIndex) {
        BackboneSegment unstructured = new UnstructuredSegment(this
                .getResidueByAbsoluteNumbering(startIndex));
        for (int i = startIndex + 1; i < endIndex + 1; i++) {
            unstructured.expandBy(this.getResidueByAbsoluteNumbering(i));
        }
        this.backboneSegments.add(unstructured);
    }

    // firstly, assume that the sses are sorted so that for i < j; i.end < j.end
    // secondly assume that, if two helices overlap, they should be merged
    public void mergeHelices() {
        for (int i = 0; i < this.backboneSegments.size() - 1; i++) {
            BackboneSegment sseA = (BackboneSegment) this.backboneSegments
                    .get(i);
            if (sseA instanceof Helix) {
                BackboneSegment sseB = (BackboneSegment) this.backboneSegments
                        .get(i + 1);
                if (sseB instanceof Helix) {
                    if (sseA.overlaps(sseB)) {
                        // System.err.println("Merging : " + sseA + " and " +
                        // sseB);
                        sseB.mergeWith(sseA);

                        // System.err.println("Removing: " + sseA);
                        this.backboneSegments.remove(i);
                        i--;
                    } else {
                        // System.err.println("No overlap : " + sseA + " and " +
                        // sseB);
                    }
                }
            }
        }
    }

    public void addTerminii() {
        if (!((BackboneSegment) this.backboneSegments.get(0) instanceof Terminus)) {
            this.backboneSegments.add(0, new Terminus("N Terminus", 'N'));
        }

        if (!((BackboneSegment) this.backboneSegments.get(this.backboneSegments
                .size() - 1) instanceof Terminus)) {
            this.backboneSegments.add(new Terminus("C Terminus", 'C'));
        }
    }

    public void sortBackboneSegments() {
        Collections.sort(this.backboneSegments);
    }

    public boolean hasResidueByPDBNumbering(int pdbResidueNumber) {
        Iterator<Residue> itr = this.residues.iterator();
        while (itr.hasNext()) {
            Residue residue = (Residue) itr.next();
            if (residue.getPDBNumber() == pdbResidueNumber) {
                return true;
            }
        }
        return false;
    }

    public Residue getResidueByPDBNumbering(int pdbResidueNumber) {
        Iterator<Residue> itr = this.residues.iterator();
        while (itr.hasNext()) {
            Residue residue = (Residue) itr.next();
            if (residue.getPDBNumber() == pdbResidueNumber) {
                return residue;
            }
        }
        return null;
    }

    public Residue getResidueByAbsoluteNumbering(int i) {
        return (Residue) this.residues.get(i);
    }

    public boolean hasResidueByAbsoluteNumbering(int i) {
        return i < this.residues.size();
    }

    public Residue firstResidue() {
        return (Residue) this.residues.get(0);
    }

    public Residue lastResidue() {
        return (Residue) this.residues.get(this.residues.size() - 1);
    }

    public Residue getNextResidue(int i) {
        if (i + 1 >= this.residues.size()) {
            return null;
        } else {
            return (Residue) this.residues.get(i + 1);
        }
    }

    public Iterator<Residue> residueIterator() {
        return this.residues.iterator();
    }

    public Iterator<Residue> residueIterator(int i) {
        return this.residues.subList(i, this.residues.size() - 1).iterator();
    }

	public Iterator<HBond> hbondIterator() {
        Collections.sort(this.hbonds);
        return this.hbonds.iterator();
    }

    public void addSheet(Sheet sheet) {
        this.sheets.add(sheet);
    }

    public void createSheet(BackboneSegment strand, BackboneSegment otherStrand) {
        if (this.sheets.size() == 0) {
            this.sheets.add(new Sheet(1, strand, otherStrand));
        } else {
            int lastSheetNumber = ((Sheet) this.sheets
                    .get(this.sheets.size() - 1)).getNumber();
            this.sheets
                    .add(new Sheet(lastSheetNumber + 1, strand, otherStrand));
        }
    }

    public void removeSheet(Sheet sheet) {
        this.sheets.remove(sheet);
    }

    public Sheet getSheetContaining(BackboneSegment strand) {
        Iterator<Sheet> sheetIterator = this.sheets.iterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = (Sheet) sheetIterator.next();
            if (sheet.contains(strand)) {
                return sheet;
            }
        }
        return null;
    }

    public Iterator<Sheet> sheetIterator() {
        return this.sheets.iterator();
    }

    public int numberOfSheets() {
        return this.sheets.size();
    }

    public void addChirality(BackboneSegment first, BackboneSegment second,
            char chirality) {
        this.chiralities.add(new Edge(first, second, chirality));
    }

    public Axis getAxis() {
        Iterator<BackboneSegment> backboneSegmentIterator = this.backboneSegments.iterator();
        ArrayList<Point3d> centroids = new ArrayList<Point3d>();
        while (backboneSegmentIterator.hasNext()) {
            BackboneSegment nextBackboneSegment = (BackboneSegment) backboneSegmentIterator
                    .next();
            Axis a = nextBackboneSegment.getAxis();
            // System.out.println("Centroid for BS " + nextBackboneSegment + "
            // is " + a.getCentroid());
            centroids.add(a.getCentroid());
        }
        // System.out.println("averagePoints = " +
        // Geometer.averagePoints(centroids));
        Axis axis = Geometer.leastSquareAxis(centroids);

        // this is a slight hack to get start and end coordinates into the chain
        // axis
        // might be better to use the N terminus as the start?
        Point3d start = new Point3d(axis.getCentroid());
        start.scale(5, axis.getAxisVector());
        axis.setStart(start);
        Point3d end = new Point3d(axis.getCentroid());
        end.scale(-5, axis.getAxisVector());
        axis.setEnd(end);

        return axis;
    }

    public void addHBond(HBond hbond) {
        this.hbonds.add(hbond);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCathCompatibleLabel() {
        if (this.label.equals("  ")) {
            return "0";
        } else {
            return this.label.trim();
        }
    }

    public String getLabel() {
        return this.label;
    }

    public void addBackboneSegment(BackboneSegment backboneSegment) {
        this.backboneSegments.add(backboneSegment);
    }

    public ListIterator<BackboneSegment> backboneSegmentListIterator() {
        return this.backboneSegments.listIterator();
    }

    public ListIterator<BackboneSegment> backboneSegmentListIterator(int i) {
        return this.backboneSegments.listIterator(i);
    }

    public ListIterator<BackboneSegment> backboneSegmentListIterator(
            BackboneSegment backboneSegment) {
        return this.backboneSegments.listIterator(this.backboneSegments
                .indexOf(backboneSegment));
    }

    public ListIterator<BackboneSegment> backboneSegmentListIterator(
            BackboneSegment startSegment, BackboneSegment endSegment) {
        return this.backboneSegments.subList(
                this.backboneSegments.indexOf(startSegment) + 1,
                this.backboneSegments.indexOf(endSegment)).listIterator();
    }

    public int numberOfSegmentsInBetween(BackboneSegment startSegment,
            BackboneSegment endSegment) {
        return this.backboneSegments.subList(
                this.backboneSegments.indexOf(startSegment) + 1,
                this.backboneSegments.indexOf(endSegment)).size();
    }

    public void calculateTorsions() {
        Iterator<Residue> residueIterator = this.residueIterator();
        Residue previousResidue = null;
        Residue thisResidue = null;

        if (residueIterator.hasNext()) {
            previousResidue = (Residue) residueIterator.next();
        }

        if (residueIterator.hasNext()) {
            thisResidue = (Residue) residueIterator.next();
        }

        while (residueIterator.hasNext()) {
            Point3d prevC = previousResidue.getCoordinates("C");
            Point3d thisN = thisResidue.getCoordinates("N");
            Point3d thisCA = thisResidue.getCoordinates("CA");
            Point3d thisC = thisResidue.getCoordinates("C");

            double phi = Geometer.torsion(prevC, thisN, thisCA, thisC);
            thisResidue.setPhi(phi);

            previousResidue = thisResidue;
            if (residueIterator.hasNext()) {
                Residue nextResidue = (Residue) residueIterator.next();

                Point3d nextN = nextResidue.getCoordinates("N");

                double psi = Geometer.torsion(thisN, thisCA, thisC, nextN);
                thisResidue.setPsi(psi);

                thisResidue = nextResidue;
            } else {
                break;
            }
        }
    }

    public void findOrientations() {
        Axis chainAxis = this.getAxis();
        // System.out.println("Chain axis = " + chainAxis);

        Iterator<BackboneSegment> backboneSegmentIterator = this.backboneSegments.iterator();
        while (backboneSegmentIterator.hasNext()) {
            BackboneSegment nextSegment = (BackboneSegment) backboneSegmentIterator
                    .next();
            nextSegment.determineOrientation(chainAxis);
        }
    }

    public String toPymolScript() {
        StringBuffer script = new StringBuffer();

        Iterator<BackboneSegment> backboneSegmentIterator = this.backboneSegments.iterator();
        int segmentNumber = 1;
        while (backboneSegmentIterator.hasNext()) {
            BackboneSegment bs = (BackboneSegment) backboneSegmentIterator
                    .next();
            String name = bs.getTopsSymbol() + "" + segmentNumber + "("
                    + bs.firstPDB() + "-" + bs.lastPDB() + ")";
            String selection = "not hetatm and name ca+c+n+o+h and resi "
                    + bs.firstPDB() + "-" + bs.lastPDB();
            script.append("cmd.select(\"" + name + "\", \"" + selection
                    + "\")\n");
            String color = "green";
            if (bs instanceof Strand) {
                script.append("line(");
                Axis axis = bs.getAxis();
                Point3d start = axis.getStart();
                Point3d end = axis.getEnd();
                script.append(String.format("%6.2f, %6.2f, %6.2f, ", start.x,
                        start.y, start.z));
                script.append(String.format("%6.2f, %6.2f, %6.2f, ", end.x,
                        end.y, end.z));
                script.append("\"").append(name).append("axis\")\n");
                color = "yellow";
            } else if (bs instanceof Helix) {
                color = "red";
            }
            script.append("cmd.color(\"" + color + "\", \"" + name + "\")\n");
            segmentNumber++;
        }
        return script.toString();
    }

    public HashMap<String, String> toTopsDomainStrings(
    		HashMap<String, List<Domain>> chainDomainMap) {
        if (!chainDomainMap.isEmpty()
                && chainDomainMap.containsKey(this.getCathCompatibleLabel())) {
            List<Domain> domains = chainDomainMap.get(this.getCathCompatibleLabel());
            return this.toTopsDomainStrings(domains);
        } else {
            HashMap<String, String> h = new HashMap<String, String>();
            h.put("0", this.toTopsString(new Domain(0)));
            return h;
        }
    }

    public HashMap<String, String> toTopsDomainStrings(List<Domain> domains) {
        HashMap<String, String> domainStrings = new HashMap<String, String>(domains.size());

        for (int i = 0; i < domains.size(); i++) {
            Domain d = domains.get(i);
            // System.err.println("Getting tops string for domain " + d);
            domainStrings.put(d.getID(), this.toTopsString(d));
        }

        return domainStrings;
    }

	public String toTopsString(Domain domain) {
        // name
        StringBuffer s = new StringBuffer();
        s.append(this.getCathCompatibleLabel() + domain.getNumber() + " ");

        // vertexstring
        Iterator<BackboneSegment> backboneSegmentIterator;
        if (domain.isEmpty()) {
            backboneSegmentIterator = this.backboneSegments.iterator();
        } else {
            List<BackboneSegment> segmentsFilteredByDomain = 
            		domain.filter(this.backboneSegments);
            segmentsFilteredByDomain.add(0, new Terminus("N Terminus", 'N'));
            segmentsFilteredByDomain.add(new Terminus("C Terminus", 'C'));
            backboneSegmentIterator = segmentsFilteredByDomain.iterator();
        }

        int vertexNumber = 0;
        while (backboneSegmentIterator.hasNext()) {
            BackboneSegment nextBackboneSegment = (BackboneSegment) backboneSegmentIterator
                    .next();
            if (nextBackboneSegment instanceof UnstructuredSegment) {
                continue;
            } else {
                nextBackboneSegment.setNumber(vertexNumber);
                s.append(nextBackboneSegment.getTopsSymbol());
                vertexNumber++;
            }
        }

        s.append(" ");

        // edgestring
        Iterator<Sheet> sheetIterator = this.sheets.iterator();
        List<Edge> edges = new ArrayList<Edge>();
        while (sheetIterator.hasNext()) {
            Sheet sheet = (Sheet) sheetIterator.next();
            edges.addAll(sheet.toTopsEdges(domain));
        }

        Collections.sort(edges);

        // merge the chirals with the hbonds
        Iterator<Edge> chiralIterator = this.chiralities.iterator();
        Iterator<Edge> edgeIterator;
        while (chiralIterator.hasNext()) {
            Edge chiral = chiralIterator.next();
            edgeIterator = edges.iterator();
            while (edgeIterator.hasNext()) {
                Edge hbond = edgeIterator.next();
                if (hbond.equals(chiral)) {
                    hbond.mergeWith(chiral);
                    break;
                }
            }
        }

        edgeIterator = edges.iterator();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            s.append(edge.toString());
        }

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Chain : " + this.getLabel() + " residue "
                + this.firstResidue().getPDBNumber() + " to "
                + this.lastResidue().getPDBNumber() + "\n");

        Iterator<Residue> itr = this.residues.iterator();
        while (itr.hasNext()) {
            Residue r = (Residue) itr.next();
            s.append(r.toFullString());
            s.append("\n");
        }

        Iterator<HBond> hbondItr = this.hbonds.iterator();
        while (hbondItr.hasNext()) {
            HBond nextBond = (HBond) hbondItr.next();
            s.append(nextBond + "\n");
        }

        Iterator<BackboneSegment> backboneSegmentIterator = this.backboneSegments.iterator();
        while (backboneSegmentIterator.hasNext()) {
            BackboneSegment nextBackboneSegment = (BackboneSegment) backboneSegmentIterator
                    .next();
            s.append(nextBackboneSegment.toFullString() + "\n");
        }

        Iterator<Sheet> sheetIterator = this.sheets.iterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = (Sheet) sheetIterator.next();
            s.append(sheet + "\n");
        }

        return s.toString();
    }

	@Override
	public Iterator<BackboneSegment> iterator() {
		return this.backboneSegments.iterator();
	}
}
