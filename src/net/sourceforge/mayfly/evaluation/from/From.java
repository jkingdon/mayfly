package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
