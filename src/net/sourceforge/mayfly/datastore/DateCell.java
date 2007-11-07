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

    @Override
    public java.sql.Date asDate(DateTimeZone zone) throws SQLException {
        return new java.sql.Date(asMillis(zone));
    }
    
    @Override
    public java.sql.Timestamp asTimestamp(DateTimeZone zone) throws SQLException {
        return new java.sql.Timestamp(asMillis(zone));
    }

    private long asMillis(DateTimeZone zone) {
        return localDate.toDateMidnight(zone).getMillis();
    }

    @Override
    public int compareTo(Cell otherCell, Location location) {
        if (otherCell instanceof DateCell) {
            DateCell dateCell = (DateCell) otherCell;
            return localDate.compareTo(dateCell.localDate);
        }
        else if (otherCell instanceof StringCell) {
            StringCell otherAsString = (StringCell)otherCell;
            DateCell otherAsDate = otherAsString.coerceToDate(location);
            return compareTo(otherAsDate);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw cannotCompare(otherCell, location);
        }
    }

    @Override
    public String displayName() {
        return "date " + asBriefString();
    }
    
    @Override
    public String asBriefString() {
        return localDate.toString();
    }
    
    @Override
    public String asSql() {
        return "'" + localDate.toString() + "'";
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
