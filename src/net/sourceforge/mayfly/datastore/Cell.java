package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

public class Cell extends ValueObject {
    private final Object content; // Needs to be immutable (currently Number, String)

    public Cell(Object content) {
        this.content = content;
    }

    public int asInt() {
        return ((Number)content).intValue();
    }

    public long asLong() {
        return ((Number)content).longValue();
    }

    public String asString() {
        return (String)content;
    }

    public String toString() {
        return content.toString();
    }

    public Object asObject() {
        return content;
    }
}
