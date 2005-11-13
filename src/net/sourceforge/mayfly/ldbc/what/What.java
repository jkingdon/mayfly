package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import java.sql.*;
import java.util.*;

public class What extends Aggregate {

    private List elements = new ArrayList();

    public What() {
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

    public Columns selectedColumns() throws SQLException {
        List result = new ArrayList();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            result.addAll(element.columns().asImmutableList());
        }
        return new Columns(new ImmutableList(result));
    }

    public int parameterCount() {
        int count = 0;
        for (int i = 0; i < elements.size(); ++i) {
            if (elements.get(i) instanceof JdbcParameter) {
                ++count;
            }
        }
        return count;
    }

    public void substitute(Iterator jdbcParameters) {
        for (int i = 0; i < elements.size(); ++i) {
            if (elements.get(i) instanceof JdbcParameter) {
                elements.set(i, Literal.fromValue(jdbcParameters.next()));
            }
        }
    }

}
