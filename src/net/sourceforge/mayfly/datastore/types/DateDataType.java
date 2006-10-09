package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DateCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.parser.Location;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateDataType extends DataType {

    public Cell coerce(Value value) {
        if (value.value instanceof StringCell) {
            return stringToDate(value.value.asString(), value.location);
        }
        else if (value.value instanceof NullCell) {
            return value.value;
        }
        else {
            throw new MayflyException("Attempt to store " + 
                value.value.displayName() + " as a date",
                value.location);
        }
    }

    DateCell stringToDate(String text) {
        return stringToDate(text, Location.UNKNOWN);
    }

    DateCell stringToDate(String text, Location location) {
        Pattern pattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            return new DateCell(year, month, day);
        }
        throw new MayflyException(
            "'" + StringEscapeUtils.escapeSql(text) + 
            "' is not in format yyyy-mm-dd",
            location);
    }

}
