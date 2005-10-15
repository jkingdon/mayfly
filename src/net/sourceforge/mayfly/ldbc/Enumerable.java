package net.sourceforge.mayfly.ldbc;

import java.util.*;

abstract public class Enumerable<AggregateT, ElementT> extends ValueObject implements Iterable<ElementT> {
    
    public <To> Collection<To> collect(Transformer<ElementT, To> transformer) {
        List<To> results = new ArrayList<To>();
        for (ElementT item : this) {
            results.add(transformer.transform(item));
        }
        return results;
    }

    public AggregateT select(Selector<ElementT> selector) {
        List<ElementT> selected = new ArrayList<ElementT>();
        for (ElementT item : this) {
            if (selector.evaluate(item)) {
                selected.add(item);
            }
        }
        return createNew(selected);
    }

    abstract protected AggregateT createNew(Collection<ElementT> items);

    public ElementT find(Selector<ElementT> selector) {
        return find(selector, false);
    }

    private ElementT find(Selector<ElementT> selector, boolean shouldReturnNull) {
        for (ElementT item : this) {
            if (selector.evaluate(item)) {
                return item;
            }
        }

        if (!shouldReturnNull) {
            throw new RuntimeException("not found");
        } else {
            return null;
        }
    }

    public boolean exists(Selector<ElementT> selector) {
        return find(selector, true)!=null;
    }

}
