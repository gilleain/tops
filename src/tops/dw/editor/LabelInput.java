package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;

public class LabelInput implements ActionListener {

    static Font DisFont = new Font("TimesRoman", Font.PLAIN, 12);

    static Font BoldFont = new Font("TimesRoman", Font.BOLD, 12);

    static int WIDTH = 350;

    static int HEIGHT = 200;

    Dialog InputDialog = null;

    TextField InField = null;

    String label = null;

    boolean cancel = true;

    boolean done = false;

    public LabelInput() {

        this.InputDialog = new Dialog(new Frame(), "Label input", true);
        this.InputDialog.setVisible(false);
        this.InputDialog.setFont(LabelInput.DisFont);
        this.InputDialog.setLayout(new GridLayout(0, 1));
        this.InputDialog.setSize(LabelInput.WIDTH, LabelInput.HEIGHT);

        Panel p1 = new Panel();
        p1.setLayout(new GridLayout(2, 1));
        p1.setFont(LabelInput.DisFont);
        Label l = new Label("Enter label below");
        l.setAlignment(Label.LEFT);
        l.setFont(LabelInput.BoldFont);
        p1.add(l);
        Panel p11 = new Panel();
        this.InField = new TextField(40);
        p11.add(this.InField);
        p1.add(p11);

        Panel p2 = new Panel();
        p2.setLayout(new GridLayout(1, 2));
        p2.setFont(LabelInput.DisFont);
        Button FinishButton = new Button("Finished");
        FinishButton.addActionListener(this);
        Button CancelButton = new Button("Cancel");
        CancelButton.addActionListener(this);
        Panel p21 = new Panel();
        p21.add(FinishButton);
        Panel p22 = new Panel();
        p22.add(CancelButton);

        p2.add(p21);
        p2.add(p22);

        this.InputDialog.add(p1);
        this.InputDialog.add(p2);

    }

    /* ActionListener interface */
    /* this class listens for action events from two buttons */
    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals("Finished")) {
            this.label = this.InField.getText();
            this.cancel = false;
        } else if (ae.getActionCommand().equals("Cancel")) {
            this.label = null;
            this.cancel = true;
        }

        this.InputDialog.dispose();

    }

    public String getLabel() {
        this.InputDialog.setVisible(true);
        if (this.cancel)
            return null;
        else
            return this.label;
    }

}
