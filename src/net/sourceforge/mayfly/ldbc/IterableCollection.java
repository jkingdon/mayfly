package net.sourceforge.mayfly.ldbc;

import java.util.*;

public class IterableCollection implements Iterable {
    private Collection c;

    public IterableCollection(Collection c) {
        this.c = c;
    }

    public Iterator iterator() {
        return c.iterator();
    }
}
