package net.sourceforge.mayfly.datastore;

import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflySqlException;
import net.sourceforge.mayfly.parser.Location;

public class NullCell extends Cell {
    
    public static final NullCell INSTANCE = new NullCell();
    
    private NullCell() {
    }

    @Override
    public byte asByte() {
        // JDBC seems to force this on us.
        return 0;
    }

    @Override
    public short asShort() {
        // JDBC seems to force this on us.
        return 0;
    }

    @Override
    public int asInt() {
        // JDBC seems to force this on us.
        return 0;
    }

    @Override
    public long asLong() {
        // JDBC seems to force this on us.
        return 0;
    }

    @Override
    public double asDouble() throws SQLException {
        // What are we supposed to return here?
        throw new SQLException("Attempt to read SQL NULL as an object");
    }
    
    @Override
    public BigDecimal asBigDecimal() throws MayflySqlException {
        return null;
    }
    
    @Override
    public String asString() {
        return null;
    }
    
    @Override
    public byte[] asBytes() throws SQLException {
        return null;
    }
    
    @Override
    public Date asDate(DateTimeZone zone) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp asTimestamp(DateTimeZone zone) throws SQLException {
        return null;
    }

    @Override
    public String displayName() {
        return "null";
    }
    
    @Override
    public String asSql() {
        return "null";
    }

    @Override
    public Object asObject() {
        // What are we supposed to return here?
        throw new MayflyException("Attempt to read SQL NULL as an object");
    }
    
    @Override
    public int compareTo(Cell otherCell, Location location) {
        return otherCell instanceof NullCell ? 0 : -1;
    }

    @Override
    public boolean sqlEquals(Cell otherCell, Location location) {
        return false;
    }

}
