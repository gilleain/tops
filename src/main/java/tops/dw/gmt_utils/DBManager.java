package tops.dw.gmt_utils;

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

    static {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("driver not found : " + cnfe);
        }
    }

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
            String dbPass) {
        this.conn = null;
        this.dbName = dbName;
        this.dbUserName = dbUserName;
        this.dbURL = "jdbc:mysql://" + dbURL + "/" + dbName; // mysql syntax
        this.dbPass = dbPass;
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
            System.out.println(squeel);
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
     * make a connection to the database, and so forth
     */
    private void doConnection() {
        System.err.println("trying : " + this.dbURL + " " + this.dbUserName + " " + this.dbPass + " " + this.dbName);
        try {
            this.conn = DriverManager.getConnection(this.dbURL, this.dbUserName, this.dbPass);
        } catch (SQLException squeel) {
            System.out.println(squeel);
        }
    }

}
