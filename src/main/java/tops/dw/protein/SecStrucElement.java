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
import tops.port.model.CartoonSymbol;
import tops.port.model.Direction;
import tops.port.model.SSEData;
import tops.port.model.SSEType;


public class SecStrucElement {
    
    private CartoonSymbol cartoonSymbol;

    private SSEType type;

    private Direction direction;
    
    private SSEData sseData;
    
    private List<Point> connectionTo;

    private List<Integer> bridgePartner;

    private List<String> bridgePartnerSide;

    private List<String> bridgePartnerType;

    private List<Integer> neighbour;

    private int chirality;
    
    private Axis axis;

    public SecStrucElement() {
        this.cartoonSymbol = new CartoonSymbol();
        this.sseData = new SSEData();
        this.axis = new Axis();
        this.connectionTo = new ArrayList<Point>();
        this.bridgePartner = new ArrayList<Integer>();
        this.bridgePartnerSide = new ArrayList<String>();
        this.bridgePartnerType = new ArrayList<String>();
        this.neighbour = new ArrayList<Integer>();
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
        if (axis.AxisStartPoint == null) {
            ret = new double[] {0, 0, 0};
        } else {
            this.axis.AxisStartPoint.get(ret);
        }
        return ret;
    }

    public void setAxesFinishPoint(double x, double y, double z) {
        axis.AxisFinishPoint = new Vector3d(x, y, z);
    }
    

    public double[] getAxesFinishPoint() {
        double[] ret = new double[3];
        if (axis.AxisFinishPoint == null) {
            ret = new double[] {0, 0, 0};
        } else {
            this.axis.AxisFinishPoint.get(ret);
        }
        return ret;
    }

    public double getAxisLength() {
        return axis.getLength();
    }

    public void setFill(boolean fill) {
        this.cartoonSymbol.setFill(fill);
    }

    public boolean getFill() {
        return this.cartoonSymbol.getFill();
    }

    public void placeElement(int x, int y) {
        cartoonSymbol.setCartoonX(x);
        cartoonSymbol.setCartoonY(y);
    }

    public void placeElementX(int x) {
        cartoonSymbol.setCartoonX(x);
    }

    public void placeElementY(int y) {
        cartoonSymbol.setCartoonY(y);
    }

    public Point getPosition() {
        return new Point(
                (int)cartoonSymbol.getCartoonX(),   // XXX cast 
                (int)cartoonSymbol.getCartoonY());  // XXX cast
    }

    public void setPosition(Point p) {
        cartoonSymbol.setCartoonX(p.x);
        cartoonSymbol.setCartoonY(p.y);
    }

    public void translate(int tx, int ty) {
        cartoonSymbol.setCartoonX(cartoonSymbol.getCartoonX() + tx);
        cartoonSymbol.setCartoonY(cartoonSymbol.getCartoonY() + ty);
    }


    public void setSymbolRadius(int radius) {
        this.cartoonSymbol.setRadius(radius);
    }

    public int getSymbolRadius() {
        return cartoonSymbol.getRadius();
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
		return cartoonSymbol.getSymbolNumber();
	}

	public void setSymbolNumber(int symbolNumber) {
		cartoonSymbol.setSymbolNumber(symbolNumber);
	}

	public SSEType getType() {
		return type;
	}

	public void setType(SSEType type) {
		this.type = type;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
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
		return cartoonSymbol.getLabel();
	}

	public void setLabel(String label) {
		this.cartoonSymbol.setLabel(label);
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
        this.cartoonSymbol.setColor(c);
    }

    public Color getColour() {
        return this.cartoonSymbol.getColor();
    }

}
