package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;

public abstract class Cell extends ValueObject {
    public static Cell fromContents(Object contents) {
        if (contents instanceof Number) {
            Number number = (Number) contents;
            return new LongCell(number.longValue());
        } else if (contents instanceof String) {
            String string = (String) contents;
            return new StringCell(string);
        } else if (contents instanceof NullCellContent) {
            return NullCell.INSTANCE;
        } else {
            throw new MayflyException("Don't know how to deal with type " + contents.getClass().getName());
        }
    }

    abstract public int asInt();

    abstract public long asLong();

    abstract public String asString();

    public String toString() {
        return asObject().toString();
    }

    abstract public Object asObject();

    abstract public int compareTo(Cell otherCell);

    abstract public String displayName();

}
