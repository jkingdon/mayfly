package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;

import java.sql.SQLException;
import java.util.Calendar;

public class DateCell extends Cell {

    private final int year;
    private final int month;
    private final int day;

    public DateCell(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
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
    
    public java.sql.Date asDate(Calendar calendar) throws SQLException {
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    public int compareTo(Cell otherCell) {
        throw new UnimplementedException();
    }

    public String displayName() {
        return "date " + asBriefString();
    }
    
    public String asBriefString() {
        return format(year, 4) + "-" + format(month, 2) + "-" + format(day, 2);
    }

    private String format(int value, int width) {
        String text = Integer.toString(value);
        while (text.length() < width) {
            text = "0" + text;
        }
        return text;
    }

    public int year() {
        return year;
    }

    public int month() {
        return month;
    }

    public int day() {
        return day;
    }

}
