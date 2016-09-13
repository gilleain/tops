package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.TSE;

public class CalculateSheets implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateDirection.class.getName());
    
    
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Calculating sheets and barrels");

        TSE barrel;
        for (SSE p : chain.getSSEs()) { 
            if (!p.isSymbolPlaced() && p.isStrand()) {
                barrel = this.detectBarrel(p);
                if (barrel.size() > 0) {
                    System.out.println("Barrel detected");
                    chain.addTSE(barrel);
                } else {
//                    System.out.println("Sheet detected");
                    SSE q = findEdgeStrand(p, null);
                    if (!q.isSymbolPlaced()) {
                       
                    }
                }
            }
        }
    }
    
    //FIXME 
    //recursive and unidirectional!//
    public SSE findEdgeStrand(SSE current, SSE last) {
        if (current == null) return last;
        
        List<BridgePartner> partners = current.getBridgePartners(); 
        BridgePartner partner0 = partners.size() > 0? partners.get(0) : null;
        BridgePartner partner1 = partners.size() > 1? partners.get(1) : null;
//        System.out.println(String.format(
//                "find edge strand at %s bridge partners %s and %s", current, partner0, partner1));
        if (partner0 == null || partner1 == null) {
            return current;
        } else {
            if (partner0.partner != last) {
                return this.findEdgeStrand(partner0.partner, current);
            } else {
                return this.findEdgeStrand(partner1.partner, current);
            }
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
