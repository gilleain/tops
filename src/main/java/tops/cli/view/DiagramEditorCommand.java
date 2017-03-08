package tops.cli.view;

import javax.swing.JFrame;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.drawing.app.DiagramEditorApplet;

public class DiagramEditorCommand implements Command {

    @Override
    public String getDescription() {
        return "Run the diagram editor";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        JFrame frame = new JFrame("Diagram Editor");
        DiagramEditorApplet editor = new DiagramEditorApplet();
        editor.init();
        frame.add(editor);
        frame.setSize(600, 600);
        
        frame.setLocation(800, 100);    // TMP XXX FIXME!
        
        frame.setVisible(true);
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }
}
