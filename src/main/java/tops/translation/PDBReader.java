package tops.translation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tops.translation.model.Chain;
import tops.translation.model.Protein;
import tops.translation.model.Residue;

public class PDBReader {

    public static Protein read(String filename) throws IOException {
    	return PDBReader.read(new File(filename));
    }
    
    public static Protein read(File file) throws IOException {
        List<String> atomRecords;
        String pdbID = "Unknown";

        BufferedReader bufferer = new BufferedReader(new FileReader(file));
        String line;
        atomRecords = new ArrayList<String>();
        while ((line = bufferer.readLine()) != null) {
            if (line.length() > 4) {
                String token = line.substring(0, 4);
                if (token.equals("ATOM")) {
                    atomRecords.add(line);
                } else if (token.equals("HEAD")) {
                    pdbID = line.substring(line.length() - 4, line.length());
                }
            }
        }
        bufferer.close();

        Protein protein = new Protein(pdbID);

        Iterator<String> itr = atomRecords.iterator();
        Chain currentChain = null;

        while (itr.hasNext()) {
            String atomRecord = (String) itr.next();
            Chain newChain = PDBReader.parseRecord(atomRecord, currentChain);
            if (!newChain.equals(currentChain)) {
                protein.addChain(newChain);
                currentChain = newChain;
            }
        }

        return protein;
    }

    public static Chain parseRecord(String atomRecord, Chain chain) {
//        String atomNumber = atomRecord.substring(4, 11).trim();
        String atomType = atomRecord.substring(11, 16).trim();
        String residueType = atomRecord.substring(17, 20);
        String chainLabel = atomRecord.substring(20, 22);
        String residueNumber = atomRecord.substring(22, 26).trim();
        String coordinates = atomRecord.substring(27, 54).trim();

        if (chain == null || (!chainLabel.equals(chain.getLabel()))) {
            chain = new Chain(chainLabel);
        }

        Residue r;
        int pdbNumber = Integer.parseInt(residueNumber);
        if (chain.hasResidueByPDBNumbering(pdbNumber)) {
            r = chain.getResidueByPDBNumbering(pdbNumber);
        } else {
            r = chain.createResidue(pdbNumber, residueType);
        }

        r.setAtom(atomType, coordinates);

        return chain;
    }
}
