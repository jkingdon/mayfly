package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Rows extends Aggregate {
    private final ImmutableList rows;

    public Rows(ImmutableList rows) {
        this.rows = rows;
    }

    public Rows() {
        this(new ImmutableList());
    }

    public Rows(Row row) {
        this(ImmutableList.singleton(row));
    }

    protected Aggregate createNew(Iterable items) {
        return new Rows(new L(items).asImmutable());
    }

    public Iterator iterator() {
        return rows.iterator();
    }
    
    public int size() {
        return rows.size();
    }

}
