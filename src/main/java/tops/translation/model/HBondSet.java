package tops.translation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HBondSet {
    
    private Set<HBond> hBonds;
    
    private BackboneSegment start;
    
    private BackboneSegment end;
    
    public BackboneSegment getStart() {
        return start;
    }

    public void setStart(BackboneSegment start) {
        this.start = start;
    }

    public BackboneSegment getEnd() {
        return end;
    }

    public void setEnd(BackboneSegment end) {
        this.end = end;
    }

    public HBondSet() {
        this.hBonds = new HashSet<>();
    }
    
    public void addHBond(HBond hBond) {
        hBonds.add(hBond);
    }
    
    public String toString() {
        List<HBond> hBondList = new ArrayList<>();
        hBondList.addAll(hBonds);
        Collections.sort(hBondList);
        return start + ":" + end + " " + hBondList.toString();
    }

    public int size() {
        return hBonds.size();
    }
}
