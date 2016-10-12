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
import tops.port.model.SSE.SSEType;
import tops.port.model.tse.BaseTSE;

public class Chain {

    private char name;
    private List<SSE> sses;
    private List<BaseTSE> tses;
    private List<String> sequence;
    private List<Integer> pdbIndices;
    private List<SSEType> secondaryStruc;
    private Map<Integer, List<BridgePartner>> bridgePartners;
    private List<Point3d> CACoords;
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
        this.sses = new ArrayList<SSE>();
        this.tses = new ArrayList<BaseTSE>();
        this.sequence = new ArrayList<String>();
        this.pdbIndices = new ArrayList<Integer>();
        this.secondaryStruc = new ArrayList<SSEType>();
        this.bridgePartners = new HashMap<Integer, List<BridgePartner>>();
        this.bridges = new ArrayList<Bridge>();
        this.CACoords = new ArrayList<Point3d>();
        
        this.acceptorHBonds = new HashMap<Integer, List<HBond>>();
        this.donatorHBonds = new HashMap<Integer, List<HBond>>();
    }
    

    public List<Bridge> getBridges(SSE current) {
        List<Bridge> selected = new ArrayList<Bridge>();
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
    
    public void forceConsistent(Protein protein) {
        for (int i = 0; i < sequenceLength(); i++) {

            if ( isSSelement(i) ) {
                int start = i;
                SSEType thissstype = getSSEType(i);
                int Dom = protein.ResidueDomain(i);
                int DBreak = -1;
                int LastDom = -1;
                while ( isSSelement(i) && getSSEType(i) == thissstype ) {
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
    
    public List<Point3d> secondaryStructureAxis(int seqStartResidue, int seqFinishResidue) {
        List<Point3d> coords = new ArrayList<Point3d>();
        for (int index = seqStartResidue - 1; index < seqFinishResidue; index++) {
            coords.add(CACoords.get(index));
        }
        return coords;
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
    
    public void addBridge(Bridge bridge) {
        this.bridges.add(bridge);
    }
    
    public List<BridgePartner> getBridgePartner(int index) {
        return this.bridgePartners.containsKey(index)?
            this.bridgePartners.get(index) : new ArrayList<BridgePartner>();
    }
    
    public void addBridgePartner(int index, BridgePartner bridgePartner) {
        System.out.println("Adding bridge partner " + index + "  " + bridgePartner);
        List<BridgePartner> list;
        if (bridgePartners.containsKey(index)) {
            list = bridgePartners.get(index);
        } else {
            list = new ArrayList<BridgePartner>();
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

    public void resolveTo() {
        int i = 0;
        for (SSE sse : this.sses) {
            if (i + 1 < this.sses.size()) {
                sse.To = this.sses.get(i + 1);
            }
            i++;
        }
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
    sure this is not right!
    */
    public  void linkOver(SSE p) {
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
    public int numberLink(SSE p) {
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
                this.linkOver(q);
            }
        }
    }

    //
    // This function checks the integrity of the fixed list - 
    // returns an error (0) if a cycle is found
    //
    public boolean checkFixedList(SSE p) {
        int CheckLen = 0;
        for (SSE q : this.iterFixed(p)) {
            CheckLen += 1;
            int i = 0;
            for (SSE r : this.iterFixed(p)) {
//                System.out.println(String.format("checking fixed list %s %s %s", i, r, CheckLen));
                if (i > CheckLen) break;
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
            return new ArrayList<HBond>();
        }
    }
    
    public List<HBond> getAcceptedHBonds(int index) {
        if (acceptorHBonds.containsKey(index)) {
            return acceptorHBonds.get(index);
        } else {
            return new ArrayList<HBond>();
        }
    }
    
    public void addDonatedBond(Integer fromResidue, Integer toResidue, double energy) {
        List<HBond> hbonds;
        if (donatorHBonds.containsKey(fromResidue)) {
            hbonds = donatorHBonds.get(fromResidue);
        } else {
            hbonds = new ArrayList<HBond>();
            donatorHBonds.put(fromResidue, hbonds);
        }
        hbonds.add(new HBond(fromResidue, toResidue, energy));
    }

    public void addAcceptedBond(Integer fromResidue, Integer toResidue, double energy) {
        List<HBond> hbonds;
        if (acceptorHBonds.containsKey(toResidue)) {
            hbonds = acceptorHBonds.get(toResidue);
        } else {
            hbonds = new ArrayList<HBond>();
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
        List<SSE> groupList = new ArrayList<SSE>();
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
    public SSE closestInFixed(SSE FixedStart, SSE p) {
        SSE closest = null;

        double minsep = Double.MIN_VALUE;
        for (SSE q : this.iterFixed(FixedStart)) {
            double sep = DistanceCalculator.secStrucSeparation(p, q);
            if (sep < minsep) {
                minsep = sep;
                closest = q;
            }
        }
        return closest;
    }

    public SSE longestInFixed(SSE FixedStart) {
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
        Function to calculate separation of two secondary structure vectors
        DW added (cut from CalculateNeighbours for more general use) : 23/7/96
    */
    public boolean parallel(SSE p, SSE q) {
//        pk, pj, sk, sj, torsion = p.ClosestApproach(q);
        TorsionResult values = p.ClosestApproach(q);
        return Math.abs(values.torsion) < 90.0;
    }
        
    

    public String topsHeader(String proteinCode) {
        return String.format("DOMAIN_NUMBER %d %s%s%d", 0, proteinCode, this.name, 0);   //FIXME : domains!
    }

    public String toTopsFile() {
        StringBuffer sseStringList = new StringBuffer(); 
        for (SSE sse : this.sses) {
            sseStringList.append(sse.toTopsFile(this)).append("\n");
        }
        return sseStringList.toString();
    }

    public String getEdgeString() {
        StringBuffer edges = new StringBuffer();
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
        StringBuffer str = new StringBuffer();
        for (SSE sse : this.sses) {
            str.append(sse.toString());
        }
        return this.name + " " + str.toString() + " " + this.getEdgeString();
    }
    
    /*
     * Following methods moved from Cartoon...
     */
    

   

    public double fixedSpan(SSE p, double GridUnitSize) {
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
        if (GridUnitSize <= 0) {
            span = (maxx - minx) / 1;
        } else {
            span = (maxx - minx) / GridUnitSize;
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
        double LeftMost = p.getCartoonX();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() < LeftMost) LeftMost = q.getCartoonX();
        }
        return LeftMost;
    }

    public SSE leftMost(SSE p) {
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
    
    public double rightMostPos(SSE p) {
        double RightMost = p.getCartoonX();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonX() > RightMost) RightMost = q.getCartoonX();
        }
        return RightMost;
    }
   

    public SSE rightMost(SSE p) {
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

    public double lowestPos(SSE p) {
        double BottomMost = p.getCartoonY();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonY() < BottomMost) BottomMost = q.getCartoonX();
        }
        return BottomMost;
    }

    public double highestPos(SSE p) {
        double TopMost = p.getCartoonY();
        for (SSE q : this.iterFixed(p)) {
            if (q.getCartoonY() > TopMost) TopMost = q.getCartoonX();
        }
        return TopMost;
    }
    


    /**
    returns center and radius
    **/
    public Circle fixedBoundingCircle(SSE FixedStart) {

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

    public double angle(SSE p, SSE q, SSE r) {
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

    public void calculateConnections(double radius) {
        int ExtensionIndex = 0;
        double[] Extensions = new double[] { 2.7, 7.7, 12.7, 17.7, 22.7 };

        for (SSE sse : this.sses) {
            makeConnection(radius, sse, ExtensionIndex, Extensions);
        }
    }

    public double nextExtension(int ExtensionIndex, double[] Extensions) {
        if (ExtensionIndex >= Extensions.length) ExtensionIndex = 0;
        double extension = Extensions[ExtensionIndex];
        ExtensionIndex += 1;    // XXX incrementing a scope-local variable FIXME 
        return extension;
    }

    public void makeConnection(double radius, SSE sse, int ExtensionIndex, double[] Extensions) {
        double PSMALL = 0.001;
        if (sse.To == null) return;

        // these are the condition under which the code generates a bent connection,
        // rather than the usual straight line joining symbols 
        if (this.findFixedStart(sse) != this.findFixedStart(sse.To)) return;
        if (sse.hasFixedType(FixedType.SHEET) && !sse.To.hasFixedType(FixedType.SANDWICH)) return;
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
        
            double my = r.getCartoonY() + ( d1 * ( PSMALL * Math.abs(px - qx) + radius + nextExtension(ExtensionIndex, Extensions) ));
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

    