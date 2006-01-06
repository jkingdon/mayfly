package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

abstract public class Expression extends WhatElement implements Transformer {

    abstract public Cell evaluate(Row row);

    abstract public Cell aggregate(Rows rows);

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }

    public Object transform(Object from) {
        throw new UnimplementedException();
    }

}
