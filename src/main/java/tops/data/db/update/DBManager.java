package tops.data.db.update;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

/**
 * Manages connections and queries to a database
 * 
 * @author GMT
 * @version 07/03/03
 */
public class DBManager {

    /*
     * static { try { Class.forName("org.gjt.mm.mysql.Driver"); } catch
     * (ClassNotFoundException cnfe) { System.out.println("driver not found : " +
     * cnfe); } }
     */

    // connection to the database
    private Connection conn;

    // name of the database
    private String dbName;

    // name of the user
    private String dbUserName;

    // URL of the database
    private String dbURL;

    // password, nice'n'secure
    private String dbPass;

    /**
     * Constructor for objects of class DBManager
     */
    public DBManager(String dbName, String dbUserName, String dbURL,
            String dbPass) throws ClassNotFoundException {
        this.conn = null;
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.dbURL = "jdbc:mysql://" + dbURL + "/" + dbName; // mysql syntax
        this.dbPass = dbPass;
        Class.forName("org.gjt.mm.mysql.Driver");
    }

    /**
     * make a connection to the database, and so forth
     */

    private void doConnection() {
        // System.err.println("trying : " + dbURL + " " + dbUserName + " " +
        // dbPass);
        try {
            this.conn = DriverManager.getConnection(this.dbURL, this.dbUserName, this.dbPass);
        } catch (SQLException squeel) {
            System.err.println(squeel);
        }
    }

    /**
     * Return a connection to the client
     */

    public Connection getConnection() {
        if (this.conn == null) {
            this.doConnection();
        }
        return this.conn;
    }

    /**
     * Make a query
     * 
     * @param sql
     *            an SQL string
     * @return the result set
     */

    public ResultSet doQuery(String sql) {
        if (this.conn == null)
            this.doConnection();
        Statement s = null;
        try {
            s = this.conn.createStatement();
            return s.executeQuery(sql);
        } catch (SQLException squeel) {
            System.err.println(squeel);
        }
        /*
         * finally { if (s != null) { try { s.close(); } catch (SQLException
         * squeel) { System.out.println(squeel); } } if (r != null) { try {
         * r.close(); } catch (SQLException squeel) {
         * System.out.println(squeel); } } }
         */
        return null;
    }

    /**
     * Make a batch query
     * 
     * @param sql
     *            an SQL string
     * @return the result set
     */
    public ResultSet[] doQueries(String[] sql) {
        this.doConnection();
        ResultSet[] res = new ResultSet[sql.length];
        for (int i = 0; i < sql.length; i++) {
            res[i] = this.doQuery(sql[i]);
        }
        return res;
    }

    /**
     * Make an insert query
     * 
     * @param sql
     *            an SQL string
     */
    public void doInsert(String sql) {
        if (this.conn == null)
            this.doConnection();
        Statement s = null;
        try {
            s = this.conn.createStatement();
            s.executeUpdate(sql);
        } catch (SQLException squeel) {
            System.err.println(squeel);
        }
        /*
         * finally { if (s != null) { try { s.close(); } catch (SQLException
         * squeel) { System.out.println(squeel); } } if (r != null) { try {
         * r.close(); } catch (SQLException squeel) {
         * System.out.println(squeel); } } }
         */
    }

    /**
     * Make a batch query
     * 
     * @param sql
     *            an SQL string
     */
    public void doInserts(String[] sql) {
        if (this.conn == null)
            this.doConnection();
        for (int i = 0; i < sql.length; i++) {
            this.doInsert(sql[i]);
        }
    }

    /*
     * return a string that describes the database that this manager is
     * connecting to
     */
    @Override
    public String toString() {
        return this.dbURL + " " + this.dbUserName + " " + this.dbName;
    }

}
