package tops.cli.view;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.view.app.TOPSViewer;

public class MultiViewerCommand implements Command {

    @Override
    public String getDescription() {
        return "View multiple diagrams";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        try {
            new TOPSViewer(args[0], args);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            System.out.println(aioobe + " No file specified : using default!");
            new TOPSViewer("test.str", args);
        }
    }

}
