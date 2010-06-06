package tops.dw.editor;

import java.awt.Graphics;
import java.awt.Point;

public class Annotation {
	
	// TODO : make this the abstract base class for UserArrow and UserLabel, 
	// and make a cross class
	
	private Point location;
	
	public Annotation(Point location) {
		this.location = location;
	}
	
	public void draw(Graphics g) {
		//g.drawString("X", this.location.x, this.location.y);
		g.fillRect(this.location.x, this.location.y, 10, 10);
	}
	
	public String toString() {
		return this.location.toString();
	}

}
