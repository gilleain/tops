package tops.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HBondSet {
    
    private Set<HBond> hBonds;
    
    private SSE start;
    
    private SSE end;
    
    public SSE getStart() {
        return start;
    }

    public void setStart(SSE start) {
        this.start = start;
    }

    public SSE getEnd() {
        return end;
    }

    public void setEnd(SSE end) {
        this.end = end;
    }

    public HBondSet() {
        this.hBonds = new HashSet<HBond>();
    }
    
    public void addHBond(HBond hBond) {
        if (hBonds.add(hBond)) {
            System.out.println("Adding bond " + hBond);
        } else {
            System.out.println("Duplicate bond " + hBond);
        }
    }
    
    public String toString() {
        List<HBond> hBondList = new ArrayList<HBond>();
        hBondList.addAll(hBonds);
        Collections.sort(hBondList, sort);
        return start + ":" + end + " " + hBondList.toString();
    }

    public int size() {
        return hBonds.size();
    }
    
    private Comparator<HBond> sort = new Comparator<HBond>() {

        @Override
        public int compare(HBond o1, HBond o2) {
            int d1 = o1.getAminoPos() - o2.getAminoPos();
            int d2 = o1.getCarboxylPos() - o2.getCarboxylPos();
            if (d1 == 0) {
                if (d2 < 0) {
                    return -1;
                } else if (d2 > 0) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                if (d1 < 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        
    };

}
