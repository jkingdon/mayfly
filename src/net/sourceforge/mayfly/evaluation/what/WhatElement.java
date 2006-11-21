package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.ResultRow;

/**
 * @internal
 * Something mention in a select clause (*, table.*, or expression).
 * It is possible that the expression case should be handled by having
 * a subclass which wraps
 * an expression, rather than having 
 * {@link net.sourceforge.mayfly.evaluation.Expression}
 * inherit from us.  Then the distinction between 
 * {@link net.sourceforge.mayfly.evaluation.what.Selected}
 * and {@link net.sourceforge.mayfly.evaluation.what.What} would carry down
 * to here.
 */
abstract public class WhatElement {

    abstract public Selected selected(ResultRow dummyRow);

    abstract public String displayName();

}
