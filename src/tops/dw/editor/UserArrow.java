package tops.dw.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Vector;

public class UserArrow
{
  
  Point start = null;
  Point end = null;

  public UserArrow()
    {
      this.start = new Point(0,0);
      this.end = new Point(0,0);
    }

  public void setStart( Point p )
    {
      this.start.x = p.x;
      this.start.y = p.y;
    }

  public void setEnd( Point p )
    {
      this.end.x = p.x;
      this.end.y = p.y;
    }

  public Point getStart()
    {
      return this.start;
    }

  public Point getEnd()
    {
      return this.end;
    }

  public void Draw( Graphics g )
    {
      Color old_colour = g.getColor();
      g.setColor( Color.lightGray );

      g.drawLine( this.start.x, this.start.y, this.end.x, this.end.y );

      g.setColor(old_colour);

    }

  // a method to draw as postscript (added to the vector ps)
  // remember that postscript has a different coordinate system to the canvas 
  public Vector<String> Draw( Vector<String> ps, int canv_height )
    {

      if ( ps == null ) ps = new Vector<String>();

      ps.addElement( PostscriptFactory.newPath() );
      ps.addElement( PostscriptFactory.makeMove( this.start.x, canv_height-this.start.y ) );
      ps.addElement( PostscriptFactory.makeLine( this.end.x, canv_height-this.end.y ) );
      ps.addElement( PostscriptFactory.setColour( Color.lightGray ) );
      ps.addElement( PostscriptFactory.stroke() );
      ps.addElement( PostscriptFactory.setColour( Color.black ) );

      return ps;
    }

}
