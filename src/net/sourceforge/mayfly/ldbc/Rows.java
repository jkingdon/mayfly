package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Rows extends Aggregate {
    private ImmutableList rows;

    public Rows(ImmutableList rows) {
        this.rows = rows;
    }

    protected Object createNew(Iterable items) {
        return new Rows(new L(items).asImmutable());
    }

    public Iterator iterator() {
        return rows.iterator();
    }

    public Rows join(Rows rightSide) {
        final L joined = new L();

        for (Iterator leftSideIter = rows.iterator(); leftSideIter.hasNext();) {
            final ImmutableMap leftRow = (ImmutableMap) leftSideIter.next();

            rightSide.each(
                new Each() {
                    public void each(Object element) {
                        Map rightRow = (Map) element;
                        joined.add(new M(leftRow).plus(rightRow));
                    }
                }
            );

        }

        return new Rows(joined.asImmutable());
    }
}
