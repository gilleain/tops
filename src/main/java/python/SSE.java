package python;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;

class SSE {
    
    private int MAXBP = 6;

    private char Direction;
    private char SSEType;                       // H, E, N, C, D 
    private List<BridgePartner> BridgePartners;    // Bridge partners
    private List<BridgeRange> BridgeRange;
    public int SeqStartResidue = 0;
    public int SeqFinishResidue = 0;           // Number of starting and finishing residues in DSSP file 
    public int PDBStartResidue = 0;
    public int PDBFinishResidue = 0;           // Number of starting and finishing residues in PDB file 
    public int Merges = 0;                     // The number of merges made in creation 
    public int[][] MergeRanges;            // The start and end of the Merged structures 
    public int DomainBreakNumber;             // Number identifying possible domain breaks (0 -> No break) 
    public DomainBreakType domainBreakType;                // Number identifying domain break type (Nterm, Cterm or both) 
    public Hand Chirality;                      // Local structure hand 
    public Axis axis;
    public FixedType fixedType;
    
    public boolean SymbolPlaced;
    public int SymbolNumber;
    
    // XXX TODO don't like these linked-list pointers, but have them for now...
    public SSE From;
    public SSE To;
    public SSE Next;        // difference from 'To'?
    
    // XXX TODO these need to be split off somewhere!
    public int CartoonX;
    public int CartoonY;
    public int[] Colour;
    public double SymbolRadius;
    public int NConnectionPoints;
    public FixedType FixedType;
    
    
    // XXX TODO Gah, upwards pointers!
    public char Chain;
    
    public SSE Fixed;
    private String Label;
    private boolean Fill;
    public List<Neighbour> Neighbours;
    public List<Point2d> ConnectionTo;
    private Object AxisLength;


    public SSE(char SSEType) {
        this.SSEType = SSEType;                  
        this.BridgePartners = new ArrayList<BridgePartner>();   
        this.Neighbours = new ArrayList<Neighbour>();                    
        this.ConnectionTo = new ArrayList<Point2d>();
        this.MergeRanges = new int[10][];   // XXX FIXME  
        this.Colour = new int[] {0, 0, 0};
        this.axis = null;
    }
    
    /**
     * TODO : copy constructor
     */
    public SSE(SSE other) {
        
    }
    
    public BridgePartner getBridgePartner(int index) {
        return this.BridgePartners.get(index);
    }
    
    public List<BridgePartner> getBridgePartners() {
        return this.BridgePartners;
    }
    
    public void sortBridgePartners() {
        this.BridgePartners.sort(new Comparator<BridgePartner>() {

            @Override
            public int compare(BridgePartner o1, BridgePartner o2) {
                return new Integer(o1.NumberBridgePartners).compareTo(
                        new Integer(o2.NumberBridgePartners));
            }
            
        });
    }
    
    public static SSE getCommonBP(SSE Start, List<SSE> CurrentList) {
        for (int i = 0; i < Start.BridgePartners.size(); i++) {
            BridgePartner commonBridgePartner = Start.BridgePartners.get(i);
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
        for (BridgePartner bridgePartner : BridgePartners) {
            BridgeType edgeType = bridgePartner.bridgeType;
            if (SeqStartResidue < bridgePartner.partner.SeqStartResidue) {
                int partnerSymbolNumber = bridgePartner.partner.SymbolNumber;
                char edgeTypeChar;
                if (edgeType == BridgeType.ANTI_PARALLEL_BRIDGE) edgeTypeChar = 'A';
                else if (edgeType == BridgeType.PARALLEL_BRIDGE) edgeTypeChar = 'P';
                else edgeTypeChar = '!';
                String edge = String.format("%i:%i%s", SymbolNumber, partnerSymbolNumber, edgeTypeChar);
                edges.append(edge).append(" ");
            }
        }
    }
    
    public List<SSE> getPartners() {
        List<SSE> sses = new ArrayList<SSE>();
        for (BridgePartner bp : BridgePartners) {
            sses.add(bp.partner);
        }
        return sses;
    }
    
    //FIXME 
    //recursive and unidirectional!//
    public SSE FindEdgeStrand(SSE last) {
        SSE partner0 = BridgePartners.get(0).partner;
        SSE partner1 = BridgePartners.get(1).partner;
        System.out.println(String.format(
                "find edge strand at %s bridge partners %s and %s", this, partner0, partner1));
        if (partner0 == null || partner1 == null) {
            return this;
        } else {
            if (partner0 != last) {
                return this.FindEdgeStrand(partner0);
            } else {
                return this.FindEdgeStrand(partner1);
            }
        }
    }
    
    /*
    function join_to_last

    Tom F. August 1992

    Function to join an sse to the previous one. returns if the
    secondary structures are different or the vectors are less than
    ninety degrees ie. antiparallel.
     */
    public void joinToLast() {

        SSE lastSSE = this.From;
        if (!lastSSE.isSameType(this)) return;

        // Merge bridge partners 
        for (BridgePartner bridgePartner : this.BridgePartners) {
            SSE toJoinPartner = bridgePartner.partner;
            BridgePartner common = null;
            for (BridgePartner other : toJoinPartner.BridgePartners) {
                // Does the bridge partner already have a bond to lastSSE */
                if (other.partner == lastSSE) {
                    common = other;
                    break;
                }
            }

            // If Bridge partner doesn't already exist create it */
            if (common == null) {
                for (BridgePartner bp : toJoinPartner.BridgePartners) {
                    if (bp.partner == this) {
                        bp.partner = lastSSE;
                        BridgeType type = bridgePartner.bridgeType; 
                        lastSSE.BridgePartners.add(
                                new BridgePartner(toJoinPartner, bridgePartner.rangeMin, bridgePartner.rangeMax, type, null));
                        break;
                    }
                }
            } else {

                // Coalesce common bridge partner 
                for (BridgePartner lastBridgePartner : lastSSE.BridgePartners) {
                    if (lastBridgePartner.partner == toJoinPartner) {
                        lastBridgePartner.NumberBridgePartners += bridgePartner.NumberBridgePartners;
                        common.NumberBridgePartners = lastBridgePartner.NumberBridgePartners;
                        lastBridgePartner.rangeMax = bridgePartner.rangeMax;
                        toJoinPartner.removeBridgePartner(this);
                    }
                }
            }
        }

        // Switch ends 
        lastSSE.axis.AxisFinishPoint = (Vector3d)this.axis.AxisFinishPoint.clone();

        // Merge residues - don't include fixed 
        lastSSE.SeqFinishResidue = this.SeqFinishResidue;
        lastSSE.PDBFinishResidue = this.PDBFinishResidue;
        lastSSE.To = this.To;
        if (lastSSE.To != null) lastSSE.To.From = lastSSE;
        lastSSE.Next = this.Next;

        lastSSE.Merges += 1;

        // FIXME - merge ranges only seem to be needed for chiral
        //        p.MergeRanges.append(new int[] {q.SeqStartResidue, q.SeqFinishResidue});
    }
    
    public void assignRelativeSides() {
        if (isStrand()) {
            for (BridgePartner bp : BridgePartners) {
                bp.side = BridgePartner.Side.UNKNOWN;
            }
            int RefBP = LongestBridgeRange();
            BridgePartner BP1 = BridgePartners.get(RefBP);
            if (RefBP >= 0 && BP1 != null) {
                BP1.side = BridgePartner.Side.LEFT;
                // Assign side for those 'BridgeOverlap'ing with the RefBP //
                for (int j = 0; j < BridgePartners.size(); j++) {
                    BridgePartner BP2 = BridgePartners.get(j);
                    if (j != RefBP && BP2 != null) {
                        if (bridgeOverlap(this, BP1.partner, BP2.partner)) {
                            BP2.side = BridgePartner.Side.RIGHT;
                        }
                    }
                }
                // Sort out any other sides which can be calculated by BridgeOverlaps with other than the RefBP //
                for (int j = 0; j < BridgePartners.size(); j++) {
                    BridgePartner BP2 = BridgePartners.get(j);
                    if (BP2 != null && BP2.side == BridgePartner.Side.UNKNOWN) {
                        for (int k = 0; k < BridgePartners.size(); k++) {
                            BridgePartner BP3 = BridgePartners.get(k);
                            if (k != j && BP3 != null && BP3.side == BridgePartner.Side.UNKNOWN) {
                                if (bridgeOverlap(this, BP2.partner, BP3.partner)) {
                                    if (BP3.side == BridgePartner.Side.LEFT)
                                        BP2.side = BridgePartner.Side.RIGHT;
                                    } else {
                                        BP2.side = BridgePartner.Side.LEFT;
                                    }
                                }
                            }
                        }
                    }

                // The rest have to be done geometrically //
                for (int j = 0; j < BridgePartners.size(); j++) {
                    BridgePartner BP2 = BridgePartners.get(j);
                    if (BP2 != null && BP2.side == BridgePartner.Side.UNKNOWN) {
                        if (GeometricSameSide(this, BP1.partner, BP2.partner)) {
                            BP2.side = BridgePartner.Side.LEFT;
                        } else {
                            BP2.side = BridgePartner.Side.RIGHT;
                        }
                    }
                }
            }
        }
    }
    
    /*
    Function to determine whether the bridge partners of p overlap.
    ie. are the same residues hydrogen bonding to q and r?
     */
    public boolean bridgeOverlap(SSE p, SSE q, SSE r) {

        // Find the index to q //
        int i = p.FindBPIndex(q);

        // Find the index to r //
        int j = p.FindBPIndex(r);

        // Compare ranges //
        int a = p.BridgeRange.get(i).start;
        int b = p.BridgeRange.get(i).end;
        int x = p.BridgeRange.get(j).start;
        int y = p.BridgeRange.get(j).end;

        // Not a---b x---y or x---y a---b //
        return !(b < x || y < a);  
    }

    
    /*
    A function to decide geometrically whether strand p lies on the same or opposite side 
    of strand q as strand r
  */
  public boolean GeometricSameSide(SSE q, SSE r, SSE p) {

//      v1[i] = q.AxisFinishPoint[i] - q.AxisStartPoint[i];
//      midr[i] = (r.AxisFinishPoint[i] + r.AxisStartPoint[i]) / 2.0;
//      midp[i] = (p.AxisFinishPoint[i] + p.AxisStartPoint[i]) / 2.0;
//      v2[i] = midr[i] - q.AxisStartPoint[i];
//      v3[i] = midp[i] - q.AxisStartPoint[i];
//
//      double normal2 = v2.cross(v1).normal();
//      double normal1 = v1.cross(v3).normal();
//
//      double costheta = -(normal1 * normal2);
//
//      return costheta > 0.0;
      return false;
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

    public void SecondaryStructureAxis(Chain chain) {
        this.axis = chain.secondaryStructureAxis(SeqStartResidue, SeqFinishResidue);
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
        for (BridgePartner partner : other.BridgePartners) {   // TODO XXX
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
        if (this.BridgePartners.get(1).partner == other) {
            return this.BridgePartners.get(0).partner;
        } else {
            return this.BridgePartners.get(1).partner;
        }
    }

    public void swapBridgePartners(int a, int b) {
        BridgePartner tmp = this.BridgePartners.get(a);
        this.BridgePartners.set(a, this.BridgePartners.get(b));
        this.BridgePartners.set(b, tmp);
    }

    // XXX following method was surely intended for removing BP, which is not really necessary
    // when you have arraylists ...
//    public void ShuffleDownBPs() {
//        for (int i = 0; i < this.BridgePartners.size(); i++) {
//            if (this.BridgePartners.get(i) == null) {
//                int j = i;
//                while ((j < MAXBP) && (this.BridgePartners.get(j) == null)) j += 1;
//                if (j < MAXBP) {
//                    this.MoveBridgePartner(j, i);
//                }
//            }
//        }
//    }
    // XXX similarly, this method is only called from above (ShuffleDownBPs). Delete it!
//    public void MoveBridgePartner(int j, int i) {
//        int MAXNB = this.Neighbours.size();
//        if ((i < 0) || (i >= MAXNB)) return;
//        if ((j < 0) || (j >= MAXNB)) return;
//
//        this.BridgePartner[i] = this.BridgePartner[j];
//        this.BridgeRange[i][0] = this.BridgeRange[j][0];
//        this.BridgeRange[i][1] = this.BridgeRange[j][1];
//        this.BridgePartnerSide[i] = this.BridgePartnerSide[j];
//        this.BridgePartnerType[i] = this.BridgePartnerType[j];
//
//        //the following is equivalent to a call to 'RemoveBP(p.BridgePartner[j])'
//        this.BridgePartner[j] = null;
//        this.BridgeRange[j][0] = 0;
//        this.BridgeRange[j][1] = 0;
//        this.BridgePartnerSide[j] = ' ';
//        this.BridgePartnerType[j] = UNK_BRIDGE_TYPE;
//    }

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
        this.BridgePartners.remove(bp);
    }

    public Hand Chiral3d(SSE other) {

        int a1s, a1f, a2s, a2f;
        if (this.Merges > 0) {
            a1s = this.MergeRanges[this.Merges - 1][0];
            a1f = this.MergeRanges[this.Merges - 1][1];
        } else {
            a1s = this.SeqStartResidue;
            a1f = this.SeqFinishResidue;
        }

        if (other.Merges > 0) {
            a2s = other.MergeRanges[0][0];
            a2f = other.MergeRanges[0][1];
        } else {
            a2s = other.SeqStartResidue;
            a2f = other.SeqFinishResidue;
        }

        return this.motifChirality(a1s, a1f, a2s, a2f);
    }

    //FIXME!! : link in the slidel code!
    public Hand motifChirality(int a, int b, int c, int d) { 
        return Hand._unk_hand;
    }

    public int FindBPIndex(SSE bp) {
        for (int i = 0; i < this.BridgePartners.size(); i++) {
            if (this.BridgePartners.get(i).partner == bp) return i;
        }
        return this.BridgePartners.size();
    }

    public int SecStrucLength() {
        return this.PDBFinishResidue - this.PDBStartResidue + 1;
    }

    public SSE getFirstCommonBP(SSE other) {
        for (BridgePartner bridgePartner : this.BridgePartners) {
            for (BridgePartner otherBridgePartner : other.BridgePartners) {
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
        for (BridgePartner other : sse.BridgePartners) {
            if (other.partner == this) return other.side.name();    // XXX is name correct here?
        }
        return "";
    }

    public int NumberPartners() {
        return this.BridgePartners.size();
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

    public int LongestBridgeRange() {
        int longest = -1;
        double maxlen = -1.0;
        for (int i = 0; i < MAXBP; i++) {
            if (this.BridgePartners.get(i) != null) {
                int len = this.BridgeRange.get(i).length();
                if (len > maxlen) {
                    maxlen = len;
                    longest = i;
                }
            }
        }
        return longest;
    }

    public boolean HasBPonList(List<SSE> sseList) {
        for (BridgePartner q : this.BridgePartners) {
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
        for (BridgePartner partner : this.BridgePartners) {
            if (partner == null) break;
            numbers.append(String.valueOf(partner.partner.SymbolNumber)).append(" ");
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
            numbers.append(neighbour.sse.SymbolNumber);
            numbers.append(" ");
        }
        return numbers.toString();
    }

    public String BridgePartnerSides() {
        StringBuffer sides  = new StringBuffer();
        for (BridgePartner partner : this.BridgePartners) {
            if (partner.side == BridgePartner.Side.UNKNOWN) break;
            sides.append(partner.side.toString().charAt(0));    // XXX ugh!
            sides.append(" ");
        }
        return sides.toString();
    }

    public String BridgePartnerTypes() {
        StringBuffer types = new StringBuffer();
        for (BridgePartner partner : this.BridgePartners ) {
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
            return String.valueOf(sse.SymbolNumber);
        }
    }

    public String getLabel() {
        if (this.Label == null) return "";
        else return this.Label;
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
        stringRepr.append(String.format("%s %s %s %s\n", "Colour", this.Colour[0], this.Colour[1], this.Colour[2]));
        stringRepr.append(String.format("%s %s\n", "Next", this.getSymbolNumber(this.Next)));
        stringRepr.append(String.format("%s %s\n", "Fixed", this.getSymbolNumber(this.Fixed)));
        stringRepr.append(String.format("%s %s\n", "FixedType", this.getFixedType()));
        stringRepr.append(String.format("%s %s\n", "BridgePartner", this.BridgePartnerNumbers()));
        stringRepr.append(String.format("%s %s\n", "BridgePartnerSide", this.BridgePartnerSides()));
        stringRepr.append(String.format("%s %s\n", "BridgePartnerType", this.BridgePartnerTypes()));
        stringRepr.append(String.format("%s %s\n", "Neighbour", this.NeighbourNumbers()));
        stringRepr.append(String.format("%s %s\n", "SeqStartResidue", this.SeqStartResidue));
        stringRepr.append(String.format("%s %s\n", "SeqFinishResidue", this.SeqFinishResidue));
        stringRepr.append(String.format("%s %s\n", "PDBStartResidue", this.PDBStartResidue));
        stringRepr.append(String.format("%s %s\n", "PDBFinishResidue", this.PDBFinishResidue));
        stringRepr.append(String.format("%s %s\n", "SymbolNumber", this.SymbolNumber));
        stringRepr.append(String.format("%s %s\n", "Chain", this.getChain()));
        stringRepr.append(String.format("%s %s\n", "Chirality", this.Chirality));
        stringRepr.append(String.format("%s %s\n", "CartoonX", this.CartoonX));
        stringRepr.append(String.format("%s %s\n", "CartoonY", this.CartoonY));
//        stringRepr.append(this.AxisRepr());
        stringRepr.append(String.format("%s %s\n", "SymbolRadius", this.SymbolRadius));
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

    public boolean IsInCircle(double centerX, double centerY, double radius) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasBridgePartner(SSE p) {
        for (BridgePartner partner : this.BridgePartners) {
            if (partner.partner == p) {
                return true;
            }
        }
        return false;
    }
    
    public void removeBridgePartner(SSE targetPartner) {
        BridgePartner toRemove = null;
        for (BridgePartner bp : this.BridgePartners) {
            if (bp.partner == targetPartner) {
                toRemove = bp;
                break;
            }
        }
        if (toRemove != null) {
            this.BridgePartners.remove(toRemove);
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
        for (BridgePartner bridgePartner : this.BridgePartners) {
            if (bridgePartner.partner == partner) {
                commonBridgePartner = bridgePartner;
                break;
            }
        }
        if (commonBridgePartner == null) {
            this.BridgePartners.add(new BridgePartner(partner, residueNumber, bridgeType, side));
        } else {
            commonBridgePartner.update(residueNumber);
        }
    }

    public void setLabel(String label) {
        this.Label = label;
    }

    public boolean getFill() {
        return Fill;
    }
}