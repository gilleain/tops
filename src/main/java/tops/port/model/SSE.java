package tops.port.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point2d;

public class SSE {
    
    private char Direction;
    private char SSEType;                       // H, E, N, C, D 
    private List<BridgePartner> bridgePartners;    // Bridge partners
    
    private int Merges = 0;                     // The number of merges made in creation 
    private int[][] MergeRanges;            // The start and end of the Merged structures 
    public int DomainBreakNumber;             // Number identifying possible domain breaks (0 -> No break) 
    public DomainBreakType domainBreakType;                // Number identifying domain break type (Nterm, Cterm or both)
    
    public Hand Chirality;                      // Local structure hand 
    public Axis axis;
    private FixedType fixedType;
    
    // XXX TODO don't like these linked-list pointers, but have them for now...
    public SSE From;
    public SSE To;
    public SSE Next;        // difference from 'To'?
    
    // XXX TODO these need to be split off somewhere!
    private int NConnectionPoints;
    
    private CartoonSymbol cartoonSymbol;
    public SSEData sseData;
    
    // XXX TODO Gah, upwards pointers!
    public char Chain;
    
    private SSE fixed;
    private boolean Fill;
    public List<Neighbour> Neighbours;
    private List<Point2d> ConnectionTo;
    private Object AxisLength;


    public SSE(char SSEType) {
        this.SSEType = SSEType;                  
        this.bridgePartners = new ArrayList<BridgePartner>();   
        this.Neighbours = new ArrayList<Neighbour>();                    
        this.ConnectionTo = new ArrayList<Point2d>();
        this.MergeRanges = new int[10][];   // XXX FIXME  
        this.axis = null;
        this.cartoonSymbol = new CartoonSymbol();
        this.sseData = new SSEData();
    }
    
    /**
     * TODO : copy constructor
     */
    public SSE(SSE other) {
        
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
    
    public void incrementMerges() {
        this.Merges++;  // hmmm
    }
    
    public void addConnection(Point2d point) {
        this.NConnectionPoints++;   // TODO convert to getConnections().size()?
        this.ConnectionTo.add(point);
    }
    
    public void setSymbolNumber(int symbolNumber) {
        this.cartoonSymbol.setSymbolNumber(symbolNumber);
    }
    
    public int getSymbolNumber() {
        return cartoonSymbol.getSymbolNumber();
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
        this.bridgePartners.sort(new Comparator<BridgePartner>() {

            @Override
            public int compare(BridgePartner o1, BridgePartner o2) {
                return new Integer(o1.NumberBridgePartners).compareTo(
                        new Integer(o2.NumberBridgePartners));
            }
            
        });
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
    
    public void getEdgeString(StringBuffer edges) {
        for (BridgePartner bridgePartner : bridgePartners) {
            BridgeType edgeType = bridgePartner.bridgeType;
            if (sseData.SeqStartResidue < bridgePartner.partner.sseData.SeqStartResidue) {
                int partnerSymbolNumber = bridgePartner.partner.getSymbolNumber();
                char edgeTypeChar;
                if (edgeType == BridgeType.ANTI_PARALLEL_BRIDGE) edgeTypeChar = 'A';
                else if (edgeType == BridgeType.PARALLEL_BRIDGE) edgeTypeChar = 'P';
                else edgeTypeChar = '!';
                String edge = String.format("%i:%i%s", getSymbolNumber(), partnerSymbolNumber, edgeTypeChar);
                edges.append(edge).append(" ");
            }
        }
    }
    
    public List<SSE> getPartners() {
        List<SSE> sses = new ArrayList<SSE>();
        for (BridgePartner bp : bridgePartners) {
            sses.add(bp.partner);
        }
        return sses;
    }
    
   

    public char getSSEType() {
        return this.SSEType;
    }

    public boolean isStrand() {
        return this.SSEType == 'E';
    }
    
    public boolean isHelix() {
        return this.SSEType == 'H';
    }
    
    public boolean isSameType(SSE other) {
        return this.SSEType == other.SSEType;
    }
    
    public char getDirection() {
        return this.Direction;
    }
    
    public void setDirection(char direction) {
        this.Direction = direction;
    }

    public boolean isTerminus() {
        return this.SSEType == 'C' || this.SSEType == 'N';
    }
    
    public boolean isParallel(SSE other) {
        return Math.abs(ClosestApproach(other).torsion) > 90;
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

    public Hand Chiral3d(SSE other) {

        int a1s, a1f, a2s, a2f;
        if (this.Merges > 0) {
            a1s = this.MergeRanges[this.Merges - 1][0];
            a1f = this.MergeRanges[this.Merges - 1][1];
        } else {
            a1s = this.sseData.SeqStartResidue;
            a1f = this.sseData.SeqFinishResidue;
        }

        if (other.Merges > 0) {
            a2s = other.MergeRanges[0][0];
            a2f = other.MergeRanges[0][1];
        } else {
            a2s = other.sseData.SeqStartResidue;
            a2f = other.sseData.SeqFinishResidue;
        }

        return this.motifChirality(a1s, a1f, a2s, a2f);
    }

    //FIXME!! : link in the slidel code!
    public Hand motifChirality(int a, int b, int c, int d) { 
        return Hand._unk_hand;
    }

    public int FindBPIndex(SSE bp) {
        for (int i = 0; i < this.bridgePartners.size(); i++) {
            if (this.bridgePartners.get(i).partner == bp) return i;
        }
        return this.bridgePartners.size();
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
        for (int i = 0; i < this.Neighbours.size(); i++) {
            Neighbour neighbour = this.Neighbours.get(i); 
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
        this.Neighbours.add(new Neighbour(sse, distance));
    }

    public String NeighbourNumbers() {
        StringBuffer numbers = new StringBuffer();
        for (Neighbour neighbour : this.Neighbours) {
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
        for (Point2d connection : this.ConnectionTo) {
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

    public String getChain() {
        if (this.Chain == ' ') return "";
        else return String.valueOf(this.Chain);
    }

    public String toTopsFile() {
        //sys.stderr.write(this.dump())
        StringBuffer stringRepr = new StringBuffer();
        stringRepr.append(String.format("%s %s\n", "SecondaryStructureType", this.SSEType));
        stringRepr.append(String.format("%s %s\n", "Direction", this.Direction));
        stringRepr.append(String.format("%s %s\n", "Label", this.getLabel()));
        stringRepr.append(String.format("%s %s %s %s\n", "Colour", cartoonSymbol.getColor()[0], cartoonSymbol.getColor()[1], cartoonSymbol.getColor()[2]));
        stringRepr.append(String.format("%s %s\n", "Next", this.getSymbolNumber(this.Next)));
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
        stringRepr.append(String.format("%s %s\n", "Chain", this.getChain()));
        stringRepr.append(String.format("%s %s\n", "Chirality", this.Chirality));
        stringRepr.append(String.format("%s %s\n", "CartoonX", this.getCartoonX()));
        stringRepr.append(String.format("%s %s\n", "CartoonY", this.getCartoonY()));
//        stringRepr.append(this.AxisRepr());
        stringRepr.append(String.format("%s %s\n", "SymbolRadius", cartoonSymbol.getSymbolRadius()));
        stringRepr.append(String.format("%s %2f\n", "AxisLength", this.AxisLength));
        stringRepr.append(String.format("%s %s\n", "NConnectionPoints", this.NConnectionPoints));
        stringRepr.append(String.format("%s %s\n", "ConnectionTo", this.Connections()));
        stringRepr.append(String.format("%s %sn", "Fill", this.Fill));
        return stringRepr.toString();
    }

    private String AxisRepr() {
        // TODO Auto-generated method stub
        return null;
    }

    public String toString() {
        if (this.Direction == 'U' || (this.SSEType == 'N' || this.SSEType ==  'C')) {
            return String.valueOf(this.SSEType);
        } else {   
            return String.valueOf(this.SSEType).toLowerCase();
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
        return Fill;
    }

    public double getSymbolRadius() {
        return cartoonSymbol.getSymbolRadius();
    }
    
    public void WriteSecStr(PrintStream out) {

        char ch;

        print(out, "SecondaryStructureType %c\n", getSSEType());
        print(out, "Direction %c\n", getDirection());
        print(out, "Label %s\n", getLabel());
        print(out, "Colour %d %d %d\n", 
                cartoonSymbol.getColor()[0], 
                cartoonSymbol.getColor()[1], 
                cartoonSymbol.getColor()[2]);

        if (Next != null) {
            print(out, "Next %d\n", Next.getSymbolNumber());
        } else {
            print(out, "Next -1\n");
        }

        if (fixed != null) {
            print(out, "Fixed %d\n", fixed.getSymbolNumber());
        } else {
            print(out, "Fixed -1\n");
        }

        switch (fixedType) {
        case FT_BARREL:
            print(out, "FixedType BARREL\n");
            break;
        case FT_SHEET:
            print(out, "FixedType SHEET\n");
            break;
        case FT_CURVED_SHEET:
            print(out, "FixedType CURVED_SHEET\n");
            break;
        case FT_V_CURVED_SHEET:
            print(out, "FixedType V_CURVED_SHEET\n");
            break;
        case FT_SANDWICH:
            print(out, "FixedType SANDWICH\n");
            break;
        case FT_TEMPLATE:
            print(out, "FixedType TEMPLATE\n");
            break;
        case FT_UNKNOWN:
            print(out, "FixedType UNKNOWN\n");
            break;
        default:
            print(out, "FixedType UNKNOWN\n");
            break;
        }

        print(out, "BridgePartner");
        for (BridgePartner BridgePartner : getBridgePartners())
            print(out, " %d", BridgePartner.partner.getSymbolNumber());
        print(out, "\n");

        print(out, "BridgePartnerSide");
        for (BridgePartner BridgePartner : getBridgePartners())
            print(out, " %c", BridgePartner.side);
        print(out, "\n");

        print(out, "BridgePartnerType");
        for (BridgePartner BridgePartner : getBridgePartners()) {
            switch (BridgePartner.bridgeType) {
            case ANTI_PARALLEL_BRIDGE:
                print(out, " %c", 'A');
                break;
            case PARALLEL_BRIDGE:
                print(out, " %c", 'P');
                break;
            case UNK_BRIDGE_TYPE:
                print(out, " %c", 'U');
                break;
            default:
                print(out, " %c", 'U');
                break;
            }
        }
        print(out, "\n");

        print(out, "Neighbour");
        for (Neighbour neighbour : Neighbours) {
            print(out, " %d", neighbour.sse.getSymbolNumber());
        }
        print(out, "\n");

        print(out, "SeqStartResidue %d\n", sseData.SeqStartResidue);
        print(out, "SeqFinishResidue %d\n", sseData.SeqFinishResidue);

        print(out, "PDBStartResidue %d\n", sseData.PDBStartResidue);
        print(out, "PDBFinishResidue %d\n", sseData.PDBFinishResidue);

        print(out, "SymbolNumber %d\n", getSymbolNumber());

        ch = Chain;
        if (ch == '\0')
            ch = ' ';
        print(out, "Chain %c\n", ch);

        print(out, "Chirality %d\n", Chirality);

        print(out, "CartoonX %d\n", getCartoonX());
        print(out, "CartoonY %d\n", getCartoonY());

        print(out, "AxesStartPoint");
        print(out, " %s", axis.AxisStartPoint);
        print(out, "\n");

        print(out, "AxesFinishPoint");
        print(out, " %s", axis.AxisFinishPoint);
        print(out, "\n");

        print(out, "SymbolRadius %d\n", cartoonSymbol.getSymbolRadius());

        print(out, "AxisLength %f\n", axis.getLength());

        print(out, "NConnectionPoints %d\n", NConnectionPoints);

        print(out, "ConnectionTo");
        for (Point2d point : ConnectionTo)
            print(out, " %f %f", point.x, point.y);
        print(out, "\n");

        print(out, "Fill %d\n", getFill());

        return;

    }

    private void print(String s, Object... vars) {
        print(System.out, s, vars);
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

    
}
