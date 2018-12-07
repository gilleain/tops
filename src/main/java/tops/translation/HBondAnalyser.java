package tops.translation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.Point3d;

import tops.translation.model.Chain;
import tops.translation.model.HBond;
import tops.translation.model.Protein;
import tops.translation.model.Residue;

public class HBondAnalyser {
    
    private final Logger LOG = Logger.getLogger(HBondAnalyser.class.getName());

    private Properties properties;

    private int threeTenHelixStart;
    private int threeTenHelixEnd;

    private int alphaHelixStart;
    private int alphaHelixEnd;

    private int piHelixStart;
    private int piHelixEnd;

    private int strandStart;
    private int strandEnd;
    
    private enum Tag {
        LOOP, 
        HELIX_THREE_TEN_END,
        HELIX_ALPHA_END,
        HELIX_PI_END, 
        SINGLE_STRAND_BOND,
        HELIX_THREE_TEN_START,
        HELIX_ALPHA_START, 
        HELIX_PI_START,
        HELIX_ALPHA_MIDDLE,
        STRAND_PARALLEL,
        STRAND_ANTIPARALLEL;
    }

    public HBondAnalyser() {
        this.properties = new Properties();
    }

    public HBondAnalyser(Properties properties) {
        this.properties = properties;
    }

    public void setDefaultProperties() {
        this.properties.setProperty("CALCULATE_BACKBONE_AMIDE_HYDROGENS", "TRUE");
        this.properties.setProperty("MAX_HO_DISTANCE", "3.0");
        this.properties.setProperty("MIN_NHO_ANGLE", "120");
        this.properties.setProperty("MIN_HOC_ANGLE", "90");
    }

    public void resetEndpoints() {
        this.threeTenHelixStart = -1;
        this.threeTenHelixEnd   = -1;

        this.alphaHelixStart    = -1;
        this.alphaHelixEnd      = -1;

        this.piHelixStart       = -1;
        this.piHelixEnd         = -1;

        this.strandStart        = -1;
        this.strandEnd          = -1;
    }

    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public void analyse(Protein protein) throws PropertyException {
        boolean calculateBackboneHydrogens = this.properties.getProperty("CALCULATE_BACKBONE_AMIDE_HYDROGENS").equals("TRUE");

        for (Chain chain : protein) {
            if (calculateBackboneHydrogens) {
                chain.addBackboneAmideHydrogens();
            }
            this.analyse(chain);
        }
    }

    public void analyse(Chain chain) throws PropertyException {
        double maxHODistance = 0.0;
        double minNHOAngle   = 0.0;
        double minHOCAngle   = 0.0;

        try {
            maxHODistance = Double.parseDouble(this.properties.getProperty("MAX_HO_DISTANCE"));
            minNHOAngle   = Double.parseDouble(this.properties.getProperty("MIN_NHO_ANGLE"));
            minHOCAngle   = Double.parseDouble(this.properties.getProperty("MIN_HOC_ANGLE"));
            LOG.log(Level.INFO, "HO {0} NHO {1} HOC {2}", new Object[] {maxHODistance, minNHOAngle, minHOCAngle});
        } catch (NumberFormatException nfe) {
            throw new PropertyException("Error in properties!");
        }

        int index = -1;
        Iterator<Residue> residues = chain.residueIterator();

        while (residues.hasNext()) {
            Residue first = residues.next();
            index++;

            // we ignore gamma turns!
            int position = first.getAbsoluteNumber();
            int nextPosition = position + 3;

            Point3d firstN = first.getCoordinates("N");
            Point3d firstH = first.getCoordinates("H");
            Point3d firstO = first.getCoordinates("O");
            Point3d firstC = first.getCoordinates("C");

            // FIXME : unfortunately, this misses out on PRO residues (also below)
            if (firstN == null || firstH == null || firstO == null || firstC == null) {
                continue;
            }

            // allow for chain breaks, or return if we have reached the end
            if (!chain.hasResidueByAbsoluteNumbering(nextPosition)) {
                Residue second = chain.getNextResidue(position);
                if (second == null) {   //probably reached the end of the chain!
                    break;
                }
                nextPosition = second.getAbsoluteNumber();
            }

            // now, compare the first residue to the residues further on in the chain
            Iterator<Residue> itr = chain.residueIterator(nextPosition);
            while (itr.hasNext()) {
                int secondPosition = itr.next().getAbsoluteNumber();

                // we must still check this, since a chain break might move us to i + 2
                if (secondPosition < (position + 3)) {
                    continue;
                }

                Residue second;
                try {
                    second = chain.getResidueByAbsoluteNumbering(secondPosition);
                    if (!second.isStandardAminoAcid()) {
                        continue;
                    }
                } catch (IndexOutOfBoundsException i) {
                    break;
                }

                Point3d secondN = second.getCoordinates("N");
                Point3d secondH = second.getCoordinates("H");
                Point3d secondO = second.getCoordinates("O");
                Point3d secondC = second.getCoordinates("C");

                // FIXME : PRO residues...
                if (secondN == null || secondH == null || secondO == null || secondC == null) {
                    continue;
                }

                // bonds from first N-H to second C=O
                double firstHODistance = firstH.distance(secondO);
                double firstNHOAngle = Geometer.angle(firstN, firstH, secondO);
                double firstHOCAngle = Geometer.angle(firstH, secondO, secondC);
            
                HBond firstSecondBond = null;
                if (firstHODistance < maxHODistance && firstNHOAngle > minNHOAngle && firstHOCAngle > minHOCAngle) {
                    firstSecondBond = new HBond(first, second, firstHODistance, firstNHOAngle, firstHOCAngle);
                }

                if (firstSecondBond != null) {
                    chain.addHBond(firstSecondBond);
                    first.addHBond(firstSecondBond);
                    second.addHBond(firstSecondBond);
                }

                // bonds from second N-H to first C=O
                double secondHODistance = secondH.distance(firstO);
                double secondNHOAngle = Geometer.angle(secondN, secondH, firstO);
                double secondHOCAngle = Geometer.angle(secondH, firstO, firstC);

                HBond secondFirstBond = null;
                if (secondHODistance < maxHODistance && secondNHOAngle > minNHOAngle && secondHOCAngle > minHOCAngle) {
                    secondFirstBond = new HBond(second, first, secondHODistance, secondNHOAngle, secondHOCAngle);
                }

                if (secondFirstBond != null) {
                    chain.addHBond(secondFirstBond);
                    first.addHBond(secondFirstBond);
                    second.addHBond(secondFirstBond);
                }
            }
            
            // now, use these hbond assignments to determine the residue's environment
            List<Tag> tags = this.convertBondsToTags(first);
            LOG.info(first.toFullString() + " " + tags);

            this.updateSSEEndpoints(index, tags, chain);
        }

        // finally, finish off the SSEs
        this.finishSSES(chain);
        chain.sortBackboneSegments();
        chain.mergeHelices();
        chain.addTerminii();
    }

    public List<Tag> convertBondsToTags(Residue residue) {
    	List<HBond> nTerminalHBonds = residue.getNTerminalHBonds();
    	List<HBond> cTerminalHBonds = residue.getCTerminalHBonds();

        List<Tag> tags = new ArrayList<>();

        for (int nIndex = 0; nIndex < nTerminalHBonds.size(); nIndex++) {
            int nSeparation = nTerminalHBonds.get(nIndex).getResidueSeparation();
            Tag nTag = this.convertBondsToTag(nSeparation, 0);
            if (nTag != null) {
                tags.add(nTag);
            }
        }

        for (int cIndex = 0; cIndex < cTerminalHBonds.size(); cIndex++) {
            int cSeparation = cTerminalHBonds.get(cIndex).getResidueSeparation();
            Tag cTag = this.convertBondsToTag(0, cSeparation);
            if (cTag != null) {
                tags.add(cTag);
            }
        }

        for (int nIndex = 0; nIndex < nTerminalHBonds.size(); nIndex++) {
            int nSeparation = nTerminalHBonds.get(nIndex).getResidueSeparation();

            for (int cIndex = 0; cIndex < cTerminalHBonds.size(); cIndex++) {
                int cSeparation = cTerminalHBonds.get(cIndex).getResidueSeparation();

                Tag ncTag = this.convertBondsToTag(nSeparation, cSeparation);
                if (ncTag != null) {
                    tags.add(ncTag);
                }
            }
        }
        
        return tags;
    }

    public Tag convertBondsToTag(int nSeparation, int cSeparation) {

        // neither has a bond
        if (nSeparation == 0 && cSeparation == 0) {
            return Tag.LOOP;
        }

        // one or the other has a bond
        else if (nSeparation != 0 && cSeparation == 0) {
            if (nSeparation == 3) {
                return Tag.HELIX_THREE_TEN_END;
            } else if (nSeparation == 4) {
                return Tag.HELIX_ALPHA_END;
            } else if (nSeparation == 5) {
                return Tag.HELIX_PI_END;
            } else if (nSeparation < -5 || nSeparation > 5) {
                return Tag.SINGLE_STRAND_BOND;
            }
        }

        else if (nSeparation == 0 && cSeparation != 0) {
            if (cSeparation == 3) {
                return Tag.HELIX_THREE_TEN_START;
            } else if (cSeparation == 4) {
                return Tag.HELIX_ALPHA_START;
            } else if (cSeparation == 5) {
                return Tag.HELIX_PI_START;
            } else if (cSeparation < -5 || cSeparation > 5) {
                return Tag.SINGLE_STRAND_BOND;
            }
        }

        // both have one bond
        else if (nSeparation != 0 && cSeparation != 0) {

            // a standard : middle of a helix
            if (nSeparation == 4 && cSeparation == 4) {
                return Tag.HELIX_ALPHA_MIDDLE;
            // we counter-intuitively take the SUM here, because one will always be negative, and the other positive 
            } else if ((Math.abs(nSeparation - cSeparation) == 2) && (nSeparation > 5 || nSeparation < -5)) {
                return Tag.STRAND_PARALLEL;
            } else if ((nSeparation == cSeparation)  && (nSeparation > 5 || nSeparation < -5)) {
                return Tag.STRAND_ANTIPARALLEL;
            }
        }

        return null;
    }

    public void updateSSEEndpoints(int index, List<Tag> tags, Chain chain) {
        if (tags.contains(Tag.HELIX_THREE_TEN_START)) {

            // not seen any three ten bond before
            if (this.threeTenHelixStart == -1) {
                this.threeTenHelixStart = index;

            // this three ten bond does not overlap with the previous one
            } else if (index + 3 > this.threeTenHelixEnd + 1) {

                // only create three-ten helices with more than one bond
                if (this.threeTenHelixEnd - this.threeTenHelixStart > 3) {
                    chain.createHelix(this.threeTenHelixStart + 1, this.threeTenHelixEnd - 1); 
                    LOG.info("310 : " + this.threeTenHelixStart + ":" +  this.threeTenHelixEnd); 
                }

                // start a potential new helix with this bond
                this.threeTenHelixStart = index;
            }

            // in any case, the helix will start three places further on
            this.threeTenHelixEnd = index + 3;
        }

        if (tags.contains(Tag.HELIX_ALPHA_START)) {

            // not seen any alpha bonds before
            if (this.alphaHelixStart == -1) {
                this.alphaHelixStart = index;

            // this three ten bond does not overlap with the previous one
            } else if (index + 4 > this.alphaHelixEnd + 1) {

                // only create three-ten helices with more than one bond
                if (this.alphaHelixEnd - this.alphaHelixStart > 4) {
                    chain.createHelix(this.alphaHelixStart + 1, this.alphaHelixEnd - 1); 
                    LOG.info("Alpha: " + this.alphaHelixStart + ":" +  this.alphaHelixEnd); 
                }

                // start a potential new helix with this bond
                this.alphaHelixStart = index;
            }

            // in any case, the helix will start three places further on
            this.alphaHelixEnd = index + 4;
        }

        if (tags.contains(Tag.HELIX_PI_START)) {

            // not seen any three ten bond before
            if (this.piHelixStart == -1) {
                this.piHelixStart = index;

            // this three ten bond does not overlap with the previous one
            } else if (index + 5 > this.piHelixEnd + 1) {

                // only create three-ten helices with more than one bond
                if (this.piHelixEnd - this.piHelixStart > 5) {
                    chain.createHelix(this.piHelixStart + 1, this.piHelixEnd - 1); 
                    LOG.info("Pi: " + this.piHelixStart + ":" +  this.piHelixEnd); 
                }

                // start a potential new helix with this bond
                this.piHelixStart = index;
            }

            // in any case, the helix will start three places further on
            this.piHelixEnd = index + 5;
        }
        
        if (tags.contains(Tag.STRAND_ANTIPARALLEL) || 
                tags.contains(Tag.STRAND_PARALLEL) || 
                tags.contains(Tag.SINGLE_STRAND_BOND)) {
            LOG.info("Strand like bond at residue " + chain.getResidueByAbsoluteNumbering(index) + " indices = " + "" + " tags = " + tags);
            if (this.strandStart == -1) {
                this.strandStart = index;
                this.strandEnd = index;
            } else {
                boolean partnerStrandContinuous = this.residuePartnerNeighboursPrevious(index, chain);
                if (this.strandEnd < index - 2 || !partnerStrandContinuous) {
                    if (this.strandEnd - this.strandStart > 0) {
                        chain.createStrand(this.strandStart, this.strandEnd);
                    }
                    this.strandStart = index;
                    this.strandEnd = index;
                } else {
                    if (this.strandStart != -1) {
                        this.strandEnd = index;
                    }
                }
            }
        }
    }

    // there must be a better way. avoiding tags altogether, storing the n and c terminal hbond separately in Residue,
    // aaaaaaannnnnd all sorts of other optimisations
    public boolean residuePartnerNeighboursPrevious(int index, Chain chain) {
        // 2 or 1?
        if (index < 2) {
            return true;
        }

        // could, of course, just pass the residue along to this method...
        Residue residue = chain.getResidueByAbsoluteNumbering(index);
        Residue twoBehind = chain.getResidueByAbsoluteNumbering(index - 2);

        // get the hbond partners for these two residues as arrays of indices
        int[] residueHBondPartners = residue.getHBondPartners();
        int[] twoBehindHBondPartners = twoBehind.getHBondPartners();

        // this allows bond in the other register (odd/even) to start without starting a new strand
        if (twoBehindHBondPartners.length == 0) {
            return true;
        }

        // check to see if the indices match up
        for (int i = 0; i < residueHBondPartners.length; i++) {
            int iIndex = residueHBondPartners[i];
            for (int j = 0; j < twoBehindHBondPartners.length; j++) {
                int jIndex = twoBehindHBondPartners[j];
                if (this.indicesArePartners(iIndex, jIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean indicesArePartners(int i, int j) {
        return (Math.abs(i - j) <= 2);
    }

    public void finishSSES(Chain chain) {
        if (this.threeTenHelixEnd != -1 && this.threeTenHelixEnd - this.threeTenHelixStart > 3) {
            LOG.info("Last 310 : " + this.threeTenHelixStart + ":" +  this.threeTenHelixEnd); 
            chain.createHelix(this.threeTenHelixStart + 1, this.threeTenHelixEnd - 1);
        }

        if (this.alphaHelixEnd != -1 && this.alphaHelixEnd - this.alphaHelixStart > 4) {
            LOG.info("Last Alpha: " + this.alphaHelixStart + ":" +  this.alphaHelixEnd); 
            chain.createHelix(this.alphaHelixStart + 1, this.alphaHelixEnd - 1);
        }

        if (this.piHelixEnd != -1 && this.piHelixEnd - this.piHelixStart > 5) {
            LOG.info("Last Pi: " + this.piHelixStart + ":" +  this.piHelixEnd); 
            chain.createHelix(this.piHelixStart + 1, this.piHelixEnd - 1);
        }

        if (this.strandStart != -1 && this.strandEnd - this.strandStart > 0) {
            chain.createStrand(this.strandStart, this.strandEnd);
        }

        this.resetEndpoints();
    }

    public void storeProperties(OutputStream out) throws IOException {
        String header = "HBond Analysis Parameters";

        this.properties.store(out, header);
    }

    public void loadProperties(InputStream in) throws IOException {
        this.properties.load(in); 
    }

    public String toString() {
        return this.properties.toString();
    }

    public static void main(String[] args) {
        String pdbFilename        = args[0];
        String propertiesFilename = args[1];

        try {
            HBondAnalyser hBondAnalyser = new HBondAnalyser();
            hBondAnalyser.loadProperties(new FileInputStream(propertiesFilename));
            hBondAnalyser.storeProperties(System.err);

            Protein protein = PDBReader.read(pdbFilename);

            hBondAnalyser.analyse(protein);
            System.out.println(protein);
        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (PropertyException pe) {
            System.err.println(pe);
        }
    }
}
