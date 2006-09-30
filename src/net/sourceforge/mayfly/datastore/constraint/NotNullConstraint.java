package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.parser.Location;

public class NotNullConstraint extends Constraint {

    private final Column column;

    public NotNullConstraint(Column column) {
        this.column = column;
    }

    public void check(Rows existingRows, Row proposedRow, Location location) {
        Cell proposedCell = proposedRow.cell(column);
        if (proposedCell instanceof NullCell) {
            throw new MayflyException(
                "column " + column.columnName() + " cannot be null",
                location);
        }
    }

}
