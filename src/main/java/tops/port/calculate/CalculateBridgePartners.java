package tops.port.calculate;

import static tops.port.model.BridgeType.ANTI_PARALLEL_BRIDGE;
import static tops.port.model.BridgeType.PARALLEL_BRIDGE;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.port.model.BridgePartner;
import tops.port.model.BridgePartner.Side;
import tops.port.model.BridgeType;
import tops.port.model.Chain;
import tops.port.model.Chain.SSEType;
import tops.port.model.HBond;
import tops.port.model.SSE;

public class CalculateBridgePartners {
    
    private static Logger log = Logger.getLogger(CalculateBridgePartners.class.getName());
    
    /*
    function to obtain bridge partners from main chain H bond information
    assumes that DonatedHBonds and AcceptedHBonds are fully redundant
     */
    public void bridgePartFromHBonds(Chain chain) {
        int SequenceLength = chain.sequenceLength();
        

        //conditions for an anti-parallel bridge: first is another residue 
        // to which there are both donor and acceptor HBonds
        for (int i = 0; i < SequenceLength; i++) {
//            System.out.println("i " + i + " isExtended " + chain.isExtended(i));
//            System.out.println(i + " " + chain.getDonatedHBonds(i) + " " + chain.getAcceptedHBonds(i));
            if (chain.isExtended(i)) {

                for (HBond j : chain.getDonatedHBonds(i)) {
                    for (HBond k : chain.getAcceptedHBonds(i)) {
//                        System.out.println(i + ":" + j + " vs " + k);
                        if (j.getAcceptorIndex() == k.getDonorIndex()) {
                            double be = j.getEnergy() + k.getEnergy();
                            addBridgeByEnergy(chain, i, j.getAcceptorIndex(), ANTI_PARALLEL_BRIDGE, be);
                        }
                    }
                }

                // second is an accepted HBond for i-1 from l+1 
                // and a donated one from i+1 to l-1 for some l //
                if (i > 0) {
                    for (HBond j : chain.getAcceptedHBonds(i - 1)) {
                        int l = j.getAcceptorIndex() - 1;
                        double be = j.getEnergy();
                        if (l > 0) {
                            for (HBond k : chain.getAcceptedHBonds(l - 1)) {
                                if (k.getDonorIndex() == i + 1) {
                                    be += k.getEnergy();
                                    addBridgeByEnergy(chain, i, l, ANTI_PARALLEL_BRIDGE, be);
                                }
                            }
                        }
                    }
                }

                // conditions for a parallel bridge //
                // first: i has a donated H bond to l-1 and an accepted H bond from l+1 //
                for (HBond j : chain.getDonatedHBonds(i)) {
                    int l = j.getAcceptorIndex() + 1;
                    double be = j.getEnergy();
                    for (HBond k : chain.getAcceptedHBonds(i)) {
                        if (k.getDonorIndex() == l + 1) {
                            be += k.getEnergy();
                            addBridgeByEnergy(chain, i, l, PARALLEL_BRIDGE, be);
                        }
                    }
                }

                // second: i-1 has an accepted H bond from l and i+1 has a donated H bond to l //
                if (i > 0) {
                    for (HBond j : chain.getAcceptedHBonds(i - 1)) {
                        int l = j.getDonorIndex();
                        double be = j.getEnergy();
                        for (HBond k : chain.getAcceptedHBonds(l)) {
                            if (k.getDonorIndex() == i + 1) {
                                be += k.getEnergy();
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


    public void addBridgeByEnergy(Chain chain, int seqRes1, int seqRes2, BridgeType type, double energy) {
//        log.log(Level.INFO, String.format("%s %s %s", seqRes1, seqRes2, Type));
        System.out.println(String.format("%s %s %s", seqRes1, seqRes2, type));
        
        
        if ((seqRes1 >= chain.sequenceLength()) || (seqRes1 < 0) || 
            (seqRes2 >= chain.sequenceLength()) || (seqRes2 < 0)) return;
    
        if (!addBridge(chain, seqRes1, seqRes2, type, energy)) {
            replaceBridgeByEnergy(chain, seqRes1, seqRes2, type, energy);
        }
    }
    
    private boolean leftBridgeExists(Chain chain, int SeqRes1, int SeqRes2) {
        BridgePartner partner = chain.getLeftBridgePartner(SeqRes1);
        if (partner == null) {
            return false;
        } else {
            return partner.partnerResidue == SeqRes2;
        }
    }
    
    private boolean rightBridgeExists(Chain chain, int SeqRes1, int SeqRes2) {
        BridgePartner partner = chain.getRightBridgePartner(SeqRes1);
        if (partner == null) {
            return false;
        } else {
            return partner.partnerResidue == SeqRes2;
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
            // TODO : is this what was intended?
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
