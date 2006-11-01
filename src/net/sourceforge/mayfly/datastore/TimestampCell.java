package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.parser.Location;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.SQLException;

public class TimestampCell extends Cell {
    
    public static final DateTimeFormatter FORMATTER = 
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime stamp;

    public TimestampCell(int year, int month, int day, 
        int hour, int minute, int second) {
        this(new LocalDateTime(year, month, day, hour, minute, second, 000));
    }
    
    public TimestampCell(LocalDateTime time) {
        this.stamp = time;
    }

    public java.sql.Timestamp asTimestamp(DateTimeZone zone)
    throws SQLException {
        return new java.sql.Timestamp(stamp.toDateTime(zone).getMillis());
    }

    public int compareTo(Cell otherCell, Location location) {
        if (otherCell instanceof TimestampCell) {
            TimestampCell stampCell = (TimestampCell) otherCell;
            return stamp.compareTo(stampCell.stamp);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        throw cannotCompare(otherCell, location);
    }

    public String displayName() {
        return "timestamp " + asBriefString();
    }
    
    public String asBriefString() {
        return FORMATTER.print(stamp);
    }

    public int year() {
        return stamp.getYear();
    }

    public int month() {
        return stamp.getMonthOfYear();
    }

    public int day() {
        return stamp.getDayOfMonth();
    }

    public int hour() {
        return stamp.getHourOfDay();
    }

    public int minute() {
        return stamp.getMinuteOfHour();
    }

    public int second() {
        return stamp.getSecondOfMinute();
    }

}
