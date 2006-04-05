package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.datastore.constraint.NotNullConstraint;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;
import net.sourceforge.mayfly.datastore.constraint.UniqueConstraint;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateTable extends Command {

    private String table;
    private Columns columns;
    private List primaryKeyColumns;
    private List /* <List<String>> */ uniqueConstraints = new ArrayList();
    private List notNullConstraints = new ArrayList();

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
        Constraints constraints = makeConstraints();
        return schema.createTable(table(), columns, constraints);
    }

    private Constraints makeConstraints() {
        ImmutableList constraints = ImmutableList.fromIterable(uniqueConstraints().plus(notNullConstraints()));
        return new Constraints(primaryKey(), constraints);
    }

    private PrimaryKey primaryKey() {
        if (primaryKeyColumns == null) {
            return null;
        }

        return new PrimaryKey(resolveColumns(primaryKeyColumns));
    }
    
    private L uniqueConstraints() {
        L result = new L();
        for (Iterator iter = uniqueConstraints.iterator(); iter.hasNext();) {
            List columns = (List) iter.next();
            result.add(new UniqueConstraint(resolveColumns(columns)));
        }
        return result;
    }
    
    private L notNullConstraints() {
        L result = new L();
        for (Iterator iter = notNullConstraints.iterator(); iter.hasNext();) {
            String columnName = (String) iter.next();
            result.add(new NotNullConstraint(columns.columnFromName(columnName)));
        }
        return result;
    }

    private Columns resolveColumns(List constraintColumns) {
        List resolvedColumns = new ArrayList();
        for (Iterator iter = constraintColumns.iterator(); iter.hasNext();) {
            String columnName = (String) iter.next();
            Column column = columns.columnFromName(columnName);
            resolvedColumns.add(column);
        }
        return new Columns(new ImmutableList(resolvedColumns));
    }

    public int rowsAffected() {
        return 0;
    }

    public void addColumn(Column column) {
        columns = (Columns) columns.with(column);
    }

    public void setPrimaryKey(List columns) {
        if (primaryKeyColumns != null) {
            throw new MayflyException("attempt to define more than one primary key for table " + table);
        }
        primaryKeyColumns = columns;
    }
    
    public void addUniqueConstraint(List columns) {
        uniqueConstraints.add(columns);
    }

    public void addNotNullConstraint(String column) {
        notNullConstraints.add(column);
    }

}
