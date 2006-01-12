package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class WhatElementTest extends TestCase {

    public void testDisplayNameNonExpressions() {
        assertEquals("*", new All().displayName());
        assertEquals("foo.*", new AllColumnsFromTable("foo").displayName());
        assertEquals("?", JdbcParameter.INSTANCE.displayName());
    }
    
    public void testDisplayNameBasicExpressions() throws Exception {
        assertEquals("foo.x", new SingleColumn("foo", "x").displayName());
        assertEquals("x", new SingleColumn("x").displayName());

        assertEquals("3", new MathematicalInt(3).displayName());
        assertEquals("'don''t'", new QuotedString("'don''t'").displayName());
    }
    
    public void testDisplayNameAggregates() throws Exception {
        assertEquals("COunt(*)", new CountAll("COunt").displayName());
        assertEquals("COunt(x)", new Count(new SingleColumn("x"), "COunt", false).displayName());
        assertEquals("COunt(distinct x)", new Count(new SingleColumn("x"), "COunt", true).displayName());
        assertEquals("MAX(x)", new Maximum(new SingleColumn("x"), "MAX", false).displayName());
    }
    
    public void xtestOperatorsNoPrecedence() throws Exception {
        assertEquals("x || y", new Concatenate(new SingleColumn("x"), new SingleColumn("y")).displayName());
        assertEquals("x * y", new Multiply(new SingleColumn("x"), new SingleColumn("y")).displayName());
        assertEquals("x / y", new Divide(new SingleColumn("x"), new SingleColumn("y")).displayName());
        assertEquals("x + y", new Plus(new SingleColumn("x"), new SingleColumn("y")).displayName());
        assertEquals("x - y", new Minus(new SingleColumn("x"), new SingleColumn("y")).displayName());
    }
    
    public void xtestPrecedence() throws Exception {
        assertEquals(
            "x + y * z", 
            new Plus(
                new SingleColumn("x"),
                new Multiply(new SingleColumn("y"), new SingleColumn("z"))
            )
            .displayName()
        );

        assertEquals(
            "x * y * z",
            new Multiply(
                new SingleColumn("x"),
                new Multiply(new SingleColumn("y"), new SingleColumn("z"))
            )
            .displayName()
        );

        assertEquals(
            "(x + y) * z", 
            new Multiply(
                new Plus(new SingleColumn("x"), new SingleColumn("y")),
                new SingleColumn("z")
            )
            .displayName()
        );
    }
    
    public void testPrecedence2() throws Exception {
        String actual = new Parser("x + y + z * (w / 4) - y - x / (y * z)").parseWhatElement().displayName();
//        assertEquals("x + y + z * (w / 4) - y - x / (y * z)", actual);
        assertEquals("expression", actual);
    }

}
