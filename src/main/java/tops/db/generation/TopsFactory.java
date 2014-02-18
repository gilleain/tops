package tops.db.generation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

/**
 * Base class for this package's factories - simply handles database
 * connections.
 * 
 * @author maclean
 */

public class TopsFactory {

    static {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("driver not found : " + cnfe);
        }
    }

    /*
     * LEEDS
     */
    private static final String dbURL = "jdbc:mysql://bioinformatics.leeds.ac.uk/tops";

    // private static final String dbURL =
    // "jdbc:mysql://bmbpcu15.leeds.ac.uk/tops"; //IOANNIS' MACHINE! CHANGE!
    private static final String dbUserName = "tops";

    private static final String dbPass = "top1gtwm";

    // private final static String dbURL =
    // "jdbc:mysql://tabuaeran.dcs.gla.ac.uk/mallika";
    // MALLIKA's MIRROR
    // private final static String dbUserName = "mallika";
    // private final static String dbPass = "1883";

    private Connection conn = null;

    /**
     * No-argument, null constructor.
     */
    public TopsFactory() {
    }

    /**
     * Take a String of SQL and execute it, returning a ResultSet, or null if it
     * fails.
     * 
     * @param sql
     *            a String of SQL
     * @return the ResultSet that is produced, or null
     */
    public ResultSet doQuery(String sql) {
        // System.err.println("query : " + sql);
        if (this.conn == null) {
            this.doConnection();
        }
        Statement s = null;
        try {
            s = this.conn.createStatement();
            return s.executeQuery(sql);
        } catch (SQLException squeel) {
            System.out.println(squeel);
        }
        return null;
    }

    /**
     * A private method to connect to the database, lazily evaluated.
     */
    private void doConnection() {
        try {
            this.conn = DriverManager.getConnection(TopsFactory.dbURL, TopsFactory.dbUserName, TopsFactory.dbPass);
        } catch (SQLException squeel) {
            System.out.println(squeel);
        }
    }
}
