package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.util.*;

import java.util.*;

public class What extends Aggregate {


    private List masks = new ArrayList();

    public What() {
    }

    public What(List masks) {
        this.masks = masks;
    }


    protected Object createNew(Iterable items) {
        return new What(asList(items));
    }

    public Iterator iterator() {
        return masks.iterator();
    }

    public What add(WhatElement maskElement) {
        masks.add(maskElement);
        return this;
    }


}
