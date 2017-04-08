package tops.dw.protein;

import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.vecmath.Vector3d;

import tops.port.model.Axis;
import tops.port.model.SSEData;
import tops.port.model.SSEType;


public class SecStrucElement {

    private int symbolNumber;

    private SSEType type;

    private String direction;
    
    private SSEData sseData;
    
    private String label;

    private Color colour;

    private Point position;

    private int symbolRadius;

    private List<Point> connectionTo;

    private List<Integer> bridgePartner;

    private List<String> bridgePartnerSide;

    private List<String> bridgePartnerType;

    private List<Integer> neighbour;

    private int chirality;
    
    private Axis axis;

    private int fill;

    public SecStrucElement() {
        this.position = new Point(0, 0);
        this.connectionTo = new ArrayList<Point>();
        this.bridgePartner = new ArrayList<Integer>();
        this.bridgePartnerSide = new ArrayList<String>();
        this.bridgePartnerType = new ArrayList<String>();
        this.neighbour = new ArrayList<Integer>();
        this.colour = null;
    }
    
    public boolean containsResidue(int residueNumber) {
    	return sseData.pdbStartResidue <= residueNumber && sseData.pdbFinishResidue >= residueNumber;
    }

    public void addBridgePartner(int bp) {
        this.bridgePartner.add(new Integer(bp));
    }

    public void addBridgePartnerSide(String side) {
        this.bridgePartnerSide.add(side);
    }

    public void addBridgePartnerType(String type) {
        this.bridgePartnerType.add(type);
    }

    public List<Integer> getBridgePartner() {
        return this.bridgePartner;
    }

    public List<String> getBridgePartnerSide() {
        return this.bridgePartnerSide;
    }

    public List<String> getBridgePartnerType() {
        return this.bridgePartnerType;
    }

    public void addNeighbour(int nb) {
        this.neighbour.add(new Integer(nb));
    }

    public List<Integer> getNeighbour() {
        return this.neighbour;
    }

    public void setSeqStartResidue(int ssr) {
        sseData.seqStartResidue = ssr;
    }

    public int getSeqStartResidue() {
        return sseData.seqStartResidue;
    }

    public void setSeqFinishResidue(int sfr) {
        sseData.seqFinishResidue = sfr;
    }

    public int getSeqFinishResidue() {
        return sseData.seqFinishResidue;
    }

    public void setChirality(int c) {
        this.chirality = c;
    }

    public int getChirality() {
        return this.chirality;
    }

    public void setAxesStartPoint(double x, double y, double z) {
        axis.AxisStartPoint = new Vector3d(x, y, z);
    }

    public double[] getAxesStartPoint() {
        double[] ret = new double[3];
        this.axis.AxisStartPoint.get(ret);
        return ret;
    }

    public void setAxesFinishPoint(double x, double y, double z) {
        axis.AxisFinishPoint = new Vector3d(x, y, z);
    }
    

    public double[] getAxesFinishPoint() {
        double[] ret = new double[3];
        this.axis.AxisFinishPoint.get(ret);
        return ret;
    }

    public double getAxisLength() {
        return axis.getLength();
    }

    public void setFill(int f) {
        this.fill = f;
    }

    public int getFill() {
        return this.fill;
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
   
    public boolean isTerminus() {
        return this.type == NTERMINUS || this.type == SSEType.CTERMINUS;
    }

    public int length() {
        return sseData.pdbFinishResidue - sseData.pdbStartResidue + 1;
    }
  
    public String getRelDirection(SecStrucElement s) {
        if (this.direction.equals(s.direction))
            return "P";
        else
            return "A";
    }

    public int getSymbolNumber() {
		return symbolNumber;
	}

	public void setSymbolNumber(int symbolNumber) {
		this.symbolNumber = symbolNumber;
	}

	public SSEType getType() {
		return type;
	}

	public void setType(SSEType type) {
		this.type = type;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public int getPDBStartResidue() {
		return sseData.pdbStartResidue;
	}

	public void setPDBStartResidue(int pDBStartResidue) {
	    sseData.pdbStartResidue = pDBStartResidue;
	}

	public int getPDBFinishResidue() {
		return sseData.pdbFinishResidue;
	}

	public void setPDBFinishResidue(int pDBFinishResidue) {
	    sseData.pdbFinishResidue = pDBFinishResidue;
	}

    public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
            sb.append(" " + sseData.pdbStartResidue + " to " + sseData.pdbFinishResidue);
        }

        return sb.toString();

    }

    public void setColour(Color c) {
        this.colour = c;
    }

    public Color getColour() {
        return this.colour;
    }

    /* START I/O methods */

}
