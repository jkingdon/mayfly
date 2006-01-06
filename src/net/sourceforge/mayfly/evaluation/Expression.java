package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

abstract public class Expression extends WhatElement {

    abstract public Cell evaluate(Row row);

    abstract public Cell aggregate(Rows rows);

}
