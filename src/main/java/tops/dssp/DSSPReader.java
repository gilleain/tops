package tops.dssp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import tops.translation.model.BackboneSegment;
import tops.translation.model.Chain;
import tops.translation.model.HBond;
import tops.translation.model.Helix;
import tops.translation.model.Protein;
import tops.translation.model.Residue;
import tops.translation.model.Strand;
import tops.translation.model.UnstructuredSegment;


public class DSSPReader {
    
    /**
     * A filter for hydrogen bonds - the more negative this value, 
     * the more bonds will be discarded 
     */
    private double minEnergy;
    
    public DSSPReader() {
        this.minEnergy = -0.01;   // no limit
    }
    
    public DSSPReader(double minEnergy) {
        this.minEnergy = minEnergy;
    }
    
    public Protein read(String filename) throws IOException{
        return read(new FileReader(new File(filename)));
    }
    
    public Protein read(Reader reader) throws IOException {
        BufferedReader dsspfile = new BufferedReader(reader);
        DsspModel dsspModel = createModel(dsspfile);
        dsspfile.close();
        return createProtein(dsspModel);
    }
    
    public Protein createProtein(DsspModel dsspModel) {
        Protein protein = new Protein();
        String currentSSEType = null;
        BackboneSegment currentSSE = null;
        Chain currentChain = null;
        
        Map<BackboneSegment, List<HBond>> sseToHBondMap = new HashMap<BackboneSegment, List<HBond>>();
        Map<String, List<Residue>> residueMap = parseResidues(dsspModel);
        int index = 0;
        List<Residue> residues = new ArrayList<Residue>();
        for (DsspModel.Line line : dsspModel.getLines()) {
            if (currentChain == null) {
                index = 0;  // start a new chain index
                currentChain = new Chain(line.chainName);
//                System.out.println("Making new chain " + line.chainName);
            }
            if (!currentChain.getLabel().equals(line.chainName)) {
//                System.out.println("finishing chain " + line.chainName + " " + sseToHBondMap);
                finishChain(protein, currentChain, residues, sseToHBondMap);
                sseToHBondMap.clear();
                currentChain = new Chain(line.chainName);
            }
            if (currentSSEType == null || !currentSSEType.equals(line.sseType)) {
                currentSSE = makeSSE(line.sseType);
                currentChain.addBackboneSegment(currentSSE);
                sseToHBondMap.put(currentSSE, new ArrayList<HBond>());
                currentSSEType = line.sseType;
            }
            Point3d caPosition = parsePosition(line.xca, line.yca, line.zca);
            residues = residueMap.get(line.chainName);
            Residue residue = residues.get(index);
            residue.setAtom("CA", caPosition);
            currentSSE.expandBy(residue);
            
            HBond nho1 = makeNHOBond(residue, residues, line.nho1, currentChain);
            addToHBondMap(sseToHBondMap, currentSSE, nho1);
                    
            HBond ohn1 = makeOHNBond(residue, residues, line.ohn1, currentChain);
            addToHBondMap(sseToHBondMap, currentSSE, ohn1);
            
            HBond nho2 = makeNHOBond(residue, residues, line.nho2, currentChain);
            addToHBondMap(sseToHBondMap, currentSSE, nho2);
            
            HBond ohn2 = makeNHOBond(residue, residues, line.ohn2, currentChain);
            addToHBondMap(sseToHBondMap, currentSSE, ohn2);
            
            index++;
        }
//        System.out.println("finishing chain " + currentChain.getName() + " " + sseToHBondMap);
        finishChain(protein, currentChain, residues, sseToHBondMap);
        return protein;
    }
    
    private Map<String, List<Residue>> parseResidues(DsspModel dsspModel) {
        Map<String, List<Residue>> residueMap = new HashMap<String, List<Residue>>();
        for (DsspModel.Line line : dsspModel.getLines()) {
            int pdbIndex = parsePdbIndex(line.pdbNumber);
            int dsspNumber = Integer.valueOf(line.dsspNumber);
            String chainName = line.chainName;
            List<Residue> residues;
            if (residueMap.containsKey(chainName)) {
                residues = residueMap.get(chainName);
            } else {
                residues = new ArrayList<Residue>();
                residueMap.put(chainName, residues);
            }
            residues.add(new Residue(dsspNumber, pdbIndex, line.aminoAcidName));
        }
        return residueMap;
    }
    
    private void finishChain(Protein protein, Chain currentChain, List<Residue> residues, Map<BackboneSegment, List<HBond>> sseToHBondMap) {
        currentChain.addHBondSets(HBondHelper.makeHBondSets(sseToHBondMap));
        currentChain.setResidues(residues);
        protein.addChain(currentChain);
    }

    private void addToHBondMap(Map<BackboneSegment, List<HBond>> map, BackboneSegment sse, HBond hBond) {
        if (hBond != null) {
            List<HBond> hBonds;
            if (map.containsKey(sse)) {
                hBonds = map.get(sse);
            } else {
                hBonds = new ArrayList<HBond>();
                map.put(sse, hBonds);
            }
            hBonds.add(hBond);
        }
    }
    
    private HBond makeNHOBond(Residue residue, List<Residue> residues, String hbondString, Chain currentChain) {
        String[] parts = hbondString.split(",");
        int offset = Integer.parseInt(parts[0].trim());
        double energy = Double.parseDouble(parts[1].trim());
        if (offset < 0 && isEnergySignificant(energy)) {
            int partnerIndex = residue.getAbsoluteNumber() + offset;
            Residue partner = residues.get(partnerIndex);
            return new HBond(residue, partner, energy);
        } else {
            return null;
        }
    }
    
    private HBond makeOHNBond(Residue residue, List<Residue> residues, String hbondString, Chain currentChain) {
        String[] parts = hbondString.split(",");
        int offset = Integer.parseInt(parts[0].trim());
        double energy = Double.parseDouble(parts[1].trim());
        if (offset < 0 && isEnergySignificant(energy)) {
            int partnerIndex = residue.getAbsoluteNumber() + offset;
            Residue partner = residues.get(partnerIndex);
            return new HBond(partner, residue, energy);
        } else {
            return null;
        }
    }
    
    private boolean isEnergySignificant(double energy) {
        return energy < minEnergy; 
    }
    
    private BackboneSegment makeSSE(String sseTypeString) {
        SSEType type = SSEType.fromCode(sseTypeString);
        switch (type) {
            case ALPHA_HELIX: return new Helix();
            case HELIX_310: return new Helix();
            case EXTENDED: return new Strand();
            default: return new UnstructuredSegment();
        }
    }
    
    private DsspModel createModel(BufferedReader dsspfile) throws IOException {
        DsspModel dsspModel = new DsspModel();
        
        String line;
        int lineNumber = 0;
        do {
            line = dsspfile.readLine();
            if (line != null) {
                handleLine(dsspModel, lineNumber, line);
                lineNumber++;
            }
        } while (line != null);
        
        return dsspModel;
    }

    private void handleLine(DsspModel dsspModel, int lineNumber, String lineString) {
        int numberOfResidues = 0;
        int numberOfChains = 0;
        if (lineNumber == 6) {
            String[] headerBits = lineString.split("\\s+");
            int startIndex = 0;
            if (headerBits[startIndex].equals("")) {
                startIndex++;
            }
            numberOfResidues = Integer.parseInt(headerBits[startIndex]);
            numberOfChains = Integer.parseInt(headerBits[startIndex + 1]);
            // increase number of residues to include chain termination records in file 
            numberOfResidues += numberOfChains - 1;
        } else if (lineNumber > 28) {
            dsspModel.addLine(lineString);
        }
    }
    
    private Point3d parsePosition(String x, String y, String z) {
        return new Point3d(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
    }

    private int parsePdbIndex(String token) {
        if (token.equals("!")) {
            return -99999;
        } else {
            return Integer.parseInt(token);
        }
    }
}
