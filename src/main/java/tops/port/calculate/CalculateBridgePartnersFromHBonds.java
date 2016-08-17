package tops.port.calculate;

import static tops.port.model.BridgeType.ANTI_PARALLEL_BRIDGE;
import static tops.port.model.BridgeType.PARALLEL_BRIDGE;

import tops.port.model.BridgePartner;
import tops.port.model.BridgePartner.Side;
import tops.port.model.BridgeType;
import tops.port.model.Chain;
import tops.port.model.Chain.SSEType;
import tops.port.model.SSE;

public class CalculateBridgePartnersFromHBonds {
    
    /*
    function to obtain bridge partners from main chain H bond information
    assumes that DonatedHBonds and AcceptedHBonds are fully redundant
     */
    public void bridgePartFromHBonds(Chain chain) {
        int SequenceLength = chain.sequenceLength();
        

        //conditions for an anti-parallel bridge: first is another residue to which there are both donor and acceptor HBonds//
        for (int i = 0; i < SequenceLength; i++) {
            if (chain.isExtended(i)) {

                for (int j : chain.getDonatedHBonds(i)) {
                    for (int k : chain.getAcceptedHBonds(i)) {
                        if (j == k) {
                            double be = chain.getDonatedHBondEnergy(i, j) + chain.getDonatedHBondEnergy(i, k);
                            addBridgeByEnergy(chain, i, j, ANTI_PARALLEL_BRIDGE, be);
                        }
                    }
                }

                // second is an accepted HBond for i-1 from l+1 and a donated one from i+1 to l-1 for some l //
                if (i > 0) {
                    for (int j : chain.getAcceptedHBonds(i - 1)) {
                        int l = j - 1;
                        double be = chain.getAcceptedHBondEnergy(i - 1, j);
                        if (l > 0) {
                            for (int k : chain.getAcceptedHBonds(l - 1)) {
                                if (k == i + 1) {
                                    be += chain.getAcceptedHBondEnergy(l - 1, k);
                                    addBridgeByEnergy(chain, i, l, ANTI_PARALLEL_BRIDGE, be);
                                }
                            }
                        }
                    }
                }

                // conditions for a parallel bridge //
                // first: i has a donated H bond to l-1 and an accepted H bond from l+1 //
                for (int j : chain.getDonatedHBonds(i)) {
                    int l = j + 1;
                    double be = chain.getDonatedHBondEnergy(i, j);
                    for (int k : chain.getAcceptedHBonds(i)) {
                        if (k == l + 1) {
                            be += chain.getAcceptedHBondEnergy(i, k);
                            addBridgeByEnergy(chain, i, l, PARALLEL_BRIDGE, be);
                        }
                    }
                }

                // second: i-1 has an accepted H bond from l and i+1 has a donated H bond to l //
                if (i > 0) {
                    for (int j : chain.getAcceptedHBonds(i - 1)) {
                        int l = j;
                        double be = chain.getAcceptedHBondEnergy(i - 1, j);
                        for (int k : chain.getAcceptedHBonds(l)) {
                            if (k == i + 1) {
                                be += chain.getAcceptedHBondEnergy(l, k);
                                addBridgeByEnergy(chain, i, l, PARALLEL_BRIDGE, be);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void assignBridgePartnersToSSE(int index, Chain chain, SSE p, SSEType secondaryStructure, int currentResidue) {
        //   bridge partners //
        if (secondaryStructure == SSEType.EXTENDED) {
            BridgePartner leftBridge = chain.getLeftBridgePartner(currentResidue);

            SSE leftBridgeSSE = chain.findSecStr(leftBridge.partnerResidue);
            if ((leftBridgeSSE != null) && (leftBridge.partnerResidue < index) && (leftBridgeSSE.isStrand())) {
                leftBridgeSSE.updateSecStr(p, index, BridgePartner.Side.LEFT, leftBridge.bridgeType);
                BridgePartner.Side bridgeSide = findBridgeSide(chain, leftBridge.partnerResidue, index);
                p.updateSecStr(leftBridge.partner, leftBridge.partnerResidue, bridgeSide, leftBridge.bridgeType);
            }
            
            BridgePartner rightBridge = chain.getRightBridgePartner(currentResidue);

            SSE rightBridgeSSE = chain.findSecStr(rightBridge.partnerResidue);
            if ((rightBridgeSSE != null) && (rightBridge.partnerResidue < index)  && (rightBridgeSSE.isStrand())) {
                rightBridgeSSE.updateSecStr(p, index, BridgePartner.Side.RIGHT, rightBridge.bridgeType);
                BridgePartner.Side bridgeSide = findBridgeSide(chain, rightBridge.partnerResidue, index);
                p.updateSecStr(rightBridge.partner, rightBridge.partnerResidue, bridgeSide, rightBridge.bridgeType);
            }
        }
    }
    
    /* 
    a small utility function to find the side for a bridge partner residue 
     */
    private BridgePartner.Side findBridgeSide(Chain chain, int residue, int bridge) {
        if (residue < chain.sequenceLength()) {
            if (chain.getRightBridgePartner(residue).partnerResidue == bridge) { 
                return BridgePartner.Side.LEFT;
            } else if (chain.getLeftBridgePartner(residue).partnerResidue == bridge) {
                return BridgePartner.Side.RIGHT;
            } else {
                return BridgePartner.Side.UNKNOWN;
            }
        } else {
            return BridgePartner.Side.UNKNOWN;
        }
    }


    public void addBridgeByEnergy(Chain chain, int SeqRes1, int SeqRes2, BridgeType Type, double Energy) {
    
        if ((SeqRes1 >= chain.sequenceLength()) || (SeqRes1 < 0) || 
            (SeqRes2 >= chain.sequenceLength()) || (SeqRes2 < 0)) return;
    
        if (!addBridge(chain, SeqRes1, SeqRes2, Type, Energy)) {
            replaceBridgeByEnergy(chain, SeqRes1, SeqRes2, Type, Energy);
        }
    }
    
    private boolean leftBridgeExists(Chain chain, int SeqRes1, int SeqRes2) {
        int partner = chain.getLeftBridgePartner(SeqRes1).partnerResidue;
        if (partner < 0 || partner == SeqRes2) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean rightBridgeExists(Chain chain, int SeqRes1, int SeqRes2) {
        int partner = chain.getRightBridgePartner(SeqRes1).partnerResidue;
        if (partner < 0 || partner == SeqRes2) {
            return true;
        } else {
            return false;
        }
    }


    public boolean addBridge(Chain chain, int SeqRes1, int SeqRes2, BridgeType Type, double Energy) {
        int SequenceLength = chain.sequenceLength();
        if ((SeqRes1 >= SequenceLength) || (SeqRes1 < 0) || (SeqRes2 >= SequenceLength) || (SeqRes2 < 0)) return false;

        boolean left1 = leftBridgeExists(chain, SeqRes1, SeqRes2);
        if (left1) return true;
        
        boolean right1 = rightBridgeExists(chain, SeqRes1, SeqRes2);
        if (right1) return true;
        
        boolean left2 = leftBridgeExists(chain, SeqRes2, SeqRes1);
        if (left2) return true;
        
        boolean right2 = rightBridgeExists(chain, SeqRes2, SeqRes1);
        if (right1) return true;
            
        if (left1 && left2 && right1 && right2) {
            BridgePartner a = new BridgePartner(null, SeqRes2, Type, Side.UNKNOWN);
            a.energy = Energy;
            
            BridgePartner b = new BridgePartner(null, SeqRes1, Type, Side.UNKNOWN);
            b.energy = Energy;
            return true;
        }

        return false;
    }


    public void replaceBridgeByEnergy(Chain chain, int SeqRes1, int SeqRes2, BridgeType Type, double Energy) {

        if ((SeqRes1 >= chain.sequenceLength()) || (SeqRes1 < 0) || 
            (SeqRes2 >= chain.sequenceLength()) || (SeqRes2 < 0)) return;

        BridgePartner hp1 = highestEnergyBridge(chain, SeqRes1); 
        BridgePartner hp2 = highestEnergyBridge(chain, SeqRes2);

        if (hp1 != null && hp2 != null && Energy < hp1.energy && Energy < hp2.energy) {
            hp1.energy = Energy;
            hp2.energy = Energy;
        }
    }


    public BridgePartner highestEnergyBridge(Chain chain, int SeqRes) {
        double he = Double.MIN_VALUE;
        BridgePartner left = chain.getLeftBridgePartner(SeqRes); 
        if (left.energy > he) {
            return left;
        }
        
        BridgePartner right = chain.getLeftBridgePartner(SeqRes); 
        if (right.energy > he) {
            return right;
        }
        
        return null;
     }


}
