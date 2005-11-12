package net.sourceforge.mayfly;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.drop.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.jdbc.*;
import net.sourceforge.mayfly.ldbc.*;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Database {

    private DataStore dataStore;

    public Database(DataStore store) {
        dataStore = store;
    }

    public Database() {
        this(new DataStore());
    }

    /**
     * Execute an SQL command which does not return results.
     * This is similar to the JDBC {@link java.sql.Statement#executeUpdate(java.lang.String)}
     * but is more convenient if you have a Database instance around.
     * @return Number of rows changed.
     */
    public int execute(String command) throws SQLException {
        return execute(command, Collections.EMPTY_LIST);
    }

    /**
     * Execute an SQL command which does not return results.
     * This is similar to the JDBC {@link PreparedStatement#executeUpdate()}
     * but might be more convenient if you have a Database instance around.
     * @param command SQL command, with ? in place of values to be substituted.
     * @param jdbcParameters Values to substitute for the parameters.  Currently
     * each element must be a {@link Long}.
     * @return Number of rows changed.
     */
    public int execute(String command, List jdbcParameters) throws SQLException {
        try {
            Statement statement = parse(command);
            if (statement instanceof Drop) {
                DropTable drop = DropTable.dropTableFromTree(Tree.parse(command));
                dataStore = dataStore.dropTable(drop.table());
                return 0;
            } else if (statement instanceof net.sf.jsqlparser.statement.create.table.CreateTable) {
                CreateTable create = CreateTable.createTableFromTree(Tree.parse(command));
                dataStore = dataStore.createTable(create.table(), create.columnNames());
                return 0;
            } else if (statement instanceof net.sf.jsqlparser.statement.insert.Insert) {
                return insert(command, jdbcParameters);
            } else {
                throw new SQLException("unrecognized command for execute: " + command);
            }
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    /**
     * Execute an SQL command which returns results.
     * This is similar to the JDBC {@link java.sql.Statement#executeQuery(java.lang.String)}
     * but is more convenient if you have a Database instance around.
     */
    public ResultSet query(String command) throws SQLException {
        Select select = Select.fromTree(Tree.parse(command));
        return select.select(dataStore);
    }

    private Statement parse(String command) throws SQLException {
        CCJSqlParserManager parser = new CCJSqlParserManager();
        try {
            return parser.parse(new StringReader(command));
        } catch (JSQLParserException e) {
            throw (SQLException) new SQLException("cannot parse " + command).initCause(e);
        }
    }

    private int insert(String command, List jdbcParameters) throws SQLException {
        Insert insert = Insert.insertFromTree(Tree.parse(command));
        insert.substitute(jdbcParameters);
        dataStore = dataStore.addRow(insert.table(), insert.columns(), insert.values());
        return 1;
    }

    /**
     * Return table names.
     * 
     * If this functionality is implemented in
     * {@link java.sql.DatabaseMetaData}, this method may go away or become
     * some kind of convenience method.
     */
    public Set tables() {
        return dataStore.tables();
    }

    /**
     * Column names in given table.
     * 
     * If this functionality is implemented in
     * {@link java.sql.DatabaseMetaData}, this method may go away or become
     * some kind of convenience method.
     */
    public List columnNames(String tableName) throws SQLException {
        TableData tableData = dataStore.table(tableName);
        return tableData.columnNames();
    }

    /**
     * Number of rows in given table.
     * 
     * This is a convenience method.  Your production code will almost
     * surely be counting rows (if it needs to at all) via
     * {@link ResultSet} (or the SQL COUNT, once Mayfly implements it).
     * But this method may be convenient in tests.
     */
    public int rowCount(String tableName) throws SQLException {
        TableData tableData = dataStore.table(tableName);
        return tableData.rowCount();
    }

    /**
     * Get some data out. This is now redundant with {@link ResultSet}.  Do we
     * want a non-JDBC API for convenience?  Should it look like this or more
     * like java collections?
     */
    public int getInt(String tableName, String columnName, int rowIndex) throws SQLException {
        return dataStore.table(tableName).getInt(columnName, rowIndex);
    }

    /**
     * Open a JDBC connection.
     * This is similar to the JDBC {@link DriverManager#getConnection(java.lang.String)}
     * but is more convenient if you have a Database instance around.
     */
    public Connection openConnection() throws SQLException {
        return new JdbcConnection(this);
    }

}
