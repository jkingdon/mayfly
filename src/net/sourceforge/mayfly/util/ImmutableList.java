package net.sourceforge.mayfly.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ImmutableList<T> implements List<T> {

    public static ImmutableList singleton(Object singleElement) {
        return new ImmutableList(Collections.singletonList(singleElement), true);
    }
    
    public static ImmutableList fromArray(Object[] elements) {
        return new ImmutableList(Arrays.asList(elements));
    }
    
    List<T> delegate;

    public ImmutableList() {
        delegate = Collections.EMPTY_LIST;
    }

    public ImmutableList(Collection contents) {
        delegate = Collections.unmodifiableList(new ArrayList(contents));
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

    public ImmutableList with(int index, Object newElement) {
        List copy = new ArrayList(this);
        copy.add(index, newElement);
        return new ImmutableList(copy, true);
    }

    public ImmutableList without(int index) {
        List copy = new ArrayList(this);
        copy.remove(index);
        return new ImmutableList(copy, true);
    }

    public ImmutableList withAll(List elementsToAdd) {
        List copy = new ArrayList(this);
        copy.addAll(elementsToAdd);
        return new ImmutableList(copy, true);
    }


    
    public boolean add(T o) {
        return delegate.add(o);
    }

    public void add(int index, T o) {
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

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public T get(int index) {
        return delegate.get(index);
    }

    @Override
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

    public T remove(int index) {
        return delegate.remove(index);
    }

    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }

    public T set(int index, T o) {
        return delegate.set(index, o);
    }

    public int size() {
        return delegate.size();
    }

    public ImmutableList<T> subList(int fromIndex, int toIndex) {
        /* Here we rely on the fact that delegate is immutable.
           Although we don't actually copy the sublist, there is
           no way for it to change out from under the ImmutableList
           we are returning.
         */
        return new ImmutableList<T>(delegate.subList(fromIndex, toIndex), true);
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
