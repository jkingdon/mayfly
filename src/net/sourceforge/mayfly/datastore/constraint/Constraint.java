package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;

public interface Constraint {

    abstract public void check(Rows existingRows, Row proposedRow);

}
