package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.datastore.types.DateDataType;
import net.sourceforge.mayfly.parser.Location;

import org.apache.commons.lang.StringEscapeUtils;

public class StringCell extends Cell {

    private final String content;

    public StringCell(String content) {
        this.content = content;
    }

    public String asString() {
        return content;
    }
    
    public String displayName() {
        return "string " + asQuotedString();
    }
    
    public String asSql() {
        return asQuotedString();
    }

    private String asQuotedString() {
        return "'" + StringEscapeUtils.escapeSql(content) + "'";
    }

    public Object asObject() {
        return content;
    }

    public int compareTo(Cell otherCell, Location location) {
        if (otherCell instanceof StringCell) {
            return sqlStringCompare(content, ((StringCell) otherCell).content);
        }
        else if (otherCell instanceof DateCell) {
            return coerceToDate(location).compareTo(otherCell);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw cannotCompare(otherCell, location);
        }
    }

    public static int sqlStringCompare(String mine, String theirs) {
        return mine.compareTo(theirs);
    }

    public DateCell coerceToDate(Location location) {
        return new DateDataType().stringToDate(content, location);
    }

}
