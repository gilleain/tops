
package tops.dw.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import tops.view.cartoon.PostscriptFactory;

/**
 * Class for labels added to the TopsDrawCanvas
 * 
 * @author David Westhead
 * @version 1.00 25 Nov. 1997
 */
public class UserLabel {

    private static final Font USE_FONT = new Font("TimesRoman", Font.PLAIN, 12);

    private Color colour = Color.black;
    private StringBuilder text = new StringBuilder();
    private Point position = new Point(0, 0);

    public UserLabel() {
        super();
    }

    public void setPosition(int x, int y) {
        this.position.x = x;
        this.position.y = y;
    }

    public void append(String text) {
        this.text.append(text);
    }

    public void append(char c) {
        this.text.append(c);
    }

    public String getText() {
        if (this.text == null)
            return null;
        else
            return this.text.toString();
    }

    public Point getPosition() {
        return this.position;
    }

    public void draw(Graphics g) {
        Font oldFont = g.getFont();
        g.setFont(UserLabel.USE_FONT);
        Color oldColour = g.getColor();
        g.setColor(this.colour);

        g.drawString(this.getText(), this.position.x, this.position.y);

        g.setFont(oldFont);
        g.setColor(oldColour);
    }

    // a method to draw as postscript (added to the vector ps)
    // remember that postscript has a different coordinate system to the canvas
    public List<String> draw(List<String> ps, int canvHeight) {
        return PostscriptFactory.makeText("Times-Roman", 12, this.getPosition().x, canvHeight - this.getPosition().y,
                this.getText(), ps);
    }

    // approximate width for Postscript
    public int getPSWidth() {
        int n = this.getText().length();
        float len = (UserLabel.USE_FONT.getSize() * n);
        len = len * 0.4f;
        return Math.round(len);
    }

    // approximate height for Postscript
    public int getPSHeight() {
        return UserLabel.USE_FONT.getSize();
    }
}
