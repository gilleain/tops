package tops.dw.protein;

import java.awt.Color;
import java.awt.Point;
import java.io.PrintWriter;
import java.util.Enumeration;
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

    public SecStrucElement From = null, To = null;

    private SecStrucElement Fixed = null;

    private int FixedIndex;

    private SecStrucElement Next = null;

    private int NextIndex;

    private Point Position;

    private int SymbolRadius;

    private Vector<Point> ConnectionTo;

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
        this.Position = new Point(0, 0);
        this.ConnectionTo = new Vector<Point>();
        this.BridgePartner = new Vector<Integer>();
        this.BridgePartnerSide = new Vector<String>();
        this.BridgePartnerType = new Vector<String>();
        this.Neighbour = new Vector<Integer>();
        this.Colour = null;
    }
    
    public boolean containsResidue(int residueNumber) {
    	return this.PDBStartResidue <= residueNumber && this.PDBFinishResidue >= residueNumber;
    }

    public void SetFixedType(String ft) {
        this.FixedType = ft;
    }

    public String GetFixedType() {
        return this.FixedType;
    }

    public void AddBridgePartner(int bp) {
        if (this.BridgePartner == null)
            this.BridgePartner = new Vector<Integer>();
        this.BridgePartner.addElement(new Integer(bp));
    }

    public void AddBridgePartnerSide(String side) {
        if (this.BridgePartnerSide == null)
            this.BridgePartnerSide = new Vector<String>();
        this.BridgePartnerSide.addElement(side);
    }

    public void AddBridgePartnerType(String type) {
        if (this.BridgePartnerType == null)
            this.BridgePartnerType = new Vector<String>();
        this.BridgePartnerType.addElement(type);
    }

    public Vector<Integer> GetBridgePartner() {
        return this.BridgePartner;
    }

    public Vector<String> GetBridgePartnerSide() {
        return this.BridgePartnerSide;
    }

    public Vector<String> GetBridgePartnerType() {
        return this.BridgePartnerType;
    }

    public void AddNeighbour(int nb) {
        if (this.Neighbour == null)
            this.Neighbour = new Vector<Integer>();
        this.Neighbour.addElement(new Integer(nb));
    }

    public Vector<Integer> GetNeighbour() {
        return this.Neighbour;
    }

    public void SetSeqStartResidue(int ssr) {
        this.SeqStartResidue = ssr;
    }

    public int GetSeqStartResidue() {
        return this.SeqStartResidue;
    }

    public void SetSeqFinishResidue(int sfr) {
        this.SeqFinishResidue = sfr;
    }

    public int GetSeqFinishResidue() {
        return this.SeqFinishResidue;
    }

    public void SetChirality(int c) {
        this.Chirality = c;
    }

    public int GetChirality() {
        return this.Chirality;
    }

    public void SetAxesStartPoint(float x, float y, float z) {
        this.AxesStartPoint[0] = x;
        this.AxesStartPoint[1] = y;
        this.AxesStartPoint[2] = z;
    }

    public float[] GetAxesStartPoint() {
        return this.AxesStartPoint;
    }

    public void SetAxesFinishPoint(float x, float y, float z) {
        this.AxesFinishPoint[0] = x;
        this.AxesFinishPoint[1] = y;
        this.AxesFinishPoint[2] = z;
    }

    public float[] GetAxesFinishPoint() {
        return this.AxesFinishPoint;
    }

    public void SetAxisLength(float len) {
        this.AxisLength = len;
    }

    public float GetAxisLength() {
        return this.AxisLength;
    }

    public void SetFill(int f) {
        this.Fill = f;
    }

    public int GetFill() {
        return this.Fill;
    }

    public void PlaceElement(int x, int y) {
        if (this.Position == null)
            this.Position = new Point(0, 0);
        this.Position.setLocation(x, y);
    }

    public void PlaceElementX(int x) {
        if (this.Position == null)
            this.Position = new Point(0, 0);
        this.Position.x = x;
    }

    public void PlaceElementY(int y) {
        if (this.Position == null)
            this.Position = new Point(0, 0);
        this.Position.y = y;
    }

    public Point GetPosition() {
        return this.Position;
    }

    public void SetPosition(Point p) {
        if (this.Position == null) {
            this.Position = new Point(p.x, p.y);
        } else {
            this.Position.x = p.x;
            this.Position.y = p.y;
        }
    }

    public void Translate(int tx, int ty) {
        if (this.Position == null) {
            this.Position = new Point();
        }
        this.Position.x += tx;
        this.Position.y += ty;
    }

    public void TranslateFixed(int tx, int ty) {
        SecStrucElement s;
        for (s = this.GetFixedStart(); s != null; s = s.GetFixed()) {
            s.Translate(tx, ty);
        }

    }

    public void SetSymbolRadius(int r) {
        this.SymbolRadius = r;
    }

    public int GetSymbolRadius() {
        return this.SymbolRadius;
    }

    public SecStrucElement Delete() {

        SecStrucElement root = this.GetRoot();
        if (root == this) {
            if (this.To != null)
                this.To.SetFrom(null);
            return this.To;
        }

        if (this.From != null)
            this.From.SetTo(this.To);

        if (this.To != null)
            this.To.SetFrom(this.From);

        return root;

    }

    public SecStrucElement GetRoot() {
        SecStrucElement s;
        for (s = this; s.GetFrom() != null; s = s.GetFrom())
            ;
        return s;
    }

    public SecStrucElement GetFixedStart() {

        SecStrucElement s = null, t = null;

        for (s = this.GetRoot(); s != null; s = s.GetNext()) {
            for (t = s; t != null; t = t.GetFixed()) {
                if (t == this)
                    break;
            }
        }

        if (t == this)
            return s;
        else
            return null;

    }

    public void AddConnectionTo(int x, int y) {

        Point p = new Point(x, y);

        if (this.ConnectionTo == null)
            this.ConnectionTo = new Vector<Point>();

        this.ConnectionTo.addElement(p);

    }

    public void AddConnectionTo(Point p) {
        if (p != null) {
            if (this.ConnectionTo == null)
                this.ConnectionTo = new Vector<Point>();
            this.ConnectionTo.addElement(p);
        }
    }

    public Vector<Point> GetConnectionTo() {
        if (this.ConnectionTo == null)
            this.ConnectionTo = new Vector<Point>();
        return this.ConnectionTo;
    }

    public void ClearConnectionTo() {
        this.ConnectionTo = new Vector<Point>();
    }

    public void SetFixed(SecStrucElement s) {
        this.Fixed = s;
    }

    public SecStrucElement GetFixed() {
        return this.Fixed;
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

    public void SetTo(SecStrucElement s) {
        this.To = s;
    }

    public void SetFrom(SecStrucElement s) {
        this.From = s;
    }

    public SecStrucElement GetTo() {
        return this.To;
    }

    public SecStrucElement GetFrom() {
        return this.From;
    }

    public boolean IsRoot() {
        if (this.From == null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean IsTerminus() {

        if (this.Type.equals("N") || this.Type.equals("C")) {
            return true;
        } else {
            return false;
        }

    }

    public int Length() {
        return (this.PDBFinishResidue - this.PDBStartResidue + 1);
    }

    public int GetFixNumRes() {

        SecStrucElement t;
        int size = 0;

        if (this.Type.equals("H") || this.Type.equals("E")) {
            for (t = this.GetFixedStart(), size = 0; t != null; t = t.GetFixed())
                size += t.Length();
        }

        return size;

    }

    public SecStrucElement GetSSEByNumber(int num) {

        SecStrucElement ss;
        int i = 0;
        for (ss = this; ss != null && i < num; ss = ss.To, i++)
            ;

        return ss;

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

        Point p = this.Position;
        if (p == null)
            p = new Point();
        ps.println("CartoonX " + p.x);
        ps.println("CartoonY " + p.y);

        ps.println("AxesStartPoint " + this.AxesStartPoint[0] + " "
                + this.AxesStartPoint[1] + " " + this.AxesStartPoint[2]);
        ps.println("AxesFinishPoint " + this.AxesFinishPoint[0] + " "
                + this.AxesFinishPoint[1] + " " + this.AxesFinishPoint[2]);

        ps.println("SymbolRadius " + this.SymbolRadius);

        ps.println("AxisLength " + this.AxisLength);

        Vector<Point> ct = this.GetConnectionTo();
        ps.println("NConnectionPoints " + ct.size());
        ps.print("ConnectionTo");
        Enumeration<Point> en = ct.elements();
        Point cp;
        while (en.hasMoreElements()) {
            cp = (Point) en.nextElement();
            ps.print(" " + cp.x + " " + cp.y);
        }
        ps.print("\n");

        ps.println("Fill " + this.Fill);

    }

    /* END I/O methods */

    /* START debugging methods */

    public void PrintLists() {

        SecStrucElement s, root;

        root = this.GetRoot();

        System.out.println("To list");
        System.out.println(" ");

        for (s = root; s != null; s = s.GetTo()) {
            s.PrintElement();
        }

        System.out.println(" ");
        System.out.println(" ");

    }

    public void PrintElement() {
        System.out.println("SymbolNumber " + this.SymbolNumber);
        if (this.To != null)
            System.out.println("To " + this.To.SymbolNumber);
        if (this.From != null)
            System.out.println("From " + this.From.SymbolNumber);
        System.out.println("NextIndex " + this.NextIndex);
        if (this.Next != null)
            System.out.println("Next " + this.Next.SymbolNumber);
        System.out.println("FixedIndex " + this.FixedIndex);
        if (this.Fixed != null)
            System.out.println("Fixed " + this.Fixed.SymbolNumber);
        System.out.println(" ");
    }

    /* END debugging methods */

}
