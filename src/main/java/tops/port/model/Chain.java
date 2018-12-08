package tops.port.model;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import tops.port.calculate.util.DistanceCalculator;
import tops.port.model.tse.BaseTSE;

public class Chain {

    private char name;
    private List<SSE> sses;
    private List<BaseTSE> tses;
    private List<String> sequence;
    private List<Integer> pdbIndices;
    private List<SSEType> secondaryStruc;
    private Map<Integer, List<BridgePartner>> bridgePartners;
    private List<Point3d> caCoords;
    private List<Bridge> bridges;
    
    private Map<Integer, List<HBond>> donatorHBonds;
    private Map<Integer, List<HBond>> acceptorHBonds;
    
    private static char assignChain(char chain) {
        if (chain == ' ' || chain == '-') {
            return '0';
        } else {
            return chain;
        }
    }

    public Chain(char nameChar) {
        this.name = assignChain(nameChar);
        this.sses = new ArrayList<>();
        this.tses = new ArrayList<>();
        this.sequence = new ArrayList<>();
        this.pdbIndices = new ArrayList<>();
        this.secondaryStruc = new ArrayList<>();
        this.bridgePartners = new HashMap<>();
        this.bridges = new ArrayList<>();
        this.caCoords = new ArrayList<>();
        
        this.acceptorHBonds = new HashMap<>();
        this.donatorHBonds = new HashMap<>();
    }
    

    public List<Bridge> getBridges(SSE current) {
        List<Bridge> selected = new ArrayList<>();
        for (Bridge bridge : bridges) {
            if (bridge.contains(current)) {
                selected.add(bridge);
            }
        }
        return selected;
    }
    
    public List<BaseTSE> getTSEs() {
        return this.tses;
    }
    

    public void addTSE(BaseTSE tse) {
        this.tses.add(tse);
    }

    public List<Bridge> getBridges() {
        return bridges;
    }
    
    public Bridge findBridge(SSE sseStart, SSE sseEnd) {
        for (Bridge bridge : bridges) {
            if (bridge.isBetween(sseStart, sseEnd)) {
                return bridge;
            }
        }
        return null;
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
        this.caCoords.add(point);
    }
    
    public List<Point3d> secondaryStructureAxis(int seqStartResidue, int seqFinishResidue) {
        List<Point3d> coords = new ArrayList<>();
        for (int index = seqStartResidue - 1; index < seqFinishResidue; index++) {
            coords.add(caCoords.get(index));
        }
        return coords;
    }
    
    public char getName() {
        return this.name;
    }
    
    
    public int getPDBIndex(int residue) {
        return this.pdbIndices.get(residue);
    }
    
    public void addBridge(Bridge bridge) {
        this.bridges.add(bridge);
    }
    
    public List<BridgePartner> getBridgePartner(int index) {
        return this.bridgePartners.containsKey(index)?
            this.bridgePartners.get(index) : new ArrayList<>();
    }
    
    public void addBridgePartner(int index, BridgePartner bridgePartner) {
        System.out.println("Adding bridge partner " + index + "  " + bridgePartner);
        List<BridgePartner> list;
        if (bridgePartners.containsKey(index)) {
            list = bridgePartners.get(index);
        } else {
            list = new ArrayList<>();
            bridgePartners.put(index, list);
        }
        list.add(bridgePartner);
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

    public int numberFixed() {
        SSE fixedStart = null;
        for (SSE sse : sses) {
            if (sse.hasFixed()) {
                fixedStart = sse;
                break;
            }
        }
        if (fixedStart == null) {
            return 0;
        } else {
            return this.iterFixed(fixedStart).size();
        }
    }

    /*
        function find_secstr

        Tom F. August 1992

        function to find the secondary structure that a residue lies in, 
        returns NULL if residues is not found in previously stored values
    */

    public SSE findSecStr(int residue) {
        for (SSE sse : this.sses) {
            if (sse.contains(residue)) {
                return sse;
            }
        }
        return null;
    }
    
    
    /*
    used in TopsOptimise for unknown purpose
    */
    public int numberLink(SSE sse) {
        int max = 0;
        int i = 0;
        for (SSE q : this.iterNext(this.sses.get(0))) {
            if (q != sse) {
                i += 1;
                boolean test = false;
                for (SSE r : this.iterFixed(sse)) {
                    if (test) break;
                    for (SSE s : this.iterFixed(q)) {
                        if (test) break;
                        if (getNext(s) == r) {
                            if (i > max) max = i;
                            i = 0;
                            test = true;
                        }
                    }
                }
            }
        }
        return max;
    }

    /*
      This function removes q from the Next list and adds it to the Fixed list beginning at p
    */
    public void moveFixed(SSE p, SSE q) {
//        System.out.println("moving fixed...");
        if (p != null) {
            if (!this.checkFixedList(p)) {
//                System.out.println("movefixed error!");
                return;
            }
            if (q != null) {
//                System.out.println("searching...");
                SSE lastSSE = p;
                for (SSE sse : this.iterFixed(p)) {
                    System.out.println("iterating over" + sse);
                    lastSSE = sse;
                }
//                System.out.println("setting " + lastSSE + " fixed to " + q);
                lastSSE.setFixed(q);
            }
        }
    }

    //
    // This function checks the integrity of the fixed list - 
    // returns an error (0) if a cycle is found
    //
    public boolean checkFixedList(SSE p) {
        int checkLen = 0;
        for (SSE q : this.iterFixed(p)) {
            checkLen += 1;
            int i = 0;
            for (SSE r : this.iterFixed(p)) {
//                System.out.println(String.format("checking fixed list %s %s %s", i, r, CheckLen));
                if (i > checkLen) break;
                if (r == q) {
//                    System.out.println("cycle found! " +  r + "=" + q);
                    return false;
                }
            }
        }
        return true;
    }

    public SSE findFixedStart(SSE p) {
        for (SSE q : this.sses) {
            for (SSE r : this.iterFixed(q)) {
                if (r == p) return q;
            }
        }
        return null;
    }


    public void deleteBPRelation(SSE p, SSE q) {
//        p.RemoveBP(q);
//        q.RemoveBP(p);
        //XXX
    }

   

    public int fixedSize(SSE sse) {
//        System.out.println("finding size of fixed structure from " + sse);
        return this.iterFixed(sse).size();
    }

    public SSE chainStart() {
        return this.sses.get(0);
    }

    public SSE chainEnd() {
        return this.sses.get(this.sses.size() - 1);
    }

    public void clearPlaced() {
        for (SSE p : this.sses) p.setSymbolPlaced(false);
    }

    public void clearFixedPlaced() {
//        for (SSE p : this.fixed) p.SymbolPlaced = false;
    }

    public int countStructures() {
        return this.sses.size();
    }
    
    public void setSSEType(int i, SSEType type) {
        this.secondaryStruc.set(i, type);
    }

    public SSEType getSSEType(int i) {
        if (this.isGenHelix(i)) {
            return SSEType.HELIX;
        } else if (this.isExtended(i)) {
            return SSEType.EXTENDED;
        } else {
            return SSEType.COIL;
        }
    }
    
    public List<HBond> getDonatedHBonds(int index) {
        if (donatorHBonds.containsKey(index)) {
            return donatorHBonds.get(index); 
        } else {
            return new ArrayList<>();
        }
    }
    
    public List<HBond> getAcceptedHBonds(int index) {
        if (acceptorHBonds.containsKey(index)) {
            return acceptorHBonds.get(index);
        } else {
            return new ArrayList<>();
        }
    }
    
    public void addDonatedBond(Integer fromResidue, Integer toResidue, double energy) {
        List<HBond> hbonds;
        if (donatorHBonds.containsKey(fromResidue)) {
            hbonds = donatorHBonds.get(fromResidue);
        } else {
            hbonds = new ArrayList<>();
            donatorHBonds.put(fromResidue, hbonds);
        }
        hbonds.add(new HBond(fromResidue, toResidue, energy));
    }

    public void addAcceptedBond(Integer fromResidue, Integer toResidue, double energy) {
        List<HBond> hbonds;
        if (acceptorHBonds.containsKey(toResidue)) {
            hbonds = acceptorHBonds.get(toResidue);
        } else {
            hbonds = new ArrayList<>();
            acceptorHBonds.put(toResidue, hbonds);
        }
        hbonds.add(new HBond(fromResidue, toResidue, energy));
    }

    public List<SSE> range(SSE sseFrom, SSE sseTo) {
        // Provide a list view of sses from sseFrom (included) to sseTo (included). //
        int startIndex = this.sses.indexOf(sseFrom);
        int endIndex = this.sses.indexOf(sseTo) + 1;
        return this.sses.subList(startIndex, endIndex);
    }

    /**
     * Get a sublist of the SSEs including the start point.
     * 
     * @param sseFrom
     * @return
     */
    public List<SSE> rangeFrom(SSE sseFrom) {
        int startIndex = this.sses.indexOf(sseFrom);
        return this.sses.subList(startIndex, this.sses.size());
    }
    
    public List<SSE> iterFixed(SSE sseStart) {
        List<SSE> fixed = new ArrayList<>();
        SSE sse = sseStart;
        while (sse != null) {
            if (sse.hasFixed()) fixed.add(sse.getFixed());
            sse = sse.getFixed();
        }
        return fixed;
    }
    
    public List<SSE> iterFixedInclusive(SSE sseStart) {
        List<SSE> fixed = new ArrayList<>();
        // XXX TODO!
        return fixed;
    }

    public List<SSE> iterNext(SSE sseStart) {
        return sses.subList(sses.indexOf(sseStart), sses.size());
    }

    public int sequenceLength() {
        return this.sequence.size();
    }
    
    public boolean isExtended(int i) {
        if (i < 0 || i >= this.sequenceLength()) return false;
        return this.secondaryStruc.get(i) == SSEType.EXTENDED;
    }

    private List<SSEType> helixTypes = new ArrayList<SSEType>() {{
        add(SSEType.HELIX);
        add(SSEType.RIGHT_ALPHA_HELIX); 
        add(SSEType.HELIX_310); 
        add(SSEType.PI_HELIX); 
        add(SSEType.LEFT_ALPHA_HELIX);
    }};
    
    public boolean isGenHelix(int i) {
        if (i < 0 || i >= this.sequenceLength()) return false;
        return helixTypes.contains(this.secondaryStruc.get(i));
    }

    public boolean isSSelement(int i) {
        if (i < 0 || i >= this.sequenceLength()) return false;
        return this.isGenHelix(i) || this.isExtended(i);
    }


    public List<SSE> listBPGroup(SSE p, List<SSE> sseList) {
        List<SSE> groupList = new ArrayList<>();
        groupList.add(p);
        for (SSE q : sseList){
            if (p.getFirstCommonBP(q) != null) {
                groupList.add(p);
            }
        }
        return groupList;
    }
 
    //
    // a chain method
    // alternatively, an sse method 'separation(this, other)'
    // where you would call:
    //
    public SSE closestInFixed(SSE fixedStart, SSE p) {
        SSE closest = null;

        double minsep = Double.MIN_VALUE;
        for (SSE q : this.iterFixed(fixedStart)) {
            double sep = DistanceCalculator.secStrucSeparation(p, q);
            if (sep < minsep) {
                minsep = sep;
                closest = q;
            }
        }
        return closest;
    }

    public SSE longestInFixed(SSE fixedStart) {
        SSE longest = null;

        int maxlen = 0;
        for (SSE p : this.iterFixed(fixedStart)) {
            int len = p.secStrucLength();
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
        Function to calculate separation of two secondary structure vectors
        DW added (cut from CalculateNeighbours for more general use) : 23/7/96
    */
    public boolean parallel(SSE p, SSE q) {
//        pk, pj, sk, sj, torsion = p.ClosestApproach(q);
        TorsionResult values = p.closestApproach(q);
        return Math.abs(values.getTorsion()) < 90.0;
    }

    public String getEdgeString() {
        StringBuilder edges = new StringBuilder();
        for (Bridge bridge : bridges) {
            edges.append(toString(bridge));
        }
        return edges.toString();
    }
    
    private String toString(Bridge bridge) {
        return bridge.getSseStart().getSymbolNumber() + ":"
                + bridge.getSseEnd().getSymbolNumber()
                + bridge.getType();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (SSE sse : this.sses) {
            str.append(sse.toString());
        }
        return this.name + " " + str.toString() + " " + this.getEdgeString();
    }
    
    /*
     * Following methods moved from Cartoon...
     */

    public double fixedSpan(SSE p, double gridUnitSize) {
        double minx = 0;
        double maxx = 0;
        SSE q = this.findFixedStart(p);
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
        if (gridUnitSize <= 0) {
            span = (maxx - minx) / 1;
        } else {
            span = (maxx - minx) / gridUnitSize;
        }
        span += 1;
        return span;
    }

    public void reflectFixedXY(SSE p) {
        for (SSE q : this.iterFixed(p)) {
            q.flip();
        }
    }
    

    /**
        function to flip all symbols in a fixed list AND CENTER!
    **/
    public void flipSymbols(SSE p) {
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

    public List<SSE> getListAtXPosition(SSE startSSE, double XPosition) {
        List<SSE> filtered = new ArrayList<SSE>();
        for (SSE p : this.iterFixed(startSSE)) {
            if (p.getCartoonX() == XPosition) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<SSE> getListAtPosition(SSE startSSE, double XPosition, double YPosition) {
        List<SSE> filtered = new ArrayList<SSE>();
        for (SSE p : this.iterFixed(startSSE)) {
            if (p.getCartoonX() == XPosition && p.getCartoonY() == YPosition) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public double leftMostPos(SSE p) {
        double leftMost = p.getCartoonX();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() < leftMost) leftMost = q.getCartoonX();
        }
        return leftMost;
    }

    public SSE leftMost(SSE p) {
        double leftMost = p.getCartoonX();
        SSE leftMostSymbol = p;
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() < leftMost) {
                leftMost = p.getCartoonX();
                leftMostSymbol = q;
            }
        }
        return leftMostSymbol;
    }
    
    public double rightMostPos(SSE p) {
        double rightMost = p.getCartoonX();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() > rightMost) {
                rightMost = q.getCartoonX();
            }
        }
        return rightMost;
    }
   

    public SSE rightMost(SSE p) {
        double rightMost = p.getCartoonX();
        SSE rightMostSymbol = p;
        for (SSE q : this.iterFixed(p)) {
            if (p.getCartoonX() < rightMost) {
                rightMost = p.getCartoonX();
                rightMostSymbol = q;
            }
        }
        return rightMostSymbol;
    }

    public double lowestPos(SSE p) {
        double bottomMost = p.getCartoonY();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonY() < bottomMost) {
                bottomMost = q.getCartoonX();
            }
        }
        return bottomMost;
    }

    public double highestPos(SSE p) {
        double topMost = p.getCartoonY();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonY() > topMost) {
                topMost = q.getCartoonX();
            }
        }
        return topMost;
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

    public SSE getLast(SSE sse) {
        int index = sses.indexOf(sse);
        return (index == 0)? null : sses.get(index - 1);
    }

    public SSE getNext(SSE sse) {
        int index = sses.indexOf(sse);
        return (index == sses.size() - 1)? null : sses.get(index + 1);
    }
}

    