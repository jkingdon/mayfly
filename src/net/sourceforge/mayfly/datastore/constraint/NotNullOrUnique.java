package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.util.StringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class NotNullOrUnique implements Constraint {

    private final Columns columns;

    protected NotNullOrUnique(Columns columns) {
        this.columns = columns;
        if (columns.size() == 0) {
            throw new MayflyInternalException("must have at least one column for a constraint");
        }
    }

    public void check(Rows existingRows, Row proposedRow) {
        List proposedValues = collectProposedValues(proposedRow);

        for (Iterator iter = existingRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            List valuesForRow = valuesForRow(row);
            if (proposedValues.equals(valuesForRow)) {
                throw new MayflyException(
                    constraintName() + " already has a value " + describeValues(valuesForRow));
            }
        }
    }

    private List valuesForRow(Row row) {
        List valuesForRow = new ArrayList();
        for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
            Column column = (Column) iterator.next();
            valuesForRow.add(row.cell(column));
        }
        return valuesForRow;
    }

    static String describeValues(List valuesForRow) {
        StringBuilder message = new StringBuilder();
        Iterator iter = valuesForRow.iterator();
        message.append(((Cell) iter.next()).asString());
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            message.append(",");
            message.append(cell.asString());
        }
        return message.toString();
    }

    private String constraintName() {
        StringBuilder message = new StringBuilder();
        message.append(description());
        message.append(" ");
        Iterator iter = columns.iterator();
        message.append(((Column) iter.next()).columnName());
        while (iter.hasNext()) {
            Column column = (Column) iter.next();
            message.append(",");
            message.append(column.columnName());
        }
        return message.toString();
    }

    private List collectProposedValues(Row proposedRow) {
        List proposedValues = new ArrayList();
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            Cell proposedCell = proposedRow.cell(column);
            checkForNull(column, proposedCell);
            proposedValues.add(proposedCell);
        }
        return proposedValues;
    }

    protected abstract void checkForNull(Column column, Cell proposedCell);

    protected abstract String description();

}
