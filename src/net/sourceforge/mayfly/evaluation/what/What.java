package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @internal
 * Not yet immutable, because of {@link #add(WhatElement)}
 */
public class What extends Aggregate {

    private List elements;

    public What() {
        this(new ArrayList());
    }

    public What(List elements) {
        this.elements = elements;
    }


    protected Aggregate createNew(Iterable items) {
        return new What(new L().addAll(items));
    }

    public Iterator iterator() {
        return elements.iterator();
    }

    public What add(WhatElement element) {
        elements.add(element);
        return this;
    }

    public Selected selected(ResultRow dummyRow) {
        L result = new L();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            result.addAll(element.selected(dummyRow));
        }
        return new Selected(result);
    }

}
