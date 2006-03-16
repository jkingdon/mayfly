package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.ldbc.Columns;
import net.sourceforge.mayfly.ldbc.Rows;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.StringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PrimaryKey {

    private final Columns columns;

    public PrimaryKey(Columns columns) {
        this.columns = columns;
    }

    public PrimaryKey() {
        this(new Columns(new ImmutableList()));
    }

    public void check(Rows existingRows, Row proposedRow) {
        int columnCount = columns.size();
        if (columnCount == 0) {
            return;
        }
        
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

    String describeValues(List valuesForRow) {
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
        message.append("primary key ");
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
            if (proposedCell instanceof NullCell) {
                throw new MayflyException("primary key " + column.columnName() + " cannot be null");
            }
            proposedValues.add(proposedCell);
        }
        return proposedValues;
    }

}
