package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.CellHeader;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.StringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @internal
 * Mapping from Expression to Cell.
 */
public class ResultRow {
    
    private final ImmutableList elements;

    public ResultRow(Row row) {
        this.elements = fromRow(row);
    }

    public ResultRow() {
        this(new ImmutableList());
    }
    
    private ResultRow(ImmutableList elements) {
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
    
    public Cell cell(int index) {
        return element(index).value;
    }
    
    public Expression expression(int index) {
        return element(index).expression;
    }

    public Expression findColumn(String columnName) {
        return findColumn(null, columnName);
    }

    public Expression findColumn(String tableOrAlias, String columnName) {
        Expression found = null;
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
            throw new MayflyException("no column " + Column.displayName(tableOrAlias, columnName));
        } else {
            return found;
        }
    }
    
    public Cell findValue(Expression target) {
        Cell result = findValueOrNull(target);
        if (result == null) {
            throw new MayflyInternalException(
                "Where did expression " + target.displayName() + " come from?");
        }
        else {
            return result;
        }
    }

    public Cell findValueOrNull(Expression target) {
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            if (target.matches(element.expression)) {
                return element.value;
            }
        }
        return null;
    }

    public Cell findOrEvaluate(Expression expression) {
        Cell result = findValueOrNull(expression);
        if (result != null) {
            return result;
        }
        else {
            return expression.evaluate(this);
        }
    }
    
    public ResultRow withColumn(String tableOrAlias, String columnName, Cell cell) {
        return with(new SingleColumn(tableOrAlias, columnName), cell);
    }

    public ResultRow withColumn(String column, String cellValue) {
        return withColumn(column, new StringCell(cellValue));
    }

    public ResultRow withColumn(String columnName, Cell cell) {
        return with(new SingleColumn(columnName), cell);
    }

    public ResultRow with(Expression expression, Cell value) {
        return new ResultRow(elements.with(new Element(expression, value)));
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Result Row:\n");
        for (int i = 0; i < elements.size(); ++i) {
            Element element = (Element) elements.get(i);
            out.append("  ");
            out.append(element.expression.displayName());
            out.append(" = ");
            out.append(element.value.displayName());
            out.append("\n");
        }
        return out.toString();
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
