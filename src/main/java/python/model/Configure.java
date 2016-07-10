package python.model;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

/**
 * TODO : rename class!!
 * 
 * @author maclean
 *
 */
public class Configure {

    int neighbourCutoffDistance = 10;   // XXX ?
    double GridUnitSize = 50;
    double MergeStrands = 5;
    double MergeBetweenSheets = 0;

    public void configure(Chain chain) {
        System.out.println("Beginning to configure the master linked list");
        this.calculateStructureAxes(chain);
        this.calculateNeighbours(neighbourCutoffDistance, chain);
        this.calculateSheets(GridUnitSize, chain);
        this.calculateSandwiches(chain);
        this.calculateFixedHands(chain);
        this.CalculateDirection(chain);
        this.calculateHands(chain);
        chain.ClearPlaced();
    }

    public void calculateStructureAxes(Chain chain) {

        // Calculate axes //
        System.out.println("Calculating secondary structure vectors");
        for (SSE sse : chain.getSSEs()) {
            if (!sse.isStrand() && !sse.isHelix()) continue;
            SecondaryStructureAxis(chain, sse);
        }

        /* 
         * At this point we assign relative position of bridge partner strands  
         * Although this is already set from the dssp file it is reset here since the latter is unreliable
         */

        System.out.println("Assigning relative sides to bridge partner strands");
        for (SSE p : chain.getSSEs()) {
            p.assignRelativeSides();
        }
        if (this.MergeStrands > 0) {
            this.mergeStrands((int)this.MergeStrands, chain);
        }
    }

    public void SecondaryStructureAxis(Chain chain, SSE sse) {
        sse.setAxis(
                chain.secondaryStructureAxis(
                        sse.sseData.SeqStartResidue, sse.sseData.SeqFinishResidue));
    }

    /** 
     * Merge structures if necessary 
     * The conditions are these: p and p.From are strands separated by less than MergeStrands residues
     * they are part of the same sheet, either on the same side of a common bridge partner, or so far separated that
     * merging will create a barrel of at least 6 strands, or are not in the same sheet but separated by a very short loop
     * they satisfy geometric criteria (NB ClosestApproach returns a torsion angle).
     **/
    public void mergeStrands(int MergeStrands, Chain chain) {
        System.out.println("Searching for strand merges");
        for (SSE p : chain.getSSEs()) {
            int ConnectLoopLen = p.sseData.SeqStartResidue - p.From.sseData.SeqFinishResidue - 1;
            if (p.isStrand() && p.From != null && p.From.isStrand() && ConnectLoopLen < MergeStrands) {
                int VShortLoop = 1;
                int cbpd = this.ConnectBPDistance(p, p.From);
                boolean sheetMerge = this.MergeBetweenSheets != 0 && ConnectLoopLen <= VShortLoop;
                if ((cbpd == 2 && p.sameBPSide(p.From)) || (cbpd > 5) ||sheetMerge ) {
                    TorsionResult result = p.ClosestApproach(p.From);
                    if (Math.abs(result.torsion) < 90.0) { 
                        System.out.println(String.format("Merging Strands %d %d\n", p.From.getSymbolNumber(), p.getSymbolNumber()));
                        this.joinToLast(chain, p);
                        p.sortBridgePartners(); // XXX not sure why we have to sort
                    }
                }
            }
        }
    }

    public int ConnectBPDistance(SSE p, SSE q) {
        List<SSE> connectList = new ArrayList<SSE>();
        List<Integer> distances = new ArrayList<Integer>();
        this.ListBPConnected(p, 0, connectList, distances);
        if (connectList.contains(q)) {
            return distances.get(connectList.indexOf(q));
        } else {
            return -1;
        }
    }

    public void ListBPConnected(SSE p, int currentDistance, List<SSE> connectList, List<Integer> distances) {
        if (connectList.contains(p)) {
            int listPos = connectList.indexOf(p);
            if (currentDistance < distances.get(listPos)) {
                distances.set(listPos, currentDistance);
            }
            return;
        } else {
            connectList.add(p);
            distances.add(currentDistance);
            currentDistance += 1;
            for (SSE q : p.getPartners()) {
                if (q == null) break;
                this.ListBPConnected(q, currentDistance, connectList, distances);
            }
        }
    }
    
    public void joinToLast(Chain chain, SSE sseToJoin) {
        sseToJoin.joinToLast();
        // This is yet another hack for the time being to deal with neighbours */
        for (SSE sse : chain.getSSEs()) {
//            for (int j = 0; j < r.Neighbour.size(); j++) {
//                if (r.Neighbour[j] == q) r.Neighbour[j] = p;
//            }
        }
    }
    
    /**
    * Function to assign spatial neighbours
    **/
    public void calculateNeighbours(int CutoffDistance, Chain chain) {
        CutoffDistance = 20;
        System.out.println("Calculating secondary structure neighbour lists");

        for (SSE p : chain.getSSEs()) {
            if (!p.isStrand() && !p.isHelix()) continue;
            for (SSE q : chain.rangeFrom(p.To)) {
                if (!q.isStrand() && !q.isHelix()) continue;
                if (p.hasBridgePartner(q)) continue;
                double shdis = chain.SimpleSSESeparation(p, q);
                if (shdis > CutoffDistance) continue;

                // Update first secondary structure //
                p.addNeighbour(q, (int)shdis);

                // Update second secondary structure //
                q.addNeighbour(p, (int)shdis);
            }
        }
    }
    
    public void calculateSheets(double GridUnitSize, Chain chain) {
        System.out.println("Calculating sheets and barrels");

        List<SSE> Barrel;
        for (SSE p : chain.getSSEs()) { 
            if (!p.isSymbolPlaced() && p.isStrand()) {
                Barrel = this.DetectBarrel(p);
                if (Barrel.size() > 0) {
                    System.out.println("Barrel detected");
                    this.makeBarrel(chain, Barrel, GridUnitSize);
                } else {
                    System.out.println("Sheet detected");
                    SSE q = p.FindEdgeStrand(null);
                    if (!q.isSymbolPlaced()) {
                        this.makeSheet(chain, q, null, GridUnitSize);
                        if (q.hasFixedType(FixedType.FT_SHEET)) {
                            this.SheetCurvature(chain, q);
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
    public List<SSE> DetectBarrel(SSE p) {
        List<SSE> barrel = new ArrayList<SSE>();
        List<SSE> visited = new ArrayList<SSE>();
        this.FindBarrel(p, barrel, visited, null);
        return barrel;
    }
    

    /**
     * This function detects and enumerates the first cycle found in a set of
     * strands connected by BridgePartner relationships
     */
    public boolean FindBarrel(SSE p, List<SSE> barrel, List<SSE> visited, SSE AddFrom) {

        if (visited.contains(p)) {
            //If we've been to this node before then we've detected a barrel - return true//
            if (!barrel.contains(p)) barrel.add(p);
            return true;
        } else {
            //else continue looking//
            visited.add(p);

            for (SSE bridgePartner : p.getPartners()) {
                if (bridgePartner == AddFrom) continue;

                if (this.FindBarrel(bridgePartner, barrel, visited, p)) {
                    if (barrel.get(0) != p) barrel.add(p);
                    return true;
                }
            }
        }
        return false;
    }
    
    public void makeBarrel(Chain chain, List<SSE> Barrel, double GridUnitSize) {
        System.out.println("making barrel...");

        int NStrands = Barrel.size();

        double Rads = Math.PI / (double) NStrands;
        double X = Rads;
        double Y = 0.5 / Math.sin(Rads);
        double Z = 0.5 / Math.tan(Rads);

        // this bit ensures that TIM barrels get the correct chirality """
        int start = 0;
        int increment = 0;
        if (Barrel.get(1).getSymbolNumber() < Barrel.get(Barrel.size() - 1).getSymbolNumber()) {
            start = 0;
            increment = 1;
        } else {
            start = NStrands - 1;
            increment = -1;
        }

        SSE p = Barrel.get(start);
        SSE LastInBarrel = p;
        p.setDirection('D');
        
        System.out.println(String.format("make barrel start, nstrands, increment= %s %s %s", start, NStrands, increment));
        int i = start;
        for (int count = 0; count < NStrands; count++) { 
            System.out.println("make barrel count= " + count);
            X = Rads + 2.0 * count * Rads;

            SSE q = Barrel.get(i);
            q.setFixedType(FixedType.FT_BARREL);

            if (count != 0) {
                chain.moveFixed(p, q);
                q.AssignRelDirection(LastInBarrel);
            }
            LastInBarrel = q;
            q.setCartoonX((int) ((Y * Math.sin(X) - 0.5) * GridUnitSize));
            q.setCartoonY((int) ((Y * Math.cos(X) - Z) * GridUnitSize));
            q.setSymbolPlaced(true);

            double Y1 = Y+1;
            for (BridgePartner bridgePartner : q.getBridgePartners()) {
                SSE r = bridgePartner.partner;
                if (!Barrel.contains(r)) {
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
            double CurrentXPos = chain.LeftMostPos(p);
            List<SSE> CurrentList = chain.GetListAtXPosition(p, CurrentXPos);
            while (!CurrentList.isEmpty()) {
                for (int i = 0; i < CurrentList.size(); i++) {
                    SSE r = CurrentList.get(i);
                    for (int j = i + 1; j < CurrentList.size(); j++) {
                        SSE s = CurrentList.get(j);
                        if (r.getFirstCommonBP(s) == null) {
                            TwoLayers = true;
                            StartXPos = (int) CurrentXPos;
                            StartUpperList = chain.ListBPGroup(r, CurrentList);
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
                CurrentList = chain.GetListAtXPosition(p, CurrentXPos);
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
        this.SplitSheet(chain, p, StartUpperList, StartLowerList, StartXPos, 1);
        this.SplitSheet(chain, p, StartUpperList, StartLowerList, StartXPos, -1);
    }
    
    private void splitStrands(Chain chain, SSE p, int MaxListLen) {
        for (SSE r : chain.iterFixed(p)) {
            
            List<SSE> CurrentList = chain.GetListAtPosition(p, r.getCartoonX(), r.getCartoonY());

            if (CurrentList.size() > 1) { 
                SSE bp = chain.getCommonBP(CurrentList, MaxListLen);
                if (bp != null) {
                    chain.SortListByBridgeRange(bp, CurrentList);
                    this.SpreadList(CurrentList, bp.getDirection());
                }
            }
        }
    }
    
    public void SpreadList(List<SSE> sseList, char Direction) {
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
    
    public void SplitSheet(Chain chain, SSE p, List<SSE> StartUpperList, List<SSE> StartLowerList, int StartXPos, int Direction) {

        int UPPER_LAYER = 3;
        int MIDDLE_LAYER = 2;
        int LOWER_LAYER = 1;
        int UNK_LAYER = 0;

        double CurrentXPos = StartXPos + Direction * GridUnitSize;
        List<SSE> CurrentList = chain.GetListAtXPosition(p, CurrentXPos);
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
            CurrentList = chain.GetListAtXPosition(p, CurrentXPos);
        }
    }
    
    /** 
     * this function checks ordinary sheets to see if curvature is strong enough for plotting on arc 
     * the test q.FixedType == FT_SHEET is necessary since Make Sheet can produce FT_V_CURVED_SHEET as well
     * as FT_SHEET, and the former type should not be examined by SheetCurvature
     * 
     */
    public void SheetCurvature(Chain chain, SSE p) {

        double MinSpan = 5;
        double MaxSep = 10.0;

        SSE Start = chain.FindFixedStart(p);

        if (Start == null || !Start.hasFixedType(FixedType.FT_SHEET)) return;

        // first determine left and right most strands in the sheet """
        SSE left = chain.GetListAtXPosition(Start, chain.LeftMostPos(Start)).get(0);
        SSE right = chain.GetListAtXPosition(Start, chain.RightMostPos(Start)).get(0);
        
        double span = chain.FixedSpan(Start, GridUnitSize);

        // MaxSep is 0.65 of the separation if the sheet were flat with 4.5A between each pair of strands """
        MaxSep = 4.5 * (span - 1.0) * 0.65;

        double sep = chain.SecStrucSeparation(left, right);

        System.out.println(String.format("SheetCurvature %d %d %f %f", left.getSymbolNumber(), right.getSymbolNumber(), sep, MaxSep));

        if (span >= MinSpan && sep<MaxSep) {

            System.out.println("Sheet configured as CURVED_SHEET");

            for (SSE q : chain.iterFixed(Start)) q.setFixedType(FixedType.FT_CURVED_SHEET);

            // this direction calculation is to ensure that sheets from AB barrels are plotted with the correct chirality """
            SeqDirResult result = this.FindSheetSeqDir(chain, Start);
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

            int CurrXPos = (int) chain.LeftMostPos(Start);
            List<SSE> sseList = chain.GetListAtXPosition(Start, CurrXPos);

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
                sseList = chain.GetListAtXPosition(Start, CurrXPos);

                X += AddDir * 2.0 * Rads;
           }
        }
    }
    
    private class SeqDirResult {
        public int SeqDir;
        public char Dir;
        public SeqDirResult(int SeqDir, char Dir) {
            this.SeqDir = SeqDir;
            this.Dir = Dir;
        }
    }


    public SeqDirResult FindSheetSeqDir(Chain chain, SSE Start) {
        SSE prev = null;
        SSE curr = null;

        double CurrXPos = chain.LeftMostPos(Start);
        List<SSE> sseList = chain.GetListAtXPosition(Start, CurrXPos);
        if (sseList.size() == 1) prev = sseList.get(0);

        char lastdir = 'X';
        int lastsdir = 0;
        char dir = 'X';
        int sdir = 0;
        boolean done = false;

        while (sseList.size() > 0) {

            CurrXPos += GridUnitSize;
            sseList = chain.GetListAtXPosition(Start, CurrXPos);

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
    
    /*
    Function to detect beta sandwich structures
    re-written by DW 26/02/97
     */
    public void calculateSandwiches(Chain chain) {
        System.out.println("Searching for beta sandwiches");

        // detect and form sandwiches //
        List<SSE[]> sandwiches = new ArrayList<SSE[]>();
        for (SSE r : chain.getSSEs()) {
            if (r.isStrand() && r.hasFixedType(FixedType.FT_SHEET)) {
                String rDomain = this.FindDomain(r);
                for (SSE s : chain.iterNext(r)) {
                    String sDomain = this.FindDomain(s);
                    if (s.isStrand() && s.hasFixedType(FixedType.FT_SHEET) && sDomain == rDomain) { 
                        if (this.IsSandwich(chain, r, s)) {
                            System.out.println(String.format("Sandwich detected between %s and %s" , r, s));
                            sandwiches.add(new SSE[] {r, s});
                            this.makeSandwich(chain, r, s);
                        }
                    }
                }
            }
        }


        // turn the sandwiches into fixed structures //
        for (SSE[] sandwich : sandwiches) {
            chain.moveFixed(sandwich[0], sandwich[1]);
        }
    }

    // FIXME! : need to implement domains asap
    public String FindDomain(SSE sse) {
        return "YES!";
    }
    
    /**
    Function to determine whether two sheets constitute a sandwich.
    Both must have more than two strands, there must be at least a certain number of 
    pairwise contacts - this number is the the number of strands in the smallest sheet - ,
    and these contacts must span at least two strands of the larger sheet 
    **/
    public boolean IsSandwich(Chain chain, SSE r, SSE s) {
        int ncontacts = 0;
        double MaxContactSeparation = 13.0;
        double MinOverLap = 2 * this.GridUnitSize;

        SSE p = chain.FindFixedStart(r);
        SSE q = chain.FindFixedStart(s);

        double s1 = chain.FixedSpan(p, this.GridUnitSize);
        double s2 = chain.FixedSpan(q, this.GridUnitSize);

        double MinContacts = 0;
        if (s1 < s2) {
            double tmp = s2;
            s2 = s1;
            s1 = tmp;
            SSE tmpC = q;
            q = p;
            p = tmpC;
            MinContacts = s1;
        } else {
            MinContacts = s2;
        }

        if ((p == q) || (s1 < 3) || (s2 < 3) 
                || (!p.hasFixedType(FixedType.FT_SHEET) 
                || (!q.hasFixedType(FixedType.FT_SHEET)))) {
            return false;
        }

        int LeftOverlap = Integer.MAX_VALUE;
        int RightOverlap = Integer.MAX_VALUE;
        for (SSE p1 : chain.iterFixed(p)) {
            for (SSE q1 : chain.iterFixedInclusive(q)) {
                if (p1.separation(q1) <= MaxContactSeparation || q1.separation(p1) <= MaxContactSeparation) {
                    ncontacts += 1;
                    int p1x = p1.getCartoonX();
                    if (p1x < LeftOverlap) LeftOverlap = p1x;
                    if (p1x > RightOverlap) RightOverlap = p1x;
                }
            }
        }

        int OverLap = RightOverlap - LeftOverlap;
        if (ncontacts >= MinContacts && OverLap >= MinOverLap) {
            return true;
        } else {
            return false;
        }
    }

    public void makeSandwich(Chain chain, SSE r, SSE s) {
        // determine relative sizes of sheets """
        SSE rSheetStart = chain.FindFixedStart(r);
        SSE sSheetStart = chain.FindFixedStart(s);

        double spanR = chain.FixedSpan(rSheetStart, this.GridUnitSize);
        double spanS = chain.FixedSpan(sSheetStart, this.GridUnitSize);

        SSE longSheetStart, shortSheetStart;
        if (spanR > spanS) {
            longSheetStart = rSheetStart;
            shortSheetStart = sSheetStart;
        } else {   
            longSheetStart = sSheetStart;
            shortSheetStart = rSheetStart;
        }

        // leave the longest alone and move the shortest """

        // first determine left and right most strands in the short sheet """
        SSE shortSheetLeftMost = chain.LeftMost(shortSheetStart);
        SSE shortSheetRightMost = chain.RightMost(shortSheetStart);

        // determine the left most and right most strands in the long sheet """
        SSE longSheetLeftMost = chain.LeftMost(longSheetStart);
        SSE longSheetRightMost = chain.RightMost(longSheetStart);

        // is the short sheet the right way round?, if not turn it """
        double leftSeparation = chain.SecStrucSeparation(shortSheetLeftMost, longSheetLeftMost);
        double rightSeparation = chain.SecStrucSeparation(shortSheetRightMost, longSheetLeftMost);
        if (leftSeparation > rightSeparation) {
            chain.FlipSymbols(shortSheetStart);
            SSE tmp = shortSheetRightMost;
            shortSheetRightMost = shortSheetLeftMost;
            shortSheetLeftMost = tmp;
        }

        // now check directions of symbols """
        // this is done geometrically using the longest strand in each sheet """
        SSE longestStrandInShortSheet = chain.LongestInFixed(rSheetStart);
        SSE longestStrandInLongSheet = chain.LongestInFixed(sSheetStart);
        TorsionResult torsionResult = longestStrandInLongSheet.ClosestApproach(longestStrandInShortSheet);
        if (Math.abs(torsionResult.torsion) > 90.0) {
            if (longestStrandInShortSheet.getDirection() == longestStrandInLongSheet.getDirection()) {
                for (SSE sse : chain.iterFixed(shortSheetStart)) {
                    sse.flip();
                }
            }
        } else {
            if (longestStrandInShortSheet.getDirection() != longestStrandInLongSheet.getDirection()) {
                for (SSE sse : chain.iterFixed(shortSheetStart)) {
                    sse.flip();
                }
            }
        }
        
        // now reposition the short sheet, x direction try to get best position of left and right most strands in the
        //   short sheet subject with the constraint that the short sheet must lie within the x range of the long sheet  """
        SSE closestLongShortLeft = chain.ClosestInFixed(longSheetStart, shortSheetLeftMost);
        SSE closestLongShortRight = chain.ClosestInFixed(longSheetStart, shortSheetRightMost);
        double xmove = (closestLongShortLeft.getCartoonX() - shortSheetLeftMost.getCartoonX() + closestLongShortRight.getCartoonX() - shortSheetRightMost.getCartoonX()) / 2;
        double loff = longSheetLeftMost.getCartoonX() - shortSheetLeftMost.getCartoonX() - xmove;
        double roff = shortSheetRightMost.getCartoonX() + xmove - longSheetRightMost.getCartoonX();
        if (loff > 0) xmove += loff;
        if (roff > 0) xmove -= roff;

        double ymove = chain.LowestPos(longSheetStart) - this.GridUnitSize - shortSheetLeftMost.getCartoonY();

        for (SSE sse : chain.iterFixed(shortSheetStart)) {
            sse.setCartoonX((int)(sse.getCartoonX() + xmove));
            sse.setCartoonY((int) (sse.getCartoonY() + ymove));
        }
        

        // set type of fixed structure """
        for (SSE sse : chain.iterFixed(shortSheetStart)) {
            sse.setFixedType(FixedType.FT_SANDWICH);
        }
        for (SSE sse : chain.iterFixed(longSheetStart)) {
            sse.setFixedType(FixedType.FT_SANDWICH);
        }
    }
    
    public void calculateFixedHands(Chain chain) {
        for (SSE sse : chain.getSSEs()) {
            if (sse.hasFixed()) this.SetFixedHand(chain, sse);
        }
    }
    
    List<FixedType> allowedTypes = new ArrayList<FixedType>() {{
        add(FixedType.FT_BARREL); 
        add(FixedType.FT_SANDWICH); 
        add(FixedType.FT_CURVED_SHEET); 
        add(FixedType.FT_V_CURVED_SHEET); 
    }};

    /**
     * This function checks that a given fixed structure is drawn with correct
     * handedness. It only works where the handedness is easy to calculate (ie.
     * we can use TS's routine) that being where we can find an appropriate
     * Beta-x-Beta unit. Note that TIM barrels do not lie in this category but
     * are already drawn with correct chirality.
     **/
    public void SetFixedHand(Chain chain, SSE p) {
        if (allowedTypes.contains(p.getFixedType())) {
            System.out.println(String.format("Checking fixed structure chirality for fixed start %d", p.getSymbolNumber()));
            SSE q = find(chain, p);
            if (q != null) {
                System.out.println("Found suitable motif for fixed chirality check");
                SSE r = null;   // XXX FIXME XXX
                Hand chir = Chiral2d(chain, q, r);
                if (chir != Hand._unk_hand) {
                    System.out.println(String.format("Changing chirality of fixed structure starting at %d", p.getSymbolNumber()));
                    chain.ReflectFixedXY(p);
                }
            } else {
                System.out.println("No suitable motif found for fixed chirality check");
            }

        }
    }

    private SSE find(Chain chain, SSE p) {
        // TODO : FIXME - see SetFixedHand!! XXX
        for (SSE q : chain.getSSEs()) {
            if (chain.FindFixedStart(q) == p) {
                boolean found = false;
                int n = 0;
                for (SSE r : chain.rangeFrom(q.To)) {
                    if (chain.FindFixedStart(r) != p) break;
                    n += 1;
                    if (r.getDirection() == q.getDirection()) {
                        Hand chir = Chiral2d(chain, q, r);
                        if (n > 1 && chir != Hand._unk_hand) {
                            found = true;
                        } else {
                            found = false;
                        }
                        break;
                    }
                    if (found) return q;
                }
            }   
        }
        return null;
    }
  
    
    /**
     * This could be a Cartoon method if it was recast as finding the sign of
     * the determinant of the matrix of course, this would mean not re-using the
     * Torsion method so cunningly, but hey.
     */
    public Hand Chiral2d(Chain chain, SSE p, SSE q) {
        Hand hand = Hand._unk_hand;
        Hand lasthand = Hand._unk_hand;

        Vector3d a, b, c, d;
        if (p.getDirection() == 'U') a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 1.0);
        else a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        b = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        c = new Vector3d(q.getCartoonX(), q.getCartoonY(), 0.0);

        int i = 0;
        for (SSE r : chain.range(p.To, q)) {
            d = new Vector3d(r.getCartoonX(), r.getCartoonY(), 0.0);
            lasthand = hand;
            double theta = this.AngleBetweenLines(b, c, d);
            if (theta < 0.5 || theta > 179.5) {
                hand = Hand._unk_hand;
            } else {
                double torsion = Axis.Torsion(a, b, c, d);
                if (torsion < 0.0) {
                    hand = Hand._Left;
                } else {
                    hand = Hand._Right;
                }
            }
            if (i > 0 && hand != lasthand) { 
                return Hand._unk_hand;
            }
        }
        return hand;
    }

    // might not be as accurate as the original
    public double AngleBetweenLines(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d ba = diff(b, a);
        Vector3d bc = diff(b, c);
        if (ba.length() == 0.0 || bc.length() == 0.0) return 0.0;
        return Math.toDegrees(ba.angle(bc));
    }

    private Vector3d diff(Vector3d a, Vector3d b) {
        Vector3d ab = a;
        ab.sub(b);
        return ab;
    }
    
    /**
    Directions of cartoon symbols are binary, whereas SSEs have full blown vectors.
    **/
    public void CalculateDirection(Chain chain) {
        System.out.println("Assigning directions to secondary structures\n");
        SSE Root = chain.getSSEs().get(0);
        SSE q = Root;
        for (SSE p : chain.iterNext(Root)) {
            if (p != Root) {
                if (q.isParallel(p)) {
                    if (p.getDirection() != q.getDirection()) chain.FlipSymbols(p);
                } else {
                    if (p.getDirection() == q.getDirection()) chain.FlipSymbols(p);
                }
                q = p;
            }
        }

        /*
          This loop makes some local direction changes to the directions of ss elements which
          have no fixed list attached. Their direction is determined from the previous element in
          the sequence rather than the previous element in the Next list which is what is used
          above.
          This does a better job for beta-alpha-beta units.
          DW 5/9/96
        */
        for (SSE p : chain.iterNext(Root)) {
            if (p != Root && p != Root.To && p.hasFixed()) {
                q = p.From;
                if (q.isParallel(p)) {
                    if (q.getDirection() == 'U') {
                        p.setDirection('U');
                    } else {
                        p.setDirection('D');
                    }
                } else {
                    if (q.getDirection() == 'U') { 
                        p.setDirection('D');
                    } else { 
                        p.setDirection('U');
                    }
                }
            }
        }
     }
    
    public void calculateHands(Chain chain) {
        System.out.println("Calculating chiralities");
        for (SSE p : chain.getSSEs()) {
            p.Chirality = this.Hand3D(chain, p);
        }
    }
    
    /*
     * Hand calculator Updated to call T. Slidel's chirality calculation by D.
     * Westhead 20/5/97 If handedness is uncertain, or an error occurrs, right
     * handed is assumed an sse method
     */
    public Hand Hand3D(Chain chain, SSE p) {

        SSE q = this.topsChiralPartner(chain, p);
        if (q != null) {
            Hand chir = p.Chiral3d(q);
            if (chir == Hand._unk_hand) {
                if (p.isStrand()) chir = Hand._Right;
                else if (p.isHelix()) chir = Hand._no_hand;
                else chir = Hand._no_hand;
            }
            return chir;
        } else {
            return Hand._no_hand;
        }
    }
    
    public SSE topsChiralPartner(Chain chain, SSE p) {
        /*
        this piece of code finds sequences of ss elements to which a chirality should be attached
        for TOPS this is two parallel strands in the same fixed structure with a connection of at least one and no 
        more than five ss elements, none of which should be in the same sheet,
        OR two parallel helices each of more than 12 residues connected by at least one and no more than 2 other helices.
        */
        int startIndex = chain.getSSEs().indexOf(p) + 1;

        if (p.isStrand()) {
            int endIndex = startIndex + 5;
            for (int i = startIndex; i < endIndex; i++) {
                SSE q;
                if (i >= chain.getSSEs().size()) return null;
                else q = chain.getSSEs().get(i);
                if ((q.isStrand()) && (chain.FindFixedStart(q) == chain.FindFixedStart(p))) {
                    if (q.getDirection() == p.getDirection()) return q;
                    else return null;
                }
            }
        } else if ((p.isHelix()) && (p.SecStrucLength() > 12)) {
            int endIndex = startIndex + 2;
            for (int i = startIndex; i < endIndex; i++) {
                SSE q;
                if (i >= chain.getSSEs().size()) return null;
                else q = chain.getSSEs().get(i);
                if (!q.isHelix()) return null;
                if ((q.getDirection() == p.getDirection()) && (q.SecStrucLength() > 12)) return q;
            }
        }
        return null;    // XXX added to satisfy compiler
    }

}
