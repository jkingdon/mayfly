package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
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

    @Override
    public ResultRows tableContents(Evaluator evaluator) {
        TableData table = evaluator.table(this);
        return applyAlias(table.rows(), table.columns(), evaluator.options());
    }

    @Override
    public ResultRow dummyRow(Evaluator evaluator) {
        TableData table = evaluator.table(this);
        return applyAlias(table.dummyRow(), table.columns(), 
            evaluator.options());
    }

    private ResultRows applyAlias(Rows storedRows, Columns tableColumns, 
        Options options) {
        List rows = new ArrayList();
        for (Iterator iter = storedRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            rows.add(applyAlias(row, tableColumns, options));
        }
        return new ResultRows(new ImmutableList(rows));
    }

    private ResultRow applyAlias(Row row, Columns tableColumns,
        Options options) {
        ResultRow result = new ResultRow();
        for (Iterator iter = tableColumns.asCaseNames().iterator(); 
            iter.hasNext(); ) {
            CaseInsensitiveString column = (CaseInsensitiveString) iter.next();
            result = result.with(
                new SingleColumn(alias, column.getString(), options), 
                row.cell(column));
        }
        return result;
    }

}
