package net.sourceforge.mayfly.util;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.*;
import org.apache.commons.collections.*;
import org.apache.commons.lang.*;

import java.util.*;
import java.text.*;

abstract public class Aggregate extends ValueObject implements Iterable {
    private String messageIfNotFound = "{0} not found";

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
            throw new MayflyException(MessageFormat.format(messageIfNotFound, new Object[] {selector.toString()}));
        } else {
            return null;
        }
    }

    public boolean exists(Selector selector) {
        return find(selector, true)!=null;
    }

    public Aggregate plus(Aggregate other) {
        L list =
            new L()
                .addAll(this)
                .addAll(other);

        return createNew(list);
    }

    public L asList() {
        return new L().addAll(this);
    }


    public M zipper(Aggregate mapValues) {
        return zipper(Transformer.JUST_RETURN, mapValues, Transformer.JUST_RETURN);
    }

    public M zipper(Transformer keyTransformer, Aggregate mapValues, Transformer valueTransformer) {
        L keys = collect(keyTransformer);
        L values = mapValues.collect(valueTransformer);

        if (keys.size()!=values.size()) {
            throw new RuntimeException("mapify only supports equal-sized key and value lists. \n" +
                                       "there were (" + keys.size() + " keys and " + values.size() + " values)");
        }

        if (keys.size()!= new HashSet(keys).size()) {
            throw new RuntimeException("mapify only supports unique keysets. \n" +
                                       "keys: " + keys.toString());
        }

        M result = new M();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.get(i));
        }

        return result;
    }

    public Aggregate with(Object newElement) {
        Aggregate asAnotherAggregate = createNew(new IterableCollection(new L().append(newElement)));
        return plus(asAnotherAggregate);
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

    public Aggregate cartesianJoin(final Aggregate rightSide) {
        final L joinResult = new L();

        each(
            new Each() {
                public void each(Object element) {
                    mustBeAggregateType(element);

                    final Aggregate leftElement = (Aggregate) element;

                    rightSide.each(new Each() {
                        public void each(Object element) {
                            mustBeAggregateType(element);

                            Aggregate rightElement = (Aggregate) element;

                            Aggregate combined = leftElement.plus(rightElement);

                            joinResult.append(combined);
                        }
                    });

                }
            }
        );

        return createNew(joinResult);
    }

    private static void mustBeAggregateType(Object element) {
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
