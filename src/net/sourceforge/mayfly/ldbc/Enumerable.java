package net.sourceforge.mayfly.ldbc;

import java.util.*;

abstract public class Enumerable extends ValueObject {
    
    public Collection collect(Transformer transformer) {
        List results = new ArrayList();
        Iterator iter = iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            results.add(transformer.transform(item));
        }
        return results;
    }

    abstract protected Iterator iterator();

    public Object select(Selector selector) {
        List selected = new ArrayList();
        Iterator iter = iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (selector.evaluate(item)) {
                selected.add(item);
            }
        }
        return createNew(selected);
    }

    abstract protected Object createNew(Collection items);

    public Object find(Selector selector) {
        return find(selector, false);
    }

    private Object find(Selector selector, boolean shouldReturnNull) {
        Iterator iter = iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
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

    public boolean exists(Selector selector) {
        return find(selector, true) != null;
    }

}
