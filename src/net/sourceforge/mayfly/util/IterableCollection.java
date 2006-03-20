package net.sourceforge.mayfly.util;

import java.util.Collection;
import java.util.Iterator;

public class IterableCollection implements Iterable {
    private Collection c;

    public IterableCollection(Collection c) {
        this.c = c;
    }

    public Iterator iterator() {
        return c.iterator();
    }
}
