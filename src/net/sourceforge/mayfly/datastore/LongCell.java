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
        return String.valueOf(value);
    }
    
    public String displayName() {
        return "number " + asString();
    }

    public int compareTo(Cell otherCell) {
        if (otherCell instanceof LongCell) {
            return new Long(value).compareTo(new Long(((LongCell) otherCell).value));
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
