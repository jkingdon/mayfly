package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;

import org.apache.commons.lang.StringEscapeUtils;

public class StringCell extends Cell {

    private final String content;

    public StringCell(String content) {
        this.content = content;
    }

    public int asInt() {
        throw new MayflyException("Attempt to read string " + content + " as an int");
    }

    public long asLong() {
        throw new MayflyException("Attempt to read string " + content + " as a long");
    }

    public String asString() {
        return content;
    }
    
    public String displayName() {
        return "string '" + StringEscapeUtils.escapeSql(content) + "'";
    }

    public Object asObject() {
        return content;
    }

    public int compareTo(Cell otherCell) {
        if (otherCell instanceof StringCell) {
            return content.compareTo(((StringCell) otherCell).content);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw new MayflyInternalException(
                "Attempt to compare a " + this.getClass().getName() + " to a " + otherCell.getClass().getName()
            );
        }
    }

}
