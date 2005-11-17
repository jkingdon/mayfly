package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;

public class LongCell extends Cell {

    private final long value;

    public LongCell(long value) {
        this.value = value;
    }

    public int asInt() {
        return (int) value;
    }

    public long asLong() {
        return value;
    }

    public Object asObject() {
        return new Long(value);
    }

    public String asString() {
        throw new MayflyException("Attempt to read number " + value + " as a string");
    }
    
    

}
