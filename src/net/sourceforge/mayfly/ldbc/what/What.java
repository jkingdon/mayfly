package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.util.*;

import java.sql.*;
import java.util.*;

public class What extends Aggregate {


    private List masks = new ArrayList();

    public What() {
    }

    public What(List masks) {
        this.masks = masks;
    }


    protected Aggregate createNew(Iterable items) {
        return new What(new L().slurp(items));
    }

    public Iterator iterator() {
        return masks.iterator();
    }

    public What add(WhatElement maskElement) {
        masks.add(maskElement);
        return this;
    }

    public List selectedColumns() throws SQLException {
        List result = new ArrayList();
        for (Iterator iter = masks.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            result.add(element.columnName());
        }
        return result;
    }


}
