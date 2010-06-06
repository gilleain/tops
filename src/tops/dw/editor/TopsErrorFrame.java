package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;

public class TopsErrorFrame extends Frame implements ActionListener,
        WindowListener {

    private int WIDTH = 350, HEIGHT = 150;

    private Button OKbutton;

    public TopsErrorFrame(String Message) {

        super("TOPS error");

        this.setFont(new Font("TimesRoman", Font.PLAIN, 12));

        this.add("Center", new Label(Message));

        this.OKbutton = new Button("O.K.");
        this.OKbutton.addActionListener(this);

        this.add("South", this.OKbutton);

        this.setSize(this.WIDTH, this.HEIGHT);

        this.addWindowListener(this);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.WIDTH, this.HEIGHT);
    }

    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }

    /* the WindowListener interface */
    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.dispose();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    /* end of WindowListener interface */

}
