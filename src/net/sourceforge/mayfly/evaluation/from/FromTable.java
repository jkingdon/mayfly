package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FromTable extends FromElement {

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

    public ResultRows tableContents(Evaluator evaluator) {
        return applyAlias(
            evaluator.store().table(evaluator.currentSchema, tableName)
                .rows());
    }

    public ResultRow dummyRow(Evaluator evaluator) {
        return applyAlias(
            evaluator.store().table(evaluator.currentSchema, tableName)
                .dummyRows());
    }

    private ResultRows applyAlias(Rows storedRows) {
        List rows = new ArrayList();
        for (Iterator iter = storedRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            rows.add(applyAlias(row));
        }
        return new ResultRows(new ImmutableList(rows));
    }

    private ResultRow applyAlias(Row row) {
        ResultRow result = new ResultRow();
        for (Iterator iter = row.iterator(); iter.hasNext(); ) {
            TupleElement entry = (TupleElement) iter.next();
            result = result.withColumn(
                alias, entry.columnName(), entry.cell());
        }
        return result;
    }

}
