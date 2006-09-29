package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DateCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateDataType extends DataType {

    public Cell coerce(Cell value) {
        if (value instanceof StringCell) {
            return stringToDate(value.asString());
        }
        else if (value instanceof NullCell) {
            return value;
        }
        else {
            throw new MayflyException("Attempt to store " + 
                value.displayName() + " as a date");
        }
    }

    DateCell stringToDate(String text) {
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
            "' is not in format yyyy-mm-dd");
    }

}
