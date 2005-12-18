package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;
import antlr.collections.*;

import java.util.*;

public class FromTable extends ValueObject implements FromElement {

    public static FromElement fromSeletedTableTree(Tree table) {
        AST firstIdentifier = table.getFirstChild();
        String tableName = firstIdentifier.getText();

        AST secondIdentifier = firstIdentifier.getNextSibling();

        FromElement fromElement;
        if (secondIdentifier==null) {
            fromElement = new FromTable(tableName);
        } else {
            String alias = secondIdentifier.getText();
            fromElement = new FromTable(tableName, alias);
        }
        return fromElement;
    }

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
