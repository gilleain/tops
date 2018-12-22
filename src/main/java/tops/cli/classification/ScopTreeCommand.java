package tops.cli.classification;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.BaseCommand;
import tops.model.classification.SCOPTree;

public class ScopTreeCommand extends BaseCommand {

    @Override
    public String getDescription() {
        return "Print a SCOP tree";
    }
    
    @Override
    public String getHelp() {
        return "<filename>";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        try {
            SCOPTree tree = SCOPTree.fromFile(new File(args[0]));
            tree.printToStream(System.out);
        } catch (IOException ioe) {
            error(ioe);
        }
    }
}
