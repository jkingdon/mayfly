package net.sourceforge.mayfly.datastore;

import java.util.*;

public class ImmutableList implements List {
    
    List delegate;

    public ImmutableList() {
        delegate = Collections.EMPTY_LIST;
    }

    public ImmutableList(List list) {
        delegate = Collections.unmodifiableList(new ArrayList(list));
    }
    
    private ImmutableList(List alreadyCopied, boolean didICopyIt) {
        if (!didICopyIt) {
            throw new RuntimeException("Call with() or public constructor instead");
        }
        delegate = Collections.unmodifiableList(alreadyCopied);
    }

    public ImmutableList with(Object newElement) {
        List copy = new ArrayList(this);
        copy.add(newElement);
        return new ImmutableList(copy, true);
    }


    
    public boolean add(Object o) {
        return delegate.add(o);
    }

    public void add(int index, Object o) {
        delegate.add(index, o);
    }

    public boolean addAll(Collection c) {
        return delegate.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return delegate.addAll(index, c);
    }

    public void clear() {
        delegate.clear();
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Object get(int index) {
        return delegate.get(index);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Iterator iterator() {
        return delegate.iterator();
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return delegate.listIterator();
    }

    public ListIterator listIterator(int index) {
        return delegate.listIterator(index);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public Object remove(int index) {
        return delegate.remove(index);
    }

    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }

    public Object set(int index, Object o) {
        return delegate.set(index, o);
    }

    public int size() {
        return delegate.size();
    }

    public List subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }

    public String toString() {
        return delegate.toString();
    }

}
