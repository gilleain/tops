package tops.web.display.applet;

import java.applet.Applet;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.Panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;

public class SimpleEditorApplet extends Applet implements ActionListener,
        ItemListener {

    private TopsDrawCanvas canvas;

    // basic buttons
    private Panel buttonPanel;

    private Button toStringButton;

    private Button clearButton;

    // checkboxes that would ideally be JToggleButtons if using swing
    private Panel checkboxPanel;

    private CheckboxGroup controls;

    private Checkbox addStrandCheck;

    private Checkbox addHelixCheck;

    private Checkbox addHBondCheck;

    private Checkbox selectSymbolCheck;

    private Checkbox flipSymbolsCheck;

    private Checkbox flipMultipleSymbolsCheck;

    private Checkbox lineLayoutCheck;

    private Checkbox circleLayoutCheck;

    private Checkbox moveSymbolsCheck;

    private Checkbox deleteSymbolsCheck;

    public SimpleEditorApplet() {
        this.buttonPanel = new Panel();
        this.buttonPanel.setLayout(new GridLayout(1, 2));

        this.toStringButton = new Button("To String");
        this.toStringButton.addActionListener(this);
        this.toStringButton.setActionCommand("tostring");
        this.buttonPanel.add(this.toStringButton);

        this.clearButton = new Button("Clear");
        this.clearButton.addActionListener(this);
        this.clearButton.setActionCommand("clear");
        this.buttonPanel.add(this.clearButton);

        this.checkboxPanel = new Panel();
        this.checkboxPanel.setLayout(new GridLayout(10, 1));

        this.controls = new CheckboxGroup();

        this.addStrandCheck = new Checkbox("Add Strand", true, this.controls);
        this.addStrandCheck.addItemListener(this);
        this.checkboxPanel.add(this.addStrandCheck);

        this.addHelixCheck = new Checkbox("Add Helix", false, this.controls);
        this.addHelixCheck.addItemListener(this);
        this.checkboxPanel.add(this.addHelixCheck);

        this.addHBondCheck = new Checkbox("Add HBonds", false, this.controls);
        this.addHBondCheck.addItemListener(this);
        this.checkboxPanel.add(this.addHBondCheck);

        this.selectSymbolCheck = new Checkbox("Select Symbol", false, this.controls);
        this.selectSymbolCheck.addItemListener(this);
        this.checkboxPanel.add(this.selectSymbolCheck);

        this.flipSymbolsCheck = new Checkbox("Flip Symbols", false, this.controls);
        this.flipSymbolsCheck.addItemListener(this);
        this.checkboxPanel.add(this.flipSymbolsCheck);

        this.flipMultipleSymbolsCheck = new Checkbox("Multi-Flip Symbols", false,
                this.controls);
        this.flipMultipleSymbolsCheck.addItemListener(this);
        this.checkboxPanel.add(this.flipMultipleSymbolsCheck);

        this.lineLayoutCheck = new Checkbox("Sheet Layout", false, this.controls);
        this.lineLayoutCheck.addItemListener(this);
        this.checkboxPanel.add(this.lineLayoutCheck);

        this.circleLayoutCheck = new Checkbox("Barrel Layout", false, this.controls);
        this.circleLayoutCheck.addItemListener(this);
        this.checkboxPanel.add(this.circleLayoutCheck);

        this.moveSymbolsCheck = new Checkbox("Move Symbol", false, this.controls);
        this.moveSymbolsCheck.addItemListener(this);
        this.checkboxPanel.add(this.moveSymbolsCheck);

        this.deleteSymbolsCheck = new Checkbox("Delete Symbol", false, this.controls);
        this.deleteSymbolsCheck.addItemListener(this);
        this.checkboxPanel.add(this.deleteSymbolsCheck);

        this.setLayout(new GridLayout(2, 2));
        this.add(this.buttonPanel);

        this.canvas = new TopsDrawCanvas();
        this.add(this.canvas);
        this.add(this.checkboxPanel);
    }

    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command.equals("tostring")) {
            String result = this.canvas.convertStructureToString();
            System.out.println("result : " + result);
        } else if (command.equals("clear")) {
            this.canvas.clear();
        }
    }

    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            ItemSelectable source = ie.getItemSelectable();

            if (source == this.addStrandCheck) {
                System.out.println("add strand mode");
                this.canvas.setEditMode(TopsDrawCanvas.ADD_STRAND_MODE);
            } else if (source == this.addHelixCheck) {
                System.out.println("add helix mode");
                this.canvas.setEditMode(TopsDrawCanvas.ADD_HELIX_MODE);
            } else if (source == this.addHBondCheck) {
                System.out.println("add hbond mode");
                this.canvas.setEditMode(TopsDrawCanvas.ADD_HBOND_MODE);
            } else if (source == this.selectSymbolCheck) {
                System.out.println("select symbol mode");
                this.canvas.setEditMode(TopsDrawCanvas.SELECT_SYMBOL_MODE);
            } else if (source == this.flipSymbolsCheck) {
                System.out.println("flip symbols mode");
                this.canvas.setEditMode(TopsDrawCanvas.FLIP_MODE);
            } else if (source == this.flipMultipleSymbolsCheck) {
                System.out.println("flip multiple symbols mode");
                this.canvas.setEditMode(TopsDrawCanvas.FLIP_MULTIPLE_MODE);
            } else if (source == this.lineLayoutCheck) {
                System.out.println("layout lines mode");
                this.canvas.setEditMode(TopsDrawCanvas.LINEAR_LAYOUT_MODE);
            } else if (source == this.circleLayoutCheck) {
                System.out.println("layout barrels mode");
                this.canvas.setEditMode(TopsDrawCanvas.CIRCULAR_LAYOUT_MODE);
            } else if (source == this.moveSymbolsCheck) {
                System.out.println("move symbols mode");
                this.canvas.setEditMode(TopsDrawCanvas.MOVE_SYMBOLS_MODE);
            } else if (source == this.deleteSymbolsCheck) {
                System.out.println("delete symbols mode");
                this.canvas.setEditMode(TopsDrawCanvas.DELETE_SYMBOLS_MODE);
            }
        }
    }
}
