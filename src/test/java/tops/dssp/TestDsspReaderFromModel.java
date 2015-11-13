package tops.dssp;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Test;

import cern.colt.Arrays;
import tops.model.Chain;
import tops.model.HBondSet;
import tops.model.Protein;
import tops.model.SSE;

public class TestDsspReaderFromModel {
    
    private Random random = new Random();
    
    @Test
    public void singleChainBetaAlphaBeta() {
        DsspModel model = new DsspModel();
        int[] nho1 = new int[12];
        int[] ohn1 = new int[12];
        int[] nho2 = new int[12];
        int[] ohn2 = new int[12];
        
        Map<Integer, Integer> nhoBonds = new HashMap<Integer, Integer>();
        nhoBonds.put(0, 11);
        nhoBonds.put(1, 10);
        
        Map<Integer, Integer> ohnBonds = new HashMap<Integer, Integer>();
        ohnBonds.put(3, 7);
        ohnBonds.put(4, 8);
        
        fill(nho1, ohn1, nhoBonds, ohnBonds);
        
        make(model, "A", 1, 1, "TYGAPLEDMAEC", "EEEHHHHHHEEE", nho1, ohn1, nho2, ohn2);
        Protein protein = new DSSPReader().createProtein(model);
        
        List<Chain> chains = protein.getChains();
        assertEquals("Number of chains", 1, chains.size());
        Chain chain = chains.get(0);
        assertEquals("Number of sses", 3, chain.getSSES().size());
        
        SSE sse1 = chain.getSSES().get(0);
        assertEquals("First sse size", 3, sse1.getResidues().size());
        assertEquals("First sse type", SSE.Type.EXTENDED, sse1.getType());
        
        SSE sse2 = chain.getSSES().get(1);
        assertEquals("Second sse size", 6, sse2.getResidues().size());
        assertEquals("Second sse type", SSE.Type.ALPHA_HELIX, sse2.getType());
        
        SSE sse3 = chain.getSSES().get(2);
        assertEquals("Third sse size", 3, sse3.getResidues().size());
        assertEquals("Third sse type", SSE.Type.EXTENDED, sse3.getType());

        List<HBondSet> hBondSets = chain.getHBondSets();
        assertEquals("Two hbond sets", 2, hBondSets.size());
        for (HBondSet hBondSet : hBondSets) {
            System.out.println(hBondSet);
        }
    }
    
    private void make(DsspModel model, 
                      String chainName, 
                      int startDsspNum, 
                      int startPdbNum, 
                      String residues, 
                      String sseTypes,
                      int[] nho1,
                      int[] ohn1,
                      int[] nho2,
                      int[] ohn2) {
        assert residues.length() == sseTypes.length();
        int dsspNum = startDsspNum;
        int pdbNum = startPdbNum;
        System.out.println(Arrays.toString(nho1));
        System.out.println(Arrays.toString(ohn1));
        System.out.println(Arrays.toString(nho2));
        System.out.println(Arrays.toString(ohn2));
        for (int index = 0; index < residues.length(); index++) {
            String residueName = String.valueOf(residues.charAt(index));
            String sseType = String.valueOf(sseTypes.charAt(index));
            model.addLine(new DsspModel.Line(
                    // dsspnum, pdbnum, chainName, aminoAcid, sseType  
                   String.valueOf(dsspNum), String.valueOf(pdbNum), chainName, residueName, sseType, 
                    // structure, bp1, bp2, strandName, acc
                    "", "", "", "A", "0", 
                    // hbonds
                    hbond(nho1, index), hbond(ohn1, index),
                    hbond(nho2, index), hbond(ohn1, index),
                    // tco, kapp, alpha, phi, psi 
                    "0.0",  "0.0", "0.0", "0.0", "0.0",
                    // x, y, z
                    coord(), coord(), coord())
            );
            dsspNum++;
            pdbNum++;
        }
    }
    
    private void fill(int[] nho, int[] ohn, 
            Map<Integer, Integer> nhoBonds, Map<Integer, Integer> ohnBonds) {
        for (Entry<Integer, Integer> entry : nhoBonds.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            int dist = end - start;
            nho[start] = dist;
            ohn[end] = -dist;
        }
        
        for (Entry<Integer, Integer> entry : ohnBonds.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            int dist = end - start;
            ohn[start] = dist;
            nho[end] = -dist;
        }
    }

    private String hbond(int[] partners, int index) {
        if (partners[index] == 0) {
            return "0, 0.0";
        } else {
            return String.format("%d, %2f", partners[index], 1.1);
        }
    }
    
    private String coord() {
        return String.valueOf(random.nextDouble() * 10 * (random.nextBoolean()? 1 : -1));
    }

}
