package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

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

    public What selected(Row dummyRow) {
        L result = new L();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            result.addAll(element.selected(dummyRow));
        }
        return new What(result);
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

    public Cell evaluate(int oneBasedColumn, Row row) {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= elements.size()) {
            throw new MayflyException("no column " + oneBasedColumn);
        }
        WhatElement element = (WhatElement) elements.get(zeroBasedColumn);
        return element.findValue(zeroBasedColumn, row);
    }

    public Rows aggregate(Rows rows) {
        TupleBuilder builder = new TupleBuilder();
        for (int i = 0; i < elements.size(); ++i) {
            WhatElement element = (WhatElement) elements.get(i);
            builder.append(new PositionalHeader(i), element.aggregate(rows));
        }
        Row resultRow = new Row(builder);
        return new Rows(resultRow);
    }

}
