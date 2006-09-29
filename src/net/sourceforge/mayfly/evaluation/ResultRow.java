package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.CellHeader;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @internal
 * Intention is that this will evolve into a mapping
 * from Expression to Cell, basically.
 */
public class ResultRow {
    
    private final Row row;
    private final ImmutableList elements;

    public ResultRow(Row row) {
        this.row = row;
        this.elements = fromRow(row);
    }

    public ResultRow() {
        this(new ImmutableList());
    }
    
    private ResultRow(ImmutableList elements) {
        this.row = null;
        this.elements = elements;
    }

    private static ImmutableList fromRow(Row row) {
        List result = new ArrayList();
        for (Iterator iter = row.iterator(); iter.hasNext();) {
            TupleElement columnAndValue = (TupleElement) iter.next();
            result.add(new Element(findExpression(columnAndValue), columnAndValue.cell()));
        }
        return new ImmutableList(result);
    }

    private static Expression findExpression(TupleElement columnAndValue) {
        CellHeader header = columnAndValue.header();
        if (header instanceof Column) {
            Column column = (Column) header;
            String tableOrAlias = column.tableOrAlias();
            return new SingleColumn(tableOrAlias, column.columnName());
        }
        else if (header instanceof PositionalHeader) {
            PositionalHeader positional = (PositionalHeader) header;
            return positional.expression;
        }
        else {
            throw new UnimplementedException();
        }
    }

    public int size() {
        return elements.size();
    }

    public Element element(int index) {
        return (Element) elements.get(index);
    }

    public SingleColumn findColumn(String columnName) {
        return findColumn(null, columnName);
    }

    public SingleColumn findColumn(String tableOrAlias, String columnName) {
        SingleColumn found = null;
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            if (element.expression instanceof SingleColumn) {
                SingleColumn candidate = (SingleColumn) element.expression;
                if (candidate.matches(tableOrAlias, columnName)) {
                    if (found != null) {
                        throw new MayflyException("ambiguous column " + columnName);
                    }
                    else {
                        found = candidate;
                    }
                }
            }
        }
        if (found == null) {
            throw new MayflyException("no column " + columnName);
        } else {
            return found;
        }
    }
    
    public Cell findValue(Expression target) {
        Cell result = findValueOrNull(target);
        if (result == null) {
            throw new MayflyInternalException("Where did expression " + target.displayName() + " come from?");
        }
        else {
            return result;
        }
    }

    private Cell findValueOrNull(Expression target) {
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            if (element.expression.sameExpression(target)) {
                return element.value;
            }
        }
        return null;
    }

    public Cell findValue(int zeroBasedColumn, Expression expression) {
        Cell result = findValueOrNull(expression);
        if (result != null) {
            return result;
        }
        else {
            return expression.evaluate(row);
        }
    }
    
    public ResultRow with(Expression expression, Cell value) {
        return new ResultRow(elements.with(new Element(expression, value)));
    }

    public static class Element {
        public final Expression expression;
        public final Cell value;

        public Element(Expression expression, Cell value) {
            this.expression = expression;
            this.value = value;
        }

        public SingleColumn column() {
            return (SingleColumn)expression;
        }
        
    }

}
