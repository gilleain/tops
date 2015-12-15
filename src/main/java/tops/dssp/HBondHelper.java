package tops.dssp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tops.translation.model.BackboneSegment;
import tops.translation.model.HBond;
import tops.translation.model.HBondSet;
import tops.translation.model.Residue;

public class HBondHelper {
    
    public static List<HBondSet> makeHBondSets(Map<BackboneSegment, List<HBond>> sseToHBondMap) {
        
        Set<BackboneSegment> sseSet = sseToHBondMap.keySet();
        Map<BackboneSegment, List<HBondSet>> hBondSetMap = new HashMap<BackboneSegment, List<HBondSet>>();
        for (BackboneSegment sse : sseSet) {
            for (HBond hBond : sseToHBondMap.get(sse)) {
                Residue otherEnd = getOtherEnd(hBond, sse);
                if (otherEnd.getAbsoluteNumber() < sse.firstResidue().getAbsoluteNumber()) {
                    continue;
                }
                BackboneSegment otherBackboneSegment = lookup(otherEnd, sseSet);
                HBondSet hBondSet;
                if (otherBackboneSegment == null) {
                    // TODO : throw exception?
                    continue;
                } else {
                    if (hBondSetMap.containsKey(sse)) {
                        List<HBondSet> hBondSets = hBondSetMap.get(sse);
                        if (hBondSets == null) {
                            hBondSet = null;
                        } else {
                            hBondSet = getHBondSet(hBondSets, otherBackboneSegment);
                            if (hBondSet == null) {
                                hBondSet = makeHBondSet(sse, otherBackboneSegment);
                                hBondSets.add(hBondSet);
                            }
                        }
                    } else {
                        hBondSet = makeHBondSet(sse, otherBackboneSegment, hBondSetMap);
                    }
                }
                if (hBondSet == null) {
                } else {
                    hBondSet.addHBond(hBond);
                }
            }
        }
        
        // collect all the hbond sets 
        List<HBondSet> hBondSets = new ArrayList<HBondSet>();
        for (List<HBondSet> hBondSetList : hBondSetMap.values()) {
            hBondSets.addAll(hBondSetList);
        }
        return hBondSets;
    }
    
    private static Residue getOtherEnd(HBond hBond, BackboneSegment sse) {
        if (sse.contains(hBond.donor())) {
            return hBond.acceptor();
        } else {
            return hBond.donor();
        }
    }
    
    private static HBondSet makeHBondSet(BackboneSegment sse, BackboneSegment otherBackboneSegment) {
        HBondSet targetHBondSet = new HBondSet();
        targetHBondSet.setStart(sse);
        targetHBondSet.setEnd(otherBackboneSegment);
        return targetHBondSet;
    }
    
    private static HBondSet makeHBondSet(BackboneSegment sse, BackboneSegment otherBackboneSegment, Map<BackboneSegment, List<HBondSet>> hBondSetMap) {
        List<HBondSet> hBondSets = new ArrayList<HBondSet>();
        HBondSet targetHBondSet = makeHBondSet(sse, otherBackboneSegment);
        hBondSets.add(targetHBondSet);
        hBondSetMap.put(sse, hBondSets);
        return targetHBondSet;
    }
    
    private static HBondSet getHBondSet(List<HBondSet> hBondSets, BackboneSegment otherBackboneSegment) {
        for (HBondSet hBondSet : hBondSets) {
            if (hBondSet.getEnd().equals(otherBackboneSegment)) {
                return hBondSet;
            }
        }
        return null;
    }
    
    private static BackboneSegment lookup(Residue residue, Set<BackboneSegment> sseSet) {
        for (BackboneSegment sse : sseSet) {
            if (sse.contains(residue)) {
                return sse;
            }
        }
        return null;
    }

}
