package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.*;

import java.util.*;

public abstract class AggregateExpression extends Expression {

    private final SingleColumn column;
    private final String functionName;
    private final boolean distinct;

    protected AggregateExpression(SingleColumn column, String functionName, boolean distinct) {
        this.column = column;
        this.functionName = functionName;
        this.distinct = distinct;
    }

    public Cell evaluate(Row row) {
        /** This is just for checking; aggregation happens in {@link #aggregate(Rows)}. */
        return column.evaluate(row);
    }
    
    public Cell findValue(int zeroBasedColumn, Row row) {
        return row.byPosition(zeroBasedColumn);
    }

    public String firstAggregate() {
        return displayName();
    }

    public String displayName() {
        return functionName + "(" + column.displayName() + ")";
    }

    public Cell aggregate(Rows rows) {
        Collection values = findValues(rows);
        
        if (!values.isEmpty() && values.iterator().next() instanceof Cell) {
            return aggregateNonNumeric(values);
        }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long count = 0;
        long sum = 0;
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            long value = ((Long) iter.next()).longValue();
            min = Math.min(min, value);
            max = Math.max(max, value);
            count++;
            sum += value;
        }
        
        if (count > 0) {
            return pickOne(new LongCell(min), new LongCell(max),
                new LongCell(count), new LongCell(sum), new LongCell(sum / count));
        } else {
            return pickOne(NullCell.INSTANCE, NullCell.INSTANCE, 
                new LongCell(0), 
                
                // Lame (0 would be more convenient), but standard.
                // Is it possible/desirable for Mayfly to help?
                // (giving an error and pointing out a better way, or whatever).
                NullCell.INSTANCE,
                
                NullCell.INSTANCE);
        }
    }

    protected Cell aggregateNonNumeric(Collection values) {
        Cell first = (Cell) values.iterator().next();
        throw new MayflyException("attempt to apply " + displayName() + " to " + first.displayName());
    }

    private Collection findValues(Rows rows) {
        Collection values;
        if (distinct) {
            values = new HashSet();
        }
        else {
            values = new ArrayList();
        }

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            Cell cell = evaluate(row);
            if (cell instanceof NullCell) {
            }
            else if (cell instanceof LongCell) {
                values.add(new Long(cell.asLong()));
            }
            else {
                values.add(cell);
            }
        }
        return values;
    }

    abstract protected Cell pickOne(Cell minimum, Cell maximum, Cell count, Cell sum, Cell average);
    
}
