package tops.dssp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import tops.data.dssp.HBondHelper;
import tops.data.dssp.SSEType;
import tops.translation.model.BackboneSegment;
import tops.translation.model.HBond;
import tops.translation.model.HBondSet;
import tops.translation.model.Helix;
import tops.translation.model.Residue;
import tops.translation.model.Strand;
import tops.translation.model.UnstructuredSegment;


public class TestHBondHelper {
    
    @Test
    public void testEmptyMap() {
        Map<BackboneSegment, List<HBond>> sseToHBondMap = new HashMap<BackboneSegment, List<HBond>>();
        List<HBondSet> hBondSetMap = HBondHelper.makeHBondSets(sseToHBondMap);
        assertTrue(hBondSetMap.isEmpty());
    }
    
    @Test
    public void testHelix() {
        Map<BackboneSegment, List<HBond>> sseToHBondMap = new HashMap<BackboneSegment, List<HBond>>();
        BackboneSegment helix = makeBackboneSegment(1, 10, SSEType.ALPHA_HELIX);
        makeHelicalBonds(sseToHBondMap, helix, 1, 10);
        List<HBondSet> hBondSets = HBondHelper.makeHBondSets(sseToHBondMap);
        assertEquals(1, hBondSets.size());
        assertEquals(6, hBondSets.get(0).size());
    }
    
    @Test
    public void testBAB() {
        Map<BackboneSegment, List<HBond>> sseToHBondMap = new HashMap<BackboneSegment, List<HBond>>();
        BackboneSegment strand1 = makeBackboneSegment(0, 10, SSEType.EXTENDED);
        BackboneSegment helix   = makeBackboneSegment(11, 20, SSEType.ALPHA_HELIX);
        BackboneSegment strand2 = makeBackboneSegment(21, 30, SSEType.EXTENDED);
        
        makeHelicalBonds(sseToHBondMap, helix, 11, 20);
        connectParallelStrands(sseToHBondMap, strand1, strand2, 1, 10, 21, 30);
        
        //TODO
//        List<HBondSet> hBondSets = HBondHelper.makeHBondSets(sseToHBondMap);
//        System.out.println("HBondSets : ");
//        for (HBondSet hBondSet : hBondSets) {
//            System.out.println(hBondSet);
//        }
    }
    
    @Test
    public void testHairpin() {
        Map<BackboneSegment, List<HBond>> sseToHBondMap = new HashMap<BackboneSegment, List<HBond>>();
        BackboneSegment strand1 = makeBackboneSegment(0, 10, SSEType.EXTENDED);
        BackboneSegment strand2 = makeBackboneSegment(12, 22, SSEType.EXTENDED);
        connectAntiparallelStrands(sseToHBondMap, strand1, strand2, 0, 10, 12, 22);
        
        // TODO
//        List<HBondSet> hBondSets = HBondHelper.makeHBondSets(sseToHBondMap);
//        System.out.println("HBondSets : ");
//        for (HBondSet hBondSet : hBondSets) {
//            System.out.println(hBondSet);
//        }
    }
    
    private void connectAntiparallelStrands(Map<BackboneSegment, List<HBond>> sseToHBondMap, BackboneSegment strand1, BackboneSegment strand2, int startA, int endA, int startB, int endB) {
        List<HBond> hBonds = new ArrayList<HBond>();
        int indexA = startA;
        int indexB = endB;
        while (indexA < endA && indexB > startB) {
            addAntiparallelPair(indexA, indexB, strand1, strand2, hBonds);
            indexA += 2; indexB -= 2;
        }
        sseToHBondMap.put(strand1, hBonds);
        sseToHBondMap.put(strand2, hBonds);
        System.out.println(strand1 + " ; " + hBonds);
        System.out.println(strand2 + " ; " + hBonds);
    }
    
    private void addAntiparallelPair(int i, int j, BackboneSegment strand1, BackboneSegment strand2, List<HBond> hBonds) {
        Residue ri = strand1.getResidueByAbsoluteNumber(i);
        Residue rj = strand2.getResidueByAbsoluteNumber(i);
        hBonds.add(new HBond(ri, rj, 0));  // N(i) -> O(j+2)
        hBonds.add(new HBond(rj, ri, 0));  // O(i) -> N(j+2)
    }
    
    private void connectParallelStrands(Map<BackboneSegment, List<HBond>> sseToHBondMap, BackboneSegment strand1, BackboneSegment strand2, int startA, int endA, int startB, int endB) {
        List<HBond> hBonds = new ArrayList<HBond>();
        int indexA = startA;
        int indexB = startB;
        while (indexA < endA && indexB < endB) {
            addParallelPair(indexA, indexB, strand1, strand2, hBonds);
            indexA += 2; indexB += 2;
        }
        sseToHBondMap.put(strand1, hBonds);
        sseToHBondMap.put(strand2, hBonds);
        System.out.println(strand1 + " ; " + hBonds);
        System.out.println(strand2 + " ; " + hBonds);
    }
    
    private void addParallelPair(int j, int k, BackboneSegment strand1, BackboneSegment strand2, List<HBond> hBonds) {
        Residue rkPlus1 = strand1.getResidueByAbsoluteNumber(k+1);
        Residue rkMinus1 = strand1.getResidueByAbsoluteNumber(k-1);
        Residue rj = strand2.getResidueByAbsoluteNumber(j);
        hBonds.add(new HBond(rkPlus1, rj, 0));   // O(j) -> N(k + 1)
        hBonds.add(new HBond(rj, rkMinus1 ,0));  // N(j) -> O(k - 1)
    }
    
    private void makeHelicalBonds(Map<BackboneSegment, List<HBond>> sseToHBondMap, BackboneSegment helix, int start, int end) {
        List<HBond> hBonds = new ArrayList<HBond>();
        int lowerEnd = start;
        int count = (end + 1 - start) - 4;
        for (int counter = 0; counter < count; counter++) {
            Residue r4 = helix.getResidueByAbsoluteNumber(lowerEnd + 4);
            Residue r = helix.getResidueByAbsoluteNumber(lowerEnd);
            hBonds.add(new HBond(r4, r, 0));
            lowerEnd++;
        }
        sseToHBondMap.put(helix, hBonds);
        System.out.println(helix + " ; " + hBonds);
    }
    
    private BackboneSegment makeBackboneSegment(int start, int end, SSEType type) {
        BackboneSegment sse = makeSSE(type);
        for (int index = start; index <= end; index++) {
//            System.out.println("adding residue " + index);
            sse.expandBy(new Residue(index, index, "GLY"));
        }
        System.out.println(sse);
        return sse;
    }
    
    private BackboneSegment makeSSE(SSEType type) {
        switch (type) {
            case ALPHA_HELIX: return new Helix();
            case EXTENDED: return new Strand();
            default: return new UnstructuredSegment();
        }
    }

}
