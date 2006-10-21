package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class SetClause {

    private final String column;
    private final Expression value;
    
    /**
     * @param value The expression to set to, or null for the default expression.
     */
    public SetClause(String column, Expression value) {
        this.column = column;
        this.value = value;
    }

    public Column column(Columns columns) {
        return columns.columnFromName(column);
    }

    public Cell value(Row row, String table, Column column) {
        if (value == null) {
            return column.coerce(column.defaultValue(), Location.UNKNOWN);
        }
        else {
            return column.coerce(value.evaluate(row, table), value.location);
        }
    }

}
