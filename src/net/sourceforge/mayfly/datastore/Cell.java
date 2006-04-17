package net.sourceforge.mayfly.datastore;

import java.math.BigDecimal;
import java.sql.SQLException;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.util.ValueObject;

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

    abstract public byte asByte() throws SQLException;

    abstract public short asShort() throws SQLException;
    
    abstract public int asInt() throws SQLException;

    abstract public long asLong();

    abstract public String asString();

    public String toString() {
        return asObject().toString();
    }

    abstract public Object asObject();
    
    public BigDecimal asBigDecimal() {
        throw new UnimplementedException("cannot yet get BigDecimal for " + getClass().getName());
    }

    public Object asContents() {
        return asObject();
    }

    abstract public int compareTo(Cell otherCell);

    public boolean sqlEquals(Cell otherCell) {
        return compareTo(otherCell) == 0;
    }

    abstract public String displayName();

}
