package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.parser.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public abstract class AggregateExpression extends Expression {

    protected final SingleColumn column;
    protected final String functionName;
    protected final boolean distinct;

    protected AggregateExpression(SingleColumn column, String functionName, boolean distinct, 
        Location location) {
        super(location);
        this.column = column;
        this.functionName = functionName;
        this.distinct = distinct;
    }

    protected AggregateExpression(SingleColumn column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
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
        return functionName + "(" + (distinct ? "distinct " : "") + column.displayName() + ")";
    }

    public Cell aggregate(Rows rows) {
        Collection values = findValues(rows);
        return aggregate(values);
    }

    abstract Cell aggregate(Collection values);

    Cell aggregateSumAverage(Collection values, boolean isSum) {
        if (values.isEmpty()) {
            /* In this case of sum, this is lame (0 would be more convenient), but standard.
               Is it possible/desirable for Mayfly to help?
               (giving an error and pointing out a better way, or whatever). */
            return NullCell.INSTANCE;
        }

        if (!(values.iterator().next() instanceof LongCell)) {
            Cell first = (Cell) values.iterator().next();
            throw new MayflyException("attempt to apply " + displayName() + " to " + first.displayName());
        }

        long count = 0;
        long sum = 0;
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            long value = ((LongCell) iter.next()).asLong();
            count++;
            sum += value;
        }
        
        return new LongCell(isSum ? sum : sum / count);
    }

    Cell aggregateMinMax(Collection values) {
        Iterator iter = values.iterator();
        if (!iter.hasNext()) {
            return NullCell.INSTANCE;
        }
        
        Cell bestSoFar = (Cell) iter.next();
        while (iter.hasNext()) {
            Cell candidate = (Cell) iter.next();
            if (isBetter(candidate, bestSoFar)) {
                bestSoFar = candidate;
            }
        }
        
        return bestSoFar;
    }

    boolean isBetter(Cell candidate, Cell bestSoFar) {
        throw new MayflyInternalException("Override this for min/max");
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
            if (!(cell instanceof NullCell)) {
                values.add(cell);
            }
        }
        return values;
    }

    public boolean sameExpression(Expression other) {
        if (getClass().equals(other.getClass())) {
            AggregateExpression otherExpression = (AggregateExpression) other;
            return column.sameExpression(otherExpression.column) && distinct == otherExpression.distinct;
        }
        else {
            return false;
        }
    }
    
}
