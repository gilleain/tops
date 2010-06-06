package tops.dw.editor;

import java.awt.*;

public class PleaseWaitFrame extends Frame {

    private int WIDTH = 350, HEIGHT = 150;

    public PleaseWaitFrame(String Message) {

        super("Please wait");

        this.setFont(new Font("TimesRoman", Font.PLAIN, 12));

        this.add("Center", new Label(Message));

        this.setSize(this.WIDTH, this.HEIGHT);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.WIDTH, this.HEIGHT);
    }

}
