package tops.data.db.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import tops.engine.TParser;
import tops.model.classification.ClassificationTree;

/**
 * This is where the strings and cartoons produced by the
 * ClassificationConverter subclasses are loaded into the database/filesystem.
 */

public class DBUpdater {

    private static Logger LOG = Logger.getLogger("tops.db.update.DBUpdater");

    /**
     * Convert a file of "TOPS Strings" (graphs in string form) into a list of
     * Strings.
     * 
     * @param scratchDirectory
     *            the directory used for temporary storage
     * @param filename
     *            the name of the stringfile
     * @return an ArrayList of Strings
     */

    public static ArrayList<String> readStringFile(
    		String scratchDirectory, String filename) throws FatalException {
        File file;

        try {
            file = new File(scratchDirectory, filename);
        } catch (NullPointerException npe) {
            throw new FatalException("Has the scratch directory been deleted?");
        }

        // let the user know what's happening
        DBUpdater.LOG.info("Reading strings from " + file);

        String line;
        BufferedReader in;
        ArrayList<String> stringList = new ArrayList<String>();

        try {
            in = new BufferedReader(new FileReader(file));
            while ((line = in.readLine()) != null) {
                stringList.add(line);
            }
            in.close();
            return stringList;
        } catch (IOException ioe) {
            throw new FatalException("Problem with the string file!");
        }
    }

    /**
     * Clear any existing data, and reset the AUTO_INCREMENT.
     * 
     * @param dbManager
     *            a connection wrapper utility class
     */

    public static void resetDB(DBManager dbManager) {
        DBUpdater.LOG.info("resetting " + dbManager);

        // clear the existing data
        DBUpdater.LOG.info("deleting everything from TOPS_nr and TOPS_instance_nr");
        dbManager.doInsert("DELETE FROM TOPS_nr");
        dbManager.doInsert("DELETE FROM TOPS_instance_nr");

        // reset the auto increment on the group_id
        DBUpdater.LOG.info("resetting auto_increment on TOPS_nr");
        dbManager.doInsert("ALTER TABLE TOPS_nr AUTO_INCREMENT = 0");
    }

    /**
     * Do the update, adding the string data to the database and adding
     * classification info.
     * 
     * @param dbManager
     *            a connection wrapper utility class
     * @param topsStrings
     *            a list of Strings - the data to update
     * @param tree
     *            a classification tree, used to determine rep info
     * @param scheme
     *            the name of the classification scheme (CATH or SCOP,
     *            basically)
     */

    public static void updateDB(DBManager dbManager, ArrayList<String> topsStrings,
            ClassificationTree tree, String scheme) throws FatalException {

        // let the user know what database we are using. Would be nice if it
        // indicated if a connection was available...
        DBUpdater.LOG.info("Updating database " + dbManager);
        Connection dbConnection = dbManager.getConnection();

        int gcCounter = 0;

        PreparedStatement groupQueryStatement, insertIntoInstanceNrStatement, insertIntoNrStatement, newIDQueryStatement;
        try {
            // BINARY is necessary here to ensure case-sensitivity!
            groupQueryStatement = dbConnection
                    .prepareStatement("SELECT group_id FROM TOPS_nr WHERE vertex_string = BINARY ? AND edge_string = ?");
            insertIntoInstanceNrStatement = dbConnection
                    .prepareStatement("INSERT INTO TOPS_instance_nr VALUES (?, ?, ?, ?, ?)");
            insertIntoNrStatement = dbConnection
                    .prepareStatement("INSERT INTO TOPS_nr VALUES (NULL, ?, ?)");
            newIDQueryStatement = dbConnection
                    .prepareStatement("SELECT last_insert_id() AS newID FROM TOPS_nr");

        } catch (SQLException s) {
            throw new FatalException("cannot prepare statements");
        }

        TParser parser = new TParser();
        Iterator<String> itr = topsStrings.iterator();

        // chop each string up into it's component parts,
        // query the body and tail together to get the group
        // insert into TOPS_nr and TOPS_instance_nr
        while (itr.hasNext()) {
            String topsString = (String) itr.next();

            // break the string into bits
            parser.load(topsString);
            String domID = parser.getName();
            String vertexString = parser.getVertexString();
            String edgeString = parser.getEdgeString();

            // do a quick check to see if this domain is in the classification
            // tree!
            if (!tree.isDomainIDInTree(domID)) {
                DBUpdater.LOG.warning("Domain " + domID + " not found in the tree");
                continue;
            }

            // get the classification string from the tree
            String classif = tree.getNumberForDomainID(domID);

            // get the rep status (if any) of this example from the tree
            int highestRep = tree.getHighestRep(domID);

            int groupID = -1;
            try {

                // make a query to test for the group (if any) of this domid
                groupQueryStatement.setString(1, vertexString);
                groupQueryStatement.setString(2, edgeString);
                ResultSet groupResult = groupQueryStatement.executeQuery();

                if (groupResult.first()) {

                    // if there is a group_id for this tops graph, get it and
                    // use it
                    groupID = groupResult.getInt("group_id");

                } else {
                    // otherwise, insert the new graph and get its group id from
                    // the db
                    insertIntoNrStatement.setString(1, vertexString);
                    insertIntoNrStatement.setString(2, edgeString);
                    insertIntoNrStatement.executeUpdate();

                    ResultSet newGroupIDResult = newIDQueryStatement
                            .executeQuery();
                    newGroupIDResult.first();
                    groupID = newGroupIDResult.getInt("newID");
                }

                insertIntoInstanceNrStatement.setString(1, domID);
                insertIntoInstanceNrStatement.setInt(2, groupID);
                insertIntoInstanceNrStatement.setString(3, classif);
                insertIntoInstanceNrStatement.setString(4, scheme);
                insertIntoInstanceNrStatement.setInt(5, highestRep);

                insertIntoInstanceNrStatement.executeUpdate();

            } catch (SQLException squeel) {
                throw new FatalException(
                        "Problem with inserting into the database!");
            }

            // insertStatements[i++] = insertBase + "\"" + domID + "\"," +
            // groupID + ",\"" + classif + "\",\"" + scheme + "\"," + repStatus
            // + ")";
            // String insertStatement = insertBase + "\"" + domID + "\"," +
            // groupID + ",\"" + classif + "\",\"" + scheme + "\"," + repStatus
            // + ")";
            // LOG.fine("Insert statement " + insertStatements[i - 1]);
            // LOG.fine("Insert statement " + insertStatement);
            // dbManager.doInsert(insertStatement);

            // this is the most horrendous attempt at a hack. I don't even know
            // if it does any good!
            if (gcCounter % 1000 == 0) {
                gcCounter = 0;
                System.gc();
            }
            gcCounter++;
            tree.removeDomainID(domID);
        }

        // domains missing from the classification tree will make the insert
        // statement array shorter than it should be
        /*
         * we are inserting as we go along... if (i < topsStrings.size()) {
         * LOG.warning(topsStrings.size() - i + " domains are not being inserted
         * as they were not found in the tree"); String[] tmp = new String[i];
         * System.arraycopy(insertStatements, 0, tmp, 0, i); insertStatements =
         * tmp; }
         */

        // dbManager.doInserts(insertStatements);
    }
}
