package tops.port.model;

import java.util.HashMap;
import java.util.Map;

public class Bridge {
    
    private Map<HBond, BridgeType> types;
    
    private SSE sseStart;
    
    private SSE sseEnd;
    
    public Bridge() {
        types = new HashMap<HBond, BridgeType>();
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
        StringBuffer sb = new StringBuffer();
        for (HBond hBond : types.keySet()) {
            sb.append(hBond).append(" : ").append(types.get(hBond)).append("\n");
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
        return bridgeType == BridgeType.ANTI_PARALLEL_BRIDGE? "A":
                bridgeType == BridgeType.PARALLEL_BRIDGE? "P": "U";
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
