package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.parser.Location;

public abstract class Constraint {

    abstract public void check(
        Rows existingRows, Row proposedRow, Location location);

    /**
     * @return Should we keep this column?
     */
    abstract public boolean checkDropColumn(String column);

}
