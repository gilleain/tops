package tops.web.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import javax.sql.DataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Simple wrapper for a data source, provides Connections. This is the
 * alternative to the {@link tops.data.db.update.DBManager DBManager} in tops.db, for use in
 * a servlet environment.
 */

public class DataSourceWrapper {

    private static DataSource ds;
    static {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSourceWrapper.ds = (DataSource) envCtx.lookup("jdbc/TopsDB");
        } catch (NamingException ne) {
            System.err.println("NAMING ERROR : " + ne);
        }
    }

    /**
     * Get a connection from the underlying
     * {@link javax.sql.DataSource DataSource}
     * 
     * @return a {@link java.sql.Connection Connection} to the underlying
     *         DataSource.
     * @throws SQLException
     *             if there is a problem with the DataSource.
     */

    public static Connection getConnection() throws SQLException {
        return DataSourceWrapper.ds.getConnection();
    }

    /**
     * Query the DataSource to return a ResultSet.
     * 
     * @param sql
     *            an SQL string to send to the database (source)
     * @return a {@link java.sql.ResultSet ResultSet} from the query.
     * @throws SQLException
     *             if there is a problem with the DataSource.
     */

    public static ResultSet executeQuery(String sql) throws SQLException {
        Connection connection = DataSourceWrapper.getConnection();
        Statement statement = connection.createStatement();
        statement.executeQuery(sql);
        return statement.getResultSet();
    }
}
