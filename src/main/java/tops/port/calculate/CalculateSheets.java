package tops.port.calculate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Bridge;
import tops.port.model.Chain;
import tops.port.model.SSE;
import tops.port.model.tse.Barrel;
import tops.port.model.tse.Sheet;

public class CalculateSheets implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateDirection.class.getName());
    
    
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Calculating sheets and barrels");

        BitSet seen = new BitSet(chain.countStructures());
        List<List<SSE>> components = new ArrayList<>();
        for (SSE sse : chain.getSSEs()) {
            int index = chain.getSSEs().indexOf(sse);
            if (sse.isStrand() && !seen.get(index)) {
                List<SSE> component = new ArrayList<>();
                search(chain, sse, null, component, seen);
                components.add(component);
            }
        }
        for (List<SSE> component : components) {
            if (isBarrel(chain, component)) {
                chain.addTSE(new Barrel(component));
            } else {
                chain.addTSE(new Sheet(component));
            }
        }
    }
    
    private void search(Chain chain, SSE current, SSE last, List<SSE> tse, BitSet visited) {
        tse.add(current);
        visited.set(chain.getSSEs().indexOf(current));
        List<Bridge> bridges = chain.getBridges(current);
        for (Bridge bridge : bridges) {
            SSE next = bridge.getOther(current);
            if (last != null && next.getSymbolNumber() == last.getSymbolNumber()) {
                continue;
            } else {
                int nextIndex = chain.getSSEs().indexOf(next);
                if (!visited.get(nextIndex)) {
                    search(chain, next, current, tse, visited);
                }
            }
        }
    }
    
    private String s(List<SSE> sses) {
        StringBuffer sb = new StringBuffer("[");
        for (SSE sse : sses) {
            sb.append(s(sse)).append(" ");
        }
        sb.append("]");
        return sb.toString();
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
    public boolean isBarrel(Chain chain, List<SSE> component) {
        List<SSE> visited = new ArrayList<>();
        return this.isBarrel(chain, component.get(0), visited, null);
    }
    
    /**
     * This function detects and enumerates the first cycle found in a set of
     * strands connected by BridgePartner relationships
     */
    private boolean isBarrel(Chain chain, SSE sse, List<SSE> visited, SSE addFrom) {
//        System.out.println("Visiting " + s(sse));
        if (visited.contains(sse)) {
            //If we've been to this node before then we've detected a barrel - return true//
            log.info("Found barrel");
            return true;
        } else {
            //else continue looking//
            visited.add(sse);

            for (Bridge bridge : chain.getBridges(sse)) {
                SSE other = bridge.getOther(sse);
//                System.out.println("Trying partner " + bridge + " other " + s(other));
                if (other == addFrom) continue;

                if (isBarrel(chain, other, visited, sse)) {
                    return true;
                }
            }
        }
        log.info("No barrel");
        return false;
    }
    
    @Override
    public void setParameter(String key, double value) {
        // no-op
    }

}
