package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;

/**
 * @internal
 * Rows that we might return as part of evaluating.
 * That is, they may have aliases applied, or
 * various things other than just a cell in
 * each column.
 */
public class ResultRows {

    private final Rows rows;

    public ResultRows(Rows rows) {
        this.rows = rows;
    }

    public int size() {
        return rows.size();
    }

    public Row row(int index) {
        return (Row) rows.element(index);
    }

}
