package tops.cli.engine.classification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.data.db.update.DBManager;
import tops.data.db.update.DBUpdater;
import tops.data.db.update.FatalException;
import tops.model.classification.CATHTree;
import tops.model.classification.ClassificationTree;
import tops.model.classification.SCOPTree;

public class DBUpdateCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {

        // CATH or SCOP
        String classification = args[0];

        // the graph (string) database parameters
        String graphDBName = args[1];
        String graphDBUserName = args[2];
        String graphDBURL = args[3];
        String graphDBPass = args[4];

        // the location of the string file
        String scratchDir = args[5];
        String stringFileName = args[6];

        // the location of the rep classification file
        String classifDir = args[7];
        String classifFileName = args[8];

        // a flag to indicate whether to reset the database or not
        String resetDB = args[9];

        // control the logging level
        if (args.length > 10) {
            String logLevelStr = args[10];
            Level logLevel = Level.parse(logLevelStr);

            Logger.getLogger("tops.db.update.DBUpdater").setUseParentHandlers(
                    false);
            Logger.getLogger("tops.db.update.DBUpdater").setLevel(logLevel);

            Handler handler = new ConsoleHandler();
            handler.setFormatter(new Formatter() {

                @Override
                public String format(LogRecord r) {
                    return r.getLevel() + " " + r.getMessage() + "\n";
                }
            });
            handler.setLevel(logLevel);

            Logger.getLogger("tops.db.update.DBUpdater").addHandler(handler);
        }

        try {

            // attempt to connect to the database before doing anything else
            DBManager dbManager = null;
            try {
                dbManager = new DBManager(graphDBName, graphDBUserName,
                        graphDBURL, graphDBPass);
            } catch (ClassNotFoundException cnfe) {
                throw new FatalException("Database connection class not found!");
            }

            // only reset the db if asked to
            if (resetDB.equals("true")) {
                DBUpdater.resetDB(dbManager);
            }

            // read in the data from files
            ArrayList<String> topsStrings = DBUpdater.readStringFile(scratchDir, stringFileName);
            ClassificationTree tree;
            if (classification.equals("CATH")) {
                tree = CATHTree.fromFile(new File(classifDir, classifFileName));
            } else if (classification.equals("SCOP")) {
                tree = SCOPTree.fromFile(new File(classifDir, classifFileName));
            } else {
                throw new FatalException(
                        "Unknown domain classification scheme : "
                                + classification);
            }

            // finally, write the data to the database
            DBUpdater.updateDB(dbManager, topsStrings, tree, classification);

        } catch (FatalException fe) {
            System.err.println(fe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
}
