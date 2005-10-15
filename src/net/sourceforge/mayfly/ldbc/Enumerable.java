package net.sourceforge.mayfly.ldbc;

import java.util.*;

abstract public class Enumerable extends ValueObject implements Iterable {

    public Collection collect(final Transformer transformer) {
        final List results = new ArrayList();

        each(new Each() {
            public void each(Object obj) {
                results.add(transformer.transform(obj));
            }
        });

        return results;
    }

    public void each(Each e) {
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            e.each(o);
        }
    }

    public Object select(final Selector selector) {
        final List selected = new ArrayList();

        each(
            new Each() {
                public void each(Object obj) {
                    if (selector.evaluate(obj)) {
                        selected.add(obj);
                    }
                }
            }
        );

        return createNew(new IterableCollection(selected));
    }

    abstract protected Object createNew(Iterable items);

    public Object find(Selector selector) {
        return find(selector, false);
    }

    private Object find(Selector selector, boolean shouldReturnNull) {

        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object element = iterator.next();
            if (selector.evaluate(element)) {
                return element;
            }
        }

        if (!shouldReturnNull) {
            throw new RuntimeException("not found");
        } else {
            return null;
        }
    }

    public boolean exists(Selector selector) {
        return find(selector, true)!=null;
    }

    public static List asList(Iterable iter) {
        List l = new ArrayList();

        for (Iterator iterator = iter.iterator(); iterator.hasNext();) {
            Object o =  iterator.next();
            l.add(o);
        }

        return l;
    }

}
