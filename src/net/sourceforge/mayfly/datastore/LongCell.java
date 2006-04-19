package net.sourceforge.mayfly.datastore;

import java.sql.SQLException;

import net.sourceforge.mayfly.MayflyInternalException;


public class LongCell extends Cell {

    private final long value;

    public LongCell(long value) {
        this.value = value;
    }

    public byte asByte() throws SQLException {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return (byte) value;
        }
        else {
            throw new SQLException("Value " + value + " does not fit in a byte");
        }
    }

    public short asShort() throws SQLException {
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return (short) value;
        }
        else {
            throw new SQLException("Value " + value + " does not fit in a short");
        }
    }

    public int asInt() throws SQLException {
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            return (int) value;
        }
        else {
            throw new SQLException("Value " + value + " does not fit in an int");
        }
    }

    public long asLong() {
        return value;
    }
    
    public double asDouble() {
        return value;
    }

    public Object asObject() {
        return new Long(value);
    }

    public String asString() {
        return String.valueOf(value);
    }
    
    public String displayName() {
        return "number " + asString();
    }

    public int compareTo(Cell otherCell) {
        if (otherCell instanceof LongCell) {
            return new Long(value).compareTo(new Long(((LongCell) otherCell).value));
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw new MayflyInternalException(
                "Attempt to compare a " + this.getClass().getName() + " to a " + otherCell.getClass().getName()
            );
        }
    }

}
