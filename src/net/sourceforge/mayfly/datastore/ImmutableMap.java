package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

import java.util.*;

public class ImmutableMap extends ValueObject implements Map {

    Map delegate;

    public ImmutableMap() {
        delegate = Collections.EMPTY_MAP;
    }

    public ImmutableMap(Map map) {
        delegate = Collections.unmodifiableMap(new LinkedHashMap(map));
    }

    public ImmutableMap(Map alreadyCopied, boolean didICopyIt) {
        if (!didICopyIt) {
            throw new RuntimeException("Call with() or public constructor instead");
        }
        delegate = Collections.unmodifiableMap(alreadyCopied);
    }

    public ImmutableMap with(Object key, Object value) {
        Map copy = new LinkedHashMap(this);
        copy.put(key, value);
        return new ImmutableMap(copy, true);
    }

    public ImmutableMap without(Object key) {
        Map copy = new LinkedHashMap(this);
        if (copy.remove(key) == null) {
            throw new NoSuchKeyException(key.toString());
        }
        return new ImmutableMap(copy, true);
    }


    


    public void clear() {
        delegate.clear();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public Set entrySet() {
        return delegate.entrySet();
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Object get(Object key) {
        return delegate.get(key);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Set keySet() {
        return delegate.keySet();
    }

    public Object put(Object key, Object value) {
        return delegate.put(key, value);
    }

    public void putAll(Map m) {
        delegate.putAll(m);
    }

    public Object remove(Object key) {
        return delegate.remove(key);
    }

    public int size() {
        return delegate.size();
    }

    public Collection values() {
        return delegate.values();
    }

    public String toString() {
        return delegate.toString();
    }

}
