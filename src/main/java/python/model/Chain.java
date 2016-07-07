package python.model;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Chain {

    public enum SSEType {
        HELIX, EXTENDED, COIL, RIGHT_ALPHA_HELIX, HELIX_310, PI_HELIX, TURN, ISO_BRIDGE, LEFT_ALPHA_HELIX;
    }
    
    private char name;
    private List<SSE> sses;
    private List<String> sequence;
    private List<Integer> pdbIndices;
    private List<SSEType> secondaryStruc;
    private List<int[]> bridgePartners;
    private List<BridgeType[]> bridgeTypes;
    private List<Point3d> CACoords;
    
    private Map<Integer, List<Integer>> donatedHBonds;
    private Map<Integer, List<Double>> donatedHBondEnergy;
    
    private Map<Integer, List<Integer>> acceptedHBonds;
    private Map<Integer, List<Double>> acceptedHBondEnergy;

    private static char assignChain(char chain) {
        if (chain == ' ' || chain == '-') {
            return '0';
        } else {
            return chain;
        }
    }

    public Chain(char nameChar) {
        this.name = assignChain(nameChar);
        this.sses = new ArrayList<SSE>();
        this.sequence = new ArrayList<String>();
        this.pdbIndices = new ArrayList<Integer>();
        this.secondaryStruc = new ArrayList<SSEType>();
        this.bridgePartners = new ArrayList<int[]>();
        this.bridgeTypes = new ArrayList<BridgeType[]>();
        this.CACoords = new ArrayList<Point3d>();
        
        this.donatedHBonds = new HashMap<Integer, List<Integer>>();
        this.acceptedHBonds = new HashMap<Integer, List<Integer>>();
        this.acceptedHBondEnergy = new HashMap<Integer, List<Double>>();
        this.donatedHBondEnergy = new HashMap<Integer, List<Double>>();
    }
    
    public void ForceConsistent(Protein protein) {
        for (int i = 0; i < SequenceLength(); i++) {

            if ( IsSSelement(i) ) {
                int start = i;
                SSEType thissstype = getSSEType(i);
                int Dom = protein.ResidueDomain(i);
                int DBreak = -1;
                int LastDom = -1;
                while ( IsSSelement(i) && getSSEType(i) == thissstype ) {
                    i++;
                    LastDom = Dom;
                    Dom = protein.ResidueDomain(i);
                    if ( Dom != LastDom ) DBreak = i;
                }

                int finish = --i;

                if ( DBreak > -1 ) {

                    if ( (DBreak-start) > (finish-DBreak) ) {
                        for (int j=DBreak ; j<=finish ; j++) setSSEType(i, SSEType.COIL);
                    } else {
                        for (int j=start ; j<DBreak ; j++) setSSEType(j, SSEType.COIL);
                    }
                }
            }
        }
    }
    
    /**
     * XXX intended to be the same as Protein.PdbIndex[Protein.ChainStarts[i]]
     */
    public int getStartIndex() {
        return pdbIndices.get(0);   // XXX ?
    }
    
    public int getFinishIndex() {
        return pdbIndices.get(pdbIndices.size() - 1);
    }

    public void addCACoord(Point3d point) {
        this.CACoords.add(point);
    }
    
    public Axis secondaryStructureAxis(int seqStartResidue, int seqFinishResidue) {
        List<Point3d> coords = new ArrayList<Point3d>();
        for (int index = seqStartResidue - 1; index < seqFinishResidue; index++) {
            coords.add(CACoords.get(index));
        }
        return new Axis(coords);
    }
    
    public char getName() {
        return this.name;
    }
    
    public final class Circle {
        double centerX;
        double centerY;
        double radius;
        public Circle(double centerX, double centerY, double radius) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
    }
    
    public int getPDBIndex(int residue) {
        return this.pdbIndices.get(residue);
    }
    
    public BridgeType getLeftBridgeType(int index) {
        return this.bridgeTypes.get(index)[0];
    }
    
    public BridgeType getRightBridgeType(int index) {
        return this.bridgeTypes.get(index)[1];
    }
    
    public void addBridgeTypes(BridgeType[] bridgeTypes) {
        this.bridgeTypes.add(bridgeTypes);
    }
    
    public int getLeftBridgePartner(int index) {
        return this.bridgePartners.get(index)[0];
    }
    
    public int getRightBridgePartner(int index) {
        return this.bridgePartners.get(index)[1];
    }
    
    public void removeLeftBridge(int index) {
        this.bridgePartners.get(index)[0] = -1;
        this.bridgeTypes.get(index)[0] = BridgeType.UNK_BRIDGE_TYPE;
    }
    
    public void removeRightBridge(int index) {
        this.bridgePartners.get(index)[1] = -1;
        this.bridgeTypes.get(index)[1] = BridgeType.UNK_BRIDGE_TYPE;
    }
    
    public void addBridgePartners(int[] partners) {
        this.bridgePartners.add(partners);
    }
    
    public void addSecondaryStructure(SSEType secondaryStructure) {
        this.secondaryStruc.add(secondaryStructure);
    }
    
    public void addSequence(String residueName) {
        this.sequence.add(residueName);
    }
    
    public void addPDBIndex(int pdbIndex) {
        this.pdbIndices.add(pdbIndex);
    }
    
    public void addSSE(SSE sse) {
        this.sses.add(sse);
    }
    
    public List<SSE> getSSEs() {
        return this.sses;
    }
    
    public int numberOfSSEs() {
        return this.sses.size();
    }


    int neighbourCutoffDistance = 10;   // XXX ? 
    /* -------------------- //
    // Configure Structures //
    // -------------------- */

    public void Configure() {

        System.out.println("Beginning to configure the master linked list");
        this.calculateStructureAxes();
        this.calculateNeighbours(neighbourCutoffDistance);
        this.calculateSheets(GridUnitSize);
        this.calculateSandwiches();
        this.calculateFixedHands();
        this.CalculateDirection();
        this.calculateHands();
        this.ClearPlaced();
    }

    /* ------------------------------------------- //
    // Function to calculate axes, neighbours etc. //
    // ------------------------------------------- */
    double GridUnitSize = 50;
    double MergeStrands = 5;
    double MergeBetweenSheets = 0;
    public void calculateStructureAxes() {

        // Calculate axes //
        System.out.println("Calculating secondary structure vectors");
        for (SSE sse : this.sses) {
            if (!sse.isStrand() && !sse.isHelix()) continue;
            sse.SecondaryStructureAxis(this);
        }
        
        /* 
         * At this point we assign relative position of bridge partner strands  
         * Although this is already set from the dssp file it is reset here since the latter is unreliable
         */

        System.out.println("Assigning relative sides to bridge partner strands");
        for (SSE p : this.sses) {
           p.assignRelativeSides();
        }
        if (this.MergeStrands > 0) {
            this.mergeStrands((int)this.MergeStrands);
        }
    }

    /**
        Merge structures if necessary 
        The conditions are these: p and p.From are strands separated by less than MergeStrands residues
        they are part of the same sheet, either on the same side of a common bridge partner, or so far separated that
        merging will create a barrel of at least 6 strands, or are not in the same sheet but separated by a very short loop
        they satisfy geometric criteria (NB ClosestApproach returns a torsion angle).

     **/
    public void mergeStrands(int MergeStrands) {
        System.out.println("Searching for strand merges");
        for (SSE p : this.sses.subList(1, this.sses.size())) {
            int ConnectLoopLen = p.sseData.SeqStartResidue - p.From.sseData.SeqFinishResidue - 1;;
            if (p.isStrand() && p.From != null && p.From.isStrand() && ConnectLoopLen < MergeStrands) {
                int VShortLoop = 1;
                int cbpd = this.ConnectBPDistance(p, p.From);
                boolean sheetMerge = this.MergeBetweenSheets != 0 && ConnectLoopLen <= VShortLoop;
                if ((cbpd == 2 && p.sameBPSide(p.From)) || (cbpd > 5) ||sheetMerge ) {
                    TorsionResult result = p.ClosestApproach(p.From);
                    if (Math.abs(result.torsion) < 90.0) { 
                        System.out.println(String.format("Merging Strands %d %d\n", p.From.getSymbolNumber(), p.getSymbolNumber()));
                        this.joinToLast(p);
                        p.sortBridgePartners(); // XXX not sure why we have to sort
                    }
                }
            }
        }
    }

    /*
      Function to detect beta sandwich structures
      re-written by DW 26/02/97
    */
    public void calculateSandwiches() {
        System.out.println("Searching for beta sandwiches");

        // detect and form sandwiches //
        List<SSE[]> sandwiches = new ArrayList<SSE[]>();
        for (SSE r : this.iterNext(this.sses.get(0))) {
            if (r.isStrand() && r.hasFixedType(FixedType.FT_SHEET)) {
                String rDomain = this.FindDomain(r);
                for (SSE s : this.iterNext(r)) {
                    String sDomain = this.FindDomain(s);
                    if (s.isStrand() && s.hasFixedType(FixedType.FT_SHEET) && sDomain == rDomain) { 
                        if (this.IsSandwich(r, s)) {
                            System.out.println(String.format("Sandwich detected between %s and %s" , r, s));
                            sandwiches.add(new SSE[] {r, s});
                            this.makeSandwich(r, s);
                        }
                    }
                }
            }
        }
                            

        // turn the sandwiches into fixed structures //
        for (SSE[] sandwich : sandwiches) {
            this.moveFixed(sandwich[0], sandwich[1]);
        }
    }

    // FIXME! : need to implement domains asap
    public String FindDomain(SSE sse) {
        return "YES!";
    }

    public void calculateFixedHands() {
        for (SSE sse : this.iterNext(this.sses.get(0))) {
            if (sse.hasFixed()) this.SetFixedHand(sse);
        }
    }

    /*
       Function to assign spatial neighbours
    */
    public void calculateNeighbours(int CutoffDistance) {
        CutoffDistance = 20;
        System.out.println("Calculating secondary structure neighbour lists");

        for (SSE p : this.sses) {
            if (!p.isStrand() && !p.isHelix()) continue;
            for (SSE q : this.rangeFrom(p.To)) {
                if (!q.isStrand() && !q.isHelix()) continue;
                if (p.hasBridgePartner(q)) continue;
                double shdis = this.SimpleSSESeparation(p, q);
                if (shdis > CutoffDistance) continue;
        
                // Update first secondary structure //
                p.addNeighbour(q, (int)shdis);

                // Update second secondary structure //
                q.addNeighbour(p, (int)shdis);
            }
        }
    }
    
    
    public void calculateSheets(double GridUnitSize) {
        System.out.println("Calculating sheets and barrels");

        List<SSE> Barrel;
        for (SSE p : this.sses) { 
            if (!p.isSymbolPlaced() && p.isStrand()) {
                Barrel = this.DetectBarrel(p);
                if (Barrel.size() > 0) {
                    System.out.println("Barrel detected");
                    this.makeBarrel(Barrel, GridUnitSize);
                } else {
                    System.out.println("Sheet detected");
                    SSE q = p.FindEdgeStrand(null);
                    if (!q.isSymbolPlaced()) {
                        this.makeSheet(q, null, GridUnitSize);
                        if (q.hasFixedType(FixedType.FT_SHEET)) {
                            this.SheetCurvature(q);
                        }
                    }
                }
            }
        }
    }


    public void calculateHands() {
        System.out.println("Calculating chiralities");
        for (SSE p : this.sses) {
            p.Chirality = this.Hand3D(p);
        }
    }



    public void resolveTo() {
        int i = 0;
        for (SSE sse : this.sses) {
            if (i + 1 < this.sses.size()) {
                sse.To = this.sses.get(i + 1);
            }
            i++;
        }
    }

    public int NumberFixed() {
        return this.iterFixed(sses.get(0)).size();
    }

    /*
        function find_secstr

        Tom F. August 1992

        function to find the secondary structure that a residue lies in, 
        returns NULL if residues is not found in previously stored values
    */

    public SSE FindSecStr(int residue) {
        for (SSE sse : this.sses) {
            if (residue >= sse.sseData.SeqStartResidue 
                    && residue <= sse.sseData.SeqFinishResidue) return sse;
        }
        return null;
      }
    
    public void joinToLast(SSE sseToJoin) {
        sseToJoin.joinToLast();
        // This is yet another hack for the time being to deal with neighbours */
        for (SSE sse : this.sses) {
//            for (int j = 0; j < r.Neighbour.size(); j++) {
//                if (r.Neighbour[j] == q) r.Neighbour[j] = p;
//            }
        }
    }

    
    /*
    sure this is not right!
    */
    public  void LinkOver(SSE p) {
        for (SSE q : this.sses) {
            if (q.Next == p) { 
                q.Next = p.Next;
            }
        }
        p.Next = null;
    }

    /*
    used in TopsOptimise for unknown purpose
    */
    public int NumberLink(SSE p) {
        int Number = 0;
        int i = 0;
        for (SSE q : this.iterNext(this.sses.get(0))) {
            if (q != p) {
                i += 1;
                boolean test = false;
                for (SSE r : this.iterFixed(p)) {
                    if (test) break;
                    for (SSE s : this.iterFixed(q)) {
                        if (test) break;
                        if (s.To == r) {
                            if (i > Number) Number = i;
                            i = 0;
                            test = true;
                        }
                    }
                }
            }
        }
        return Number;
    }

    /*
      This function removes q from the Next list and adds it to the Fixed list beginning at p
    */
    public void moveFixed(SSE p, SSE q) {
        System.out.println("moving fixed...");
        if (p != null) {
            if (!this.CheckFixedList(p)) {
                System.out.println("movefixed error!");
                return;
            }
            if (q != null) {
                System.out.println("searching...");
                SSE lastSSE = p;
                for (SSE sse : this.iterFixed(p)) {
                    System.out.println("iterating over" + sse);
                    lastSSE = sse;
                }
                System.out.println("setting" + lastSSE + " fixed to " + q);
                lastSSE.setFixed(q);
                this.LinkOver(q);
            }
        }
    }

    //
    // This function checks the integrity of the fixed list - 
    // returns an error (0) if a cycle is found
    //
    public boolean CheckFixedList(SSE p) {
        int CheckLen = 0;
        for (SSE q : this.iterFixed(p)) {
            CheckLen += 1;
            int i = 0;
            for (SSE r : this.iterFixed(p)) {
                System.out.println(String.format("checking fixed list %s %s %s", i, r, CheckLen));
                if (i > CheckLen) break;
                if (r == q) {
                    System.out.println("cycle found! " +  r + "=" + q);
                    return false;
                }
            }
        }
        return true;
      }

    public SSE FindFixedStart(SSE p) {
        for (SSE q : this.sses) {
            for (SSE r : this.iterFixed(q)) {
                if (r == p) return q;
            }
        }
        return null;
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

    public void DeleteBPRelation(SSE p, SSE q) {
//        p.RemoveBP(q);
//        q.RemoveBP(p);
        //XXX
    }

    public int FixedNumber() {
//        return this.fixed.size();
        return -1;  /// XXX 
    }

    /*
        Hand calculator
        Updated to call T. Slidel's chirality calculation by D. Westhead 20/5/97
        If handedness is uncertain, or an error occurrs, right handed is assumed
        an sse method
    */
    public Hand Hand3D(SSE p) {

        SSE q = this.topsChiralPartner(p);
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


    public int FixedSize(SSE sse) {
        System.out.println("finding size of fixed structure from " + sse);
        return this.iterFixed(sse).size();
    }

    public SSE ChainStart() {
        return this.sses.get(0);
    }

    public SSE ChainEnd() {
        return this.sses.get(this.sses.size() - 1);
    }

    public SSE LargestFixed() {
        int largestSize = 0;
        SSE largest = null;
        for (SSE sse : this.sses) {
            for (SSE fixedStart : this.iterFixed(sse)) {
                int i = this.FixedSize(fixedStart);
                if (i > largestSize) {
                    largestSize = i;
                    largest = sse;
                }
            }
        }
        return largest;
    }

    public void ClearPlaced() {
        for (SSE p : this.sses) p.setSymbolPlaced(false);
    }

    public void ClearFixedPlaced() {
//        for (SSE p : this.fixed) p.SymbolPlaced = false;
    }

    public SSE topsChiralPartner(SSE p) {
        /*
        this piece of code finds sequences of ss elements to which a chirality should be attached
        for TOPS this is two parallel strands in the same fixed structure with a connection of at least one and no 
        more than five ss elements, none of which should be in the same sheet,
        OR two parallel helices each of more than 12 residues connected by at least one and no more than 2 other helices.
        */
        int startIndex = this.sses.indexOf(p) + 1;

        if (p.isStrand()) {
            int endIndex = startIndex + 5;
            for (int i = startIndex; i < endIndex; i++) {
                SSE q;
                if (i >= this.sses.size()) return null;
                else q = this.sses.get(i);
                if ((q.isStrand()) && (this.FindFixedStart(q) == this.FindFixedStart(p))) {
                    if (q.getDirection() == p.getDirection()) return q;
                    else return null;
                }
            }
        } else if ((p.isHelix()) && (p.SecStrucLength() > 12)) {
            int endIndex = startIndex + 2;
            for (int i = startIndex; i < endIndex; i++) {
                SSE q;
                if (i >= this.sses.size()) return null;
                else q = this.sses.get(i);
                if (!q.isHelix()) return null;
                if ((q.getDirection() == p.getDirection()) && (q.SecStrucLength() > 12)) return q;
            }
        }
        return null;    // XXX added to satisfy compiler
    }

    public int CountStructures() {
        return this.sses.size();
    }
    
    public void setSSEType(int i, SSEType type) {
        this.secondaryStruc.set(i, type);
    }

    public SSEType getSSEType(int i) {
        if (this.IsGenHelix(i)) {
            return SSEType.HELIX;
        } else if (this.IsExtended(i)) {
            return SSEType.EXTENDED;
        } else {
            return SSEType.COIL;
        }
    }

    public void AddDonatedBond(Integer a, Integer b, double energy) {
        List<Integer> bonds;
        if (this.donatedHBonds.containsKey(a)) {
            bonds = this.donatedHBonds.get(a);
        } else {
            bonds = new ArrayList<Integer>();
            this.donatedHBonds.put(a, bonds);
        }
        bonds.add(b);

        List<Double> energies;
        if (this.donatedHBondEnergy.containsKey(a)) {
            energies = this.donatedHBondEnergy.get(a);
        } else {
            energies = new ArrayList<Double>();
            this.donatedHBondEnergy.put(a, energies);
        }
        energies.add(energy);
    }

    public void AddAcceptedBond(Integer a, Integer b, double energy) {
        //
        List<Integer> bonds;
        if (this.acceptedHBonds.containsKey(a)) {
            bonds = this.acceptedHBonds.get(a);
        } else {
            bonds = new ArrayList<Integer>();
            this.acceptedHBonds.put(a, bonds);
        }
        bonds.add(b);

        List<Double> energies;
        if (this.acceptedHBondEnergy.containsKey(a)) {
            energies = this.acceptedHBondEnergy.get(a);
        } else {
            energies = new ArrayList<Double>();
            this.acceptedHBondEnergy.put(a, energies);
        }
        energies.add(energy);
    }

    public List<SSE> range(SSE sseFrom, SSE sseTo) {
        // Provide a list view of sses from sseFrom (included) to sseTo (included). //
        int startIndex = this.sses.indexOf(sseFrom);
        int endIndex = this.sses.indexOf(sseTo) + 1;
        return this.sses.subList(startIndex, endIndex);
    }

    public List<SSE> rangeFrom(SSE sseFrom) {
        int startIndex = this.sses.indexOf(sseFrom);
        return this.sses.subList(startIndex, this.sses.size());
    }
    
    public List<SSE> iterFixed(SSE sseStart) {
        List<SSE> fixed = new ArrayList<SSE>();
        SSE sse = sseStart;
        while (sse != null) {
            if (sse.hasFixed()) fixed.add(sse.getFixed());
            sse = sse.getFixed();
        }
        return fixed;
    }
    
    public List<SSE> iterFixedInclusive(SSE sseStart) {
        List<SSE> fixed = new ArrayList<SSE>();
        // XXX TODO!
        return fixed;
    }

    public List<SSE> iterNext(SSE sseStart) {
        List<SSE> fixed = new ArrayList<SSE>();
        SSE sse = sseStart;
        while (sse != null) {
            if (sse.Next != null) fixed.add(sse.Next);
            sse = sse.Next;
        }
        return fixed;
    }

    public int SequenceLength() {
        return this.sequence.size();
    }
    
    public boolean IsExtended(int i) {
        if (i < 0 || i >= this.SequenceLength()) return false;
        return this.secondaryStruc.get(i) == SSEType.EXTENDED;
    }

    private List<SSEType> helixTypes = new ArrayList<SSEType>() {{
        add(SSEType.HELIX);
        add(SSEType.RIGHT_ALPHA_HELIX); 
        add(SSEType.HELIX_310); 
        add(SSEType.PI_HELIX); 
        add(SSEType.LEFT_ALPHA_HELIX);
    }};
    
    public boolean IsGenHelix(int i) {
        if (i < 0 || i >= this.SequenceLength()) return false;
        return helixTypes.contains(this.secondaryStruc.get(i));
    }

    public boolean IsSSelement(int i) {
        if (i < 0 || i >= this.SequenceLength()) return false;
        return this.IsGenHelix(i) || this.IsExtended(i);
    }


    public List<SSE> ListBPGroup(SSE p, List<SSE> sseList) {
        List<SSE> GroupList = new ArrayList<SSE>();
        GroupList.add(p);
        for (SSE q : sseList){
            if (p.getFirstCommonBP(q) != null) {
                GroupList.add(p);
            }
        }
        return GroupList;
    }
 
    public SSE getCommonBP(List<SSE> CurrentList, int MaxListLen) {
        SSE Start = CurrentList.get(0);
        if (Start == null) return null;
        return SSE.getCommonBP(Start, CurrentList);
    }


    public void SortListByBridgeRange(SSE bp, List<SSE> sseList) {
        int n = sseList.size();
        if (n < 1) return;
        boolean test = true;
        while (test) {
            test = false;
            for (int i = 0; i < n; i++) {
                int bpind1 = bp.FindBPIndex(sseList.get(i-1));
                int bpind2 = bp.FindBPIndex(sseList.get(i));
//                if (bp.BridgeRange[bpind2][0] < bp.BridgeRange[bpind1][0]) {
//                    swap(sseList, i, j);
//                    test = true;
//                }
            }
        }
    }
    
    private void swap(List<SSE> sses, int i, int j) {
        SSE tmp = sses.get(i);
        sses.set(i, sses.get(j));
        sses.set(j, tmp);
    }

    //
    // a chain method
    // alternatively, an sse method 'separation(this, other)'
    // where you would call:
    //
    public SSE ClosestInFixed(SSE FixedStart, SSE p) {
        SSE closest = null;

        double minsep = Double.MIN_VALUE;
        for (SSE q : this.iterFixed(FixedStart)) {
            double sep = this.SecStrucSeparation(p, q);
            if (sep < minsep) {
                minsep = sep;
                closest = q;
            }
        }
        return closest;
    }

    public SSE LongestInFixed(SSE FixedStart) {
        SSE longest = null;

        int maxlen = 0;
        for (SSE p : this.iterFixed(FixedStart)) {
            int len = p.SecStrucLength();
            if (len > maxlen) {
                maxlen = len;
                longest = p;
            }
        }
        return longest;
    }

   


    public int indexOfFirstZero(int[] l) {
        for (int i = 0; i < l.length; i++) {
            if (l[i] == 0) return i;
        }
        return -1;
    }
     
    /*
    This function detects barrels of the slightly more general variety ie. a cycle of strands but with the possibility of
    some strands also participating in another sheet 
    a chain method
    */

    public List<SSE> DetectBarrel(SSE p) {
        List<SSE> barrel = new ArrayList<SSE>();
        List<SSE> visited = new ArrayList<SSE>();
        this.FindBarrel(p, barrel, visited, null);
        return barrel;
    }

    /*
     This function detects and enumerates the first cycle found in a 
     set of strands connected by BridgePartner relationships
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
    
    /*
        Function to calculate separation of two secondary structure vectors
        DW added (cut from CalculateNeighbours for more general use) : 23/7/96
    */
    public boolean parallel(SSE p, SSE q) {
//        pk, pj, sk, sj, torsion = p.ClosestApproach(q);
        TorsionResult values = p.ClosestApproach(q);
        return Math.abs(values.torsion) < 90.0;
    }
        
    public double SimpleSSESeparation(SSE p, SSE q) {
        Vector3d pk = plus(p.axis.AxisStartPoint, p.axis.AxisFinishPoint);
        Vector3d pj = plus(q.axis.AxisStartPoint, q.axis.AxisFinishPoint);
        pk.scale(1/2.0);
        pj.scale(1/2.0);
        return this.distance3D(pk, pj);
    }
    
    private Vector3d plus(Vector3d a, Vector3d b) {
        Vector3d c = new Vector3d(a);
        c.add(b);
        return c; 
    }

    public double SecStrucSeparation(SSE p, SSE q) {
//        pk, pj, sk, sj, torsion
        TorsionResult result = p.ClosestApproach(q);
        double sk = result.sk;
        double sj = result.sj;
        double torsion = result.torsion;

        //check error
        if (torsion < -990.0 ) return 0.0;

        Vector3d pk = null;
        if (sk < 0.0) pk = p.axis.AxisStartPoint;
        else if (sk > 1.0) pk = p.axis.AxisFinishPoint;

        Vector3d pj = null;
        if (sj < 0.0) pj = q.axis.AxisStartPoint;
        else if (sj > 1.0) pj = q.axis.AxisFinishPoint;

        return this.distance3D(pk, pj);
    }



    //TODO : tie this to the prosec python code!
    public Hand Chiral3d(SSE a, SSE b) {
        return Hand._unk_hand;
    }

    public double distance3D(Vector3d a, Vector3d b) {
        return diff(b, a).length();
    }
    
    private Vector3d diff(Vector3d a, Vector3d b) {
        Vector3d ab = a;
        ab.sub(b);
        return ab;
    }

    //might not be as accurate as the original
    public double AngleBetweenLines(Vector3d a, Vector3d b, Vector3d c) {
        Vector3d ba = diff(b, a);
        Vector3d bc = diff(b, c);
        if (ba.length() == 0.0 || bc.length() == 0.0) return 0.0;
        return Math.toDegrees(ba.angle(bc));
    }

    /*
        function to obtain bridge partners from main chain H bond information
        assumes that DonatedHBonds and AcceptedHBonds are fully redundant
    */
    public void BridgePartFromHBonds() {
        int SequenceLength = this.SequenceLength();
        for (int i = 0; i < SequenceLength; i++) {
//            this.BridgeEnergy.add(new int[] {LARGE, LARGE});
        }

        //conditions for an anti-parallel bridge: first is another residue to which there are both donor and acceptor HBonds//
        for (int i = 0; i < SequenceLength; i++) {
            if (this.IsExtended(i)) {

//                for (int j = 0; j < this.DonatedHBonds[i]; j++) {
//                    for (int k = 0; k < this.AcceptedHBonds[i]; k++) {
//                        if (this.DonatedHBonds[i][j] == this.AcceptedHBonds[i][k]) {
//                            be = this.DonatedHBondEnergy[i][j] + this.AcceptedHBondEnergy[i][k];
//                            this.AddBridgeByEnergy(i, this.DonatedHBonds[i][j], SSEPartner.ANTI_PARALLEL_BRIDGE, be);
//                        }
//                    }
//                }
                        
                // second is an accepted HBond for i-1 from l+1 and a donated one from i+1 to l-1 for some l //
                if (i > 0) {
//                    for (int j; j < this.AcceptedHBonds[i - 1]; j++) {
//                        l = this.AcceptedHBonds[i - 1][j] - 1;
//                        double be = this.AcceptedHBondEnergy[i - 1][j];
//                        if (l > 0) {
//                            for (int k = 0; k < this.AcceptedHBonds[l - 1]; k++) {
//                                if (this.AcceptedHBonds[l - 1][k] == (i+1)) {
//                                    be += this.AcceptedHBondEnergy[l - 1][k];
//                                    this.AddBridgeByEnergy(i, l, SSEPartner.ANTI_PARALLEL_BRIDGE, be);
//                                }
//                            }
//                        }
//                    }
                }

                // conditions for a parallel bridge //
                // first: i has a donated H bond to l-1 and an accepted H bond from l+1 //
//                for (int j  = 0; j < this.DonatedHBonds[i].size(); j++) {
//                    l = this.DonatedHBonds[i][j] + 1;
//                    be = this.DonatedHBondEnergy[i][j];
//                    for (int k = 0; k < this.AcceptedHBonds[i]; k++) {
//                        if (this.AcceptedHBonds[i][k] == (l+1)) {
//                            be += this.AcceptedHBondEnergy[i][k];
//                            this.AddBridgeByEnergy(i, l, PARALLEL_BRIDGE, be);
//                        }
//                    }
//                }

                // second: i-1 has an accepted H bond from l and i+1 has a donated H bond to l //
                if (i > 0) {
//                    for (int j = 0; j < this.AcceptedHBonds[i - 1]; j++) {
//                        l = this.AcceptedHBonds[i-1][j];
//                        be = this.AcceptedHBondEnergy[i-1][j];
//                        for (int k = 0; k < this.AcceptedHBonds[l]; k++) {
//                            if (this.AcceptedHBonds[l][k] == (i+1)) {
//                                be += this.AcceptedHBondEnergy[l][k];
//                                this.AddBridgeByEnergy(i, l, PARALLEL_BRIDGE, be);
//                            }
//                        }
//                    }
                }
            }
        }
    }


    public boolean AddBridge(int SeqRes1, int SeqRes2, int Type, double Energy) {
        int SequenceLength = this.SequenceLength();
        if ((SeqRes1 >= SequenceLength) || (SeqRes1 < 0) || (SeqRes2 >= SequenceLength) || (SeqRes2 < 0)) return false;

        int i;
        for (i = 0; i < 2; i++) {
            if (this.bridgePartners.get(SeqRes1)[i] < 0) break;
        
            if (this.bridgePartners.get(SeqRes1)[i] == SeqRes2) return true;
        }

        int j;
        for (j = 0; j < 2; j++) {
            if (this.bridgePartners.get(SeqRes2)[j] < 0) break;
        
            if (this.bridgePartners.get(SeqRes2)[j] == SeqRes1) return true;
        }
            
        //using the variables outside the block! don't like it but...   
        if ( (i<2) && (j<2) ) {
        
//            this.BridgePartners.get(SeqRes1)[i] = SeqRes2;
//            this.BridgeEnergy.get(SeqRes1)[i] = Energy;
//            this.BridgeType.get(SeqRes1)[i] = Type;
//            this.BridgePartners.get(SeqRes2)[j] = SeqRes1;
//            this.BridgeEnergy.get(SeqRes2)[j] = Energy;
//            this.BridgeType.get(SeqRes2)[j] = Type;

            return true;
        }

        return false;
    }


    public void AddBridgeByEnergy(int SeqRes1, int SeqRes2, int Type, double Energy) {

        if ((SeqRes1 >= this.SequenceLength()) || (SeqRes1 < 0) || (SeqRes2 >= this.SequenceLength()) || (SeqRes2 < 0)) return;

        if (!this.AddBridge(SeqRes1, SeqRes2, Type, Energy)) {
            this.ReplaceBridgeByEnergy(SeqRes1, SeqRes2, Type, Energy);
        }
    }

    public void ReplaceBridgeByEnergy(int SeqRes1, int SeqRes2, int Type, double Energy) {

        if ((SeqRes1 >= this.SequenceLength()) || (SeqRes1 < 0) || (SeqRes2 >= this.SequenceLength()) || (SeqRes2 < 0)) return;

//        int hep1 = this.HighestEnergyBridge(SeqRes1);
//        double he1 = this.BridgeEnergy[SeqRes1][hep1];
//        int hep2 = this.HighestEnergyBridge(SeqRes2);
//        double he2 = this.BridgeEnergy[SeqRes2][hep2];

//        if ((Energy < he1) && (Energy < he2)) {
//        
//            this.ReplaceBridge(SeqRes1, hep1, SeqRes2, Type, Energy);
//            this.ReplaceBridge(SeqRes2, hep2, SeqRes1, Type, Energy);
//        }
    }


    public void ReplaceBridge(int SeqRes, int BridgePos, int NewBridgeRes, int NewType, double NewEnergy) {

//        int OldBridgeRes = this.BridgePartners[SeqRes][BridgePos];
//        this.BridgePartners[SeqRes][BridgePos] = NewBridgeRes;
//        this.BridgeType[SeqRes][BridgePos] = NewType;
//        this.BridgeEnergy[SeqRes][BridgePos] = NewEnergy;

//        if (OldBridgeRes > 0) {
//            for (int i = 0; i < 2; i++) {
//                if (this.BridgePartners[OldBridgeRes][i] == SeqRes) break;
//                this.BridgePartners[OldBridgeRes][i] = UNSET_PDB;
//                this.BridgeType[OldBridgeRes][i] = UNK_BRIDGE_TYPE;
//                this.BridgeEnergy[OldBridgeRes][i] = LARGE;
//            }
//        }
    }

    public int HighestEnergyBridge(int SeqRes) {
        int heb = 0;
        double he = Double.MIN_VALUE;

        for (int i = 0; i < 2; i++) {
//            if (this.BridgeEnergy[SeqRes][i] > he) {
//                he = this.BridgeEnergy[SeqRes][i];
//                heb = i;
//            }
        }
        return heb;
     }


    public String topsHeader(String proteinCode) {
        return String.format("DOMAIN_NUMBER %d %s%s%d", 0, proteinCode, this.name, 0);   //FIXME : domains!
    }

    public String toTopsFile() {
        StringBuffer sseStringList = new StringBuffer(); 
        for (SSE sse : this.sses) {
            sseStringList.append(sse.toTopsFile()).append("\n\n");
        }
        return sseStringList.toString();
    }

    public String getEdgeString() {
        StringBuffer edges = new StringBuffer();
        for (SSE sse : this.sses) {
            sse.getEdgeString(edges);
        }
        return edges.toString();
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        for (SSE sse : this.sses) {
            str.append(sse.toString());
        }
        return this.name + " " + str.toString() + " " + this.getEdgeString();
    }
    
    /*
     * Following methods moved from Cartoon...
     */
    

    /**
     * Calculates chiralities for 2D TOPS cartoon
     */
    public Hand Hand2D(SSE p) {
        if (p.Chirality != Hand._no_hand) {
            SSE q = this.topsChiralPartner(p);
            if (q != null) return this.Chiral2d(p, q);
        }
        return Hand._no_hand;
    }

    /**
     * This could be a Cartoon method if it was recast as finding the sign of the determinant of the matrix
     *   of course, this would mean not re-using the Torsion method so cunningly, but hey.
     */
    public Hand Chiral2d(SSE p, SSE q) {
        Hand hand = Hand._unk_hand;
        Hand lasthand = Hand._unk_hand;

        Vector3d a, b, c, d;
        if (p.getDirection() == 'U') a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 1.0);
        else a = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        b = new Vector3d(p.getCartoonX(), p.getCartoonY(), 0.0);

        c = new Vector3d(q.getCartoonX(), q.getCartoonY(), 0.0);

        int i = 0;
        for (SSE r : this.range(p.To, q)) {
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

    public double FixedSpan(SSE p, double GridUnitSize) {
        double minx = 0;
        double maxx = 0;
        SSE q = this.FindFixedStart(p);
        if (q != null) {
            minx = q.getCartoonX();
            maxx = q.getCartoonX();

            for (SSE r : this.iterFixed(q)) {
                int rx = r.getCartoonX();
                if (rx > maxx) maxx = rx;
                if (rx < minx) minx = rx;
            }
        }

        double span;
        if (GridUnitSize <= 0) {
            span = (maxx - minx) / 1;
        } else {
            span = (maxx - minx) / GridUnitSize;
        }
        span += 1;
        return span;
    }

    public void ReflectFixedXY(SSE p) {
        for (SSE q : this.iterFixed(p)) {
            q.flip();
        }
    }
    
    /**
    Function to determine whether two sheets constitute a sandwich.
    Both must have more than two strands, there must be at least a certain number of 
    pairwise contacts - this number is the the number of strands in the smallest sheet - ,
    and these contacts must span at least two strands of the larger sheet 
    **/
    public boolean IsSandwich(SSE r, SSE s) {
        int ncontacts = 0;
        double MaxContactSeparation = 13.0;
        double MinOverLap = 2 * this.GridUnitSize;

        SSE p = this.FindFixedStart(r);
        SSE q = this.FindFixedStart(s);

        double s1 = this.FixedSpan(p, this.GridUnitSize);
        double s2 = this.FixedSpan(q, this.GridUnitSize);

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
        for (SSE p1 : this.iterFixed(p)) {
            for (SSE q1 : this.iterFixedInclusive(q)) {
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

    /**
        function to flip all symbols in a fixed list AND CENTER!
    **/
    public void FlipSymbols(SSE p) {
        double cen = 0;
        int i = 0;
        for (SSE q : this.iterFixed(p)) {
            cen += q.getCartoonX();
            i += 1;
        }

        if (i > 0) cen = (cen * 2) / i;

        //TODO : separate centering and flipping functions!
        for (SSE q : this.iterFixed(p)) {
            q.setCartoonX((int) (cen - q.getCartoonX()));
            q.flip();
         }
     }

    public List<SSE> GetListAtXPosition(SSE startSSE, double XPosition) {
        List<SSE> filtered = new ArrayList<SSE>();
        for (SSE p : this.iterFixed(startSSE)) {
            if (p.getCartoonX() == XPosition) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<SSE> GetListAtPosition(SSE startSSE, double XPosition, double YPosition) {
        List<SSE> filtered = new ArrayList<SSE>();
        for (SSE p : this.iterFixed(startSSE)) {
            if (p.getCartoonX() == XPosition && p.getCartoonY() == YPosition) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public double LeftMostPos(SSE p) {
        double LeftMost = p.getCartoonX();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() < LeftMost) LeftMost = q.getCartoonX();
        }
        return LeftMost;
    }

    public SSE LeftMost(SSE p) {
        double LeftMost = p.getCartoonX();
        SSE leftMostSymbol = p;
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() < LeftMost) {
                LeftMost = p.getCartoonX();
                q = p;
                leftMostSymbol = q;
            }
        }
        return leftMostSymbol;
    }
    
    public double RightMostPos(SSE p) {
        double RightMost = p.getCartoonX();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() > RightMost) RightMost = q.getCartoonX();
        }
        return RightMost;
    }
   

    public SSE RightMost(SSE p) {
        double RightMost = p.getCartoonX();
        SSE r = p;
        for (SSE q : this.iterFixed(p)) {
            if (p.getCartoonX() < RightMost) {
                RightMost = p.getCartoonX();
                q = p;
            }
            r = q;
        }
        return r;
    }

    public double LowestPos(SSE p) {
        double BottomMost = p.getCartoonY();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonY() < BottomMost) BottomMost = q.getCartoonX();
        }
        return BottomMost;
    }

    public double HighestPos(SSE p) {
        double TopMost = p.getCartoonY();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonY() > TopMost) TopMost = q.getCartoonX();
        }
        return TopMost;
    }
    
    List<FixedType> allowedTypes = new ArrayList<FixedType>() {{
            add(FixedType.FT_BARREL); 
            add(FixedType.FT_SANDWICH); 
            add(FixedType.FT_CURVED_SHEET); 
            add(FixedType.FT_V_CURVED_SHEET); 
    }};

    /**
      This function checks that a given fixed structure is drawn with
      correct handedness.  It only works where the handedness is easy to
      calculate (ie. we can use TS's routine) that being where we can find
      an appropriate Beta-x-Beta unit.  Note that TIM barrels do not lie
      in this category but are already drawn with correct chirality.
    **/
    public void SetFixedHand(SSE p) {
        if (allowedTypes.contains(p.getFixedType())) {
            System.out.println(String.format("Checking fixed structure chirality for fixed start %d", p.getSymbolNumber()));
            SSE q = find(p);
            if (q != null) {
                System.out.println("Found suitable motif for fixed chirality check");
                SSE r = null;   // XXX FIXME XXX
                Hand chir = Chiral2d(q, r);
                if (chir != Hand._unk_hand) {
                    System.out.println(String.format("Changing chirality of fixed structure starting at %d", p.getSymbolNumber()));
                    this.ReflectFixedXY(p);
                }
            } else {
                System.out.println("No suitable motif found for fixed chirality check");
            }
            
        }
    }
    
    private SSE find(SSE p) {
        // TODO : FIXME - see SetFixedHand!! XXX
        for (SSE q : this.sses) {
            if (this.FindFixedStart(q) == p) {
                boolean found = false;
                int n = 0;
                for (SSE r : this.rangeFrom(q.To)) {
                    if (this.FindFixedStart(r) != p) break;
                    n += 1;
                    if (r.getDirection() == q.getDirection()) {
                        Hand chir = Chiral2d(q, r);
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
    Directions of cartoon symbols are binary, whereas SSEs have full blown vectors.
    **/
    public void CalculateDirection() {
        System.out.println("Assigning directions to secondary structures\n");
        SSE Root = this.sses.get(0);
        SSE q = Root;
        for (SSE p : this.iterNext(Root)) {
            if (p != Root) {
                if (q.isParallel(p)) {
                    if (p.getDirection() != q.getDirection()) this.FlipSymbols(p);
                } else {
                    if (p.getDirection() == q.getDirection()) this.FlipSymbols(p);
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
        for (SSE p : this.iterNext(Root)) {
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
    

    /**
    returns center and radius
    **/
    public Circle FixedBoundingCircle(SSE FixedStart) {

        int n = 0;
        double centerX = 0.0;
        double centerY = 0.0;
        for (SSE p : this.iterFixed(FixedStart)) {
            centerX += p.getCartoonX();
            centerY += p.getCartoonY();
            n +=1;
        }
        if (n > 0) {
            centerX /= (double) n;
            centerY /= (double) n;
        }

        double rim = 0.0;
        for (SSE p : this.iterFixed(FixedStart)) {
            double x = p.getCartoonX();
            double y = p.getCartoonY();

            //FIXME : distance2D method needs a class home!
            double separation = distance2D(x, y, centerX, centerY);
            if (separation > rim) rim = separation;
        }

        double radius = rim - (0.5 * FixedStart.getSymbolRadius());
        return new Circle(centerX, centerY, radius);
    }


    public void makeSheet(SSE p, SSE q, double GridUnitSize) {

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
                this.moveFixed(q, p);
                p.AssignRelDirection(q);
            }
      }
        

        // For all bridge partners except q repeat
        for (BridgePartner bridgePartner : p.getBridgePartners()) {
            if (bridgePartner.partner != q) { 
                this.makeSheet(bridgePartner.partner, p, GridUnitSize);
            }
        }

        sortStacking(q, q, 0, Layer, Layer, 0); // XXX args!
    }
                    
    private void sortStacking(SSE p, SSE q, int StartXPos, List<SSE> StartLowerList, List<SSE> StartUpperList, int MaxListLen) {
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
            double CurrentXPos = this.LeftMostPos(p);
            List<SSE> CurrentList = this.GetListAtXPosition(p, CurrentXPos);
            while (!CurrentList.isEmpty()) {
                for (int i = 0; i < CurrentList.size(); i++) {
                    SSE r = CurrentList.get(i);
                    for (int j = i + 1; j < CurrentList.size(); j++) {
                        SSE s = CurrentList.get(j);
                        if (r.getFirstCommonBP(s) == null) {
                            TwoLayers = true;
                            StartXPos = (int) CurrentXPos;
                            StartUpperList = this.ListBPGroup(r, CurrentList);
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
                CurrentList = this.GetListAtXPosition(p, CurrentXPos);
            }

            // divide layers if necessary
            if (TwoLayers) {

              twoLayers(p, StartXPos, StartLowerList, StartUpperList);  
            }

            // deal with cases where there are split strands
            splitStrands(p, MaxListLen);
        }
    }
                    
                        
    private void twoLayers(SSE p, int StartXPos, List<SSE> StartLowerList, List<SSE> StartUpperList) {
        System.out.println("Sheet configured as V_CURVED_SHEET");
        for (SSE t : this.iterFixed(p)) {
            t.setFixedType(FixedType.FT_V_CURVED_SHEET);
        }

        // divide at the point of splitting identified above
        for (SSE k : StartLowerList) {
            k.setCartoonY((int) (k.getCartoonY() - GridUnitSize));
        }
        

        // divide to right, then left
        this.SplitSheet(p, StartUpperList, StartLowerList, StartXPos, 1);
        this.SplitSheet(p, StartUpperList, StartLowerList, StartXPos, -1);
    }
                        
    private void splitStrands(SSE p, int MaxListLen) {
        for (SSE r : this.iterFixed(p)) {
            
            List<SSE> CurrentList = this.GetListAtPosition(p, r.getCartoonX(), r.getCartoonY());

            if (CurrentList.size() > 1) { 
                SSE bp = this.getCommonBP(CurrentList, MaxListLen);
                if (bp != null) {
                    this.SortListByBridgeRange(bp, CurrentList);
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


    // Cartoon method
    public void SplitSheet(SSE p, List<SSE> StartUpperList, List<SSE> StartLowerList, int StartXPos, int Direction) {

        int UPPER_LAYER = 3;
        int MIDDLE_LAYER = 2;
        int LOWER_LAYER = 1;
        int UNK_LAYER = 0;

        double CurrentXPos = StartXPos + Direction * GridUnitSize;
        List<SSE> CurrentList = this.GetListAtXPosition(p, CurrentXPos);
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
            CurrentList = GetListAtXPosition(p, CurrentXPos);
        }
    }


    /** 
     * this function checks ordinary sheets to see if curvature is strong enough for plotting on arc 
     * the test q.FixedType == FT_SHEET is necessary since Make Sheet can produce FT_V_CURVED_SHEET as well
     * as FT_SHEET, and the former type should not be examined by SheetCurvature
     * 
     */
    public void SheetCurvature(SSE p) {

        double MinSpan = 5;
        double MaxSep = 10.0;

        SSE Start = this.FindFixedStart(p);

        if (Start == null || !Start.hasFixedType(FixedType.FT_SHEET)) return;

        // first determine left and right most strands in the sheet """
        SSE left = this.GetListAtXPosition(Start, this.LeftMostPos(Start)).get(0);
        SSE right = this.GetListAtXPosition(Start, this.RightMostPos(Start)).get(0);
        
        double span = this.FixedSpan(Start, GridUnitSize);

        // MaxSep is 0.65 of the separation if the sheet were flat with 4.5A between each pair of strands """
        MaxSep = 4.5 * (span - 1.0) * 0.65;

        double sep = this.SecStrucSeparation(left, right);

        System.out.println(String.format("SheetCurvature %d %d %f %f", left.getSymbolNumber(), right.getSymbolNumber(), sep, MaxSep));

        if (span >= MinSpan && sep<MaxSep) {

            System.out.println("Sheet configured as CURVED_SHEET");

            for (SSE q : this.iterFixed(Start)) q.setFixedType(FixedType.FT_CURVED_SHEET);

            // this direction calculation is to ensure that sheets from AB barrels are plotted with the correct chirality """
            SeqDirResult result = this.FindSheetSeqDir(Start);
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

            int CurrXPos = (int) LeftMostPos(Start);
            List<SSE> sseList = this.GetListAtXPosition(Start, CurrXPos);

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
                sseList = this.GetListAtXPosition(Start, CurrXPos);

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

    public SeqDirResult FindSheetSeqDir(SSE Start) {
        SSE prev = null;
        SSE curr = null;

        double CurrXPos = this.LeftMostPos(Start);
        List<SSE> sseList = this.GetListAtXPosition(Start, CurrXPos);
        if (sseList.size() == 1) prev = sseList.get(0);

        char lastdir = 'X';
        int lastsdir = 0;
        char dir = 'X';
        int sdir = 0;
        boolean done = false;

        while (sseList.size() > 0) {

            CurrXPos += GridUnitSize;
            sseList = this.GetListAtXPosition(Start, CurrXPos);

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


    public void makeBarrel(List<SSE> Barrel, double GridUnitSize) {
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
                this.moveFixed(p, q);
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

                    this.moveFixed(p, r);

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

    public void makeSandwich(SSE r, SSE s) {
        // determine relative sizes of sheets """
        SSE rSheetStart = this.FindFixedStart(r);
        SSE sSheetStart = this.FindFixedStart(s);

        double spanR = this.FixedSpan(rSheetStart, this.GridUnitSize);
        double spanS = this.FixedSpan(sSheetStart, this.GridUnitSize);

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
        SSE shortSheetLeftMost = this.LeftMost(shortSheetStart);
        SSE shortSheetRightMost = this.RightMost(shortSheetStart);

        // determine the left most and right most strands in the long sheet """
        SSE longSheetLeftMost = this.LeftMost(longSheetStart);
        SSE longSheetRightMost = this.RightMost(longSheetStart);

        // is the short sheet the right way round?, if not turn it """
        double leftSeparation = this.SecStrucSeparation(shortSheetLeftMost, longSheetLeftMost);
        double rightSeparation = this.SecStrucSeparation(shortSheetRightMost, longSheetLeftMost);
        if (leftSeparation > rightSeparation) {
            this.FlipSymbols(shortSheetStart);
            SSE tmp = shortSheetRightMost;
            shortSheetRightMost = shortSheetLeftMost;
            shortSheetLeftMost = tmp;
        }

        // now check directions of symbols """
        // this is done geometrically using the longest strand in each sheet """
        SSE longestStrandInShortSheet = this.LongestInFixed(rSheetStart);
        SSE longestStrandInLongSheet = this.LongestInFixed(sSheetStart);
        TorsionResult torsionResult = longestStrandInLongSheet.ClosestApproach(longestStrandInShortSheet);
        if (Math.abs(torsionResult.torsion) > 90.0) {
            if (longestStrandInShortSheet.getDirection() == longestStrandInLongSheet.getDirection()) {
                for (SSE sse : this.iterFixed(shortSheetStart)) {
                    sse.flip();
                }
            }
        } else {
            if (longestStrandInShortSheet.getDirection() != longestStrandInLongSheet.getDirection()) {
                for (SSE sse : this.iterFixed(shortSheetStart)) {
                    sse.flip();
                }
            }
        }
        
        // now reposition the short sheet, x direction try to get best position of left and right most strands in the
        //   short sheet subject with the constraint that the short sheet must lie within the x range of the long sheet  """
        SSE closestLongShortLeft = this.ClosestInFixed(longSheetStart, shortSheetLeftMost);
        SSE closestLongShortRight = this.ClosestInFixed(longSheetStart, shortSheetRightMost);
        double xmove = (closestLongShortLeft.getCartoonX() - shortSheetLeftMost.getCartoonX() + closestLongShortRight.getCartoonX() - shortSheetRightMost.getCartoonX()) / 2;
        double loff = longSheetLeftMost.getCartoonX() - shortSheetLeftMost.getCartoonX() - xmove;
        double roff = shortSheetRightMost.getCartoonX() + xmove - longSheetRightMost.getCartoonX();
        if (loff > 0) xmove += loff;
        if (roff > 0) xmove -= roff;

        double ymove = LowestPos(longSheetStart) - this.GridUnitSize - shortSheetLeftMost.getCartoonY();

        for (SSE sse : this.iterFixed(shortSheetStart)) {
            sse.setCartoonX((int)(sse.getCartoonX() + xmove));
            sse.setCartoonY((int) (sse.getCartoonY() + ymove));
        }
        

        // set type of fixed structure """
        for (SSE sse : this.iterFixed(shortSheetStart)) {
            sse.setFixedType(FixedType.FT_SANDWICH);
        }
        for (SSE sse : this.iterFixed(longSheetStart)) {
            sse.setFixedType(FixedType.FT_SANDWICH);
        }
    }


    public double scaleToFit(double canvasWidth, double canvasHeight, double symbolRadius) {

        Dimension size = this.calculateSize();
        double chainWidth = size.width;
        double chainHeight = size.height;
        //border = symbolRadius * 3
        
        double scaleX = canvasWidth / chainWidth + (2 * symbolRadius);
        double scaleY = canvasHeight / chainHeight + (2 * symbolRadius);
        double scaleFactor = Math.min(scaleX, scaleY);
        this.scaleBy(scaleFactor, scaleFactor);

        double canvasCenterX = canvasWidth / 2.0;
        double canvasCenterY = canvasHeight / 2.0;

        this.moveTo(canvasCenterX, canvasCenterY);
//        chainCenterX, chainCenterY = this.calculateCenter(); // XXX wat?
        return scaleFactor;
    }

    public void moveTo(double x, double y) {
        Point2d center = this.calculateCenter();
        this.moveBy(x - center.x, y - center.y);
    }

    public void moveBy(double dx, double dy) {
        for (SSE sse : this.sses) {
            sse.setCartoonX((int)(sse.getCartoonX() + dx));
            sse.setCartoonY((int)(sse.getCartoonY() + dy));
        }
    }

    public void scaleBy(double scaleFactorX, double scaleFactorY) {
        for (SSE sse : this.sses) {
            sse.setCartoonX((int) (sse.getCartoonX() * scaleFactorX));
            sse.setCartoonY((int) (sse.getCartoonY() * scaleFactorY));
        }
    }
    

    public Rectangle2D calculateBoundingBox(double symbolRadius) {
        Dimension size = this.calculateSize();
//        centerX, centerY = this.calculateCenter();
        Point2d center = this.calculateCenter();
        double w2 = ((double)size.width) / 2;
        double h2 = ((double)size.height) / 2;
        // TODO : use Rectangle2D.Double?
        return new Rectangle2D.Double(center.x - w2, center.y - h2, center.x + w2, center.y + h2);
    }

    public Rectangle2D getMinMaxCoords() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for (SSE sse : this.sses) {
            double x = sse.getCartoonX();
            double y = sse.getCartoonY();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }
        return new Rectangle2D.Double(minX, minY, maxX, maxY);
    }
    
    public Point2d calculateCenter() {
        return null;    // TODO
    }

    public Dimension calculateSize() {
        // TODO
        return null;
    }

    public static SSE LineHitSymbol(Chain chain, SSE p,SSE q) {
        double TOL = 0.001;

        double px = p.getCartoonX();
        double py = p.getCartoonY();
        double qx = q.getCartoonX();
        double qy = q.getCartoonY();

        for (SSE r : chain.getSSEs()) {
            double rx = r.getCartoonX();
            double ry = r.getCartoonY();

            double dprx = Math.abs(px - rx);
            double dpry = Math.abs(py - ry);
            double dqrx = Math.abs(qx - rx);
            double dqry = Math.abs(qy - ry);

            if (!((dprx < TOL && dpry < TOL) || (dqrx < TOL && dpry < TOL))) {

                double pr = (dprx * dprx) + (dpry * dpry);
                double pq = Math.pow((px - qx), 2) + Math.pow((py - qy), 2);
                double qr = (dqrx * dqrx) + (dqry * dqry);

                if (Math.min(pr,qr) + pq > Math.max(pr,qr)) {
                    double a  = (pr + pq - qr);
                    pr = Math.sqrt(pr);
                    pq = Math.sqrt(pq);

                    
                    a /= (2.0 * pr * pq);
                    if (Math.abs(a) > 1.0) {
                        if (a < 0) a = -1.0;
                        else     a = 1.0;
                    }
                    a  = Math.acos(a);

                    if (2 * pr * Math.tan(a) <= r.getSymbolRadius()) return r;
                }
            }
        }
        return null;
    }


    /*
        function angle

        Tom F. August 1992

        Function to return the angle between two 2D vectors
    */

    public double Angle(SSE p, SSE q, SSE r) {
        int pX = p.getCartoonX();
        int qX = q.getCartoonX();
        int rX = q.getCartoonX();
        double l1 = distance2D(pX, p.getCartoonY(), qX, q.getCartoonY());
        double l2 = distance2D(rX, r.getCartoonY(), qX, q.getCartoonY());
        if (l1 == 0.0 || l2 == 0.0) return 0.0;
        double pqX = pX - qX;
        double pqY = p.getCartoonY() - q.getCartoonY();
        double rqX = rX - qX;
        double rqY = r.getCartoonY() - q.getCartoonY();
        double l = ((pqX * rqX) + (pqY * rqY)) / (l1 * l2);
        if (l < -1.0) l = -1.0;
        if (l > 1.0) l = 1.0 ;
        return Math.acos(l);
    }

    //
    //really an sse method
    //
    public static double distance2D(SSE a, SSE b) {
        double x1 = a.getCartoonX();
        double y1 = a.getCartoonY();
        double x2 = b.getCartoonX();
        double y2 = b.getCartoonY();
        return distance2D(x1, y1, x2, y2);
    }
    
    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dX = x1 - x2;
        double dY = y1 - y2;
        return Math.sqrt((dX * dX) + (dY * dY));
    }

    public void CalculateConnections(double radius) {
        int ExtensionIndex = 0;
        double[] Extensions = new double[] { 2.7, 7.7, 12.7, 17.7, 22.7 };

        for (SSE sse : this.sses) {
            MakeConnection(radius, sse, ExtensionIndex, Extensions);
        }
    }

    public double NextExtension(int ExtensionIndex, double[] Extensions) {
        if (ExtensionIndex >= Extensions.length) ExtensionIndex = 0;
        double extension = Extensions[ExtensionIndex];
        ExtensionIndex += 1;    // XXX incrementing a scope-local variable FIXME 
        return extension;
    }

    public void MakeConnection(double radius, SSE sse, int ExtensionIndex, double[] Extensions) {
        double PSMALL = 0.001;
        if (sse.To == null) return;

        // these are the condition under which the code generates a bent connection,
        // rather than the usual straight line joining symbols 
        if (this.FindFixedStart(sse) != this.FindFixedStart(sse.To)) return;
        if (sse.hasFixedType(FixedType.FT_SHEET) && !sse.To.hasFixedType(FixedType.FT_SANDWICH)) return;
        SSE q = sse.To;
        if (q == null) return;
        SSE r = LineHitSymbol(this, sse, q);
        if (r != null && sse.getCartoonY() == sse.To.getCartoonY()) {

            // we are guaranteed horizontal lines 
            double px = sse.getCartoonX();
            double py = sse.getCartoonY();
            double qx = sse.To.getCartoonX();
            double qy = sse.To.getCartoonY();

            double d1 = (sse.getDirection() == 'U')? -1.0 : 1.0;
        
            double my = r.getCartoonY() + ( d1 * ( PSMALL * Math.abs(px - qx) + radius + NextExtension(ExtensionIndex, Extensions) ));
            double ny = my;
            
            double d2, d3;
            if (px < qx) { 
                d2 = 1.0;
                d3 =-1.0;
            } else{ 
                d2 = -1.0;
                d3 =  1.0;
            }

            double mx = px + (d2 * (PSMALL * Math.abs(px - qx) + radius));
            double nx = qx + (d3 * (PSMALL * Math.abs(px - qx) + radius));

            sse.addConnection(new Point2d(mx, my));
            sse.addConnection(new Point2d(nx, ny));
        }
    }
  
}

    