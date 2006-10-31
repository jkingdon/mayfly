package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TimestampCell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.parser.Location;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampDataType extends DataType {

    public Cell coerce(Value value) {
        if (value.value instanceof StringCell) {
            return stringToDate(value.value.asString(), value.location);
        }
        else {
            return genericCoerce(value, "a timestamp");
        }
    }

    TimestampCell stringToDate(String text) {
        return stringToDate(text, Location.UNKNOWN);
    }

    TimestampCell stringToDate(String text, Location location) {
        Pattern pattern = Pattern.compile(
            "([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            int hour = Integer.parseInt(matcher.group(4));
            int minute = Integer.parseInt(matcher.group(5));
            int second = Integer.parseInt(matcher.group(6));
            return new TimestampCell(year, month, day, hour, minute, second);
        }
        throw new MayflyException(
            "'" + StringEscapeUtils.escapeSql(text) + 
            "' is not in format yyyy-mm-dd hh:mm:ss",
            location);
    }

}
