package net.sourceforge.mayfly.util;


import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class M implements Map {

    public static M fromEntries(Iterable entries) {
        M newMap = new M();

        Iterator entryIter = entries.iterator();
        while (entryIter.hasNext()) {
            Map.Entry entry = (Map.Entry) entryIter.next();
            newMap.put(entry.getKey(), entry.getValue());
        }

        return newMap;
    }


    private Map delegate;

    public M() {
        this(new LinkedHashMap());
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


    public Iterator iterator() {
        return entrySet().iterator();
    }


    @Override
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

    @Override
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

    @Override
    public String toString() {
        return delegate.toString();
    }

}
