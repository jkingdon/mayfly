package net.sourceforge.mayfly.util;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

import java.util.*;

public class M extends Aggregate implements Map {

    public static M fromEntries(Iterator entryIter) {
        M newMap = new M();

        while (entryIter.hasNext()) {
            Entry entry = (Entry) entryIter.next();
            newMap.put(entry.getKey(), entry.getValue());
        }

        return newMap;
    }


    private Map delegate;

    public M() {
        this(new HashMap());
    }

    public M(Map map) {
        this.delegate = map;
    }

    public M entry(Object key, Object value) {
        put(key, value);
        return this;
    }

    public ImmutableMap asImmutable() {
        return new ImmutableMap(this);
    }

    protected Aggregate createNew(Iterable items) {

        Iterator iter = items.iterator();

        return fromEntries(iter);
    }



    public Iterator iterator() {
        return entrySet().iterator();
    }


    public int hashCode() {
        return delegate.hashCode();
    }

    public int size() {
        return delegate.size();
    }

    public void clear() {
        delegate.clear();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Collection values() {
        return delegate.values();
    }

    public void putAll(Map t) {
        delegate.putAll(t);
    }

    public Set entrySet() {
        return delegate.entrySet();
    }

    public Set keySet() {
        return delegate.keySet();
    }

    public Object get(Object key) {
        return delegate.get(key);
    }

    public Object remove(Object key) {
        return delegate.remove(key);
    }

    public Object put(Object key, Object value) {
        return delegate.put(key, value);
    }

    public String toString() {
        return delegate.toString();
    }

    public M subMap(L keysWanted) {
        final M result = new M(new TreeMap());

        keysWanted.each(new Each() {
            public void each(Object element) {
                result.put(element, get(element));
            }
        });

        return result;
    }
}
