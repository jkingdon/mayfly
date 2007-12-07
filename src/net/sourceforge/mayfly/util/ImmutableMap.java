package net.sourceforge.mayfly.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ImmutableMap<K, V> implements Map<K, V> {

    Map<K, V> delegate;

    public ImmutableMap() {
        delegate = Collections.EMPTY_MAP;
    }

    public ImmutableMap(Map map) {
        delegate = Collections.unmodifiableMap(new LinkedHashMap(map));
    }

    public ImmutableMap(K key, V value) {
        delegate = Collections.singletonMap(key, value);
    }

    private ImmutableMap(Map alreadyCopied, boolean didICopyIt) {
        if (!didICopyIt) {
            throw new RuntimeException("Call with() or public constructor instead");
        }
        delegate = Collections.unmodifiableMap(alreadyCopied);
    }

    public ImmutableMap with(K key, V value) {
        Map copy = new LinkedHashMap(this);
        copy.put(key, value);
        return new ImmutableMap(copy, true);
    }
    
    public ImmutableMap add(K key, V value) {
        Map copy = new LinkedHashMap(this);
        if (copy.put(key, value) != null) {
            throw new RuntimeException("key " + key + " already exists");
        }
        return new ImmutableMap(copy, true);
    }

    public ImmutableMap without(K key) {
        Map copy = new LinkedHashMap(this);
        if (copy.remove(key) == null) {
            throw new NoSuchKeyException(key.toString());
        }
        return new ImmutableMap(copy, true);
    }


    


    public void clear() {
        throw new UnsupportedOperationException("Attempt to mutate immutable map");
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

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Set keySet() {
        return delegate.keySet();
    }

    public V put(K key, V value) {
        throw new UnsupportedOperationException("Attempt to mutate immutable map");
    }

    public void putAll(Map m) {
        throw new UnsupportedOperationException("Attempt to mutate immutable map");
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException("Attempt to mutate immutable map");
    }

    public int size() {
        return delegate.size();
    }

    public Collection values() {
        return delegate.values();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
