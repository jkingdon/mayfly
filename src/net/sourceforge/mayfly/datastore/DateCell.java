package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.parser.Location;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.sql.SQLException;

public class DateCell extends Cell {

    private final LocalDate localDate;

    public DateCell(int year, int month, int day) {
        this(new LocalDate(year, month, day));
    }

    public DateCell(LocalDate localDate) {
        this.localDate = localDate;
    }

    public java.sql.Date asDate(DateTimeZone zone) throws SQLException {
        return new java.sql.Date(localDate.toDateMidnight(zone).getMillis());
    }

    public int compareTo(Cell otherCell, Location location) {
        if (otherCell instanceof DateCell) {
            DateCell dateCell = (DateCell) otherCell;
            return localDate.compareTo(dateCell.localDate);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw cannotCompare(otherCell, location);
        }
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
