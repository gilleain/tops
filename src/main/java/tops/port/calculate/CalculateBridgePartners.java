package tops.port.calculate;

import static tops.port.model.BridgeType.ANTI_PARALLEL_BRIDGE;
import static tops.port.model.BridgeType.PARALLEL_BRIDGE;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.Bridge;
import tops.port.model.BridgeType;
import tops.port.model.Chain;
import tops.port.model.HBond;
import tops.port.model.SSE;
import tops.port.model.SSEType;

public class CalculateBridgePartners implements Calculation {
    
    private static Logger log = Logger.getLogger(CalculateBridgePartners.class.getName());
    
    @Override
    public void calculate(Chain chain) {
        log.log(Level.INFO, "STEP : Calculating bridge partners");
        for (SSE sse : chain.getSSEs()) {
            if (sse.getSSEType() == SSEType.EXTENDED) {
                int start = sse.sseData.pdbStartResidue;
                int end = sse.sseData.pdbFinishResidue;
                for (int index = start; index < end; index++) {
                    typeA(chain, sse, index);
                    typeB(chain, sse, index);
                    typeC(chain, sse, index);
                    typeD(chain, sse, index);
                }
            }
        }
    }

    @Override
    public void setParameter(String key, double value) {
        // TODO Auto-generated method stub
        
    }
    
    private void log(String typeString, int index, SSE otherSSE) {
//        log.log(Level.OFF, typeString + " lookup " + index + " = " + (otherSSE == null? "null" : otherSSE.getSummary()));
    }

    private void typeA(Chain chain, SSE sse, int i) {
        // conditions for an anti-parallel bridge: first is another residue 
        // to which there are both donor and acceptor HBonds
        for (HBond j : chain.getDonatedHBonds(i)) {
            for (HBond k : chain.getAcceptedHBonds(i)) {
                if (j.getAcceptorIndex() == k.getDonorIndex()) {
                    SSE otherSSE = chain.findSecStr(k.getDonorIndex());
                    log("A", k.getDonorIndex(), otherSSE);
                    addBridge(i, chain, sse, otherSSE, j, k, ANTI_PARALLEL_BRIDGE, "A");
                }
            }
        }
    }
    
    private void typeB(Chain chain, SSE sse, int i) {
        // second is an accepted HBond for i-1 from l+1 
        // and a donated one from i+1 to l-1 for some l //
        if (i > 0) {
            for (HBond j : chain.getAcceptedHBonds(i - 1)) {
                int l = j.getAcceptorIndex() - 1;
                if (l > 0) {
                    for (HBond k : chain.getAcceptedHBonds(l - 1)) {
                        if (k.getDonorIndex() == i + 1) {
                            SSE otherSSE = chain.findSecStr(k.getAcceptorIndex());
                            log("B", k.getAcceptorIndex(), otherSSE);
                            addBridge(i, chain, sse, otherSSE, j, k, ANTI_PARALLEL_BRIDGE, "B");
                        }
                    }
                }
            }
        }
    }
    
    private void typeC(Chain chain, SSE sse, int i) {
        // conditions for a parallel bridge //
        // first: i has a donated H bond to l-1 and an accepted H bond from l+1 //
        for (HBond j : chain.getDonatedHBonds(i)) {
            int l = j.getAcceptorIndex() + 1;
            for (HBond k : chain.getAcceptedHBonds(i)) {
                if (k.getDonorIndex() == l + 1) {
                    SSE otherSSE = chain.findSecStr(k.getDonorIndex());
                    if (otherSSE == null) continue;
                    log("C", k.getDonorIndex(), otherSSE);
                    addBridge(i, chain, sse, otherSSE, j, k, PARALLEL_BRIDGE, "C");
                }
            }
        }
    }
    
    private void typeD(Chain chain, SSE sse, int i) {
        // second: i-1 has an accepted H bond from l and i+1 has a donated H bond to l //
        if (i > 0) {
            for (HBond j : chain.getAcceptedHBonds(i - 1)) {
                int l = j.getDonorIndex();
                for (HBond k : chain.getAcceptedHBonds(l)) {
                    if (k.getDonorIndex() == i + 1) {
                        SSE otherSSE = chain.findSecStr(k.getAcceptorIndex());
                        log("D", k.getAcceptorIndex(), otherSSE);
                        addBridge(i, chain, sse, otherSSE, j, k, PARALLEL_BRIDGE, "D");
                    }
                }
            }
        }
    }
   
    
    /* 
    a small utility function to find the side for a bridge partner residue 
     */
//    private BridgePartner.Side findBridgeSide(Chain chain, int residueIndex, int bridgeIndex) {
//        if (residueIndex < chain.sequenceLength()) {
//            if (chain.getRightBridgePartner(residueIndex).partnerResidue == bridgeIndex) { 
//                return BridgePartner.Side.LEFT;
//            } else if (chain.getLeftBridgePartner(residueIndex).partnerResidue == bridgeIndex) {
//                return BridgePartner.Side.RIGHT;
//            } else {
//                return BridgePartner.Side.UNKNOWN;
//            }
//        } else {
//            return BridgePartner.Side.UNKNOWN;
//        }
//    }


    private void addBridge(int index, Chain chain, SSE sseA, SSE sseB, HBond bond1, HBond bond2, BridgeType type, String func) {
        if (sseA == null || sseB == null) return;
//        log.log(Level.INFO, String.format("%s %s %s", seqRes1, seqRes2, Type));
//        System.out.println(String.format("BRIDGE %s %s %s %s %s %s %s", 
//                index, bond1, bond2, type, 
//                sseA == null? null : sseA.getSymbolNumber(), 
//                sseB == null? null : sseB.getSymbolNumber(), func));
//        if (sseStart.getSymbolNumber() > sseEnd.getSymbolNumber()) return;  // XXX could do better?
        SSE sseStart = (sseA.getSymbolNumber() < sseB.getSymbolNumber())? sseA : sseB;
        SSE sseEnd = (sseA.getSymbolNumber() < sseB.getSymbolNumber())? sseB : sseA;
        Bridge bridge = chain.findBridge(sseStart, sseEnd);
        if (bridge == null) {
            bridge = new Bridge();
            bridge.setStartSSE(sseStart);
            bridge.setEndSSE(sseEnd);
            chain.addBridge(bridge);
        }
        bridge.addHBond(bond1, type);
        bridge.addHBond(bond2, type);
    }
   
  
}
