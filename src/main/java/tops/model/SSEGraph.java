package tops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author maclean
 *
 */
public class SSEGraph {
    
    private List<SSE> sses;
    
    private List<HBondSet> hbondSets;
    
    public SSEGraph() {
        this.sses = new ArrayList<SSE>();
        this.hbondSets = new ArrayList<HBondSet>();
    }
    
    public void addSSE(SSE sse) {
        this.sses.add(sse);
    }
    
    public List<SSE> getSSES() {
        return this.sses;
    }
    
    public void addHBondSet(HBondSet hBondSet) {
        this.hbondSets.add(hBondSet);
    }
    
    public void addHBondSets(List<HBondSet> hBondSets) {
        for (HBondSet hBondSet : hBondSets) {
            addHBondSet(hBondSet);
        }
    }

    public List<HBondSet> getHBondSets() {
        return hbondSets;
    }

}
