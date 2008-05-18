package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class NotNullOrUnique extends Constraint {

    protected final ColumnNames names;

    /**
     * Here we take {@link Columns} as opposed to {@link ColumnNames}
     * merely as a way to express the concept that the names have
     * been verified to exist in the store.
     */
    protected NotNullOrUnique(Columns columns, String constraintName) {
        this(ColumnNames.fromColumns(columns), constraintName);
    }
    
    protected NotNullOrUnique(ColumnNames columns, String constraintName) {
        super(constraintName);
        this.names = columns;
        if (names.size() == 0) {
            throw new MayflyInternalException("must have at least one column for a constraint");
        }
    }
    
    @Override
    public void checkExistingRows(DataStore store, TableReference table) {
        Rows allRows = store.table(table).rows();
        for (int i = 0; i < allRows.rowCount(); ++i) {
            check(allRows.subList(0, i), allRows.row(i), table, Location.UNKNOWN);
        }
    }

    @Override
    public void check(Rows existingRows, Row proposedRow, 
        TableReference table, Location location) {
        List proposedValues = collectProposedValues(proposedRow);

        for (Iterator iter = existingRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            List valuesForRow = valuesForRow(row);
            if (sqlEquals(proposedValues, valuesForRow, location)) {
                throw new MayflyException(
                    constraintName(table) + ": duplicate value" +
                    (valuesForRow.size() == 1 ? "" : "s") +
                    " " + 
                    describeValues(valuesForRow));
            }
        }
    }

    public static boolean sqlEquals(List left, List right, Location location) {
        if (left.size() != right.size()) {
            throw new MayflyInternalException(
                "meant to compare equal size lists but were " + 
                left.size() + " and " + right.size());
        }
        for (int i = 0; i < left.size(); ++i) {
            Cell leftCell = (Cell) left.get(i);
            Cell rightCell = (Cell) right.get(i);
            if (!leftCell.sqlEquals(rightCell, location)) {
                return false;
            }
        }
        return true;
    }

    private List valuesForRow(Row row) {
        List valuesForRow = new ArrayList();
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            String column = (String) iterator.next();
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

    private String constraintName(TableReference table) {
        StringBuilder message = new StringBuilder();
        message.append(description());
        if (constraintName != null) {
            message.append(" ");
            message.append(constraintName);
            message.append(" (");
        }
        else {
            message.append(" in ");
        }
        message.append("table ");
        message.append(table.tableName());
        message.append(", column");
        if (names.size() != 1) {
            message.append("s");
        }
        message.append(" ");

        Iterator iter = names.iterator();
        String firstColumn = (String) iter.next();
        message.append(firstColumn);
        while (iter.hasNext()) {
            String column = (String) iter.next();
            message.append(",");
            message.append(column);
        }
        
        if (constraintName != null) {
            message.append(")");
        }
        return message.toString();
    }
    
    private List collectProposedValues(Row proposedRow) {
        List proposedValues = new ArrayList();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            String column = (String) iter.next();
            Cell proposedCell = proposedRow.cell(column);
            checkForNull(column, proposedCell);
            proposedValues.add(proposedCell);
        }
        return proposedValues;
    }

    protected abstract void checkForNull(String column, Cell proposedCell);

    protected abstract String description();
    
    @Override
    public boolean checkDropColumn(TableReference table, String column) {
        if (names.hasColumn(column)) {
            if (names.size() > 1) {
                throw new MayflyException(
                    "attempt to drop column " + column + 
                    " from multi-column " + constraintName(table));
            }
            return false;
        }
        return true;
    }
    
    @Override
    public boolean canBeTargetOfForeignKey(String targetColumn) {
        return names.size() == 1 && names.hasColumn(targetColumn);
    }

}
