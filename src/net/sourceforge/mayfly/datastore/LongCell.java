package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.parser.Location;

import java.sql.SQLException;


public class LongCell extends Cell {

    private final long value;

    public LongCell(long value) {
        this.value = value;
    }

    @Override
    public byte asByte() throws SQLException {
        return longToByte(value);
    }

    @Override
    public short asShort() throws SQLException {
        return longToShort(value);
    }

    @Override
    public int asInt() throws SQLException {
        return longToInt(value);
    }

    @Override
    public long asLong() {
        return value;
    }
    
    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public Object asObject() {
        return new Long(value);
    }

    @Override
    public String asString() {
        return stringValue();
    }

    private String stringValue() {
        return String.valueOf(value);
    }
    
    @Override
    public String displayName() {
        return "number " + stringValue();
    }
    
    @Override
    public String asSql() {
        return stringValue();
    }

    @Override
    public int compareTo(Cell otherCell, Location location) {
        if (otherCell instanceof LongCell) {
            return new Long(value).compareTo(new Long(((LongCell) otherCell).value));
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw cannotCompare(otherCell, location);
        }
    }

}
