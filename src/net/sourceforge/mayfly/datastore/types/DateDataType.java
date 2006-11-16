package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DateCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.parser.Location;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateDataType extends DataType {

    public Cell coerce(Value value, String columnName) {
        if (value.value instanceof StringCell) {
            return stringToDate(value.value.asString(), value.location);
        }
        else {
            return genericCoerce(value, columnName, "date", DateCell.class);
        }
    }

    DateCell stringToDate(String text) {
        return stringToDate(text, Location.UNKNOWN);
    }

    DateCell stringToDate(String text, Location location) {
        LocalDate date = parseDate(text);
        if (date != null) {
            return new DateCell(date);
        }
        
        LocalDateTime dateFromTimestamp = TimestampDataType.parseTimestamp(text);
        if (dateFromTimestamp != null) {
            return new DateCell(dateFromTimestamp.toLocalDate());
        }
        throw new MayflyException(
            "'" + StringEscapeUtils.escapeSql(text) + 
            "' is not in format yyyy-mm-dd",
            location);
    }

    public static LocalDate parseDate(String text) {
        Pattern pattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            return new LocalDate(year, month, day);
        }
        else {
            return null;
        }
    }
    
    public String dumpName() {
        return "DATE";
    }

}
