package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleElement;

/**
 * @internal
 * Intention is that this will evolve into a mapping
 * from Expression to Cell, basically.
 */
public class ResultRow {
    
    private final Row row;

    public ResultRow(Row row) {
        this.row = row;
    }

    public int size() {
        return row.size();
    }

    public TupleElement element(int index) {
        return (TupleElement) row.element(index);
    }

    public Cell cell(Column column) {
        return row.cell(column);
    }

    public Column findColumn(String columnName) {
        return row.findColumn(columnName);
    }

    public Cell findValue(int zeroBasedColumn, Expression expression) {
        return expression.findValue(zeroBasedColumn, row);
    }

}
