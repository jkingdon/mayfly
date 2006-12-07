package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.parser.Location;

import java.sql.SQLException;


public class LongCell extends Cell {

    private final long value;

    public LongCell(long value) {
        this.value = value;
    }

    public byte asByte() throws SQLException {
        return longToByte(value);
    }

    public short asShort() throws SQLException {
        return longToShort(value);
    }

    public int asInt() throws SQLException {
        return longToInt(value);
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
    
    public String asSql() {
        return asString();
    }

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
