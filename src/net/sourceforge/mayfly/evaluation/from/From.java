package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.MayflyInternalException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @internal
 * Not yet immutable, because of {@link #add(FromElement)}
 */
public class From {

    private final List fromElements;

    public From() { this.fromElements = new ArrayList(); }

    public From(List fromElements) {
        this.fromElements = fromElements;
    }

    public Iterator iterator() {
        return fromElements.iterator();
    }

    public From add(FromElement fromElement) {
        fromElements.add(fromElement);
        return this;
    }

    public void remove(int index) {
        fromElements.remove(index);
    }

    public void add(int index, FromElement element) {
        fromElements.add(index, element);
    }

    public FromElement soleElement() {
        if (size() != 1) {
            throw new MayflyInternalException(
                "optimizer left us " + size() + " elements");
        }
    
        return element(0);
    }

    public int size() {
        return fromElements.size();
    }
    
    public FromElement element(int index) {
        return (FromElement) fromElements.get(index);
    }

}
