
package tops.dw.editor;

import java.awt.*;
import java.util.*;

/**
 * Class for labels added to the TopsDrawCanvas
 * @author David Westhead
 * @version 1.00 25 Nov. 1997
 */
public class UserLabel
{

  /* START class variables */

  static Font UseFont = new Font( "TimesRoman", Font.PLAIN, 12 );

  /* END class variables */


  /* START instance variables */

  private Color colour = Color.black;
  private StringBuffer Text = new StringBuffer();
  private Point Position = new Point(0,0);

  /* END instance variables */
 
  /* START Constructors */

  public UserLabel()
    {
      super();
    }


  /* END Constructors */


  /* START instance methods */

  public void setPosition( int x, int y )
    {
      this.Position.x = x;
      this.Position.y = y;
    }

  public void append( String text )
    {
      this.Text.append(text);
    }

  public void append( char c )
    {
      this.Text.append(c);
    }

  public String getText()
    {
      if ( this.Text == null ) return null;
      else return this.Text.toString();
    }

  public Point getPosition()
    {
      return this.Position;
    }

  public void Draw( Graphics g )
    {
      Font old_font = g.getFont();
      g.setFont(UserLabel.UseFont);
      Color old_colour = g.getColor();
      g.setColor(this.colour);

      g.drawString( this.getText(), this.Position.x, this.Position.y );

      g.setFont(old_font);
      g.setColor(old_colour);
    }

  // a method to draw as postscript (added to the vector ps)
  // remember that postscript has a different coordinate system to the canvas 
  public Vector<String> Draw( Vector<String> ps, int canv_height )
    {
      ps = PostscriptFactory.makeText("Times-Roman",12, this.getPosition().x, canv_height-this.getPosition().y, this.getText(), ps);
      return ps;
    }

  // approximate width for Postscript
  public int getPSWidth()
    {
      int n = this.getText().length();
      float len = (UserLabel.UseFont.getSize()*n);
      len = len*0.4f;
      return Math.round(len);
    }

  // approximate height for Postscript
  public int getPSHeight()
    {
      return UserLabel.UseFont.getSize();
    }

  /* END instance methods */  

}
