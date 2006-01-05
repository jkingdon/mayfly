package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.util.*;

import java.util.*;

public class From extends Aggregate {

    private List fromElements = new ArrayList();


    public From() { }

    public From(List fromElements) {
        this.fromElements = fromElements;
    }


    protected Aggregate createNew(Iterable items) {
        return new From(new L().addAll(items));
    }

    public Iterator iterator() {
        return fromElements.iterator();
    }

    public From add(FromElement fromElement) {
        fromElements.add(fromElement);
        return this;
    }

}
