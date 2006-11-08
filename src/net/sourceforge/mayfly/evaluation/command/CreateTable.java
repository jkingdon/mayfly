package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateTable extends Command {

    private String table;
    private Columns columns;
    private List constraints = new ArrayList();

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
        return new Constraints(resolveAll(store, schema, this.constraints));
    }

    private ImmutableList resolveAll(
        DataStore store, String schema, List unresolved) {
        checkMultiplePrimaryKeys(unresolved);
        L result = new L();
        for (Iterator iter = unresolved.iterator(); iter.hasNext();) {
            UnresolvedConstraint constraint = 
                (UnresolvedConstraint) iter.next();
            // TODO: passing in store here is (I think)
            // subtlely wrong.  In the case of
            // CREATE SCHEMA S1 CREATE TABLE FOO ... CREATE TABLE BAR ...
            // then store won't contain FOO.
            result.add(constraint.resolve(
                store, schema, table, this.columns));
        }
        return result.asImmutable();
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

}
