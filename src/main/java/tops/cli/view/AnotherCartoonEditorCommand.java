package tops.cli.view;

import javax.swing.JFrame;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.drawing.app.TopsEditor;

public class AnotherCartoonEditorCommand implements Command {

    @Override
    public String getDescription() {
        return "Run another cartoon editor"; // XXX another?
    }

    @Override
    public void handle(String[] args) throws ParseException {
        JFrame frame = new JFrame("TOPS Designer 1.0");
        TopsEditor editor = new TopsEditor();
        editor.installInFrame(frame);
        
        frame.setLocation(750, 50); // XXX remove me
        
        frame.pack();
        frame.setSize(550,700);
        frame.setVisible(true);
        
        if (args.length > 0) {
            editor.openCartoon(args[0]);
        }
    }
    
    

}
