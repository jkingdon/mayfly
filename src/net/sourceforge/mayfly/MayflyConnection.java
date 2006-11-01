package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.SetSchema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * @internal
 * A database can have various connections (a connection corresponds to a
 * {@link net.sourceforge.mayfly.jdbc.JdbcConnection} or similar
 * concepts like {@link net.sourceforge.mayfly.Database#defaultConnection}).
 * A connection has state, like the auto-commit flag, the current schema,
 * etc.  This class holds that state and also is able to
 * perform various operations on the database.
 * 
 * This class is just for callers within Mayfly; external callers
 * should have a {@link net.sourceforge.mayfly.jdbc.JdbcConnection}.
 */
public class MayflyConnection {

    private final Database database;
    private boolean autoCommit = true;
    //private DataStore rollbackPoint;
    private String currentSchema = DataStore.ANONYMOUS_SCHEMA_NAME;

    public MayflyConnection(Database database) {
        this.database = database;
    }

    public ResultSet query(String sql) throws MayflyException {
        Command select = Command.fromSql(sql);
        return query(select);
    }

    public ResultSet query(Command select) {
        return select.select(database.dataStore(), currentSchema);
    }

    public int execute(String sql) throws MayflyException {
        Command command = Command.fromSql(sql);
        return executeUpdate(command);
    }

    public int executeUpdate(Command command) {
        if (command instanceof SetSchema) {
            SetSchema setSchema = (SetSchema) command;
            String proposed = setSchema.name();
            database.dataStore().schema(proposed);
            currentSchema = proposed;
            return 0;
        }
        return database.executeUpdate(command, currentSchema);
    }

    public Set tables() {
        return database.dataStore().tables(currentSchema);
    }

    public List columnNames(String tableName) {
        TableData tableData = database.dataStore().table(currentSchema, tableName);
        return tableData.columnNames();
    }

    public int rowCount(String tableName) {
        TableData tableData = database.dataStore().table(currentSchema, tableName);
        return tableData.rowCount();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit  = autoCommit;
        startTransaction();
    }

    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    private void startTransaction() {
        //rollbackPoint = database.dataStore();
    }

    public void commit() throws SQLException {
        startTransaction();
    }

    public void rollback() throws SQLException {
        if (autoCommit) {
            return;
        }
        throw new UnimplementedException("Rollback not implemented");
        /* Seems to me like this implementation is limited to
           the point of irresponsibility.  It clobbers changes
           which were committed in another transaction.
           
           If we could detect whether another transaction has committed
           something, and throw an exception in that case, it wouldn't
           be so bad.
         */
//        database.setDataStore(rollbackPoint);
    }

}
