package net.sourceforge.mayfly.datastore;

import org.joda.time.DateTimeZone;

import java.sql.Date;
import java.sql.SQLException;

import net.sourceforge.mayfly.MayflyException;

public class NullCell extends Cell {
    
    public static final NullCell INSTANCE = new NullCell();
    
    private NullCell() {
    }

    public byte asByte() {
        // JDBC seems to force this on us.
        return 0;
    }

    public short asShort() {
        // JDBC seems to force this on us.
        return 0;
    }

    public int asInt() {
        // JDBC seems to force this on us.
        return 0;
    }

    public long asLong() {
        // JDBC seems to force this on us.
        return 0;
    }

    public double asDouble() throws SQLException {
        // What are we supposed to return here?
        throw new SQLException("Attempt to read SQL NULL as an object");
    }
    
    public String asString() {
        return null;
    }
    
    public byte[] asBytes() throws SQLException {
        return null;
    }
    
    public Date asDate(DateTimeZone zone) throws SQLException {
        return null;
    }

    public String displayName() {
        return "null";
    }

    public Object asObject() {
        // What are we supposed to return here?
        throw new MayflyException("Attempt to read SQL NULL as an object");
    }
    
    public int compareTo(Cell otherCell) {
        return otherCell instanceof NullCell ? 0 : -1;
    }

    public boolean sqlEquals(Cell otherCell) {
        return false;
    }

}
