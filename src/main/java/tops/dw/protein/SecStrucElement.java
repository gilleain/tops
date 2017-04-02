package tops.dw.protein;

import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Color;
import java.awt.Point;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import tops.port.model.SSEType;


public class SecStrucElement {

    private int SymbolNumber;

    private SSEType type;

    private String Direction;

    private int PDBStartResidue;

    private int PDBFinishResidue;

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

    public List<Integer> getBridgePartner() {
        return this.BridgePartner;
    }

    public List<String> getBridgePartnerSide() {
        return this.BridgePartnerSide;
    }

    public List<String> getBridgePartnerType() {
        return this.BridgePartnerType;
    }

    public void addNeighbour(int nb) {
        if (this.Neighbour == null)
            this.Neighbour = new Vector<Integer>();
        this.Neighbour.addElement(new Integer(nb));
    }

    public List<Integer> getNeighbour() {
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

    public float getAxisLength() {
        return AxisLength;
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

    public void setFixedIndex(int i) {
        this.FixedIndex = i;
    }

    public int getFixedIndex() {
        return this.FixedIndex;
    }

    public void setNextIndex(int i) {
        this.NextIndex = i;
    }

    public int getNextIndex() {
        return this.NextIndex;
    }

    public void setNext(SecStrucElement s) {
        this.Next = s;
    }

    public SecStrucElement getNext() {
        return this.Next;
    }
   
    public boolean isTerminus() {

        if (this.type == NTERMINUS || this.type == SSEType.CTERMINUS) {
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

	public SSEType getType() {
		return type;
	}

	public void setType(SSEType type) {
		this.type = type;
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

    public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}

	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (this.type == HELIX) {
            sb.append("Helix");
        } else if (this.type == EXTENDED) {
            sb.append("Strand");
        } else if (this.type == NTERMINUS) {
            sb.append("N terminus");
        } else if (this.type == CTERMINUS){
            sb.append("C terminus");
        }

        if (this.type == HELIX || this.type == EXTENDED) {
            sb.append(" " + PDBStartResidue + " to " + PDBFinishResidue);
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

}
