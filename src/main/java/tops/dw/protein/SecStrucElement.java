package tops.dw.protein;

import java.awt.Color;
import java.awt.Point;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


public class SecStrucElement {

    private int SymbolNumber;

    private String Type;

    private String Direction;

    private int PDBStartResidue;

    private int PDBFinishResidue;

    private String Chain;

    private String Label;

    private Color Colour;

    private SecStrucElement Fixed = null;

    private int FixedIndex;

    private SecStrucElement Next = null;

    private int NextIndex;

    private Point position;

    private int symbolRadius;

    private List<Point> connectionTo;

	private String FixedType;

    private Vector<Integer> BridgePartner;

    private Vector<String> BridgePartnerSide;

    private Vector<String> BridgePartnerType;

    private Vector<Integer> Neighbour;

    private int SeqStartResidue, SeqFinishResidue;

    private int Chirality;

    private float AxesStartPoint[] = new float[3];

    private float AxesFinishPoint[] = new float[3];

    private float AxisLength;

    private int Fill;

    public SecStrucElement() {
        this.position = new Point(0, 0);
        this.connectionTo = new Vector<Point>();
        this.BridgePartner = new Vector<Integer>();
        this.BridgePartnerSide = new Vector<String>();
        this.BridgePartnerType = new Vector<String>();
        this.Neighbour = new Vector<Integer>();
        this.Colour = null;
    }
    
    public boolean containsResidue(int residueNumber) {
    	return this.PDBStartResidue <= residueNumber && this.PDBFinishResidue >= residueNumber;
    }

    public void setFixedType(String ft) {
        this.FixedType = ft;
    }

    public String getFixedType() {
        return this.FixedType;
    }

    public void addBridgePartner(int bp) {
        if (this.BridgePartner == null)
            this.BridgePartner = new Vector<Integer>();
        this.BridgePartner.addElement(new Integer(bp));
    }

    public void addBridgePartnerSide(String side) {
        if (this.BridgePartnerSide == null)
            this.BridgePartnerSide = new Vector<String>();
        this.BridgePartnerSide.addElement(side);
    }

    public void addBridgePartnerType(String type) {
        if (this.BridgePartnerType == null)
            this.BridgePartnerType = new Vector<String>();
        this.BridgePartnerType.addElement(type);
    }

    public Vector<Integer> getBridgePartner() {
        return this.BridgePartner;
    }

    public Vector<String> getBridgePartnerSide() {
        return this.BridgePartnerSide;
    }

    public Vector<String> getBridgePartnerType() {
        return this.BridgePartnerType;
    }

    public void addNeighbour(int nb) {
        if (this.Neighbour == null)
            this.Neighbour = new Vector<Integer>();
        this.Neighbour.addElement(new Integer(nb));
    }

    public Vector<Integer> getNeighbour() {
        return this.Neighbour;
    }

    public void setSeqStartResidue(int ssr) {
        this.SeqStartResidue = ssr;
    }

    public int getSeqStartResidue() {
        return this.SeqStartResidue;
    }

    public void setSeqFinishResidue(int sfr) {
        this.SeqFinishResidue = sfr;
    }

    public int getSeqFinishResidue() {
        return this.SeqFinishResidue;
    }

    public void setChirality(int c) {
        this.Chirality = c;
    }

    public int getChirality() {
        return this.Chirality;
    }

    public void setAxesStartPoint(float x, float y, float z) {
        this.AxesStartPoint[0] = x;
        this.AxesStartPoint[1] = y;
        this.AxesStartPoint[2] = z;
    }

    public float[] getAxesStartPoint() {
        return this.AxesStartPoint;
    }

    public void setAxesFinishPoint(float x, float y, float z) {
        this.AxesFinishPoint[0] = x;
        this.AxesFinishPoint[1] = y;
        this.AxesFinishPoint[2] = z;
    }

    public float[] getAxesFinishPoint() {
        return this.AxesFinishPoint;
    }

    public void setAxisLength(float len) {
        this.AxisLength = len;
    }

    public float setAxisLength() {
        return this.AxisLength;
    }

    public void setFill(int f) {
        this.Fill = f;
    }

    public int getFill() {
        return this.Fill;
    }

    public void placeElement(int x, int y) {
        if (this.position == null)
            this.position = new Point(0, 0);
        this.position.setLocation(x, y);
    }

    public void placeElementX(int x) {
        if (this.position == null)
            this.position = new Point(0, 0);
        this.position.x = x;
    }

    public void placeElementY(int y) {
        if (this.position == null)
            this.position = new Point(0, 0);
        this.position.y = y;
    }

    public Point getPosition() {
        return this.position;
    }

    public void setPosition(Point p) {
        if (this.position == null) {
            this.position = new Point(p.x, p.y);
        } else {
            this.position.x = p.x;
            this.position.y = p.y;
        }
    }

    public void translate(int tx, int ty) {
        if (this.position == null) {
            this.position = new Point();
        }
        this.position.x += tx;
        this.position.y += ty;
    }


    public void setSymbolRadius(int r) {
        this.symbolRadius = r;
    }

    public int getSymbolRadius() {
        return this.symbolRadius;
    }

    public void addConnectionTo(int x, int y) {
        if (this.connectionTo == null)
            this.connectionTo = new Vector<Point>();

        this.connectionTo.add(new Point(x, y));
    }

    public void addConnectionTo(Point p) {
        if (p != null) {
            if (this.connectionTo == null)
                this.connectionTo = new Vector<Point>();
            this.connectionTo.add(p);
        }
    }

    public List<Point> getConnectionTo() {
        if (this.connectionTo == null)
            this.connectionTo = new ArrayList<Point>();
        return this.connectionTo;
    }

    public void clearConnectionTo() {
        this.connectionTo = new Vector<Point>();
    }

    public void SetFixedIndex(int i) {
        this.FixedIndex = i;
    }

    public int GetFixedIndex() {
        return this.FixedIndex;
    }

    public void SetNextIndex(int i) {
        this.NextIndex = i;
    }

    public int GetNextIndex() {
        return this.NextIndex;
    }

    public void SetNext(SecStrucElement s) {
        this.Next = s;
    }

    public SecStrucElement GetNext() {
        return this.Next;
    }
    
    private boolean isRoot; // TODO

    public boolean IsRoot() {
        return isRoot;
    }

    public boolean IsTerminus() {

        if (this.Type.equals("N") || this.Type.equals("C")) {
            return true;
        } else {
            return false;
        }

    }

    public int length() {
        return (this.PDBFinishResidue - this.PDBStartResidue + 1);
    }
  
    public String getRelDirection(SecStrucElement s) {
        if (this.Direction.equals(s.Direction))
            return "P";
        else
            return "A";
    }

    public int getSymbolNumber() {
		return SymbolNumber;
	}

	public void setSymbolNumber(int symbolNumber) {
		SymbolNumber = symbolNumber;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getDirection() {
		return Direction;
	}

	public void setDirection(String direction) {
		Direction = direction;
	}

	public int getPDBStartResidue() {
		return PDBStartResidue;
	}

	public void setPDBStartResidue(int pDBStartResidue) {
		PDBStartResidue = pDBStartResidue;
	}

	public int getPDBFinishResidue() {
		return PDBFinishResidue;
	}

	public void setPDBFinishResidue(int pDBFinishResidue) {
		PDBFinishResidue = pDBFinishResidue;
	}

	public String getChain() {
		return Chain;
	}

	public void setChain(String chain) {
		Chain = chain;
	}

    public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}

	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (this.Type.equals("H"))
            sb.append("Helix");
        else if (this.Type.equals("E"))
            sb.append("Strand");
        else if (this.Type.equals("N"))
            sb.append("N terminus");
        else if (this.Type.equals("C"))
            sb.append("C terminus");

        String ch;
        if ((this.Chain == null) || (this.Chain.equals("0")))
            ch = " ";
        else
            ch = this.Chain;

        if (this.Type.equals("H") || this.Type.equals("E")) {
            sb.append(" " + ch + this.PDBStartResidue + " to " + ch
                    + this.PDBFinishResidue);
        }

        return sb.toString();

    }

    public void setColour(Color c) {
        this.Colour = c;
    }

    public Color getColour() {
        return this.Colour;
    }

    /* START I/O methods */

    public void PrintAsText(PrintWriter ps) {

        ps.println("SecondaryStructureType " + this.Type);
        ps.println("Direction " + this.Direction);
        if (this.Label != null)
            ps.println("Label " + this.Label);
        else
            ps.println("Label");

        Color c = this.getColour();
        if (c == null)
            c = Color.white;
        ps.println("Colour " + c.getRed() + " " + c.getGreen() + " "
                + c.getBlue());

        int n = -1;
        if (this.Next != null)
            n = this.Next.SymbolNumber;
        ps.println("Next " + n);

        int f = -1;
        if (this.Fixed != null)
            f = this.Fixed.SymbolNumber;
        ps.println("Fixed " + f);

        if (this.FixedType != null)
            ps.println("FixedType " + this.FixedType);
        else
            ps.println("FixedType UNKNOWN");

        ps.print("BridgePartner");
        if (this.BridgePartner != null) {
            Enumeration<Integer> en = this.BridgePartner.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print(((Integer) en.nextElement()).intValue());
            }
        }
        ps.print("\n");

        ps.print("BridgePartnerSide");
        if (this.BridgePartnerSide != null) {
            Enumeration<String> en = this.BridgePartnerSide.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print((String) en.nextElement());
            }
        }
        ps.print("\n");

        ps.print("BridgePartnerType");
        if (this.BridgePartnerType != null) {
            Enumeration<String> en = this.BridgePartnerType.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print((String) en.nextElement());
            }
        }
        ps.print("\n");

        ps.print("Neighbour");
        if (this.Neighbour != null) {
            Enumeration<Integer> en = this.Neighbour.elements();
            while (en.hasMoreElements()) {
                ps.print(" ");
                ps.print(((Integer) en.nextElement()).intValue());
            }
        }
        ps.print("\n");

        ps.println("SeqStartResidue " + this.SeqStartResidue);
        ps.println("SeqFinishResidue " + this.SeqFinishResidue);

        ps.println("PDBStartResidue " + this.PDBStartResidue);
        ps.println("PDBFinishResidue " + this.PDBFinishResidue);

        ps.println("SymbolNumber " + this.SymbolNumber);
        ps.println("Chain " + this.Chain);

        ps.println("Chirality " + this.Chirality);

        Point p = this.position;
        if (p == null)
            p = new Point();
        ps.println("CartoonX " + p.x);
        ps.println("CartoonY " + p.y);

        ps.println("AxesStartPoint " + this.AxesStartPoint[0] + " "
                + this.AxesStartPoint[1] + " " + this.AxesStartPoint[2]);
        ps.println("AxesFinishPoint " + this.AxesFinishPoint[0] + " "
                + this.AxesFinishPoint[1] + " " + this.AxesFinishPoint[2]);

        ps.println("SymbolRadius " + this.symbolRadius);

        ps.println("AxisLength " + this.AxisLength);

        List<Point> ct = this.getConnectionTo();
        ps.println("NConnectionPoints " + ct.size());
        ps.print("ConnectionTo");
        for (Point cp : ct) {
            ps.print(" " + cp.x + " " + cp.y);
        }
        ps.print("\n");

        ps.println("Fill " + this.Fill);

    }
}
