package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CreateTable extends Command {

    private String table;
    private Columns columns;
    private UnresolvedPrimaryKey primaryKey;
    private List uniqueConstraints = new ArrayList();
    private List foreignKeyConstraints = new ArrayList();

    public CreateTable(String table, List columnNames) {
        this.table = table;
        this.columns = Columns.fromColumnNames(columnNames);
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
        if (primaryKey == null) {
            return null;
        }

        return primaryKey.resolve(columns);
    }
    
    private L uniqueConstraints() {
        L result = new L();
        for (Iterator iter = uniqueConstraints.iterator(); iter.hasNext();) {
            UnresolvedUniqueConstraint constraint = 
                (UnresolvedUniqueConstraint) iter.next();
            result.add(constraint.resolve(this.columns));
        }
        return result;
    }
    
    private L foreignKeyConstraints(DataStore store, String schema) {
        L result = new L();
        for (Iterator iter = foreignKeyConstraints.iterator(); iter.hasNext();) {
            UnresolvedForeignKey key = (UnresolvedForeignKey) iter.next();
            key.checkDuplicates(Collections.unmodifiableList(result));
            result.add(key.resolve(store, schema, table));
        }
        return result;
    }

    public void addColumn(Column column) {
        columns = (Columns) columns.with(column);
    }

    public void setPrimaryKey(UnresolvedPrimaryKey unresolvedPrimaryKey) {
        if (primaryKey != null) {
            throw new MayflyException("attempt to define more than one primary key for table " + table);
        }
        primaryKey = unresolvedPrimaryKey;
    }
    
    public void addUniqueConstraint(UnresolvedUniqueConstraint constraint) {
        uniqueConstraints.add(constraint);
    }

    public void addForeignKeyConstraint(UnresolvedForeignKey key) {
        foreignKeyConstraints.add(key);
    }
    
    public void addConstraint(UnresolvedConstraint constraint) {
        if (constraint instanceof UnresolvedForeignKey) {
            addForeignKeyConstraint((UnresolvedForeignKey) constraint);
        }
        else {
            throw new UnimplementedException(
                "Do not recognize " + constraint.getClass().getName());
        }
    }
    
    /**
     * Does not include NOT NULL constraints, since they are stored
     * with the {@link Column} rather than separately.
     */
    public boolean hasConstraints() {
        return 
            primaryKey != null || 
            !uniqueConstraints.isEmpty() || 
            !foreignKeyConstraints.isEmpty();
    }

}
