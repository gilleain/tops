package tops.port.layout;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UNKNOWN;
import static tops.port.model.Direction.UP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tops.port.calculate.util.DistanceCalculator;
import tops.port.model.Bridge;
import tops.port.model.BridgePartner;
import tops.port.model.Chain;
import tops.port.model.Direction;
import tops.port.model.FixedType;
import tops.port.model.SSE;
import tops.port.model.tse.BaseTSE;

public class LayoutSheet implements TSELayout {
    
    private double gridUnitSize;

    private class SeqDirResult {
        public int seqDir;
        public Direction direction;
        public SeqDirResult(int SeqDir, Direction Dir) {
            this.seqDir = SeqDir;
            this.direction = Dir;
        }
    }
    
    public LayoutSheet(double gridUnitSize) {
        this.gridUnitSize = gridUnitSize;
    }

    @Override
    public void layout(Chain chain, BaseTSE tse) {
        this.makeSheet(chain, tse.getFirst(), null, gridUnitSize);
        this.sheetCurvature(chain, tse.getFirst());
    }
    
    /** 
     * this function checks ordinary sheets to see if curvature is strong enough for plotting on arc 
     * the test q.FixedType == FT_SHEET is necessary since Make Sheet can produce FT_V_CURVED_SHEET as well
     * as FT_SHEET, and the former type should not be examined by SheetCurvature
     * 
     */
    public void sheetCurvature(Chain chain, SSE p) {

        double minSpan = 5;
        double maxSep = 10.0;

        SSE start = chain.findFixedStart(p);

        if (start == null || !start.hasFixedType(FixedType.SHEET)) return;

        // first determine left and right most strands in the sheet """
        SSE left = chain.getListAtXPosition(start, chain.leftMostPos(start)).get(0);
        SSE right = chain.getListAtXPosition(start, chain.rightMostPos(start)).get(0);
        
        double span = chain.fixedSpan(start, gridUnitSize);

        // MaxSep is 0.65 of the separation if the sheet were flat with 4.5A between each pair of strands """
        maxSep = 4.5 * (span - 1.0) * 0.65;

        double sep = DistanceCalculator.secStrucSeparation(left, right);

//        System.out.println(String.format("SheetCurvature %d %d %f %f", left.getSymbolNumber(), right.getSymbolNumber(), sep, MaxSep));

        if (span >= minSpan && sep<maxSep) {

//            System.out.println("Sheet configured as CURVED_SHEET");

            for (SSE q : chain.iterFixed(start)) q.setFixedType(FixedType.CURVED_SHEET);

            // this direction calculation is to ensure that sheets from AB barrels are plotted with the correct chirality """
            SeqDirResult result = this.findSheetSeqDir(chain, start);
            double addDir;
            if (result.seqDir == 1) {
                if (result.direction == DOWN) addDir = 1.0;
                else addDir = -1.0;
            } else if (result.seqDir == -1) {
                if (result.direction == UP) addDir = 1.0;
                else addDir = -1.0;
            } else {
                addDir = 1.0;
            }
        
            int nSpaces = (int) (span + 1);

            double rads = Math.PI / nSpaces;
            double X = rads;
            double YStart = 0.5 / Math.sin(rads);
            double Z = 0.5 / Math.tan(rads);

            int currXPos = (int) chain.leftMostPos(start);
            List<SSE> sseList = chain.getListAtXPosition(start, currXPos);

            double YEF = 0.75;
            while (sseList.size() > 0) {
                double Y = YStart;
                for (SSE sse : sseList) {
                    sse.setCartoonX((int) ((Y * Math.sin(X) - 0.5) * gridUnitSize));
                    sse.setCartoonY((int) ((Y * Math.cos(X) - Z) * gridUnitSize));
                    sse.setSymbolPlaced(true);
                    Y += YEF;
                }

                currXPos += gridUnitSize;
                sseList = chain.getListAtXPosition(start, currXPos);

                X += addDir * 2.0 * rads;
           }
        }
    }
    

    
    public SeqDirResult findSheetSeqDir(Chain chain, SSE Start) {
        SSE prev = null;
        SSE curr = null;

        double CurrXPos = chain.leftMostPos(Start);
        List<SSE> sseList = chain.getListAtXPosition(Start, CurrXPos);
        if (sseList.size() == 1) prev = sseList.get(0);

        Direction lastdir = UNKNOWN;
        int lastsdir = 0;
        Direction dir = UNKNOWN;
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
                dir = UNKNOWN;
                sdir = 0;
            }

            prev = curr;
            lastsdir = sdir;
            lastdir = dir;
        }

        if (done) {
            return new SeqDirResult(sdir, dir);
        } else {
//            System.out.println("find seq dir not done!");
            return new SeqDirResult(0, UNKNOWN);
        }
    }
    
    private void makeSheet(Chain chain, SSE p, SSE q, double gridUnitSize) {

        List<SSE> CurrentList = new ArrayList<SSE>();
        List<SSE> startUpperList = new ArrayList<SSE>();
        List<SSE> startLowerList = new ArrayList<SSE>();
        List<SSE> Layer = new ArrayList<SSE>();

        p.setFixedType(FixedType.SHEET);

        // If this is the first strand place it based on the last strand
        if (q == null) {
            p.setCartoonX(0);
            p.setCartoonY(0);
            p.setSymbolPlaced(true);
        } else {

            // Have any of q's neighbours already been placed?
            int i = 0;
            for (Bridge bp : chain.getBridges(q)) {
                SSE r = bp.getOther(q);
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
                p.assignRelativeDirection(q);
            }
        }
        

        // For all bridge partners except q repeat
        for (BridgePartner bridgePartner : p.getBridgePartners()) {
            if (bridgePartner.partner != q) { 
                this.makeSheet(chain, bridgePartner.partner, p, gridUnitSize);
            }
        }

        sortStacking(chain, p, null, 0, startLowerList, startUpperList, 0);
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
            splitStrands(chain, p);
        }
    }
    
  
    
    private void divideLayers(Chain chain, SSE p, int startXPos, List<SSE> startLowerList, List<SSE> startUpperList) {
//        System.out.println("Sheet configured as V_CURVED_SHEET");
        for (SSE t : chain.iterFixed(p)) {
            t.setFixedType(FixedType.V_CURVED_SHEET);
        }

        // divide at the point of splitting identified above
        for (SSE k : startLowerList) {
            k.setCartoonY((int) (k.getCartoonY() - gridUnitSize));
        }

        // divide to right, then left
        this.splitSheet(chain, p, startUpperList, startLowerList, startXPos, 1);
        this.splitSheet(chain, p, startUpperList, startLowerList, startXPos, -1);
    }
    
    private void splitStrands(Chain chain, SSE p) {
        for (SSE r : chain.iterFixed(p)) {
            
            List<SSE> currentList = chain.getListAtPosition(p, r.getCartoonX(), r.getCartoonY());

            if (currentList.size() > 1) { 
                SSE bp = getCommonBP(currentList);
                if (bp != null) {
                    sortListByBridgeRange(bp, currentList);
                    this.spreadList(currentList, bp.getDirection());
                }
            }
        }
    }
    
    private SSE getCommonBP(List<SSE> currentList) {
        SSE start = currentList.get(0);
        if (start == null) return null;
      
        for (int i = 0; i < start.getBridgePartners().size(); i++) {
            BridgePartner commonBridgePartner = start.getBridgePartners().get(i);
            boolean isCommonBP = true;
            for (int j = 1; j < currentList.size(); j++) {
                if (!commonBridgePartner.partner.hasBridgePartner(currentList.get(j))) {
                    isCommonBP = false;
                    break;
                }
            }
            if (isCommonBP) {
                return commonBridgePartner.partner;
            }
        }
        return null;
    }
    
    
    public void spreadList(List<SSE> sseList, Direction direction) {
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
        if (direction == UP) {
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
}
