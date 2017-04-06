package tops.port.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.vecmath.Point2d;

public class SSE {
    
    private CartoonSymbol cartoonSymbol;
    
    private char Direction;
    private SSEType type;                       // H, E, N, C, D 
    private List<BridgePartner> bridgePartners;    // Bridge partners
    
    public int DomainBreakNumber;             // Number identifying possible domain breaks (0 -> No break) 
    public DomainBreakType domainBreakType;                // Number identifying domain break type (Nterm, Cterm or both)
    
    public Hand Chirality;                      // Local structure hand 
    public Axis axis;
    private FixedType fixedType;
    
    public SSEData sseData;
    
    // XXX TODO Gah, upwards pointers!
    
    private SSE fixed;
    private List<Neighbour> neighbours;


    public SSE(SSEType SSEType) {
        this.type = SSEType;                  
        this.bridgePartners = new ArrayList<BridgePartner>();   
        this.neighbours = new ArrayList<Neighbour>();                    
        this.axis = null;
        this.cartoonSymbol = new CartoonSymbol();
        this.sseData = new SSEData();
    }
    
    /**
     * TODO : copy constructor
     */
    public SSE(SSE other) {
        this.type = other.type;
        // TODO...
        this.bridgePartners = new ArrayList<BridgePartner>();
        this.cartoonSymbol = other.cartoonSymbol;
    }
    
    public String getSummary() {
        return getSSEType().getOneLetterName() + getSymbolNumber() + " (" 
                + sseData.PDBStartResidue + ", " + sseData.PDBFinishResidue + ")";  
    }
    
    public boolean contains(int residue) {
        return (residue >= sseData.PDBStartResidue 
             && residue <= sseData.PDBFinishResidue);
    }
    
    public void setStartPoints(int seqStart, int pdbStart) {
        sseData.SeqStartResidue = seqStart;
        sseData.PDBStartResidue = pdbStart;
    }
    
    public void setFinishPoints(int seqEnd, int pdbEnd) {
        sseData.SeqFinishResidue = seqEnd;
        sseData.PDBFinishResidue = pdbEnd;
    }
    
    public List<Neighbour> getNeighbours() {
        return this.neighbours;
    }
    
    public void setFixed(SSE fixed) {
        this.fixed = fixed;
    }
    
    public SSE getFixed() {
        return this.fixed;
    }
    
    public boolean hasFixed() {
        return fixed != null;
    }
    
    public void setFixedType(FixedType fixedType) {
        this.fixedType = fixedType;
    }
    
    /**
     * @return true if the SSE has any of these types
     */
    public boolean hasFixedType(FixedType... types) {
        for (FixedType type : types) {
            if (fixedType == type) {
                return true;
            }
        }
        return false;
    }
    
    public void addBridgePartner(BridgePartner bridgePartner) {
        this.bridgePartners.add(bridgePartner);
    }
    
    public void addConnection(Point2d point) {
        this.cartoonSymbol.addConnectionTo(point);
    }
    
    public void setSymbolNumber(int symbolNumber) {
        this.cartoonSymbol.setSymbolNumber(symbolNumber);
    }
    
    
    public int getSymbolNumber() {
        return cartoonSymbol.getSymbolNumber();
    }
    
    public Point2d getCartoonCenter() {
        // XXX note we are making a new instance here, as some methods alter the values  
        return new Point2d(cartoonSymbol.getCartoonX(), cartoonSymbol.getCartoonY());
    }
    
    public int getCartoonX() {
        return (int) cartoonSymbol.getCartoonX();
    }
    
    public void setCartoonX(int x) {
        cartoonSymbol.setCartoonX(x);
    }
    
    public void setPosition(int x, int y) {
        cartoonSymbol.setCartoonX(x);
        cartoonSymbol.setCartoonY(y);
    }

    public int getCartoonY() {
        return (int) cartoonSymbol.getCartoonY();
    }

    public void setCartoonY(int cartoonY) {
        cartoonSymbol.setCartoonY(cartoonY);
    }
    
    public BridgePartner getBridgePartner(int index) {
        return this.bridgePartners.get(index);
    }
    
    public List<BridgePartner> getBridgePartners() {
        return this.bridgePartners;
    }
    
    public void sortBridgePartners() {
        Collections.sort(this.bridgePartners);
    }
    
    public static SSE getCommonBP(SSE Start, List<SSE> CurrentList) {
        for (int i = 0; i < Start.bridgePartners.size(); i++) {
            BridgePartner commonBridgePartner = Start.bridgePartners.get(i);
            boolean IsCommonBP = true;
            for (int j = 1; j < CurrentList.size(); j++) {
                if (!commonBridgePartner.partner.hasBridgePartner(CurrentList.get(j))) {
                    IsCommonBP = false;
                    break;
                }
            }
            if (IsCommonBP) {
                return commonBridgePartner.partner;
            }
        }
        return null;
    }
    
    public List<SSE> getPartners() {
        List<SSE> sses = new ArrayList<SSE>();
        for (BridgePartner bp : bridgePartners) {
            sses.add(bp.partner);
        }
        return sses;
    }
    
   

    public SSEType getSSEType() {
        return this.type;
    }

    public boolean isStrand() {
        return this.type == SSEType.EXTENDED;
    }
    
    public boolean isHelix() {
        return this.type == SSEType.HELIX || 
                this.type == SSEType.HELIX_310 ||
                        this.type == SSEType.PI_HELIX;
    }
    
    public boolean isSameType(SSE other) {
        return this.type == other.type;
    }
    
    public char getDirection() {
        return this.Direction;
    }
    
    public void setDirection(char direction) {
        this.Direction = direction;
    }

    public boolean isTerminus() {
        return this.type == SSEType.NTERMINUS || this.type == SSEType.CTERMINUS;
    }
    
    public boolean isParallel(SSE other) {
        if (this.axis == null || other.axis == null) {
            return false;   // XXX this probably should not be necessary?
        } else {
            return Math.abs(ClosestApproach(other).torsion) > 90;
        }
    }

    public TorsionResult ClosestApproach(SSE other) {
        return this.axis.ClosestApproach(other.axis);
    }

    public double axisTorsion(SSE other) {
        TorsionResult values = this.ClosestApproach(other);
        return values.torsion;
    }

    /*
    assigns a direction to this depending on other
    */
    public void AssignRelDirection(SSE other) {

        BridgeType ConnectType = BridgeType.UNK_BRIDGE_TYPE;

        // is this a bridge partner of other with a defined connection type
        for (BridgePartner partner : other.bridgePartners) {   // TODO XXX
            if (partner.partner == this) {
                ConnectType = partner.bridgeType;
                break;
            }
        }

        if (ConnectType == BridgeType.PARALLEL_BRIDGE) {
            this.Direction = (other.Direction == 'U') ? 'U' : 'D';
        } else if (ConnectType == BridgeType.ANTI_PARALLEL_BRIDGE) {
            this.Direction = (other.Direction == 'U') ? 'D' : 'U';
        } else {
            if (Math.abs(this.axisTorsion(other)) > 90.0) {
                this.Direction = (other.Direction == 'U') ? 'D' : 'U';
            } else {
                this.Direction = (other.Direction == 'U') ? 'U' : 'D';
            }
        }
    }

    // XXX TODO FIXME - this only works for non-forked sheets!!
    public SSE NextStrand(SSE other) {
        System.out.println("next strand of " + this + " and " + other);
        if (this.bridgePartners.get(1).partner == other) {
            return this.bridgePartners.get(0).partner;
        } else {
            return this.bridgePartners.get(1).partner;
        }
    }

    public void swapBridgePartners(int a, int b) {
        BridgePartner tmp = this.bridgePartners.get(a);
        this.bridgePartners.set(a, this.bridgePartners.get(b));
        this.bridgePartners.set(b, tmp);
    }


//    public void ShuffleDownNeighbours() {
//        int MAXNB = this.Neighbours.size();
//        for (int i = 0; i < MAXNB; i++) {
//            if (this.Neighbours.get(i) == null) {
//                int j = i;
//                while ((j < MAXNB) && (this.Neighbours.get(j) == null)) j += 1;
//                if (j < MAXNB) {
//                    this.MoveNeighbour(j, i);
//                }
//            }
//        }
//    }


//    private void MoveNeighbour(int j, int i) {
//
//        if ((i < 0) || (i >= MAXNB)) return;
//        if ((j < 0) || (j >= MAXNB)) return;
//
//        this.Neighbour[i] = this.Neighbour[j];
//        this.NeighbourDistance[i] = this.NeighbourDistance[j];
//
//        this.Neighbour[j] = null;
//        this.NeighbourDistance[j] = -1.0;
//    }

    public void RemoveBP(BridgePartner bp) {
        this.bridgePartners.remove(bp);
    }


    public BridgePartner findBridgePartner(SSE sse) {
        for (BridgePartner bridgePartner : this.bridgePartners) {
            if (bridgePartner.partner == sse) return bridgePartner;
        }
        return null;
    }

    public int SecStrucLength() {
        return this.sseData.PDBFinishResidue - this.sseData.PDBStartResidue + 1;
    }

    public SSE getFirstCommonBP(SSE other) {
        for (BridgePartner bridgePartner : this.bridgePartners) {
            for (BridgePartner otherBridgePartner : other.bridgePartners) {
                if (bridgePartner.partner == otherBridgePartner.partner) {
                    return bridgePartner.partner;
                }
            }
        }
        return null;
    }

    public boolean sameBPSide(SSE other) {
        SSE commonBP = this.getFirstCommonBP(other);
        if (commonBP == null) return false;
        return this.bridgePartnerSide(commonBP) == other.bridgePartnerSide(commonBP);
    }

    public String bridgePartnerSide(SSE sse) {
        if (sse == null)  return "";
        for (BridgePartner other : sse.bridgePartners) {
            if (other.partner == this) return other.side.name();    // XXX is name correct here?
        }
        return "";
    }

    public int NumberPartners() {
        return this.bridgePartners.size();
    }

    public double separation(SSE q) {
        for (int i = 0; i < this.neighbours.size(); i++) {
            Neighbour neighbour = this.neighbours.get(i); 
            if (neighbour.sse == q) {
                return neighbour.distance;
            }
        } 
        return -1;
    }

    public BridgePartner longestBridgeRange() {
        double maxlen = -1.0;
        BridgePartner longest = null;
        for (BridgePartner bridgePartner : this.bridgePartners) {
            int len = bridgePartner.getBridgeLength();
            if (len > maxlen) {
                maxlen = len;
                longest = bridgePartner;
            }
        }
        return longest;
    }

    public boolean hasBPonList(List<SSE> sseList) {
        for (BridgePartner q : this.bridgePartners) {
            if (sseList.contains(q.partner)) return true;   // XXX is this right?
        }
        return false;
    }

    public FixedType getFixedType() {
        return this.fixedType;
    }

    public void setFixedType(String fixedTypeName) {
        this.fixedType = FixedType.valueOf(fixedTypeName);
    }

    public String BridgePartnerNumbers() {
        StringBuffer numbers = new StringBuffer();
        for (BridgePartner partner : this.bridgePartners) {
            if (partner == null) break;
            numbers.append(String.valueOf(partner.partner.getSymbolNumber())).append(" ");
        }
        return numbers.toString();
    }
    
    public void addNeighbour(SSE sse, int distance) {
        this.neighbours.add(new Neighbour(sse, distance));
    }

    public String NeighbourNumbers() {
        StringBuffer numbers = new StringBuffer();
        for (Neighbour neighbour : this.neighbours) {
            if (neighbour == null) break;
            numbers.append(neighbour.sse.getSymbolNumber());
            numbers.append(" ");
        }
        return numbers.toString();
    }

    public String BridgePartnerSides() {
        StringBuffer sides  = new StringBuffer();
        for (BridgePartner partner : this.bridgePartners) {
            if (partner.side == BridgePartner.Side.UNKNOWN) break;
            sides.append(partner.side.toString().charAt(0));    // XXX ugh!
            sides.append(" ");
        }
        return sides.toString();
    }

    public String BridgePartnerTypes() {
        StringBuffer types = new StringBuffer();
        for (BridgePartner partner : this.bridgePartners ) {
            if (partner.bridgeType == BridgeType.UNK_BRIDGE_TYPE) {
                break;
            } else if (partner.bridgeType == BridgeType.ANTI_PARALLEL_BRIDGE) {
                types.append('A');
            } else if (partner.bridgeType == BridgeType.PARALLEL_BRIDGE) {
                types.append('P');
            }
            types.append(" ");
        }
        return types.toString();
    }

    public String Connections() {
        StringBuffer connections = new StringBuffer();
        for (Point2d connection : this.cartoonSymbol.getConnectionsTo()) {
            connections.append(String.format("%0.2f %0.2f ", connection.x, connection.y));
        }
        return connections.toString();
    }

    public String getSymbolNumber(SSE sse) {
        if (sse == null) {
            return "-1";
        } else {
            return String.valueOf(sse.getSymbolNumber());
        }
    }

    public String getLabel() {
        return cartoonSymbol.getLabel();
    }
//
//    public String getChain() {
//        if (this.Chain == ' ') return "";
//        else return String.valueOf(this.Chain);
//    }

    public String toTopsFile(Chain chain) {
        //sys.stderr.write(this.dump())
        StringBuffer stringRepr = new StringBuffer();
        stringRepr.append(String.format("%s %s\n", "SecondaryStructureType", this.type));
        stringRepr.append(String.format("%s %s\n", "Direction", this.Direction));
        stringRepr.append(String.format("%s %s\n", "Label", this.getLabel()));
        stringRepr.append(String.format("%s %s %s %s\n", "Colour", cartoonSymbol.getColor()[0], cartoonSymbol.getColor()[1], cartoonSymbol.getColor()[2]));
        stringRepr.append(String.format("%s %s\n", "Next", -1));    // blank next
        stringRepr.append(String.format("%s %s\n", "Fixed", this.getSymbolNumber(this.fixed)));
        stringRepr.append(String.format("%s %s\n", "FixedType", this.getFixedType()));
        stringRepr.append(String.format("%s %s\n", "BridgePartner", this.BridgePartnerNumbers()));
        stringRepr.append(String.format("%s %s\n", "BridgePartnerSide", this.BridgePartnerSides()));
        stringRepr.append(String.format("%s %s\n", "BridgePartnerType", this.BridgePartnerTypes()));
        stringRepr.append(String.format("%s %s\n", "Neighbour", this.NeighbourNumbers()));
        stringRepr.append(String.format("%s %s\n", "SeqStartResidue", this.sseData.SeqStartResidue));
        stringRepr.append(String.format("%s %s\n", "SeqFinishResidue", this.sseData.SeqFinishResidue));
        stringRepr.append(String.format("%s %s\n", "PDBStartResidue", this.sseData.PDBStartResidue));
        stringRepr.append(String.format("%s %s\n", "PDBFinishResidue", this.sseData.PDBFinishResidue));
        stringRepr.append(String.format("%s %s\n", "SymbolNumber", this.getSymbolNumber()));
        stringRepr.append(String.format("%s %s\n", "Chain", chain.getName()));
        stringRepr.append(String.format("%s %s\n", "Chirality", chiralToString(this.Chirality)));
        stringRepr.append(String.format("%s %s\n", "CartoonX", this.getCartoonX()));
        stringRepr.append(String.format("%s %s\n", "CartoonY", this.getCartoonY()));
        stringRepr.append(this.AxisRepr());
        stringRepr.append(String.format("%s %s\n", "SymbolRadius", getRadius()));
        stringRepr.append(String.format("%s %s\n", "AxisLength", this.axis.getLength()));
        stringRepr.append(String.format("%s %s\n", "NConnectionPoints", this.getNConnectionPoints()));
        stringRepr.append(String.format("%s %s\n", "ConnectionTo", this.Connections()));
        stringRepr.append(String.format("%s %s\n", "Fill", (this.cartoonSymbol.getFill()? "1" : "0")));
        return stringRepr.toString();
    }
    
    public int getRadius() {
        return cartoonSymbol.getSymbolRadius() > 1?  (int)cartoonSymbol.getSymbolRadius() : 10;
    }
    
    private String chiralToString(Hand chirality) {
        return (chirality == Hand.NONE)? "0" : 
            (chirality == Hand.LEFT? "1" : "-1");
    }

    private String AxisRepr() { // XXX see toTopsFile() above
        return String.format("AxesStartPoint %s %s %s\n", 0.0, 0.0, 0.0) + 
                String.format("AxesFinishPoint %s %s %s\n", 0.0, 0.0, 0.0);
    }

    public String toString() {
        if (this.Direction == 'U' || (this.type == SSEType.NTERMINUS || this.type == SSEType.CTERMINUS)) {
            return String.valueOf(this.type.getOneLetterName());
        } else {   
            return String.valueOf(this.type.getOneLetterName()).toLowerCase();
        }
    }

    public void flip() {
        // TODO Auto-generated method stub
        
    }
    
    public boolean IsInCircle(Chain.Circle circle) {
        // TODO
        return false;
    }

    public boolean IsInCircle(double centerX, double centerY, double radius) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasBridgePartner(SSE p) {
        for (BridgePartner partner : this.bridgePartners) {
            if (partner.partner == p) {
                return true;
            }
        }
        return false;
    }
    
    public void removeBridgePartner(SSE targetPartner) {
        BridgePartner toRemove = null;
        for (BridgePartner bp : this.bridgePartners) {
            if (bp.partner == targetPartner) {
                toRemove = bp;
                break;
            }
        }
        if (toRemove != null) {
            this.bridgePartners.remove(toRemove);
        }
    }

    /*
     * function update_secstr
     * 
     * Tom F. August 1992
     * 
     * Function to update link list of secondary structures to include new pair
     */
    public void updateSecStr(SSE partner, int residueNumber, BridgePartner.Side side, BridgeType bridgeType) {
        BridgePartner commonBridgePartner = null;
        for (BridgePartner bridgePartner : this.bridgePartners) {
            if (bridgePartner.partner == partner) {
                commonBridgePartner = bridgePartner;
                break;
            }
        }
        if (commonBridgePartner == null) {
            this.bridgePartners.add(new BridgePartner(partner, residueNumber, bridgeType, side));
        } else {
            commonBridgePartner.update(residueNumber);
        }
    }

    public void setLabel(String label) {
        cartoonSymbol.setLabel(label);
    }

    public boolean getFill() {
        return cartoonSymbol.getFill();
    }

    public double getSymbolRadius() {
        return cartoonSymbol.getSymbolRadius();
    }
   
    
    private void print(PrintStream out, String s, Object... args) {
        out.print(String.format(s, args));
    }

    public void setSymbolPlaced(boolean b) {
        cartoonSymbol.setSymbolPlaced(b);
    }

    public boolean isSymbolPlaced() {
        return cartoonSymbol.isSymbolPlaced();
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }
    
    public boolean equals(Object other) {
        if (other instanceof SSE) {
            SSE o = (SSE) other;
            // TODO very much TODO
            return this.cartoonSymbol == o.cartoonSymbol;
        }
        return false;
    }

    public SSEData getSseData() {
        return sseData;
    }

    public Hand getChirality() {
        return Chirality;
    }

    public Axis getAxis() {
        return axis;
    }

    public CartoonSymbol getCartoonSymbol() {
        return cartoonSymbol;
    }

    public int getNConnectionPoints() {
        return cartoonSymbol.getConnectionsTo().size();
    }

    public char getChain() {
        //XXX TODO REMOVE!!
        return 0;
    }

    public Point2d getConnectionTo(int i) {
        return cartoonSymbol.getConnectionsTo().get(i);
    }
    
}
