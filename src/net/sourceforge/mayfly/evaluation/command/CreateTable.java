package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.constraint.Action;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.datastore.constraint.ForeignKey;
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
    private List foreignKeyConstraints = new ArrayList();

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

    public UpdateStore update(DataStore store, String schema) {
        Schema updatedSchema = update(store, schema, store.schema(schema));
        return new UpdateStore(store.replace(schema, updatedSchema), 0);
    }

    public Schema update(DataStore store, String schemaName, Schema schema) {
        Constraints constraints = makeTableConstraints(store, schemaName);
        return schema.createTable(table(), columns, constraints);
    }

    private Constraints makeTableConstraints(
        DataStore store, String schema) {
        ImmutableList constraints = ImmutableList.fromIterable(
            uniqueConstraints()
            .plus(notNullConstraints())
        );
        return new Constraints(primaryKey(), constraints, 
            
            // TODO: passing in store here is (I think)
            // subtlely wrong.  In the case of
            // CREATE SCHEMA S1 CREATE TABLE FOO ... CREATE TABLE BAR ...
            // then store won't contain FOO.
            foreignKeyConstraints(store, schema).asImmutable()
        );
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

    private L foreignKeyConstraints(DataStore store, String schema) {
        L result = new L();
        for (Iterator iter = foreignKeyConstraints.iterator(); iter.hasNext();) {
            UnresolvedForeignKey key = (UnresolvedForeignKey) iter.next();
            InsertTable targetTable = key.targetTable;
            result.add(
                new ForeignKey(
                    schema,
                    table,
                    key.referencingColumn,

                    targetTable.resolve(store, schema),
                    key.targetColumn,
                    
                    key.onDelete,
                    key.onUpdate
                )
            );
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

    public void addForeignKeyConstraint(String referencingColumn, 
        InsertTable targetTable, String targetColumn, 
        Action onDelete, Action onUpdate) {
        foreignKeyConstraints.add(
            new UnresolvedForeignKey(
                referencingColumn, targetTable, targetColumn, onDelete, onUpdate
            ));
    }
    
    static class UnresolvedForeignKey {
        final String referencingColumn;
        final InsertTable targetTable;
        final String targetColumn;
        final Action onDelete;
        final Action onUpdate;

        public UnresolvedForeignKey(String referencingColumn, 
            InsertTable targetTable, String targetColumn, 
            Action onDelete, Action onUpdate) {
            this.referencingColumn = referencingColumn;
            this.targetTable = targetTable;
            this.targetColumn = targetColumn;
            this.onDelete = onDelete;
            this.onUpdate = onUpdate;
        }
        
    }

}
