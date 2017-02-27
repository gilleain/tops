package tops.cli.classification;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.model.classification.CATHTree;

public class CathTreeCommand implements Command {

    @Override
    public String getDescription() {
        return "Print a CATH tree";
    }

    @Override
    public void handle(String[] args) throws ParseException {
        try {
            CATHTree tree = CATHTree.fromFile(new File(args[0]));
            tree.printToStream(System.out);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

}
