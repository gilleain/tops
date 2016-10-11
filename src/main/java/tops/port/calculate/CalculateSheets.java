package tops.port.calculate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Bridge;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.TSE;

public class CalculateSheets implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateDirection.class.getName());
    
    
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Calculating sheets and barrels");

        BitSet seen = new BitSet(chain.countStructures());
        for (SSE sse : chain.getSSEs()) {
            int index = chain.getSSEs().indexOf(sse);
            if (sse.isStrand() && !seen.get(index)) {
                TSE barrel = new TSE(FixedType.UNKNOWN);
                System.out.println("searching with " + s(sse));
                search(chain, sse, null, barrel, seen);
                chain.addTSE(barrel);
            }
        }
    }
    
    private void search(Chain chain, SSE current, SSE last, TSE tse, BitSet visited) {
        tse.add(current);
        visited.set(chain.getSSEs().indexOf(current));
        List<Bridge> bridges = chain.getBridges(current);
        for (Bridge bridge : bridges) {
            SSE next = bridge.getOther(current);
            System.out.print("Current = " + s(current) + " Bridge = " + bridge 
                    + " Next = " + s(next)
                    + " Last = " + s(last));
            if (last != null && next.getSymbolNumber() == last.getSymbolNumber()) {
                System.out.println(" Ignoring");
                continue;
            } else {
                int nextIndex = chain.getSSEs().indexOf(next);
                if (visited.get(nextIndex)) {
                    System.out.println(" Already seen");
                } else {
                    System.out.println(" Following");
                    search(chain, next, current, tse, visited);
                }
            }
        }
    }
    
    private String s(SSE sse) {
        if (sse == null) {
            return "";
        } else {
            return "" + sse.getSymbolNumber();
        }
    }
    
    /**
     * This function detects barrels of the slightly more general variety ie. a
     * cycle of strands but with the possibility of some strands also
     * participating in another sheet
     */
    public TSE detectBarrel(SSE p) {
        TSE barrel = new TSE(FixedType.BARREL);
        List<SSE> visited = new ArrayList<SSE>();
        this.findBarrel(p, barrel, visited, null);
        return barrel;
    }
    
    /**
     * This function detects and enumerates the first cycle found in a set of
     * strands connected by BridgePartner relationships
     */
    public boolean findBarrel(SSE p, TSE barrel, List<SSE> visited, SSE addFrom) {

        if (visited.contains(p)) {
            //If we've been to this node before then we've detected a barrel - return true//
            if (!barrel.contains(p)) {
                barrel.add(p);
            }
            return true;
        } else {
            //else continue looking//
            visited.add(p);

            for (SSE bridgePartner : p.getPartners()) {
                if (bridgePartner == addFrom) continue;

                if (this.findBarrel(bridgePartner, barrel, visited, p)) {
                    if (barrel.getFirst() != p) barrel.add(p);
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void setParameter(String key, double value) {
        
    }

}
