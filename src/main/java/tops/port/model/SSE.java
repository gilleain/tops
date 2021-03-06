package tops.port.model;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UP;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2d;

public class SSE {
    
    private CartoonSymbol cartoonSymbol;
    
    private Direction direction;
    private SSEType type;                       // H, E, N, C, D 
    private List<BridgePartner> bridgePartners;    // Bridge partners
    
    public int domainBreakNumber;             // Number identifying possible domain breaks (0 -> No break) 
    public DomainBreakType domainBreakType;                // Number identifying domain break type (Nterm, Cterm or both)
    
    private Hand chirality;                      // Local structure hand 
    public Axis axis;
    private FixedType fixedType;
    
    public SSEData sseData;
    
    // XXX TODO Gah, upwards pointers!
    
    private SSE fixed;
    private List<Neighbour> neighbours;


    public SSE(SSEType type) {
        this.type = type;                  
        this.bridgePartners = new ArrayList<>();   
        this.neighbours = new ArrayList<>();                    
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
        this.bridgePartners = new ArrayList<>();
        this.cartoonSymbol = other.cartoonSymbol;
    }
    
    public String getSummary() {
        return getSSEType().getOneLetterName() + getSymbolNumber() + " (" 
                + sseData.pdbStartResidue + ", " + sseData.pdbFinishResidue + ")";  
    }
    
    public boolean contains(int residue) {
        return (residue >= sseData.pdbStartResidue 
             && residue <= sseData.pdbFinishResidue);
    }
    
    public void setStartPoints(int seqStart, int pdbStart) {
        sseData.seqStartResidue = seqStart;
        sseData.pdbStartResidue = pdbStart;
    }
    
    public void setFinishPoints(int seqEnd, int pdbEnd) {
        sseData.seqFinishResidue = seqEnd;
        sseData.pdbFinishResidue = pdbEnd;
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
        for (FixedType otherFixedType : types) {
            if (this.fixedType == otherFixedType) {
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
    
   
    
    public List<SSE> getPartners() {
        List<SSE> sses = new ArrayList<>();
        for (BridgePartner bp : bridgePartners) {
            sses.add(bp.getPartner());
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
    
    public Direction getDirection() {
        return this.direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isTerminus() {
        return this.type == SSEType.NTERMINUS || this.type == SSEType.CTERMINUS;
    }
    
    public boolean isParallel(SSE other) {
        if (this.axis == null || other.axis == null) {
            return false;   // XXX this probably should not be necessary?
        } else {
            return Math.abs(closestApproach(other).getTorsion()) > 90;
        }
    }

    public TorsionResult closestApproach(SSE other) {
        return this.axis.closestApproach(other.axis);
    }

    public double axisTorsion(SSE other) {
        TorsionResult values = this.closestApproach(other);
        return values.getTorsion();
    }

    /*
    assigns a direction to this depending on other
    */
    public void assignRelativeDirection(SSE other) {

        BridgeType connectionType = BridgeType.UNK_BRIDGE_TYPE;

        // is this a bridge partner of other with a defined connection type
        for (BridgePartner partner : other.bridgePartners) {   // TODO XXX
            if (partner.getPartner() == this) {
                connectionType = partner.getBridgeType();
                break;
            }
        }

        if (connectionType == BridgeType.PARALLEL_BRIDGE) {
            this.direction = (other.direction == UP) ? UP : DOWN;
        } else if (connectionType == BridgeType.ANTI_PARALLEL_BRIDGE) {
            this.direction = (other.direction == UP) ? DOWN : UP;
        } else {
            if (Math.abs(this.axisTorsion(other)) > 90.0) {
                this.direction = (other.direction == UP) ? DOWN : UP;
            } else {
                this.direction = (other.direction == UP) ? UP : DOWN;
            }
        }
    }

    // XXX TODO FIXME - this only works for non-forked sheets!!
    public SSE nextStrand(SSE other) {
        System.out.println("next strand of " + this + " and " + other);
        if (this.bridgePartners.get(1).getPartner() == other) {
            return this.bridgePartners.get(0).getPartner();
        } else {
            return this.bridgePartners.get(1).getPartner();
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

    public void removeBP(BridgePartner bp) {
        this.bridgePartners.remove(bp);
    }


    public BridgePartner findBridgePartner(SSE sse) {
        for (BridgePartner bridgePartner : this.bridgePartners) {
            if (bridgePartner.getPartner() == sse) return bridgePartner;
        }
        return null;
    }

    public int secStrucLength() {
        return this.sseData.pdbFinishResidue - this.sseData.pdbStartResidue + 1;
    }

    public SSE getFirstCommonBP(SSE other) {
        for (BridgePartner bridgePartner : this.bridgePartners) {
            for (BridgePartner otherBridgePartner : other.bridgePartners) {
                if (bridgePartner.getPartner() == otherBridgePartner.getPartner()) {
                    return bridgePartner.getPartner();
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
            if (other.getPartner() == this) return other.getSide().name();    // XXX is name correct here?
        }
        return "";
    }

    public int numberPartners() {
        return this.bridgePartners.size();
    }

    public double separation(SSE q) {
        for (int i = 0; i < this.neighbours.size(); i++) {
            Neighbour neighbour = this.neighbours.get(i); 
            if (neighbour.getSse() == q) {
                return neighbour.getDistance();
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
            if (sseList.contains(q.getPartner())) return true;   // XXX is this right?
        }
        return false;
    }

    public FixedType getFixedType() {
        return this.fixedType;
    }

    public void setFixedType(String fixedTypeName) {
        this.fixedType = FixedType.valueOf(fixedTypeName);
    }

    public String bridgePartnerNumbers() {
        StringBuilder numbers = new StringBuilder();
        for (BridgePartner partner : this.bridgePartners) {
            if (partner == null) break;
            numbers.append(String.valueOf(partner.getPartner().getSymbolNumber())).append(" ");
        }
        return numbers.toString();
    }
    
    public void addNeighbour(SSE sse, int distance) {
        this.neighbours.add(new Neighbour(sse, distance));
    }

    public String neighbourNumbers() {
        StringBuilder numbers = new StringBuilder();
        for (Neighbour neighbour : this.neighbours) {
            if (neighbour == null) break;
            numbers.append(neighbour.getSse().getSymbolNumber());
            numbers.append(" ");
        }
        return numbers.toString();
    }

    public String bridgePartnerSides() {
        StringBuilder sides  = new StringBuilder();
        for (BridgePartner partner : this.bridgePartners) {
            if (partner.getSide() == BridgePartner.Side.UNKNOWN) break;
            sides.append(partner.getSide().toString().charAt(0));    // XXX ugh!
            sides.append(" ");
        }
        return sides.toString();
    }

    public String bridgePartnerTypes() {
        StringBuilder types = new StringBuilder();
        for (BridgePartner partner : this.bridgePartners ) {
            if (partner.getBridgeType() == BridgeType.UNK_BRIDGE_TYPE) {
                break;
            } else if (partner.getBridgeType() == BridgeType.ANTI_PARALLEL_BRIDGE) {
                types.append('A');
            } else if (partner.getBridgeType() == BridgeType.PARALLEL_BRIDGE) {
                types.append('P');
            }
            types.append(" ");
        }
        return types.toString();
    }

    public String connections() {
        StringBuilder connections = new StringBuilder();
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

    public String toTopsFile(Chain chain) {
        //sys.stderr.write(this.dump())
        StringBuilder stringRepr = new StringBuilder();
        stringRepr.append(String.format("%s %s%n", "SecondaryStructureType", this.type));
        stringRepr.append(String.format("%s %s%n", "Direction", this.direction));
        stringRepr.append(String.format("%s %s%n", "Label", this.getLabel()));
        stringRepr.append(String.format("%s %s %s %s%n", "Colour", 
                cartoonSymbol.getColor().getRed(), 
                cartoonSymbol.getColor().getGreen(), 
                cartoonSymbol.getColor().getBlue()));
        stringRepr.append(String.format("%s %s%n", "Next", -1));    // blank next
        stringRepr.append(String.format("%s %s%n", "Fixed", this.getSymbolNumber(this.fixed)));
        stringRepr.append(String.format("%s %s%n", "FixedType", this.getFixedType()));
        stringRepr.append(String.format("%s %s%n", "BridgePartner", this.bridgePartnerNumbers()));
        stringRepr.append(String.format("%s %s%n", "BridgePartnerSide", this.bridgePartnerSides()));
        stringRepr.append(String.format("%s %s%n", "BridgePartnerType", this.bridgePartnerTypes()));
        stringRepr.append(String.format("%s %s%n", "Neighbour", this.neighbourNumbers()));
        stringRepr.append(String.format("%s %s%n", "SeqStartResidue", this.sseData.seqStartResidue));
        stringRepr.append(String.format("%s %s%n", "SeqFinishResidue", this.sseData.seqFinishResidue));
        stringRepr.append(String.format("%s %s%n", "PDBStartResidue", this.sseData.pdbStartResidue));
        stringRepr.append(String.format("%s %s%n", "PDBFinishResidue", this.sseData.pdbFinishResidue));
        stringRepr.append(String.format("%s %s%n", "SymbolNumber", this.getSymbolNumber()));
        stringRepr.append(String.format("%s %s%n", "Chain", chain.getName()));
        stringRepr.append(String.format("%s %s%n", "Chirality", chiralToString(this.chirality)));
        stringRepr.append(String.format("%s %s%n", "CartoonX", this.getCartoonX()));
        stringRepr.append(String.format("%s %s%n", "CartoonY", this.getCartoonY()));
        stringRepr.append(this.axisRepr());
        stringRepr.append(String.format("%s %s%n", "SymbolRadius", getRadius()));
        stringRepr.append(String.format("%s %s%n", "AxisLength", this.axis.getLength()));
        stringRepr.append(String.format("%s %s%n", "NConnectionPoints", this.getNConnectionPoints()));
        stringRepr.append(String.format("%s %s%n", "ConnectionTo", this.connections()));
        stringRepr.append(String.format("%s %s%n", "Fill", (this.cartoonSymbol.getFill()? "1" : "0")));
        return stringRepr.toString();
    }
    
    public int getRadius() {
        return cartoonSymbol.getRadius() > 1?  (int)cartoonSymbol.getRadius() : 10;
    }
    
    private String chiralToString(Hand chirality) {
        return (chirality == Hand.NONE)? "0" : 
            (chirality == Hand.LEFT? "1" : "-1");
    }

    private String axisRepr() { // XXX see toTopsFile() above
        return String.format("AxesStartPoint %s %s %s%n", 0.0, 0.0, 0.0) + 
                String.format("AxesFinishPoint %s %s %s%n", 0.0, 0.0, 0.0);
    }

    public String toString() {
        if (this.direction == UP || (this.type == SSEType.NTERMINUS || this.type == SSEType.CTERMINUS)) {
            return String.valueOf(this.type.getOneLetterName());
        } else {   
            return String.valueOf(this.type.getOneLetterName()).toLowerCase();
        }
    }

    public void flip() {
        // TODO Auto-generated method stub
        
    }
    
    public boolean hasBridgePartner(SSE p) {
        for (BridgePartner partner : this.bridgePartners) {
            if (partner.getPartner() == p) {
                return true;
            }
        }
        return false;
    }
    
    public void removeBridgePartner(SSE targetPartner) {
        BridgePartner toRemove = null;
        for (BridgePartner bp : this.bridgePartners) {
            if (bp.getPartner() == targetPartner) {
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
            if (bridgePartner.getPartner() == partner) {
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
        return cartoonSymbol.getRadius();
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
        return chirality;
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

    public void setChirality(Hand chirality) {
        this.chirality = chirality;
    }
    
}
