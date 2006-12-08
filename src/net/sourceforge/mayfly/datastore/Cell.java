package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflySqlException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ValueObject;

import org.joda.time.DateTimeZone;

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
        throw new MayflyException(
            "attempt to read " + displayName() + " as a byte").asSqlException();
    }

    public short asShort() throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a short").asSqlException();
    }

    public int asInt() throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as an int").asSqlException();
    }

    public long asLong() throws MayflyException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a long");
    }

    public Object asObject() {
        throw new UnimplementedException(
            "attempt to read " + displayName() + " as an object");
    }

    public String asString() throws MayflyException {
        /** TODO: This method is semantically overloaded; it goes into
           various messages, and also implements
           {@link java.sql.ResultSet#getString(int)}
           Clean this up or else we'll be buggy about things
           like UNIQUE constraints on type DECIMAL.
           */
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

    abstract public String asSql();

    public BigDecimal asBigDecimal() throws MayflySqlException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a decimal")
            .asSqlException();
    }

    public java.sql.Date asDate(DateTimeZone zone) throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a date")
            .asSqlException();
    }

    public java.sql.Timestamp asTimestamp(DateTimeZone zone) throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a timestamp")
            .asSqlException();
    }

    /**
        @internal
        Convert to double.  As double is a floating-point (inexact) type,
        it is OK to truncate/round.
      */
    public double asDouble() throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as a double")
            .asSqlException();
    }

    public InputStream asBinaryStream() throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as binary data")
            .asSqlException();
    }

    public byte[] asBytes() throws SQLException {
        throw new MayflyException(
            "attempt to read " + displayName() + " as binary data")
            .asSqlException();
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
        return new MayflyException(
            "attempt to compare " + displayName() + 
            " to " + otherCell.displayName(),
            location
        );
    }

    static int longToInt(long localValue) throws SQLException {
        if (localValue >= Integer.MIN_VALUE && localValue <= Integer.MAX_VALUE) {
            return (int) localValue;
        }
        else {
            throw new SQLException("Value " + localValue + " does not fit in an int");
        }
    }

    static short longToShort(long value) throws SQLException {
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return (short) value;
        }
        else {
            throw new SQLException("Value " + value + " does not fit in a short");
        }
    }

    static byte longToByte(long value) throws SQLException {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return (byte) value;
        }
        else {
            throw new SQLException("Value " + value + " does not fit in a byte");
        }
    }

}
