package tops.port.calculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;

public class CalculateSheets implements Calculation {
    
    private double gridUnitSize = 50;

    private class SeqDirResult {
        public int SeqDir;
        public char Dir;
        public SeqDirResult(int SeqDir, char Dir) {
            this.SeqDir = SeqDir;
            this.Dir = Dir;
        }
    }
    
    public void calculate(Chain chain) {
        System.out.println("STEP : Calculating sheets and barrels");

        List<SSE> barrel;
        for (SSE p : chain.getSSEs()) { 
            if (!p.isSymbolPlaced() && p.isStrand()) {
                barrel = this.detectBarrel(p);
                if (barrel.size() > 0) {
                    System.out.println("Barrel detected");
                    this.makeBarrel(chain, barrel, gridUnitSize);
                } else {
                    System.out.println("Sheet detected");
                    SSE q = findEdgeStrand(p, null);
                    if (!q.isSymbolPlaced()) {
                        this.makeSheet(chain, q, null, gridUnitSize);
                        if (q.hasFixedType(FixedType.FT_SHEET)) {
                            this.sheetCurvature(chain, q);
                        }
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
        System.out.println(String.format(
                "find edge strand at %s bridge partners %s and %s", current, partner0, partner1));
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
     * participating in another sheet a chain method
     */
    public List<SSE> detectBarrel(SSE p) {
        List<SSE> barrel = new ArrayList<SSE>();
        List<SSE> visited = new ArrayList<SSE>();
        this.findBarrel(p, barrel, visited, null);
        return barrel;
    }
    
    /**
     * This function detects and enumerates the first cycle found in a set of
     * strands connected by BridgePartner relationships
     */
    public boolean findBarrel(SSE p, List<SSE> barrel, List<SSE> visited, SSE addFrom) {

        if (visited.contains(p)) {
            //If we've been to this node before then we've detected a barrel - return true//
            if (!barrel.contains(p)) barrel.add(p);
            return true;
        } else {
            //else continue looking//
            visited.add(p);

            for (SSE bridgePartner : p.getPartners()) {
                if (bridgePartner == addFrom) continue;

                if (this.findBarrel(bridgePartner, barrel, visited, p)) {
                    if (barrel.get(0) != p) barrel.add(p);
                    return true;
                }
            }
        }
        return false;
    }
    
    public void makeBarrel(Chain chain, List<SSE> barrel, double GridUnitSize) {
        System.out.println("making barrel...");

        int numberOfStrands = barrel.size();

        double rads = Math.PI / (double) numberOfStrands;
        double X = rads;
        double Y = 0.5 / Math.sin(rads);
        double Z = 0.5 / Math.tan(rads);

        // this bit ensures that TIM barrels get the correct chirality """
        int start = 0;
        int increment = 0;
        if (barrel.get(1).getSymbolNumber() < barrel.get(barrel.size() - 1).getSymbolNumber()) {
            start = 0;
            increment = 1;
        } else {
            start = numberOfStrands - 1;
            increment = -1;
        }

        SSE p = barrel.get(start);
        SSE lastInBarrel = p;
        p.setDirection('D');
        
        System.out.println(String.format("make barrel start, nstrands, increment= %s %s %s", start, numberOfStrands, increment));
        int i = start;
        for (int count = 0; count < numberOfStrands; count++) { 
            System.out.println("make barrel count= " + count);
            X = rads + 2.0 * count * rads;

            SSE q = barrel.get(i);
            q.setFixedType(FixedType.FT_BARREL);

            if (count != 0) {
                chain.moveFixed(p, q);
                q.AssignRelDirection(lastInBarrel);
            }
            lastInBarrel = q;
            q.setCartoonX((int) ((Y * Math.sin(X) - 0.5) * GridUnitSize));
            q.setCartoonY((int) ((Y * Math.cos(X) - Z) * GridUnitSize));
            q.setSymbolPlaced(true);

            double Y1 = Y+1;
            for (BridgePartner bridgePartner : q.getBridgePartners()) {
                SSE r = bridgePartner.partner;
                if (!barrel.contains(r)) {
                    r.setFixedType(FixedType.FT_BARREL);
                    r.AssignRelDirection(q);
                    r.setCartoonX((int) ((Y1 * Math.sin(X) - 0.5) * GridUnitSize));
                    r.setCartoonY((int) ((Y1 * Math.cos(X) - Z) * GridUnitSize));
                    r.setSymbolPlaced(true);

                    Y1 += 1;

                    chain.moveFixed(p, r);

                    // this bit of code is a cop out for about 1% of cases 
                    // where a sheet is attached to a barrel - it just detaches it
                    // TODO ...
//                    for (int k  = 0; k < MAXBP; k++) {
//                        SSE bp = r.BridgePartner.get(k);
//                        if (bp != null && bp != q) {
//                            this.DeleteBPRelation(r, bp);
////                            bp.ShuffleDownBPs(); TODO : remove
//                        }
//                    }
//                    r.ShuffleDownBPs();   TODO - remove - think this was only for NPE
                }
            }
            i += increment;
        }
    }
    
    
    public void makeSheet(Chain chain, SSE p, SSE q, double gridUnitSize) {

        List<SSE> CurrentList = new ArrayList<SSE>();
        List<SSE> startUpperList = new ArrayList<SSE>();
        List<SSE> startLowerList = new ArrayList<SSE>();
        List<SSE> Layer = new ArrayList<SSE>();

        p.setFixedType(FixedType.FT_SHEET);

        // If this is the first strand place it based on the last strand
        if (q == null) {
            p.setCartoonX(0);
            p.setCartoonY(0);
            p.setSymbolPlaced(true);
        } else {

            // Have any of q's neighbours already been placed?
            int i = 0;
            for (BridgePartner bp : q.getBridgePartners()) {
                SSE r = bp.partner;
                if (r == null) continue;
                if (r.isSymbolPlaced()) break;
                int rindex = i;
                double incr = gridUnitSize;
                if (r != null) {
                    BridgePartner qPartner = q.findBridgePartner(p);

                    if (qPartner.side == q.getBridgePartner(rindex).side) {
                        incr = (r.getCartoonX() < q.getCartoonX())? -gridUnitSize : +gridUnitSize; 
                    } else {
                        incr = (r.getCartoonX() < q.getCartoonX())? +gridUnitSize : -gridUnitSize;
                        if (r.getCartoonX() < q.getCartoonX()) {
                            incr = gridUnitSize;
                        } else {
                            incr = -gridUnitSize;
                        }
                    }
                }
                p.setCartoonX((int) (q.getCartoonX() + incr));
                p.setCartoonY(q.getCartoonY());
                p.setSymbolPlaced(true);

                // Add this to the fixed list
                chain.moveFixed(q, p);
                p.AssignRelDirection(q);
            }
        }
        

        // For all bridge partners except q repeat
        for (BridgePartner bridgePartner : p.getBridgePartners()) {
            if (bridgePartner.partner != q) { 
                this.makeSheet(chain, bridgePartner.partner, p, gridUnitSize);
            }
        }

        sortStacking(chain, p, q, 0, startLowerList, startUpperList, 0);
    }
    
    private void sortStacking(Chain chain, SSE p, SSE q, int startXPos, List<SSE> startLowerList, List<SSE> startUpperList, int maxListLen) {
        /*
        This bit sorts out stacking ( ie. y positions ) 
        Serious changes to this code done by DW (30/1/97) to deal with sheets that fold back onto themselves
        and improve behaviour for other types of sheet It only operates on the first call to this routine (q=None), 
        after the recursive calls in the loop above
        */
        if (q == null) { 

            // first decide if we have a single layer or two layers, and in the case of two determine 
            // a start point for dividing the structures. There are two layers if there exists an x 
            // position with two strands which have no common bridge partner
            boolean twoLayers = false;
            double currentXPos = chain.leftMostPos(p);
            List<SSE> currentList = chain.getListAtXPosition(p, currentXPos);
            while (!currentList.isEmpty()) {
                for (int i = 0; i < currentList.size(); i++) {
                    SSE r = currentList.get(i);
                    for (int j = i + 1; j < currentList.size(); j++) {
                        SSE s = currentList.get(j);
                        if (r.getFirstCommonBP(s) == null) {
                            twoLayers = true;
                            startXPos = (int) currentXPos;
                            startUpperList = chain.listBPGroup(r, currentList);
                            for (SSE t : currentList) {
                                if (!startUpperList.contains(t)) {
                                    startLowerList.add(t);
                                }
                            }
                            break;
                        }
                        if (twoLayers) break;
                    }
                    if (twoLayers) break;
                }

                currentXPos += gridUnitSize;
                currentList = chain.getListAtXPosition(p, currentXPos);
            }

            // divide layers if necessary
            if (twoLayers) {

              divideLayers(chain, p, startXPos, startLowerList, startUpperList);  
            }

            // deal with cases where there are split strands
            splitStrands(chain, p, maxListLen);
        }
    }
    
    private void divideLayers(Chain chain, SSE p, int startXPos, List<SSE> startLowerList, List<SSE> startUpperList) {
        System.out.println("Sheet configured as V_CURVED_SHEET");
        for (SSE t : chain.iterFixed(p)) {
            t.setFixedType(FixedType.FT_V_CURVED_SHEET);
        }

        // divide at the point of splitting identified above
        for (SSE k : startLowerList) {
            k.setCartoonY((int) (k.getCartoonY() - gridUnitSize));
        }

        // divide to right, then left
        this.splitSheet(chain, p, startUpperList, startLowerList, startXPos, 1);
        this.splitSheet(chain, p, startUpperList, startLowerList, startXPos, -1);
    }
    
    private void splitStrands(Chain chain, SSE p, int maxListLen) {
        for (SSE r : chain.iterFixed(p)) {
            
            List<SSE> currentList = chain.getListAtPosition(p, r.getCartoonX(), r.getCartoonY());

            if (currentList.size() > 1) { 
                SSE bp = getCommonBP(currentList, maxListLen);
                if (bp != null) {
                    sortListByBridgeRange(bp, currentList);
                    this.spreadList(currentList, bp.getDirection());
                }
            }
        }
    }
    
    public void sortListByBridgeRange(final SSE bp, List<SSE> sseList) {
        Comparator<SSE> sorter = new Comparator<SSE>() {

            @Override
            public int compare(SSE o1, SSE o2) {
                BridgePartner bp1 = bp.findBridgePartner(o1);
                BridgePartner bp2 = bp.findBridgePartner(o2);
                return bp2.rangeMin - bp1.rangeMin;
            }
            
        };
        Collections.sort(sseList, sorter);
    }
    
    private SSE getCommonBP(List<SSE> currentList, int MaxListLen) {
        SSE start = currentList.get(0);
        if (start == null) return null;
        return SSE.getCommonBP(start, currentList);
    }
    
    public void spreadList(List<SSE> sseList, char direction) {
        int n = sseList.size();

        int span = 0;
        for (int i = 1; i < n; i++) {
            span += sseList.get(i-1).getSymbolRadius();
            span += sseList.get(i).getSymbolRadius();
            if (sseList.get(i-1).getDirection() == sseList.get(i).getDirection()) {
                span -= (sseList.get(i).getSymbolRadius()) / 2;
            }
        }

        span /= 2;
        int directionMultiplier = 1;
        if (direction == 'U') {
            span = -1;
            directionMultiplier = -1;
        }
        
        for (int i = 0; i < n; i++) {
            sseList.get(i).setCartoonY(sseList.get(i).getCartoonY() + span);
            if ((i + 1) < n) {
                span -= directionMultiplier * sseList.get(i).getSymbolRadius();
                span -= directionMultiplier * sseList.get(i+1).getSymbolRadius();
                if (sseList.get(i).getDirection() == sseList.get(i+1).getDirection()) {
                    span += directionMultiplier * (sseList.get(i+1).getSymbolRadius() / 2);
                }
            }
        }
    }
    
    public void splitSheet(Chain chain, SSE p, List<SSE> startUpperList, List<SSE> startLowerList, int startXPos, int direction) {

        int UPPER_LAYER = 3;
        int MIDDLE_LAYER = 2;
        int LOWER_LAYER = 1;
        int UNK_LAYER = 0;

        double currentXPos = startXPos + direction * gridUnitSize;
        List<SSE> currentList = chain.getListAtXPosition(p, currentXPos);
        List<SSE> upperList = startUpperList;
        List<SSE> lowerList = startLowerList;

        List<Integer> layer = new ArrayList<Integer>();
        while (currentList.size() != 0) {
            for (SSE r : currentList) {
                if (r == null) break; 
                boolean onUpperList = r.hasBPonList(upperList);
                boolean onLowerList = r.hasBPonList(lowerList);
                if (onUpperList && onLowerList) {
                    layer.add(MIDDLE_LAYER);
                } else if (onUpperList) {
                    layer.add(UPPER_LAYER);
                } else if (onLowerList) {
                    layer.add(LOWER_LAYER);
                } else {
                    layer.add(UNK_LAYER);
                }
            }

            int i = 0;
            for (int layerValue : layer) {
                if (layerValue == UNK_LAYER) {
                    for (SSE j : upperList) {
                        if (j == null) break;
                        SSE bp = currentList.get(i).getFirstCommonBP(j); 
                        if (bp != null) { 
                            layerValue = UPPER_LAYER;
                        }
                    }
                    
                    for (SSE j : lowerList) {
                        if (j == null) break;
                        if (currentList.get(i).getFirstCommonBP(j) != null) {
                            if (layerValue == UPPER_LAYER) {
                                layerValue = MIDDLE_LAYER;
                            } else {
                                layerValue = LOWER_LAYER;
                            }
                        }
                    }
                }
                i++;
            }
            upperList = new ArrayList<SSE>();
            lowerList = new ArrayList<SSE>();

            int k = 0;
            for (SSE r : currentList) {
                if (layer.get(k) == UPPER_LAYER) {
                    upperList.add(r);
                } else if (layer.get(k) == MIDDLE_LAYER) {
                    r.setCartoonY((int) (r.getCartoonY() - (gridUnitSize / 2)));
                } else if (layer.get(k) == LOWER_LAYER) {
                    r.setCartoonY((int) (r.getCartoonY() - gridUnitSize));
                    lowerList.add(r);
                }
            }
                
            currentXPos += direction * gridUnitSize;
            currentList = chain.getListAtXPosition(p, currentXPos);
        }
    }

    
    /** 
     * this function checks ordinary sheets to see if curvature is strong enough for plotting on arc 
     * the test q.FixedType == FT_SHEET is necessary since Make Sheet can produce FT_V_CURVED_SHEET as well
     * as FT_SHEET, and the former type should not be examined by SheetCurvature
     * 
     */
    public void sheetCurvature(Chain chain, SSE p) {

        double MinSpan = 5;
        double MaxSep = 10.0;

        SSE Start = chain.findFixedStart(p);

        if (Start == null || !Start.hasFixedType(FixedType.FT_SHEET)) return;

        // first determine left and right most strands in the sheet """
        SSE left = chain.getListAtXPosition(Start, chain.leftMostPos(Start)).get(0);
        SSE right = chain.getListAtXPosition(Start, chain.rightMostPos(Start)).get(0);
        
        double span = chain.fixedSpan(Start, gridUnitSize);

        // MaxSep is 0.65 of the separation if the sheet were flat with 4.5A between each pair of strands """
        MaxSep = 4.5 * (span - 1.0) * 0.65;

        double sep = chain.secStrucSeparation(left, right);

        System.out.println(String.format("SheetCurvature %d %d %f %f", left.getSymbolNumber(), right.getSymbolNumber(), sep, MaxSep));

        if (span >= MinSpan && sep<MaxSep) {

            System.out.println("Sheet configured as CURVED_SHEET");

            for (SSE q : chain.iterFixed(Start)) q.setFixedType(FixedType.FT_CURVED_SHEET);

            // this direction calculation is to ensure that sheets from AB barrels are plotted with the correct chirality """
            SeqDirResult result = this.findSheetSeqDir(chain, Start);
            double AddDir;
            if (result.SeqDir == 1) {
                if (result.Dir == 'D') AddDir = 1.0;
                else AddDir = -1.0;
            } else if (result.SeqDir == -1) {
                if (result.Dir=='U') AddDir = 1.0;
                else AddDir = -1.0;
            } else {
                AddDir = 1.0;
            }
        
            int NSpaces = (int) (span + 1);

            double Rads = Math.PI / NSpaces;
            double X = Rads;
            double YStart = 0.5 / Math.sin(Rads);
            double Z = 0.5 / Math.tan(Rads);

            int CurrXPos = (int) chain.leftMostPos(Start);
            List<SSE> sseList = chain.getListAtXPosition(Start, CurrXPos);

            double YEF = 0.75;
            while (sseList.size() > 0) {
                double Y = YStart;
                for (SSE sse : sseList) {
                    sse.setCartoonX((int) ((Y * Math.sin(X) - 0.5) * gridUnitSize));
                    sse.setCartoonY((int) ((Y * Math.cos(X) - Z) * gridUnitSize));
                    sse.setSymbolPlaced(true);
                    Y += YEF;
                }

                CurrXPos += gridUnitSize;
                sseList = chain.getListAtXPosition(Start, CurrXPos);

                X += AddDir * 2.0 * Rads;
           }
        }
    }
    
    public SeqDirResult findSheetSeqDir(Chain chain, SSE Start) {
        SSE prev = null;
        SSE curr = null;

        double CurrXPos = chain.leftMostPos(Start);
        List<SSE> sseList = chain.getListAtXPosition(Start, CurrXPos);
        if (sseList.size() == 1) prev = sseList.get(0);

        char lastdir = 'X';
        int lastsdir = 0;
        char dir = 'X';
        int sdir = 0;
        boolean done = false;

        while (sseList.size() > 0) {

            CurrXPos += gridUnitSize;
            sseList = chain.getListAtXPosition(Start, CurrXPos);

            if (sseList.size() == 1) curr = sseList.get(0);
            else curr = null;

            if (curr != null && prev != null && curr.getDirection() == prev.getDirection()) {
                if (curr.getSymbolNumber() > prev.getSymbolNumber()) sdir = 1;
                else sdir = -1;
                dir = curr.getDirection();
                if (sdir == lastsdir && dir == lastdir) {
                    done = true;
                    break;
                }
            } else {
                dir = 'X';
                sdir = 0;
            }

            prev = curr;
            lastsdir = sdir;
            lastdir = dir;
        }

        if (done) {
            return new SeqDirResult(sdir, dir);
        } else {
            System.out.println("find seq dir not done!");
            return new SeqDirResult(0, ' ');
        }
    }

    @Override
    public void setParameter(String key, double value) {
        if (key.equals("gridUnitSize")) {
            gridUnitSize = value;
        }
    }

}
