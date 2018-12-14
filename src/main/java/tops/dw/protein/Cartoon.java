package tops.dw.protein;

import static tops.port.model.Direction.DOWN;
import static tops.port.model.Direction.UP;
import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import tops.dw.editor.Annotation;
import tops.port.model.Direction;
import tops.port.model.SSEType;

public class Cartoon {
    
    private List<SecStrucElement> sses;
    
    private List<SecStrucElement> fixed;
    
    private List<Annotation> annotations;  
    
    public Cartoon(SecStrucElement... elements) {
        this.sses = new ArrayList<>();
        for (SecStrucElement element : elements) {
            sses.add(element);
        }
        
        this.fixed = new ArrayList<>();
        
        this.annotations = new ArrayList<>();
    }
    
    public Cartoon() {
        // TODO Auto-generated constructor stub
        this(new SecStrucElement[]{}); // XXX ugh
    }

    public void translateFixed(int tx, int ty) {
        for (SecStrucElement s : fixed) {
            s.translate(tx, ty);
        }
    }
    
    public int getFixNumRes() {
        int size = 0;

        for (SecStrucElement t : fixed) {
            size += t.length();
        }

        return size;
    }
    
    public SecStrucElement addSymbol(SSEType type, Direction direction, int x, int y, SecStrucElement selectedSymbol) {
        int defaultSeparation = 30; // ARBITRARY!
        int defaultRadius = 10; // ARBITRARY!
        
        SecStrucElement newSSE = new SecStrucElement();
        newSSE.setType(type);
        newSSE.setDirection(direction);
        newSSE.placeElement(x, y);
        newSSE.setSymbolRadius(defaultRadius);
        
        if (this.sses.isEmpty()) {
            // make N and C terminii
            SecStrucElement nTerminus = new SecStrucElement();
            nTerminus.setType(NTERMINUS);
            nTerminus.setDirection(UP);
            nTerminus.setLabel("N");
            nTerminus.placeElement(x - defaultSeparation, y); // ARBITRARY!
            nTerminus.setSymbolRadius(defaultRadius);

            SecStrucElement cTerminus = new SecStrucElement();
            cTerminus.setType(CTERMINUS);
            cTerminus.setDirection(UP);
            cTerminus.setLabel("C");
            cTerminus.placeElement(x + defaultSeparation, y); // ARBITRARY!
            cTerminus.setSymbolRadius(defaultRadius);

        } else {
            // deal with the C-terminus in a special way - add the new SSE
            // _before_ it
            if (selectedSymbol.getType() == CTERMINUS) {
                sses.add(sses.indexOf(selectedSymbol), newSSE);
            } else {
                sses.add( newSSE);
            }
        }
        
        return newSSE;
    }
    
    public void delete(SecStrucElement selectedSymbol) {
        // can't delete terminii!
        if (selectedSymbol.getType() == NTERMINUS || selectedSymbol.getType() == CTERMINUS) {
            System.err.println("can't delete terminii!");
            return;
        }

        // delete terminii if we delete the final symbol
        if (sses.size() == 3) { // selected symbol plus two terminii
            sses.remove(selectedSymbol);
        }
    }

    public void flipMultiple(List<SecStrucElement> list) {
        for (SecStrucElement s : list) {
            this.flip(s);
        }
    }

    public void flip(SecStrucElement s) {
        if (s.getDirection() == UP) {
            s.setDirection(DOWN);
        } else if (s.getDirection() == DOWN) {
            s.setDirection(UP);
        }
    }
    
    public List<SecStrucElement> selectContained(Rectangle r) {
        List<SecStrucElement> list = new ArrayList<>();
        for (SecStrucElement s : sses) {
            Point pos = s.getPosition();
            if (r.contains(pos)) {
                list.add(s);
            }
        }
        return list;
    }
    
    public SecStrucElement selectByPosition(Point p) {
        SecStrucElement selected = null;
    
        if (p != null) {
            double minsep = Double.POSITIVE_INFINITY;
            for (SecStrucElement s : sses) {
                Point ps = s.getPosition();
                double sep = separation(p, ps);
                if (sep < s.getSymbolRadius() && sep < minsep) {
                    minsep = sep;
                    selected = s;
                }
            }
        }
        return selected;
    }
    
    public String convertStructureToString() {
        StringBuilder topsString = new StringBuilder();
        for (SecStrucElement s : sses) {
            char type = s.getType().getOneLetterName().charAt(0);
            type = (s.getDirection() == Direction.DOWN) ? 
                    Character.toLowerCase(type) : type;
            topsString.append(type);
        }
        return topsString.toString();
    }
    
    public void highlightByResidueNumber(int[] residueNumbers) {
        int index = 0;
        SecStrucElement last = null;
        for (SecStrucElement s : sses) {
            System.out.println("sse " + s + " trying residue " + residueNumbers[index]);
            while (tryHighlightingByResidueNumber(s, last, residueNumbers[index])) {
                System.out.println("highlighting " + residueNumbers[index]);
                index++;
                if (index >= residueNumbers.length) {
                    return;
                }
            }
            last = s;
        }
    }
    
    public void highlightByResidueNumber(int residueNumber) {
        SecStrucElement last = null;
        for (int index = 0; index < sses.size(); index++) {
            SecStrucElement ss = sses.get(index);
            if (tryHighlightingByResidueNumber(ss, last, residueNumber)) {
                return;
            }
            last = ss;
        }
    }
    
    private boolean tryHighlightingByResidueNumber(SecStrucElement sse, SecStrucElement last, int residueNumber) {
        if (sse.containsResidue(residueNumber)) {
            sse.setColour(Color.YELLOW);
            return true;
        } else {
            int loopStart;
            if (last == null) {
                loopStart = 0;
            } else {
                loopStart = last.getPDBFinishResidue();
            }
            
            if (loopStart <= residueNumber && residueNumber <= sse.getPDBStartResidue()) {
                annotateConnection(last, sse);    // FIXME!
                return true;
            }
        }
        return false;
    }
    
    public void annotateConnection(SecStrucElement a, SecStrucElement b) {
        Point pA = a.getPosition();
        Point pB = b.getPosition();
        int x = (int)((pA.x / 2.0) + (pB.x / 2.0));
        int y = (int)((pA.y / 2.0) + (pB.y / 2.0));
        Point midPoint = new Point(x, y);
        Annotation annotation = new Annotation(midPoint);
        this.annotations.add(annotation);
        System.out.println("adding annotation " + annotation);
    }
    

    public Rectangle epsBoundingBox(int h) {

        int xmin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE;
        int ymax = Integer.MIN_VALUE;
        int rmax = Integer.MIN_VALUE;

        Point pos;
        int rad;
        int x;
        int y;
        for (SecStrucElement s : sses) {
            pos = s.getPosition();
            x = pos.x;
            y = h - pos.y;
            rad = s.getSymbolRadius();
            if (x > xmax)
                xmax = x;
            if (y > ymax)
                ymax = y;
            if (x < xmin)
                xmin = x;
            if (y < ymin)
                ymin = y;
            if (rad > rmax)
                rmax = rad;
        }
        xmax += rmax;
        ymax += rmax;
        xmin -= rmax;
        ymin -= rmax;

        for (SecStrucElement s : sses) {
            if (!(s.getConnectionTo().isEmpty())) {
                for (Point pointTo : s.getConnectionTo()) {
                    x = pointTo.x;
                    y = h - pointTo.y;
                    if (x > xmax)
                        xmax = x;
                    if (y > ymax)
                        ymax = y;
                    if (x < xmin)
                        xmin = x;
                    if (y < ymin)
                        ymin = y;
                }
            }
        }

        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }
    
    /* private method calculates the separation of two points */
    /* one day I'll put it in a more sensible place for re-use */
    /* ...and one day, it has! gmt 13/05/08 */
    private double separation(Point p1, Point p2) {
        int sep = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
        return Math.sqrt(sep);
    }
    
    public SecStrucElement getSSEByNumber(int num) {
        return sses.get(num);
    }

    /* private method to apply a scaling value to the diagram */
    public void applyScale(float scale) {
        Point p;
        int x;
        int y;
        int r;
        
        for (SecStrucElement s : sses) {

            p = s.getPosition();
            x = p.x;
            y = p.y;
            x = Math.round(scale * x);
            y = Math.round(scale * y);
            p.x = x;
            p.y = y;

            r = s.getSymbolRadius();
            s.setSymbolRadius(Math.round(r * scale));
            for (Point pc : s.getConnectionTo()) {
                x = pc.x;
                y = pc.y;
                x = Math.round(scale * x);
                y = Math.round(scale * y);
                pc.x = x;
                pc.y = y;
            }

        }

    }
    
    /**
     * this methods applies the inverse of the scale to the diagram
     */
    public void invertScale(float scale) {
        if (scale > 0.0)
            scale = 1.0F / scale;
        else
            scale = 1.0F;
        this.applyScale(scale);
    }
    
    public void invertY() {
        for (SecStrucElement s : sses) {
            s.getPosition().y *= -1;
            for (Point p : s.getConnectionTo()) {
                p.y *= -1;
            }
        }
    }

    public void reflectXY() {
        for (SecStrucElement s : sses) {
            if (s.getDirection() == DOWN) {
                s.setDirection(UP);
            } else if (s.getDirection().equals(UP)) {
                s.setDirection(DOWN);
            }
        }
    }

    public void reflectZX() {
        int y;
        for (SecStrucElement s : sses) {
            Point p = s.getPosition();
            y = p.y * (-1);
            p.y = y;
            for (Point pc : s.getConnectionTo()) {
                y = pc.y * (-1);
                pc.y = y;
            }
        }
    }

    public void reflectYZ() {
        int x;
        for (SecStrucElement ss : sses) {
            Point p = ss.getPosition();
            x = p.x * (-1);
            p.x = x;
            for (Point pc : ss.getConnectionTo()) {
                x = pc.x * (-1);
                pc.x = x;
            }
        }
    }

    public void rotateX() {
        this.reflectZX();
        this.reflectXY();
    }

    public void rotateY() {
        this.reflectYZ();
        this.reflectXY();
    }

    public void rotateZ() {
        this.reflectYZ();
        this.reflectZX();
    }
    
    /**
     * Translate and scale the diagram to fit within a rectangle.
     * 
     * @param x the upper left x
     * @param y the upper left y
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @param b the border
     */
    public void fitToRectangle(int x, int y, int w, int h, int b) {
        Rectangle bb = this.boundingBox();
        float s1 = (float)(w - (2 * b)) / (float)(bb.width);
        float s2 = (float)(h - (2 * b)) / (float)(bb.height);
        float s = Math.min(s1, s2);
        if (s > 1.0F) { s = 1.0F; }
        System.out.print(s);
        this.applyScale(s);
        Point centroid = this.topsCentroid();
        int dy = -centroid.y + (h / 2);
        int dx = -centroid.x + (w / 2);
        this.translateDiagram(dx, dy);
    }
    
    public Rectangle boundingBox() {
        int minx = 0;
        int maxx = 0;
        int miny = 0;
        int maxy = 0;
        int maxr = 0;

        for (SecStrucElement ss : sses) {

            int x = ss.getPosition().x;
            int y = ss.getPosition().y;
            int r = ss.getSymbolRadius();

            if (x < minx)
                minx = x;
            if (x > maxx)
                maxx = x;
            if (y < miny)
                miny = y;
            if (y > maxy)
                maxy = y;

            if (r > maxr)
                maxr = r;
        }

        return new Rectangle(minx - maxr, miny - maxr, maxx - minx + 2 * maxr,
                maxy - miny + 2 * maxr);
    }
    
    public Point topsCentroid() {
        int n = 1;
        int centx = 0;
        int centy = 0;
        for (SecStrucElement ss : sses) {
            n++;
            centx += ss.getPosition().x;
            centy += ss.getPosition().y;
        }

        centx = (int) (((float) centx) / ((float) n));
        centy = (int) (((float) centy) / ((float) n));

        return new Point(centx, centy);
    }
    
    
    /**
     * this method translates the diagram, it does not do a repaint
     * 
     * @param x -
     *            the x translation to apply
     * @param y -
     *            the y translation to apply
     */
    public void translateDiagram(int x, int y) {
        for (SecStrucElement s : sses) {
            s.getPosition().x += x;
            s.getPosition().y += y;

            for (Point p : s.getConnectionTo()) {
                p.x += x;
                p.y += y;
            }
        }
    }

    public List<SecStrucElement> getSSEs() {
        return sses;
    }

    public void addSSE(SecStrucElement s) {
        sses.add(s);
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
}
