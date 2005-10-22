package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

public class Cell extends ValueObject {
    private Object obj;

    public Cell(Object obj) {
        this.obj = obj;
    }

    public int asInt() {
        return ((Number)obj).intValue();
    }

    public long asLong() {
        return ((Number)obj).longValue();
    }
}
