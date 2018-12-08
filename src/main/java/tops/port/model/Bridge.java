package tops.port.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Bridge {
    
    private Map<HBond, BridgeType> types;
    
    private SSE sseStart;
    
    private SSE sseEnd;
    
    public Bridge() {
        types = new HashMap<>();
    }
    
    public SSE getSseStart() {
        return sseStart;
    }

    public SSE getSseEnd() {
        return sseEnd;
    }

    public boolean isBetween(SSE sseStart, SSE sseEnd) {
        return this.sseStart == sseStart && this.sseEnd == sseEnd;
    }
    
    public void setStartSSE(SSE sse) {
        this.sseStart = sse;
    }
    
    public void setEndSSE(SSE sse) {
        this.sseEnd = sse;
    }
    
    public void addHBond(HBond hBond, BridgeType type) {
        types.put(hBond, type);
    }
    
    public String getHBondsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<HBond, BridgeType> hBondType : types.entrySet()) {
            sb.append(hBondType.getKey()).append(" : ").append(hBondType.getValue()).append("\n");
        }
        return sb.toString();
    }
    
    public String toString() {
        return sseStart.getSymbolNumber() + "-" + sseEnd.getSymbolNumber();
    }

    public String getType() {
        // XXX TODO
        HBond first = types.keySet().iterator().next();
        BridgeType bridgeType = types.get(first);
        if (bridgeType == BridgeType.ANTI_PARALLEL_BRIDGE) {
            return "A";
        } else {
            return bridgeType == BridgeType.PARALLEL_BRIDGE? "P": "U";
        }
    }

    public boolean contains(SSE current) {
        return sseStart.equals(current) || sseEnd.equals(current);
    }
    
    public SSE getOther(SSE sse) {
        if (sseStart.equals(sse)) {
            return sseEnd;
        } else if (sseEnd.equals(sse)) {
            return sseStart;
        } else {
            // should have called contains first!
            return null;
        }
    }

}
