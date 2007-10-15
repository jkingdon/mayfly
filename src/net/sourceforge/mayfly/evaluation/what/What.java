package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

public class What {

    private final ImmutableList elements;

    public What(WhatElement... elements) {
        this(ImmutableList.fromArray(elements));
    }

    public What(ImmutableList elements) {
        this.elements = elements;
    }

    public Iterator iterator() {
        return elements.iterator();
    }

    public What with(WhatElement newElement) {
        return new What(elements.with(newElement));
    }

    public Selected selected(ResultRow dummyRow) {
        L result = new L();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            result.addAll(element.selected(dummyRow));
        }
        return new Selected(result);
    }

    public Expression lookupAlias(String name) {
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            Expression result = element.lookupAlias(name);
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
        return (WhatElement) elements.get(zeroBasedColumn);
    }

}
