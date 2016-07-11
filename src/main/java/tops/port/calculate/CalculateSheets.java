package tops.port.calculate;

import java.util.ArrayList;
import java.util.List;

import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.FixedType;
import tops.port.model.SSE;

public class CalculateSheets implements Calculation {
    
    private double GridUnitSize = 50;

    private class SeqDirResult {
        public int SeqDir;
        public char Dir;
        public SeqDirResult(int SeqDir, char Dir) {
            this.SeqDir = SeqDir;
            this.Dir = Dir;
        }
    }
    
    public void calculate(Chain chain) {
        System.out.println("Calculating sheets and barrels");

        List<SSE> barrel;
        for (SSE p : chain.getSSEs()) { 
            if (!p.isSymbolPlaced() && p.isStrand()) {
                barrel = this.detectBarrel(p);
                if (barrel.size() > 0) {
                    System.out.println("Barrel detected");
                    this.makeBarrel(chain, barrel, GridUnitSize);
                } else {
                    System.out.println("Sheet detected");
                    SSE q = p.FindEdgeStrand(null);
                    if (!q.isSymbolPlaced()) {
                        this.makeSheet(chain, q, null, GridUnitSize);
                        if (q.hasFixedType(FixedType.FT_SHEET)) {
                            this.sheetCurvature(chain, q);
                        }
                    }
                }
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
    public boolean findBarrel(SSE p, List<SSE> barrel, List<SSE> visited, SSE AddFrom) {

        if (visited.contains(p)) {
            //If we've been to this node before then we've detected a barrel - return true//
            if (!barrel.contains(p)) barrel.add(p);
            return true;
        } else {
            //else continue looking//
            visited.add(p);

            for (SSE bridgePartner : p.getPartners()) {
                if (bridgePartner == AddFrom) continue;

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

        int NStrands = barrel.size();

        double Rads = Math.PI / (double) NStrands;
        double X = Rads;
        double Y = 0.5 / Math.sin(Rads);
        double Z = 0.5 / Math.tan(Rads);

        // this bit ensures that TIM barrels get the correct chirality """
        int start = 0;
        int increment = 0;
        if (barrel.get(1).getSymbolNumber() < barrel.get(barrel.size() - 1).getSymbolNumber()) {
            start = 0;
            increment = 1;
        } else {
            start = NStrands - 1;
            increment = -1;
        }

        SSE p = barrel.get(start);
        SSE lastInBarrel = p;
        p.setDirection('D');
        
        System.out.println(String.format("make barrel start, nstrands, increment= %s %s %s", start, NStrands, increment));
        int i = start;
        for (int count = 0; count < NStrands; count++) { 
            System.out.println("make barrel count= " + count);
            X = Rads + 2.0 * count * Rads;

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
    
    
    public void makeSheet(Chain chain, SSE p, SSE q, double GridUnitSize) {

        List<SSE> CurrentList = new ArrayList<SSE>();
        List<SSE> StartUpperList = new ArrayList<SSE>();
        List<SSE> StartLowerList = new ArrayList<SSE>();
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
                double incr = GridUnitSize;
                if (r != null) {
                    int pindex = q.FindBPIndex(p);

                    if (q.getBridgePartner(pindex).side == q.getBridgePartner(rindex).side) {
                        incr = (r.getCartoonX() < q.getCartoonX())? -GridUnitSize : +GridUnitSize; 
                    } else {
                        incr = (r.getCartoonX() < q.getCartoonX())? +GridUnitSize : -GridUnitSize;
                        if (r.getCartoonX() < q.getCartoonX()) {
                            incr = GridUnitSize;
                        } else {
                            incr = -GridUnitSize;
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
                this.makeSheet(chain, bridgePartner.partner, p, GridUnitSize);
            }
        }

        sortStacking(chain, q, q, 0, Layer, Layer, 0); // XXX args!
    }
    
    private void sortStacking(Chain chain, SSE p, SSE q, int StartXPos, List<SSE> StartLowerList, List<SSE> StartUpperList, int MaxListLen) {
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
            boolean TwoLayers = false;
            double CurrentXPos = chain.leftMostPos(p);
            List<SSE> CurrentList = chain.getListAtXPosition(p, CurrentXPos);
            while (!CurrentList.isEmpty()) {
                for (int i = 0; i < CurrentList.size(); i++) {
                    SSE r = CurrentList.get(i);
                    for (int j = i + 1; j < CurrentList.size(); j++) {
                        SSE s = CurrentList.get(j);
                        if (r.getFirstCommonBP(s) == null) {
                            TwoLayers = true;
                            StartXPos = (int) CurrentXPos;
                            StartUpperList = chain.listBPGroup(r, CurrentList);
                            for (SSE t : CurrentList) {
                                if (!StartUpperList.contains(t)) {
                                    StartLowerList.add(t);
                                }
                            }
                            break;
                        }
                        if (TwoLayers) break;
                    }
                    if (TwoLayers) break;
                }

                CurrentXPos += GridUnitSize;
                CurrentList = chain.getListAtXPosition(p, CurrentXPos);
            }

            // divide layers if necessary
            if (TwoLayers) {

              twoLayers(chain, p, StartXPos, StartLowerList, StartUpperList);  
            }

            // deal with cases where there are split strands
            splitStrands(chain, p, MaxListLen);
        }
    }
    
    private void twoLayers(Chain chain, SSE p, int StartXPos, List<SSE> StartLowerList, List<SSE> StartUpperList) {
        System.out.println("Sheet configured as V_CURVED_SHEET");
        for (SSE t : chain.iterFixed(p)) {
            t.setFixedType(FixedType.FT_V_CURVED_SHEET);
        }

        // divide at the point of splitting identified above
        for (SSE k : StartLowerList) {
            k.setCartoonY((int) (k.getCartoonY() - GridUnitSize));
        }
        

        // divide to right, then left
        this.splitSheet(chain, p, StartUpperList, StartLowerList, StartXPos, 1);
        this.splitSheet(chain, p, StartUpperList, StartLowerList, StartXPos, -1);
    }
    
    private void splitStrands(Chain chain, SSE p, int MaxListLen) {
        for (SSE r : chain.iterFixed(p)) {
            
            List<SSE> CurrentList = chain.getListAtPosition(p, r.getCartoonX(), r.getCartoonY());

            if (CurrentList.size() > 1) { 
                SSE bp = chain.getCommonBP(CurrentList, MaxListLen);
                if (bp != null) {
                    chain.sortListByBridgeRange(bp, CurrentList);
                    this.spreadList(CurrentList, bp.getDirection());
                }
            }
        }
    }
    
    public void spreadList(List<SSE> sseList, char Direction) {
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
        if (Direction == 'U') {
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
    
    public void splitSheet(Chain chain, SSE p, List<SSE> StartUpperList, List<SSE> StartLowerList, int StartXPos, int Direction) {

        int UPPER_LAYER = 3;
        int MIDDLE_LAYER = 2;
        int LOWER_LAYER = 1;
        int UNK_LAYER = 0;

        double CurrentXPos = StartXPos + Direction * GridUnitSize;
        List<SSE> CurrentList = chain.getListAtXPosition(p, CurrentXPos);
        List<SSE> UpperList = StartUpperList;
        List<SSE> LowerList = StartLowerList;

        List<Integer> Layer = new ArrayList<Integer>();
        while (CurrentList.size() != 0) {
            for (SSE r : CurrentList) {
                if (r == null) break; 
                boolean onUpperList = r.HasBPonList(UpperList);
                boolean onLowerList = r.HasBPonList(LowerList);
                if (onUpperList && onLowerList) {
                    Layer.add(MIDDLE_LAYER);
                } else if (onUpperList) {
                    Layer.add(UPPER_LAYER);
                } else if (onLowerList) {
                    Layer.add(LOWER_LAYER);
                } else {
                    Layer.add(UNK_LAYER);
                }
            }

            int i = 0;
            for (int layer : Layer) {
                if (layer == UNK_LAYER) {
                    for (SSE j : UpperList) {
                        if (j == null) break;
                        SSE bp = CurrentList.get(i).getFirstCommonBP(j); 
                        if (bp != null) { 
                            layer = UPPER_LAYER;
                        }
                    }
                    
                    for (SSE j : LowerList) {
                        if (j == null) break;
                        if (CurrentList.get(i).getFirstCommonBP(j) != null) {
                            if (layer == UPPER_LAYER) {
                                layer = MIDDLE_LAYER;
                            } else {
                                layer = LOWER_LAYER;
                            }
                        }
                    }
                }
                i++;
            }
            UpperList = new ArrayList<SSE>();
            LowerList = new ArrayList<SSE>();

            int k = 0;
            for (SSE r : CurrentList) {
                if (Layer.get(k) == UPPER_LAYER) {
                    UpperList.add(r);
                } else if (Layer.get(k) == MIDDLE_LAYER) {
                    r.setCartoonY((int) (r.getCartoonY() - (GridUnitSize / 2)));
                } else if (Layer.get(k) == LOWER_LAYER) {
                    r.setCartoonY((int) (r.getCartoonY() - GridUnitSize));
                    LowerList.add(r);
                }
            }
                
            CurrentXPos += Direction * GridUnitSize;
            CurrentList = chain.getListAtXPosition(p, CurrentXPos);
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
        
        double span = chain.fixedSpan(Start, GridUnitSize);

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
                    sse.setCartoonX((int) ((Y * Math.sin(X) - 0.5) * GridUnitSize));
                    sse.setCartoonY((int) ((Y * Math.cos(X) - Z) * GridUnitSize));
                    sse.setSymbolPlaced(true);
                    Y += YEF;
                }

                CurrXPos += GridUnitSize;
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

            CurrXPos += GridUnitSize;
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
            GridUnitSize = value;
        }
    }

}
