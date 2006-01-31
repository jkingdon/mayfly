package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;
import net.sourceforge.mayfly.ldbc.Rows;
import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
