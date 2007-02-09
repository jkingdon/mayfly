package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FromTable extends FromElement {

    public final String tableName;
    public final String alias;
    public final Location location = Location.UNKNOWN;

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
        return applyAlias(evaluator.table(this).rows(), evaluator.options());
    }

    public ResultRow dummyRow(Evaluator evaluator) {
        return applyAlias(evaluator.table(this).dummyRow(), evaluator.options());
    }

    private ResultRows applyAlias(Rows storedRows, Options options) {
        List rows = new ArrayList();
        for (Iterator iter = storedRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            rows.add(applyAlias(row, options));
        }
        return new ResultRows(new ImmutableList(rows));
    }

    private ResultRow applyAlias(Row row, Options options) {
        ResultRow result = new ResultRow();
        for (Iterator iter = row.iterator(); iter.hasNext(); ) {
            TupleElement entry = (TupleElement) iter.next();
            result = result.with(
                new SingleColumn(alias, entry.columnName(), options), 
                entry.cell());
        }
        return result;
    }

}
