package tops.drawing.app;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * @author maclean
 *
 */
public class DiagramEditorToolbar extends JPanel implements ActionListener {
    
    private DiagramEditorApplet controller;
    
    private JButton selectButton;
    private JButton upStrandButton;
    private JButton downStrandButton;
    private JButton upHelixButton;
    private JButton downHelixButton;
    private JButton hBondButton;
    private JButton leftChiralButton;
    private JButton rightChiralButton;
    private JButton undoButton;
    private JButton redoButton;
    private JButton deleteButton;
    private JButton flipButton;
    
    public DiagramEditorToolbar(DiagramEditorApplet controller) {
        this.controller = controller;
        
        this.selectButton = new JButton("Select");
        this.upStrandButton = new JButton("Up Strand");
        this.downStrandButton = new JButton("Down Strand");
        this.upHelixButton = new JButton("Up Helix");
        this.downHelixButton = new JButton("Down Helix");
        
        this.hBondButton = new JButton("HBond");
        this.leftChiralButton = new JButton("LChiral");
        this.rightChiralButton = new JButton("RChiral");
        this.undoButton = new JButton("Undo");
        this.redoButton = new JButton("Redo");
        
        this.deleteButton = new JButton("Delete");
        this.flipButton = new JButton("Flip");
        
        this.setLayout(new GridLayout(3, 5));
        
        this.add(this.selectButton);
        this.add(this.upStrandButton);
        this.add(this.downStrandButton);
        this.add(this.upHelixButton);
        this.add(this.downHelixButton);
        
        this.add(this.hBondButton);
        this.add(this.leftChiralButton);
        this.add(this.rightChiralButton);
        this.add(this.undoButton);
        this.add(this.redoButton);
        
        this.add(this.deleteButton);
        this.add(this.flipButton);
        
        this.selectButton.addActionListener(this);
        this.upStrandButton.addActionListener(this);
        this.downStrandButton.addActionListener(this);
        this.upHelixButton.addActionListener(this);
        this.downHelixButton.addActionListener(this);
        
        this.hBondButton.addActionListener(this);
        this.leftChiralButton.addActionListener(this);
        this.rightChiralButton.addActionListener(this);
        this.undoButton.addActionListener(this);
        this.redoButton.addActionListener(this);
        
        this.deleteButton.addActionListener(this);
        this.flipButton.addActionListener(this);
        
        //start with both buttons disabled
        this.toggleUndoButton(false);
        this.toggleRedoButton(false);
    }
    
    public void actionPerformed(ActionEvent ae) {
        JButton source = (JButton) ae.getSource();
        
        if (source == this.undoButton) {
            this.controller.undo();
            
            //when we run out of actions to undo, switch off the button
            if (!this.controller.hasMoreActionsToUndo()) {
                this.toggleUndoButton(false);
            }
        } else if (source == this.redoButton) {
            this.controller.redo();
            
            //when we run out of actions to redo, switch off the button
            if (!this.controller.hasMoreActionsToRedo()) {
                this.toggleRedoButton(false);
            }
        } else if (source == this.selectButton) {
            this.controller.setState('S');
        } else if (source == this.upStrandButton) {
            this.controller.setState('E');
        } else if (source == this.downStrandButton) {
            this.controller.setState('e');
        } else if (source == this.upHelixButton) {
            this.controller.setState('H');
        } else if (source == this.downHelixButton) {
            this.controller.setState('h');
        } else if (source == this.hBondButton) {
            this.controller.setState('B');
        } else if (source == this.leftChiralButton) {
            this.controller.setState('L');
        } else if (source == this.rightChiralButton) {
            this.controller.setState('R');
        } else if (source == this.deleteButton) {
            this.controller.setState('D');
        } else if (source == this.flipButton) {
            this.controller.setState('F');
        }
    }
    
    public void toggleUndoButton(boolean state) { 
        this.undoButton.setEnabled(state); 
    }
    
    public void toggleRedoButton(boolean state) { 
        this.redoButton.setEnabled(state); 
    }
    
    public void enableSelectionButton() { 
        this.selectButton.setEnabled(true); 
    }
    
    public void disableSelectionButton() { 
        this.selectButton.setEnabled(false); 
    }
}
