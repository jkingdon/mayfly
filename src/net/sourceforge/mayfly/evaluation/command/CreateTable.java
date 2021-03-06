package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Index;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateTable extends Command {

    private String table;
    private Columns columns;
    private List constraints = new ArrayList();
    private List indexes = new ArrayList();

    public CreateTable(String table) {
        this.table = table;
        this.columns = new Columns(new ImmutableList());
    }

    public String table() {
        return table;
    }

    @Override
    public UpdateStore update(DataStore store, String schema) {
        Schema updatedSchema = update(store, schema, store.schema(schema));
        return new UpdateStore(store.replace(schema, updatedSchema), 0);
    }

    public Schema update(DataStore store, String schemaName, Schema schema) {
        Constraints constraints = makeTableConstraints(store, schemaName);
        return schema.createTable(table(), columns, constraints,
            new ImmutableList(indexes));
    }

    private Constraints makeTableConstraints(
        DataStore store, String schema) {
        return resolveAll(store, schema, this.constraints);
    }

    private Constraints resolveAll(
        DataStore store, String schema, List unresolved) {
        checkMultiplePrimaryKeys(unresolved);
        ConstraintsBuilder result = new ConstraintsBuilder(
            store, schema, table, columns);
        for (Iterator iter = unresolved.iterator(); iter.hasNext();) {
            UnresolvedConstraint constraint = 
                (UnresolvedConstraint) iter.next();
            // TODO: using store (via the constructor) here is (I think)
            // subtlely wrong.  In the case of
            // CREATE SCHEMA S1 CREATE TABLE FOO ... CREATE TABLE BAR ...
            // then store won't contain FOO.
            result.add(constraint);
        }
        return result.asConstraints();
    }
    
    private void checkMultiplePrimaryKeys(List unresolved) {
        boolean havePrimaryKey = false;
        for (Iterator iter = unresolved.iterator(); iter.hasNext();) {
            UnresolvedConstraint constraint =
                (UnresolvedConstraint) iter.next();
            if (constraint instanceof UnresolvedPrimaryKey) {
                if (havePrimaryKey) {
                    /**
                       {@link Constraints#checkOnlyOnePrimaryKey(ImmutableList)}
                       doesn't give an error message which is quite good
                       enough for us, at least not yet.
                     */
                    throw new MayflyException("attempt to define more than " +
                        "one primary key for table " + table);
                }
                havePrimaryKey = true;
            }
        }
    }

    public void addColumn(Column column) {
        columns = columns.with(column);
    }

    public void addConstraint(UnresolvedConstraint constraint) {
        constraints.add(constraint);
    }
    
    /**
     * Does not include NOT NULL constraints, since they are stored
     * with the {@link Column} rather than separately.
     */
    public boolean hasConstraints() {
        return !constraints.isEmpty();
    }
    
    public Columns columns() {
        return columns;
    }

    public void addIndex(String name, ColumnNames indexColumns) {
        indexes.add(new Index(name, indexColumns));
    }

}
