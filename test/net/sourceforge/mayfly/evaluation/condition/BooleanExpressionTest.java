package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.*;

import java.util.*;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;

public class BooleanExpressionTest extends TestCase {
    
    public void testFirstAggregate() throws Exception {
        assertEquals(null, Where.EMPTY.firstAggregate());
        assertEquals(null, new Equal(new SingleColumn("x"), new SingleColumn("y")).firstAggregate());
        assertEquals("count(*)", new Equal(new CountAll("count"), new SingleColumn("y")).firstAggregate());
        assertEquals("count(*)", new IsNull(new CountAll("count")).firstAggregate());

        assertEquals(null, new In(new SingleColumn("x"), new ArrayList()).firstAggregate());
        assertEquals("count(*)", new In(new CountAll("count"), new ArrayList()).firstAggregate());
        assertEquals("count(*)",
            new In(
                new SingleColumn("x"), 
                Arrays.asList(new WhatElement[] { new CountAll("count") })
            ).firstAggregate()
        );

        assertEquals("count(*)", new Not(new IsNull(new CountAll("count"))).firstAggregate());
        assertEquals("count(*)", new Or(new IsNull(new CountAll("count")), Where.TRUE).firstAggregate());
        assertEquals("count(*)", new And(Where.TRUE, new IsNull(new CountAll("count"))).firstAggregate());
    }

}
