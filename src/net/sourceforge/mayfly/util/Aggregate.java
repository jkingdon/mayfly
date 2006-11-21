package net.sourceforge.mayfly.util;

import net.sourceforge.mayfly.MayflyException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

abstract public class Aggregate extends ValueObject implements Iterable {
    private String messageIfNotFound = "{0} not found";
    
    public String toString() {
        return asList().toString();
    }

    public L collect(final Transformer transformer) {
        final L results = new L();

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

    public Aggregate select(final Selector selector) {
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

    abstract protected Aggregate createNew(Iterable items);

    public Object findFirst(Selector selector) {
        return findFirst(selector, false);
    }

    private Object findFirst(Selector selector, boolean shouldReturnNull) {

        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object element = iterator.next();
            if (selector.evaluate(element)) {
                return element;
            }
        }

        if (!shouldReturnNull) {
            throw new MayflyException(MessageFormat.format(messageIfNotFound, new Object[] {selector.toString()}));
        } else {
            return null;
        }
    }
    
    public Object findOne(Selector selector) {
        Object foundElement = null;
        boolean found = false;
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object element = iterator.next();
            if (selector.evaluate(element)) {
                if (found) {
                    throw new RuntimeException("found more than one");
                }
                found = true;
                foundElement = element;
            }
        }

        if (found) {
            return foundElement;
        } else {
            throw new MayflyException(MessageFormat.format(messageIfNotFound, new Object[] {selector.toString()}));
        }
    }

    public boolean exists(Selector selector) {
        return findFirst(selector, true)!=null;
    }

    public L asList() {
        return new L().addAll(this);
    }


    public Object element(int zeroBasedIndex) {
        return asList().get(zeroBasedIndex);
    }

    public int size() {
        return asList().size();
    }

    public Aggregate subtract(Aggregate rightSide) {
        return createNew(new IterableCollection(CollectionUtils.subtract(asList(), rightSide.asList())));
    }

    public boolean hasContents() {
        return iterator().hasNext();
    }

    static void mustBeAggregateType(Object element) {
        if (!(element instanceof Aggregate)) {
            throw new RuntimeException("this only works when the elements are themselves Aggregates.\n" +
                                       "  Element type was: " + element.getClass().getName());
        }
    }

    public Aggregate elements(int[] indexes) {
        L keysWanted = new L(Arrays.asList(ArrayUtils.toObject(indexes)));

        return createNew(new IterableCollection(asList().asIndexToElementMap().subMap(keysWanted).values()));
    }

    public Aggregate messageIfNotFound(String message) {
        this.messageIfNotFound = message;
        return this;
    }
}
