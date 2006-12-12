package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;

abstract public class DefaultValue {
    
    public final static DefaultValue NOT_SPECIFIED = 
        new UnspecifiedDefaultValue();

    abstract public Cell cell();

    abstract public boolean isSpecified();

    abstract public boolean sqlEquals(Cell cell);

    abstract public String asSql();

}
