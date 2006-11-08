package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.parser.Location;

import org.apache.commons.lang.StringEscapeUtils;

import java.sql.SQLException;

public class StringCell extends Cell {

    private final String content;

    public StringCell(String content) {
        this.content = content;
    }

    public byte asByte() throws SQLException {
        throw new SQLException("Attempt to read string " + content + " as a byte");
    }

    public short asShort() throws SQLException {
        throw new SQLException("Attempt to read string " + content + " as a short");
    }

    public int asInt() throws SQLException {
        throw new SQLException("Attempt to read string " + content + " as an int");
    }

    public long asLong() {
        throw new MayflyException("Attempt to read string " + content + " as a long");
    }

    public double asDouble() throws SQLException {
        throw new MayflyException("Attempt to read string " + content + " as a double");
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
            return content.compareTo(((StringCell) otherCell).content);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw cannotCompare(otherCell, location);
        }
    }

}
