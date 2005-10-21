package net.sourceforge.mayfly.util;

import net.sourceforge.mayfly.datastore.*;

import java.util.*;

public class M implements Map {

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


    public M plus(Map other) {
        M together = new M();
        together.putAll(this);
        together.putAll(other);
        return together;
    }
}
