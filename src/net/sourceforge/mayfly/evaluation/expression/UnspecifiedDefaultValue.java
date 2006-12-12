package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;

public class UnspecifiedDefaultValue extends DefaultValue {

    public Cell cell() {
        return NullCell.INSTANCE;
    }

    public boolean isSpecified() {
        return false;
    }

    public boolean sqlEquals(Cell cell) {
        return false;
    }

    public String asSql() {
        throw new MayflyInternalException(
            "dumper should have been checking whether there is a default value");
    }

}
