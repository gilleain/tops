package tops.dssp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import tops.model.HBond;
import tops.model.HBondSet;
import tops.model.Residue;
import tops.model.SSE;

public class TestHBondHelper {
    
    @Test
    public void testEmptyMap() {
        Map<SSE, List<HBond>> sseToHBondMap = new HashMap<SSE, List<HBond>>();
        List<HBondSet> hBondSetMap = HBondHelper.makeHBondSets(sseToHBondMap);
        assertTrue(hBondSetMap.isEmpty());
    }
    
    @Test
    public void testHelix() {
        Map<SSE, List<HBond>> sseToHBondMap = new HashMap<SSE, List<HBond>>();
        SSE helix = makeSSE(0, 10, SSE.Type.ALPHA_HELIX);
        makeHelicalBonds(sseToHBondMap, helix, 0, 10);
        List<HBondSet> hBondSets = HBondHelper.makeHBondSets(sseToHBondMap);
        assertEquals(1, hBondSets.size());
        assertEquals(6, hBondSets.get(0).size());
    }
    
    @Test
    public void testBAB() {
        Map<SSE, List<HBond>> sseToHBondMap = new HashMap<SSE, List<HBond>>();
        SSE strand1 = makeSSE(0, 10, SSE.Type.EXTENDED);
        SSE helix   = makeSSE(11, 20, SSE.Type.ALPHA_HELIX);
        SSE strand2 = makeSSE(21, 30, SSE.Type.EXTENDED);
        
        makeHelicalBonds(sseToHBondMap, helix, 11, 20);
        connectParallelStrands(sseToHBondMap, strand1, strand2, 1, 10, 21, 30);
        
        List<HBondSet> hBondSets = HBondHelper.makeHBondSets(sseToHBondMap);
        System.out.println("HBondSets : ");
        for (HBondSet hBondSet : hBondSets) {
            System.out.println(hBondSet);
        }
    }
    
    @Test
    public void testHairpin() {
        Map<SSE, List<HBond>> sseToHBondMap = new HashMap<SSE, List<HBond>>();
        SSE strand1 = makeSSE(0, 10, SSE.Type.EXTENDED);
        SSE strand2 = makeSSE(12, 22, SSE.Type.EXTENDED);
        connectAntiparallelStrands(sseToHBondMap, strand1, strand2, 0, 10, 12, 22);
        
        List<HBondSet> hBondSets = HBondHelper.makeHBondSets(sseToHBondMap);
        System.out.println("HBondSets : ");
        for (HBondSet hBondSet : hBondSets) {
            System.out.println(hBondSet);
        }
    }
    
    private void connectAntiparallelStrands(Map<SSE, List<HBond>> sseToHBondMap, SSE strand1, SSE strand2, int startA, int endA, int startB, int endB) {
        List<HBond> hBonds = new ArrayList<HBond>();
        int indexA = startA;
        int indexB = endB;
        while (indexA < endA && indexB > startB) {
            addAntiparallelPair(indexA, indexB, hBonds);
            indexA += 2; indexB -= 2;
        }
        sseToHBondMap.put(strand1, hBonds);
        sseToHBondMap.put(strand2, hBonds);
        System.out.println(strand1 + " ; " + hBonds);
        System.out.println(strand2 + " ; " + hBonds);
    }
    
    private void addAntiparallelPair(int i, int j, List<HBond> hBonds) {
        hBonds.add(new HBond(i, j));  // N(i) -> O(j+2)
        hBonds.add(new HBond(j, i));  // O(i) -> N(j+2)
    }
    
    private void connectParallelStrands(Map<SSE, List<HBond>> sseToHBondMap, SSE strand1, SSE strand2, int startA, int endA, int startB, int endB) {
        List<HBond> hBonds = new ArrayList<HBond>();
        int indexA = startA;
        int indexB = startB;
        while (indexA < endA && indexB < endB) {
            addParallelPair(indexA, indexB, hBonds);
            indexA += 2; indexB += 2;
        }
        sseToHBondMap.put(strand1, hBonds);
        sseToHBondMap.put(strand2, hBonds);
        System.out.println(strand1 + " ; " + hBonds);
        System.out.println(strand2 + " ; " + hBonds);
    }
    
    private void addParallelPair(int j, int k, List<HBond> hBonds) {
        hBonds.add(new HBond(k+1, j));  // O(j) -> N(k + 1)
        hBonds.add(new HBond(j, k-1));  // N(j) -> O(k - 1)
    }
    
    private void makeHelicalBonds(Map<SSE, List<HBond>> sseToHBondMap, SSE helix, int start, int end) {
        List<HBond> hBonds = new ArrayList<HBond>();
        int lowerEnd = start;
        int count = (end - start) - 4;
        for (int counter = 0; counter < count; counter++) {
            hBonds.add(new HBond(lowerEnd, lowerEnd + 4));
            lowerEnd++;
        }
        sseToHBondMap.put(helix, hBonds);
        System.out.println(helix + " ; " + hBonds);
    }
    
    private SSE makeSSE(int start, int end, SSE.Type type) {
        SSE sse = new SSE(start, type);
        for (int index = start + 1; index <= end; index++) {
//            System.out.println("adding residue " + index);
            sse.addResidue(new Residue("", index, null));
        }
        System.out.println(sse);
        return sse;
    }

}
