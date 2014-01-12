package tops.translation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class FoldAnalyser {

    private HBondAnalyser hBondAnalyser;

    public FoldAnalyser() {
        this.hBondAnalyser = new HBondAnalyser();

        // set some default properties
        this.hBondAnalyser.setProperty("MAX_HO_DISTANCE", "3.5");
        this.hBondAnalyser.setProperty("MIN_NHO_ANGLE", "120");
        this.hBondAnalyser.setProperty("MIN_HOC_ANGLE", "90");
    }

    public Protein analyse(Protein protein) throws PropertyError {
        Iterator<Chain> chains = protein.chainIterator();

        while (chains.hasNext()) {
            Chain chain = (Chain) chains.next();

            if (chain.isDNA()) {
                continue;
            }

            this.hBondAnalyser.analyse(chain);
            this.findSheets(chain);
            this.assignOrientationsDependingOnArchitecture(chain);
            this.determineChiralities(chain);
        }

        return protein;
    }

    public void findSheets(Chain chain) {
        // for each strand, examine all strands in front (so we don't do the
        // same comparison twice)
        // each examination is a simple centroid-centroid distance
        ListIterator<?> firstSegments = chain.backboneSegmentListIterator();

        while (firstSegments.hasNext()) {
            // get the first segment, reject if not a strand
            BackboneSegment firstSegment = (BackboneSegment) firstSegments
                    .next();
            if (!(firstSegment instanceof Strand)) {
                continue;
            }

            // get the segments after the current one
            ListIterator<?> secondSegments = chain
                    .backboneSegmentListIterator(firstSegment);
            while (secondSegments.hasNext()) {
                BackboneSegment secondSegment = (BackboneSegment) secondSegments
                        .next();
                if ((secondSegment != firstSegment)
                        && (secondSegment instanceof Strand)) {
                    // make a crude distance check
                    if (this.closeApproach(firstSegment, secondSegment)) {
                        // if this passes, make a finer bonding check
                        if (this.bonded(firstSegment, secondSegment)) {
                            this.addStrandPair(firstSegment, secondSegment,
                                    chain);
                        }
                    }
                }
            }
        }
    }

    public boolean closeApproach(BackboneSegment a, BackboneSegment b) {
        Vector3d distanceVector = new Vector3d();
        distanceVector
                .sub(a.getAxis().getCentroid(), b.getAxis().getCentroid());
        double length = distanceVector.length();
        // System.out.println("Distance between " + a + " and " + b + " = " +
        // Math.rint(length));
        return length < 10.0;
    }

    public boolean bonded(BackboneSegment strand, BackboneSegment otherStrand) {
        // basically, run through the residues, checking the list of hbonds to
        // find residues that might be in the other strand
        int numberOfHBonds = 0;
        Iterator<?> strandResidues = strand.residueIterator();
        while (strandResidues.hasNext()) {
            Residue nextResidue = (Residue) strandResidues.next();
            if (otherStrand.bondedTo(nextResidue)) {
                numberOfHBonds++;
            }
        }

        // all we want to know is, are there enough hbonds for these strands to
        // qualify as bonded
        // WARNING! this is number of RESIDUES not number of HBONDS
        if (numberOfHBonds > 1) { // ?is two enough?
            return true;
        } else {
            // System.out.println(strand + " not bonded to " + otherStrand);
            return false;
        }
    }

    public void addStrandPair(BackboneSegment first, BackboneSegment second,
            Chain chain) {
        Sheet firstSheet = chain.getSheetContaining(first);
        Sheet secondSheet = chain.getSheetContaining(second);

        if (firstSheet == null && secondSheet == null) {
            chain.createSheet(first, second);
            // System.out.println("Adding " + first + " and " + second + " to
            // new sheet");
        } else {
            if (firstSheet == null) {
                // System.out.println("Adding " + first + " and " + second + "
                // to " + secondSheet);
                secondSheet.insert(second, first);
            } else if (secondSheet == null) {
                // System.out.println("Adding " + first + " and " + second + "
                // to " + firstSheet);
                firstSheet.insert(first, second);
            } else {
                // one possibility is that both sheets are the same, and this
                // edge closes a barrel
                if (firstSheet == secondSheet) {
                    firstSheet.closeBarrel(first, second);
                } else {
                    // otherwise, we have to join the sheets
                    this.joinSheets(first, second, firstSheet, secondSheet,
                            chain);
                }
            }
        }
    }

    public void joinSheets(BackboneSegment first, BackboneSegment second,
            Sheet firstSheet, Sheet secondSheet, Chain chain) {
        // check that the strands are not somehow in the middle of the sheet
        // (bifurcated sheets)
        if (firstSheet.strandInMiddle(first)
                || secondSheet.strandInMiddle(second)) {
            // System.err.println("bifurcation in : Sheet (" + firstSheet + ")
            // and Strand (" + first + ") or Sheet (" + secondSheet + ") and
            // Strand (" + second + " )");
            return;
        }

        int indexOfFirst = firstSheet.indexOf(first);
        int indexOfSecond = secondSheet.indexOf(second);

        if (indexOfSecond == 0) {
            if (indexOfFirst == 0) {
                // System.out.println("Reversing : " + firstSheet + " and adding
                // : " + secondSheet + " onto the end");
                firstSheet.reverse();
                firstSheet.extend(secondSheet);
            } else {
                // System.out.println("Adding : " + secondSheet + " onto the end
                // of " + firstSheet);
                firstSheet.extend(secondSheet);
            }
            chain.removeSheet(secondSheet);
        } else {
            if (indexOfFirst == 0) {
                // System.out.println("Adding : " + firstSheet + " onto the end
                // of " + secondSheet);
                secondSheet.extend(firstSheet);
                chain.removeSheet(firstSheet);
            } else {
                // System.out.println("Reversing : " + secondSheet + " and
                // adding it onto the end of " + firstSheet);
                secondSheet.reverse();
                firstSheet.extend(secondSheet);
                chain.removeSheet(secondSheet);
            }
        }
    }

    // basically, using the least-square plane to find the best chain axis is a
    // rubbish way to do things!
    // unfortunately, the only alternative is to have some rather ad-hoc rules
    // based on architecture
    public void assignOrientationsDependingOnArchitecture(Chain chain) {
        // for a single sheet, get the orientation of the sheet, and use that
        int sheetCount = chain.numberOfSheets();
        if (sheetCount == 1) {
            // get the sheet, and assign the orientations internally, based on
            // relative orientations
            Iterator<?> sheets = chain.sheetIterator();
            Sheet sheet = (Sheet) sheets.next();

            // System.out.println("Assigning using a single sheet");
            sheet.assignOrientationsToStrands();

            Axis sheetAxis = sheet.getAxis();
            // System.out.println("Sheet axis = " + sheetAxis.getCentroid() + ",
            // " + sheetAxis.getAxisVector());

            // assign the helices to orientations dependant on the sheet
            ListIterator<?> segments = chain.backboneSegmentListIterator();
            while (segments.hasNext()) {
                BackboneSegment segment = (BackboneSegment) segments.next();
                if ((segment instanceof Strand)
                        || (segment instanceof Terminus)) {
                    continue;
                } else {
                    segment.determineOrientation(sheetAxis);
                }
            }
        } else if (sheetCount > 1) {
            Iterator<?> sheets = chain.sheetIterator();
            while (sheets.hasNext()) {
                Sheet sheet = (Sheet) sheets.next();
                sheet.assignOrientationsToStrands();
            }
        }
    }

    // go through the fixed structures, finding helices connecting parallel
    // strands not more than 5 sses away
    public void determineChiralities(Chain chain) {
        if (chain.numberOfSheets() > 0) {
            Iterator<?> sheets = chain.sheetIterator();
            while (sheets.hasNext()) {
                Sheet sheet = (Sheet) sheets.next();
                Iterator<?> sheetIterator = sheet.iterator();
                BackboneSegment strand = (BackboneSegment) sheetIterator.next();

                while (sheetIterator.hasNext()) {
                    BackboneSegment partner = (BackboneSegment) sheetIterator
                            .next();
                    boolean isCorrectOrder = strand.compareTo(partner) < 0;
                    Vector3d upVector = strand.getAxis().getAxisVector();

                    // if the strand and its partner are less than x segments
                    // away, and parallel
                    int distance;
                    if (isCorrectOrder) {
                        distance = chain.numberOfSegmentsInBetween(strand,
                                partner);
                    } else {
                        distance = chain.numberOfSegmentsInBetween(partner,
                                strand);
                    }

                    if ((distance < 11)
                            && (strand.getOrientation().equals(partner
                                    .getOrientation()))) {
                        Point3d strandCentroid = strand.getAxis().getCentroid();
                        Point3d partnerCentroid = partner.getAxis()
                                .getCentroid();

                        // get the sses we will use for the chirality
                        // calculation
                        ListIterator<?> inBetweeners;
                        if (isCorrectOrder) {
                            inBetweeners = chain.backboneSegmentListIterator(
                                    strand, partner);
                        } else {
                            inBetweeners = chain.backboneSegmentListIterator(
                                    partner, strand);
                        }

                        // find the average center of these sses
                        ArrayList<Point3d> centroids = new ArrayList<Point3d>();
                        while (inBetweeners.hasNext()) {
                            BackboneSegment segment = (BackboneSegment) inBetweeners
                                    .next();
                            centroids.add(segment.getAxis().getCentroid());
                        }
                        Point3d averageCentroid = Geometer
                                .averagePoints(centroids);
                        // System.out.println("InBetweener centroid = " +
                        // averageCentroid);

                        // finally, do the calculation
                        char chirality;
                        if (isCorrectOrder) {
                            chirality = Geometer.chirality(strandCentroid,
                                    averageCentroid, partnerCentroid, upVector);
                        } else {
                            chirality = Geometer.chirality(partnerCentroid,
                                    averageCentroid, strandCentroid, upVector);
                        }

                        if (isCorrectOrder) {
                            chain.addChirality(strand, partner, chirality);
                        } else {
                            chain.addChirality(partner, strand, chirality);
                        }
                    }
                    strand = partner;
                }
            }
        }
    }

    public static void main(String[] args) {
        String pdbFilename = args[0];

        try {
            Protein protein = PDBReader.read(pdbFilename);

            FoldAnalyser foldAnalyser = new FoldAnalyser();
            foldAnalyser.analyse(protein);

            System.out.println(protein);

            HashMap<String, List<Domain>> cathChainDomainMap = 
            		CATHDomainFileParser.parseUpToParticularID(args[1], protein.getID());
            HashMap<String, HashMap<String, String>> chainDomainStringMap = 
            		protein.toTopsDomainStrings(cathChainDomainMap);

            Iterator<?> itr = chainDomainStringMap.keySet().iterator();
            while (itr.hasNext()) {
                String chainID = (String) itr.next();
                HashMap<?, ?> domainStrings = (HashMap<?, ?>) chainDomainStringMap
                        .get(chainID);
                Iterator<?> itr2 = domainStrings.keySet().iterator();
                while (itr2.hasNext()) {
                    String domainString = (String) domainStrings
                            .get(itr2.next());
                    System.out.println(protein.getID() + domainString);
                }
            }

        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (PropertyError pe) {
            System.err.println(pe);
        }
    }

}
