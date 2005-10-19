package net.sourceforge.mayfly;

import java.sql.*;
import java.util.*;

public class JdbcDriver implements Driver {

    static {
        try {
            DriverManager.registerDriver(new JdbcDriver());
        } catch (SQLException e) {
            // This prevents the class from being loaded and propagates
            // some kind of exception up to whoever tried to load it, right?
            // Is that right?  Is there another way to handle it?
            throw new RuntimeException(e);
        }
    }
    
    // Could wait until connect() time to instantiate this (in the case
    // where mayfly is just sitting around but not being used), but I
    // guess I'll put that off until I have reason to think there is
    // some significant cost to an empty Database object.
    Database database = new Database();

    public Connection connect(String url, Properties info) throws SQLException {
        return database.openConnection();
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:mayfly");
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) throws SQLException {
        throw new UnimplementedException();
    }

    public int getMajorVersion() {
        throw new UnimplementedException();
    }

    public int getMinorVersion() {
        throw new UnimplementedException();
    }

    public boolean jdbcCompliant() {
        return false;
    }
}
