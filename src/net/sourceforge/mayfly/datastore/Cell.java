package net.sourceforge.mayfly.datastore;

import org.joda.time.DateTimeZone;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ValueObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;

public abstract class Cell extends ValueObject {

    /**
        @internal
        Convert to byte.  As byte is an exact type,
        should throw an exception if the value cannot be
        represented in a byte (likewise for {@link #asShort()},
        {@link #asInt()}, and {@link #asLong()}.
      */
    public byte asByte() throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as a byte");
    }

    public short asShort() throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as a short");
    }

    public int asInt() throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as an int");
    }

    public long asLong() throws MayflyException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a long");
    }

    public Object asObject() {
        throw new UnimplementedException(
            "attempt to read " + displayName() + " as an object");
    }

    public String asString() {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a string");
    }
    
    public String toString() {
        return asBriefString();
    }

    /**
     * @internal
     * Must override either this or {@link #asObject()} to avoid infinite loop.
     */
    public String asBriefString() {
        return asObject().toString();
    }

    public BigDecimal asBigDecimal() {
        throw new UnimplementedException(
            "cannot yet get BigDecimal for " + getClass().getName());
    }

    public java.sql.Date asDate(DateTimeZone zone) throws SQLException {
        throw new SQLException("attempt to read " + displayName() + " as a date");
    }

    public java.sql.Timestamp asTimestamp(DateTimeZone zone) throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as a timestamp");
    }

    /**
        @internal
        Convert to double.  As double is a floating-point (inexact) type,
        it is OK to truncate/round.
      */
    public double asDouble() throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as a double");
    }

    public InputStream asBinaryStream() throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as binary data");
    }

    public byte[] asBytes() throws SQLException {
        throw new SQLException(
            "attempt to read " + displayName() + " as binary data");
    }

    final public int compareTo(Cell otherCell) {
        return compareTo(otherCell, Location.UNKNOWN);
    }

    abstract public int compareTo(Cell otherCell, Location location);

    public final boolean sqlEquals(Cell otherCell) {
        return sqlEquals(otherCell, Location.UNKNOWN);
    }

    public boolean sqlEquals(Cell otherCell, Location location) {
        return compareTo(otherCell, location) == 0;
    }

    abstract public String displayName();

    protected MayflyException cannotCompare(Cell otherCell, Location location) {
        return new MayflyInternalException(
            "Attempt to compare a " + this.getClass().getName() + 
            " to a " + otherCell.getClass().getName(),
            location
        );
    }

}
