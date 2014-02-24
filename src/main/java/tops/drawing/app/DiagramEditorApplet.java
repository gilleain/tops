package tops.drawing.app;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class DiagramEditorApplet extends JApplet  {
    
    private DiagramEditorToolbar diagramEditorToolbar;
    private DiagramEditorCanvas canvas;
    
    public void init() {
        //get applet parameters
        int w = 600;
        int h = 600;
        int cw = 600;
        int ch = 400;
        try {
            w = Integer.parseInt(this.getParameter("width"));
            h = Integer.parseInt(this.getParameter("height"));
            cw = Integer.parseInt(this.getParameter("canvaswidth"));
            ch = Integer.parseInt(this.getParameter("canvasheight"));
        } catch (NullPointerException npe) {
            // not running as an applet - there may be a better way!
        }
        
        //make the components
        this.diagramEditorToolbar = new DiagramEditorToolbar(this);
        this.canvas = new DiagramEditorCanvas(cw, ch, this.diagramEditorToolbar);
        
        //add the components to the applet
        Container contentPane = this.getContentPane();
        contentPane.add(this.diagramEditorToolbar, BorderLayout.NORTH);
        contentPane.add(this.canvas, BorderLayout.CENTER);
        contentPane.setSize(w, h);
        
        this.diagramEditorToolbar.disableSelectionButton();
    }
    
    public void setState(char stateChar) {
        this.canvas.setState(stateChar);
    }
   
    public void undo() { 
        this.canvas.undo(); 
    }
    
    public void redo() { 
        this.canvas.redo(); 
    }
    
    public boolean hasMoreActionsToUndo() { 
        return this.canvas.hasMoreActionsToUndo(); 
    }
    
    public boolean hasMoreActionsToRedo() { 
        return this.canvas.hasMoreActionsToRedo(); 
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Diagram Editor");
        DiagramEditorApplet editor = new DiagramEditorApplet();
        editor.init();
        frame.add(editor);
        frame.setSize(600, 600);
        
        frame.setLocation(800, 100);    // TMP XXX FIXME!
        
        frame.setVisible(true);
    }
}
