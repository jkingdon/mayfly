package net.sourceforge.mayfly.ldbc.what;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.Count;
import net.sourceforge.mayfly.evaluation.expression.Divide;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Minus;
import net.sourceforge.mayfly.evaluation.expression.Multiply;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.parser.Parser;

public class WhatElementTest extends TestCase {

    public void testDisplayNameNonExpressions() {
        assertEquals("*", new All().displayName());
        assertEquals("foo.*", new AllColumnsFromTable("foo").displayName());
    }
    
    public void testDisplayNameBasicExpressions() throws Exception {
        assertEquals("foo.x", new SingleColumn("foo", "x").displayName());
        assertEquals("x", new SingleColumn("x").displayName());

        assertEquals("3", new IntegerLiteral(3).displayName());
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
