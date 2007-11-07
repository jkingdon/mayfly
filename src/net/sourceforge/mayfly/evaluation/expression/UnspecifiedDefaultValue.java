package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;

public class UnspecifiedDefaultValue extends DefaultValue {

    @Override
    public Cell cell() {
        return NullCell.INSTANCE;
    }

    @Override
    public boolean isSpecified() {
        return false;
    }

    @Override
    public boolean sqlEquals(Cell cell) {
        return false;
    }

    @Override
    public String asSql() {
        throw new MayflyInternalException(
            "dumper should have been checking whether there is a default value");
    }

}
