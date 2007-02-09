package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.SetSchema;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.select.StoreEvaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
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
    private Cell lastIdentity = NullCell.INSTANCE;

    public MayflyConnection(Database database) {
        this.database = database;
    }

    public ResultSet query(String sql) throws MayflyException {
        Command select = Command.fromSql(sql, database.options());
        return query(select);
    }

    public ResultSet query(Command select) {
        return select.select(
            new StoreEvaluator(database.dataStore(), currentSchema, 
                database.options()), 
            lastIdentity);
    }

    public int execute(String sql) throws MayflyException {
        Command command = Command.fromSql(sql, database.options());
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
        UpdateStore updateResult = 
            database.executeUpdate(command, currentSchema);
        if (updateResult.newIdentityValue != null) {
            lastIdentity = updateResult.newIdentityValue;
        }
        return updateResult.rowsAffected();
    }

    public Set tables() {
        return database.dataStore().tables(currentSchema);
    }

    public List columnNames(String tableName) {
        TableData tableData = currentSchema().table(tableName);
        return tableData.columnNames();
    }

    public List columnNamesOrEmpty(String tableName) {
        Schema schema = currentSchema();
        if (!schema.hasTable(tableName)) {
            return Collections.EMPTY_LIST;
        }
        TableData tableData = schema.table(tableName);
        return tableData.columnNames();
    }

    private Schema currentSchema() {
        Schema schema = database.dataStore().schema(currentSchema);
        return schema;
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

    public ResultSet getColumns(String table, String targetColumn) {
        SingleColumn nameExpression = new SingleColumn("COLUMN_NAME");
        Selected columnColumns = new Selected(nameExpression);
        
        ResultRows rows = new ResultRows();
        List names = columnNamesOrEmpty(table);
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            String column = (String) iter.next();
            if (targetColumn.equalsIgnoreCase(column)) {
                rows = rows.with(columnToMetaRow(nameExpression, column));
            }
        }
        return new MayflyResultSet(columnColumns, rows);
    }

    private ResultRow columnToMetaRow(SingleColumn nameExpression, String column) {
        return new ResultRow().with(nameExpression, new StringCell(column));
    }

    public DataStore snapshot() {
        return database.dataStore();
    }

    public Options options() {
        return database.options();
    }

}
