package tops.dw.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import tops.view.cartoon.PostscriptFactory;

public class UserArrow {

    Point start = null;
    Point end = null;

    public UserArrow() {
        this.start = new Point(0, 0);
        this.end = new Point(0, 0);
    }

    public void setStart(Point p) {
        this.start.x = p.x;
        this.start.y = p.y;
    }

    public void setEnd(Point p) {
        this.end.x = p.x;
        this.end.y = p.y;
    }

    public Point getStart() {
        return this.start;
    }

    public Point getEnd() {
        return this.end;
    }

    public void draw(Graphics g) {
        Color oldColour = g.getColor();
        g.setColor(Color.lightGray);
        g.drawLine(this.start.x, this.start.y, this.end.x, this.end.y);
        g.setColor(oldColour);
    }

    // a method to draw as postscript (added to the vector ps)
    // remember that postscript has a different coordinate system to the canvas
    public List<String> draw(int canvHeight) {
        List<String> ps = new ArrayList<>();

        ps.add(PostscriptFactory.newPath());
        ps.add(PostscriptFactory.makeMove(this.start.x, canvHeight - this.start.y));
        ps.add(PostscriptFactory.makeLine(this.end.x, canvHeight - this.end.y));
        ps.add(PostscriptFactory.setColour(Color.lightGray));
        ps.add(PostscriptFactory.stroke());
        ps.add(PostscriptFactory.setColour(Color.black));

        return ps;
    }

}
