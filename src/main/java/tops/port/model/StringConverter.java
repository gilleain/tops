package tops.port.model;

import java.util.List;

import tops.engine.TParser;

public class StringConverter {
    
    private static SSEType toType(char sseChar) {
        switch (sseChar) {
            case 'E': return SSEType.EXTENDED;
            case 'H': return SSEType.HELIX;
            default: return SSEType.COIL;
        }
    }
    
    private static BridgeType toBridgeType(char sseChar) {
        switch (sseChar) {
            case 'A': return BridgeType.ANTI_PARALLEL_BRIDGE;
            case 'P': return BridgeType.PARALLEL_BRIDGE;
            default: return BridgeType.UNK_BRIDGE_TYPE;
        }
    }
    
    public static Chain convert(String topsString) {
        Chain chain = new Chain('A'); // TODO
        TParser parser = new TParser(topsString);
        int index = 0;
        for (char sseChar : parser.getVertices()) {
            SSE sse = new SSE(toType(sseChar));
            sse.setSymbolNumber(index);
            chain.addSSE(sse);
            index++;
        }
        
        List<SSE> sses = chain.getSSEs();
        String[] bits = parser.getEdgesAsStrings();
        for (int i = 0; i < bits.length; i += 3) {
            SSE left = sses.get(Integer.parseInt(bits[i]));
            SSE right = sses.get(Integer.parseInt(bits[i + 1]));
            BridgeType type = toBridgeType(bits[i + 2].charAt(0));
            Bridge bridge = new Bridge();
            bridge.setStartSSE(left);
            bridge.setEndSSE(right);
            // UGH
            bridge.addHBond(new HBond(0, 0, 0), type);
            chain.addBridge(bridge);
        }
        return chain;
    }
    

}
