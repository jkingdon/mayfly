package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;

public class From {

    private final ImmutableList fromElements;

    public From(FromElement... elements) { 
        this.fromElements = ImmutableList.fromArray(elements);
    }

    public From(ImmutableList fromElements) {
        this.fromElements = fromElements;
    }

    public Iterator iterator() {
        return fromElements.iterator();
    }

    public From with(FromElement fromElement) {
        return new From(fromElements.with(fromElement));
    }

    public From without(int index) {
        return new From(fromElements.without(index));
    }

    public From with(int index, FromElement element) {
        return new From(fromElements.with(index, element));
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
