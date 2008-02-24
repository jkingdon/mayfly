package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

public class What implements Iterable<WhatElement> {

    private final ImmutableList<WhatElement> elements;

    public What(WhatElement... elements) {
        this(ImmutableList.fromArray(elements));
    }

    public What(ImmutableList elements) {
        this.elements = elements;
    }

    public Iterator<WhatElement> iterator() {
        return elements.iterator();
    }

    public What with(WhatElement newElement) {
        return new What(elements.with(newElement));
    }

    public Selected selected(ResultRow dummyRow) {
        L result = new L();
        for (WhatElement element : elements) {
            result.addAll(element.selected(dummyRow));
        }
        return new Selected(new ImmutableList(result));
    }

    public Expression lookupAlias(String name) {
        for (WhatElement element : elements) {
            Expression result = element.lookupAlias(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public Expression lookupName(String name) {
        for (WhatElement element : elements) {
            Expression result = element.lookupName(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public int size() {
        return elements.size();
    }

    public WhatElement element(int zeroBasedColumn) {
        return elements.get(zeroBasedColumn);
    }

}
