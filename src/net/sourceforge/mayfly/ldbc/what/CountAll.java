package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.*;

public class CountAll extends Expression {

    private final String functionName;

    public CountAll(String functionName) {
        this.functionName = functionName;
    }

    public Cell evaluate(Row row) {
        /** This is just for checking; aggregation happens in {@link #aggregate(Rows)}. */
        return new LongCell(0);
    }

    public Cell findValue(int zeroBasedColumn, Row row) {
        return row.byPosition(zeroBasedColumn);
    }

    public String firstAggregate() {
        return functionName + "(*)";
    }

    public Cell aggregate(Rows rows) {
        return new LongCell(rows.size());
    }

}
