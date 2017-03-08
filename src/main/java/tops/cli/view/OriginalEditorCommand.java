package tops.cli.view;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.dw.editor.TopsEditor;

public class OriginalEditorCommand implements Command {

    @Override
    public String getDescription() {
        return "Run the original editor";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        new TopsEditor(false, args);
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
