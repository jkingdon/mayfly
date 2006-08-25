package net.sourceforge.mayfly.datastore;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;

import java.sql.SQLException;

public class DateCell extends Cell {

    private final LocalDate localDate;

    public DateCell(int year, int month, int day) {
        localDate = new LocalDate(year, month, day);
    }

    public byte asByte() throws SQLException {
        throw new SQLException(
            "Attempt to read date " + asBriefString() + " as a byte");
    }

    public double asDouble() throws SQLException {
        throw new SQLException(
            "Attempt to read date " + asBriefString() + " as a double");
    }

    public short asShort() throws SQLException {
        throw new SQLException(
            "Attempt to read date " + asBriefString() + " as an short");
    }

    public int asInt() throws SQLException {
        throw new SQLException(
            "Attempt to read date " + asBriefString() + " as an int");
    }

    public long asLong() {
        throw new MayflyException(
            "Attempt to read date " + asBriefString() + " as a long");
    }

    public Object asObject() {
        throw new UnimplementedException(
            "Attempt to read date " + asBriefString() + " as an object");
    }

    public String asString() {
        throw new MayflyException(
            "Attempt to read date " + asBriefString() + " as a string");
    }
    
    public java.sql.Date asDate(DateTimeZone zone) throws SQLException {
        return new java.sql.Date(localDate.toDateMidnight(zone).getMillis());
    }

    public int compareTo(Cell otherCell) {
        throw new UnimplementedException();
    }

    public String displayName() {
        return "date " + asBriefString();
    }
    
    public String asBriefString() {
        return localDate.toString();
    }

    public int year() {
        return localDate.getYear();
    }

    public int month() {
        return localDate.getMonthOfYear();
    }

    public int day() {
        return localDate.getDayOfMonth();
    }

}
