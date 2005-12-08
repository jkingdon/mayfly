package net.sourceforge.mayfly.util;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;

import org.apache.commons.collections.*;
import org.apache.commons.lang.*;

import java.util.*;

public class L extends Aggregate implements List {
    private List delegate;

    public L() {
        this(new ArrayList());
    }

    public L(List list) {
        delegate = list;
    }

    public L(Iterable items) {
        delegate = IteratorUtils.toList(items.iterator());
    }

    protected Aggregate createNew(Iterable items) {
        return new L(items);
    }

    public L append(Object o) {
        add(o);
        return this;
    }

    public Object selectObjectThatIs(final Class type) {
        return selectObjectsThatAre(type).get(0);
    }

    public L selectObjectsThatAre(final Class type) {
        return (L)
            select(new Selector() {
                public boolean evaluate(Object candidate) {
                    return type.isAssignableFrom(candidate.getClass());
                }
            });
    }

    public Object selectSingle(Class aClass, Object defaultValue) {
        int matchCount = selectObjectsThatAre(aClass).size();
        if (matchCount == 0) {
            return defaultValue;
        } else if (matchCount == 1) {
            return selectObjectThatIs(aClass);
        } else {
            throw new MayflyInternalException("Expected none or one of type " + aClass.getName() + 
                ", got " + matchCount);
        }
    }

    public boolean contains(int candidate) {
        return contains(new Integer(candidate));
    }


    public ImmutableList asImmutable() {
        return new ImmutableList(this);
    }

    public M asIndexToElementMap() {
        M result = new M();

        for (int index = 0; index < size(); index++) {
            result.put(new Integer(index), get(index));
        }

        return result;
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

    public Object[] toArray() {
        return delegate.toArray();
    }

    public Object get(int index) {
        return delegate.get(index);
    }

    public Object remove(int index) {
        return delegate.remove(index);
    }

    public void add(int index, Object element) {
        delegate.add(index, element);
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public boolean add(Object o) {
        return delegate.add(o);
    }

    public void add(int element) {
        add(new Integer(element));
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public boolean addAll(int index, Collection c) {
        return delegate.addAll(index, c);
    }

    public boolean addAll(Collection c) {
        return delegate.addAll(c);
    }

    /** Iterate through iterable, slurping each element into this list. */
    public L addAll(Iterable iterable) {
        Iterator iter = iterable.iterator();
        while (iter.hasNext()) {
            append(iter.next());
        }

        return this;
    }

    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }

    public Iterator iterator() {
        return delegate.iterator();
    }

    public List subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

	public List subList(int fromIndex) {
		return subList(fromIndex, size());
	}

    public ListIterator listIterator() {
        return delegate.listIterator();
    }

    public ListIterator listIterator(int index) {
        return delegate.listIterator(index);
    }

    public Object set(int index, Object element) {
        return delegate.set(index, element);
    }

    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }


    public static L fromArray(Object[] objects) {
        return new L(Arrays.asList(objects));
    }

    public static L fromArray(int[] objects) {
        return new L(Arrays.asList(ArrayUtils.toObject(objects)));
    }

    public String toString() {
        return delegate.toString();
    }

    public L asUnmodifiable() {
        return new L(Collections.unmodifiableList(this));
    }

}
