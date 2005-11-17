package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;

public class Cell extends ValueObject {
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

    private final Object content; // Needs to be immutable (currently Number, String)

    public Cell(Object content) {
        this.content = content;
    }

    public int asInt() {
        return ((Number)content).intValue();
    }

    public long asLong() {
        return ((Number)content).longValue();
    }

    public String asString() {
        return (String)content;
    }

    public String toString() {
        return content.toString();
    }

    public Object asObject() {
        return content;
    }

}
