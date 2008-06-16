package net.sourceforge.mayfly.evaluation.from;

import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.evaluation.condition.And;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.evaluation.condition.Greater;

import org.junit.Test;


public class FromElementTest {
    
    @Test
    public void addToCondition() {
        FromElement start =
            new InnerJoin(new FromTable("foo"), new FromTable("bar"), Condition.TRUE);
        InnerJoin result = (InnerJoin) start.addToCondition(new Equal(null, null));
        ObjectAssert.assertInstanceOf(Equal.class, result.condition);
    }
    
    @Test
    public void addToConditionCreatesAnd() {
        FromElement start =
            new InnerJoin(new FromTable("foo"), new FromTable("bar"), 
            new Greater(null, null));
        InnerJoin result = (InnerJoin) start.addToCondition(new Equal(null, null));
        And and = (And) result.condition;
        ObjectAssert.assertInstanceOf(Greater.class, and.leftSide);
        ObjectAssert.assertInstanceOf(Equal.class, and.rightSide);
    }

    @Test
    public void addToConditionWorksForLeftJoin() {
        FromElement start =
            new LeftJoin(new FromTable("foo"), new FromTable("bar"), 
            new Greater(null, null));
        LeftJoin result = (LeftJoin) start.addToCondition(new Equal(null, null));
        And and = (And) result.condition;
        ObjectAssert.assertInstanceOf(Greater.class, and.leftSide);
        ObjectAssert.assertInstanceOf(Equal.class, and.rightSide);
    }

}
