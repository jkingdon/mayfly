package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.util.ValueObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;

public abstract class Cell extends ValueObject {

    /**
        @internal
        Convert to byte.  As byte is an exact type,
        should throw an exception if the value cannot be
        represented in a byte (likewise for {@link #asShort()},
        {@link #asInt()}, and {@link #asLong()}.
      */
    abstract public byte asByte() throws SQLException;

    abstract public short asShort() throws SQLException;
    
    abstract public int asInt() throws SQLException;

    abstract public long asLong() throws MayflyException;

    abstract public String asString();

    public String toString() {
        return asBriefString();
    }
    
    public String asBriefString() {
        return asObject().toString();
    }

    abstract public Object asObject();
    
    public BigDecimal asBigDecimal() {
        throw new UnimplementedException("cannot yet get BigDecimal for " + getClass().getName());
    }

    public java.sql.Date asDate(Calendar calendar) throws SQLException {
        throw new SQLException("Attempt to read " + displayName() + " as a date");
    }

    /**
        @internal
        Convert to double.  As double is a floating-point (inexact) type,
        it is OK to truncate/round.
      */
    abstract public double asDouble() throws SQLException;

    abstract public int compareTo(Cell otherCell);

    public boolean sqlEquals(Cell otherCell) {
        return compareTo(otherCell) == 0;
    }

    abstract public String displayName();

}
