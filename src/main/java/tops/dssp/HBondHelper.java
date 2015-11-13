package tops.dssp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tops.model.HBond;
import tops.model.HBondSet;
import tops.model.SSE;

public class HBondHelper {
    
    public static List<HBondSet> makeHBondSets(Map<SSE, List<HBond>> sseToHBondMap) {
        System.out.println("Making hBondSets for ");
        for (SSE sse : sseToHBondMap.keySet()) {
            System.out.println(sse + " -> " + sseToHBondMap.get(sse));
        }
        
        Set<SSE> sseSet = sseToHBondMap.keySet();
        Map<SSE, List<HBondSet>> hBondSetMap = new HashMap<SSE, List<HBondSet>>();
        for (SSE sse : sseSet) {
            System.out.println("Assigning hbonds for sse " + sse);
            for (HBond hBond : sseToHBondMap.get(sse)) {
                int otherEnd = getOtherEnd(hBond, sse);
                if (otherEnd < sse.getStart()) {
                    continue;
                }
                SSE otherSSE = lookup(otherEnd, sseSet);
                HBondSet hBondSet;
                if (otherSSE == null) {
                    // TODO : throw exception?
                    System.out.println("No other sse for " + hBond + " " + hBondSetMap);
                    continue;
                } else {
                    if (hBondSetMap.containsKey(sse)) {
                        List<HBondSet> hBondSets = hBondSetMap.get(sse);
                        if (hBondSets == null) {
                            hBondSet = null;
                            System.out.println("No hbondset-list for " + hBond + " " + hBondSetMap);
                        } else {
                            hBondSet = getHBondSet(hBondSets, otherSSE);
                            if (hBondSet == null) {
                                hBondSet = makeHBondSet(sse, otherSSE);
                                hBondSets.add(hBondSet);
                            }
                            System.out.println("Adding bond " + hBond + " to set " + hBondSet);
                        }
                    } else {
                        hBondSet = makeHBondSet(sse, otherSSE, hBondSetMap);
                        System.out.println("Making bond " + hBond + " in set " + hBondSet);
                    }
                }
                if (hBondSet == null) {
                    System.out.println("No hbondset for " + hBond + " " + hBondSetMap);
                } else {
                    hBondSet.addHBond(hBond);
                }
            }
        }
        
        // collect all the hbond sets 
        List<HBondSet> hBondSets = new ArrayList<HBondSet>();
//        for (SSE sse : hBondSetMap.keySet()) {
//            hBondSets.addAll(hBondSetMap.get(sse));
//        }
        for (List<HBondSet> hBondSetList : hBondSetMap.values()) {
            hBondSets.addAll(hBondSetList);
        }
        return hBondSets;
    }
    
    private static int getOtherEnd(HBond hBond, SSE sse) {
        if (sse.containsResidue(hBond.getAminoPos())) {
            return hBond.getCarboxylPos();
        } else {
            return hBond.getAminoPos();
        }
    }
    
    private static HBondSet makeHBondSet(SSE sse, SSE otherSSE) {
        HBondSet targetHBondSet = new HBondSet();
        targetHBondSet.setStart(sse);
        targetHBondSet.setEnd(otherSSE);
        return targetHBondSet;
    }
    
    private static HBondSet makeHBondSet(SSE sse, SSE otherSSE, Map<SSE, List<HBondSet>> hBondSetMap) {
        List<HBondSet> hBondSets = new ArrayList<HBondSet>();
        HBondSet targetHBondSet = makeHBondSet(sse, otherSSE);
        hBondSets.add(targetHBondSet);
        hBondSetMap.put(sse, hBondSets);
        return targetHBondSet;
    }
    
    private static HBondSet getHBondSet(List<HBondSet> hBondSets, SSE otherSSE) {
        for (HBondSet hBondSet : hBondSets) {
            if (hBondSet.getEnd().equals(otherSSE)) {
                return hBondSet;
            }
        }
        return null;
    }
    
    private static SSE lookup(int dsspNumber, Set<SSE> sseSet) {
        for (SSE sse : sseSet) {
//            System.out.println("Checking " + sse);
            if (sse.containsResidue(dsspNumber)) {
//                System.out.println("returning sse " + sse);
                return sse;
            }
        }
        return null;
    }

}
