package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;

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

    public Object asObject() {
        return content;
    }

}
