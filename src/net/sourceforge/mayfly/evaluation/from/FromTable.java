package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.Iterator;

public class FromTable extends ValueObject implements FromElement {

    public final String tableName;
    public final String alias;

    public FromTable(String tableName) {
        this(tableName, tableName);
    }

    public FromTable(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
        if (alias == null) {
            throw new MayflyInternalException("need a table alias");
        }
    }

    public Rows tableContents(DataStore store, String currentSchema) {
        return applyAlias(store.table(currentSchema, tableName).rows());
    }

    public Rows dummyRows(DataStore store, String currentSchema) {
        return applyAlias(store.table(currentSchema, tableName).dummyRows());
    }

    private Rows applyAlias(Rows storedRows) {
        L rows = new L();
        for (Iterator iter = storedRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            rows.add(applyAlias(row));
        }
        return new Rows(rows.asImmutable());
    }

    private Row applyAlias(Row row) {
        TupleBuilder newTuples = new TupleBuilder();
        for (Iterator iter = row.iterator(); iter.hasNext(); ) {
            TupleElement entry = (TupleElement) iter.next();
            Column newColumn = new Column(alias, entry.column().columnName());
            newTuples.append(new TupleElement(newColumn, entry.cell()));
        }
        return new Row(newTuples);
    }

}
