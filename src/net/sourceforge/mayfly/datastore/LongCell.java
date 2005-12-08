package net.sourceforge.mayfly.datastore;


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

    public int compareTo(Cell otherCell) {
        return new Long(value).compareTo(new Long(((LongCell) otherCell).value));
    }

}
