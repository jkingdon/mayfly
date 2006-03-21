package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateTable extends Command {

    private String table;
    private Columns columns;
    private List primaryKeyElements = new ArrayList();

    public CreateTable(String table, List columnNames) {
        this.table = table;
        this.columns = Columns.fromColumnNames(table, columnNames);
    }

    public CreateTable(String table) {
        this(table, new ArrayList());
    }

    public String table() {
        return table;
    }

    public DataStore update(DataStore store, String schema) {
        Schema oldSchema = store.schema(schema);
        Schema updatedSchema = update(oldSchema);
        return store.replace(schema, updatedSchema);
    }

    public Schema update(Schema schema) {
        PrimaryKey constraints = primaryKey();
        return schema.createTable(table(), columns, constraints);
    }

    private PrimaryKey primaryKey() {
        List keyElements = new ArrayList();
        for (Iterator iter = primaryKeyElements.iterator(); iter.hasNext();) {
            String columnName = (String) iter.next();
            Column column = columns.columnFromName(columnName);
            keyElements.add(column);
        }
        return new PrimaryKey(new Columns(new ImmutableList(keyElements)));
    }

    public int rowsAffected() {
        return 0;
    }

    public void addColumn(Column column) {
        columns = (Columns) columns.with(column);
    }

    public void addPrimaryKeyElement(String columnName) {
        primaryKeyElements.add(columnName);
    }

}
