package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.Iterator;

public class FromTable extends ValueObject implements FromElement {

    private String tableName;
    private String alias;

    public FromTable(String tableName) {
        this(tableName, null);
    }

    public FromTable(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public Rows tableContents(DataStore store, String currentSchema) {
        return applyAlias(store.table(currentSchema, tableName).rows());
    }

    public Rows dummyRows(DataStore store, String currentSchema) {
        return applyAlias(store.table(currentSchema, tableName).dummyRows());
    }

    private Rows applyAlias(Rows storedRows) {
        if (alias == null) {
            return storedRows;
        } else {
            return applyAlias(alias, storedRows);
        }
    }

    private Rows applyAlias(String alias, Rows storedRows) {
        L rows = new L();
        for (Iterator iter = storedRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            rows.add(applyAlias(alias, row));
        }
        return new Rows(rows.asImmutable());
    }

    private Row applyAlias(String alias, Row row) {
        TupleBuilder newTuples = new TupleBuilder();
        for (Iterator iter = row.iterator(); iter.hasNext(); ) {
            TupleElement entry = (TupleElement) iter.next();
            Column newColumn = new Column(alias, entry.column().columnName());
            newTuples.append(new TupleElement(newColumn, entry.cell()));
        }
        return new Row(newTuples);
    }

}
