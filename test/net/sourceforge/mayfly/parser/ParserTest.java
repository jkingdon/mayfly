package net.sourceforge.mayfly.parser;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.constraint.Cascade;
import net.sourceforge.mayfly.datastore.constraint.NoAction;
import net.sourceforge.mayfly.datastore.constraint.SetDefault;
import net.sourceforge.mayfly.datastore.constraint.SetNull;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.CreateTable;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.condition.Greater;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.Count;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.Divide;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Minimum;
import net.sourceforge.mayfly.evaluation.expression.Minus;
import net.sourceforge.mayfly.evaluation.expression.Multiply;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.Sum;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;
import net.sourceforge.mayfly.evaluation.expression.literal.DecimalLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.LongLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.util.ImmutableByteArray;
import net.sourceforge.mayfly.util.MayflyAssert;

import java.util.ArrayList;
import java.util.List;

public class ParserTest extends TestCase {
    
    public void testEmptyString() throws Exception {
        expectFailure("", "expected identifier but got end of file");
    }

    public void testIdentifier() throws Exception {
        Parser parser = new Parser("foo");
        parser.parseFromTable();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testRemainingTokens() throws Exception {
        Parser parser = new Parser("foo inner");
        parser.parseFromTable();
        assertEquals("INNER", parser.remainingTokens());
    }
    
    public void testIdentifierDot() throws Exception {
        expectFailure("foo.", "expected identifier but got end of file");
    }
    
    public void testSchemaDotTable() throws Exception {
        new Parser("mars.foo").parseFromTable();
    }

    public void testDotIdentifier() throws Exception {
        expectFailure(".foo", "expected identifier but got '.'");
    }
    
    public void testSchemaDotTableAlias() throws Exception {
        Parser parser = new Parser("mars.foo f");
        parser.parseFromTable();
        assertEquals("", parser.remainingTokens());
    }

    public void testTableAlias() throws Exception {
        Parser parser = new Parser("foo f");
        parser.parseFromTable();
        assertEquals("", parser.remainingTokens());
    }

    public void testSelect() throws Exception {
        Parser parser = new Parser("select * from foo");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testExtraneousTokensAtEnd() throws Exception {
        Parser parser = new Parser("select * from foo 5");
        try {
            parser.parse();
            fail();
        } catch (MayflyException e) {
            assertEquals("expected end of file but got 5", e.getMessage());
        }
    }
    
    public void testExtraneousTokensAtEndForDrop() throws Exception {
        Parser parser = new Parser("drop table foo (");
        try {
            parser.parse();
            fail();
        } catch (MayflyException e) {
            assertEquals("expected end of file but got '('", e.getMessage());
        }
    }
    
    public void testExtraneousTokensExampleTwo() throws Exception {
        Parser parser = new Parser("select * from foo 5");
        try {
            parser.parse();
            fail();
        } catch (MayflyException e) {
            assertEquals("expected end of file but got 5", e.getMessage());
        }
    }
    
    public void testListOfFromItems() throws Exception {
        Parser parser = new Parser("select * from foo, bar b, baz");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testCrossJoin() throws Exception {
        Parser parser = new Parser("foo, bar cross join baz, quux");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testCrossJoinWithOn() throws Exception {
        Parser parser = new Parser("select a, b from foo cross join bar on 1 = 1");
        try {
            parser.parse();
            fail();
        } catch (ParserException e) {
            // In this example, we might say:
            // "Specify INNER JOIN, not CROSS JOIN, if you want an ON condition"
            // but there is a dangling ON issue.  So until we understand how the
            // parser knows which JOIN the ON goes with, let's not get too fancy.
            assertEquals("expected end of file but got ON", e.getMessage());
        }
    }
    
    public void testInnerJoin() throws Exception {
        Parser parser = new Parser("foo, bar inner join baz on a = b, quux");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testInnerJoinOnMissing() throws Exception {
        Parser parser = new Parser("foo inner join bar");
        try {
            parser.parseFromItems();
            fail();
        } catch (ParserException e) {
            assertEquals("expected ON but got end of file", e.getMessage());
        }
    }
    
    public void testLeftOuterJoin() throws Exception {
        Parser parser = new Parser("foo, bar left outer join baz on a = b");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testLeftJoin() throws Exception {
        Parser parser = new Parser("foo, bar left join baz on a = b");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testWhere() throws Exception {
        Parser parser = new Parser("select * from foo where a = b");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testTableDotColumn() throws Exception {
        Parser parser = new Parser("f.a = b");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }

    public void testTableDotColumnRightHandSide() throws Exception {
        Parser parser = new Parser("f = g.b");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }

    public void testTableDot() throws Exception {
        try {
            new Parser("f. = b").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected identifier but got '='", e.getMessage());
        }
    }

    public void testBadTokenAfterIs() throws Exception {
        try {
            new Parser("a IS BORIng").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected NULL but got BORIng", e.getMessage());
        }
    }

    public void testBadTokenAfterNot() throws Exception {
        try {
            new Parser("a NOT INTERESTING").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected IN but got INTERESTING", e.getMessage());
        }
    }

    public void testBadTokenAfterIsNot() throws Exception {
        try {
            new Parser("a IS NOT SENSIBLE").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected NULL but got SENSIBLE", e.getMessage());
        }
    }

    public void testMissingOperator() throws Exception {
        try {
            new Parser("f 5").parseWhere();
            fail();
        } catch (ParserException e) {
            /* This would be a nice message, as would 
               "expected boolean but got f" or some such. */
//            assertEquals("expected boolean operator but got 5", e.getMessage());
            assertEquals(
                "expected boolean expression but got non-boolean expression", 
                e.getMessage());
        }
    }
    
    public void testAnd() throws Exception {
        Parser parser = new Parser("a = 5 and b = c");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testLiteralNumber() throws Exception {
        Parser parser = new Parser("f = 5");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testCondition() throws Exception {
        Parser parser = new Parser("(y + z / 10) < 60");
        Condition condition = parser.parseCondition().asBoolean();
        assertEquals("", parser.remainingTokens());
        
        Greater greater = (Greater) condition;
            IntegerLiteral sixty = (IntegerLiteral) greater.leftSide;
                assertEquals(60, sixty.value);
    
            Plus plus = (Plus) greater.rightSide;
                MayflyAssert.assertColumn("y", plus.left());
                
                Divide divide = (Divide) plus.right();
                    MayflyAssert.assertColumn("z", divide.left());
                    
                    MayflyAssert.assertInteger(10, divide.right());
    }

    public void testExpectedBooleanGotNonBoolean() throws Exception {
        lookForConditionGetExpression("5 + x");
    }

    private void lookForConditionGetExpression(String sql) {
        try {
            new Parser(sql).parseCondition().asBoolean();
            fail();
        } catch (ParserException e) {
            // Would be really nice to say what is going on with a more specific error message
//            assertEquals("expected boolean expression but got 5 + x", e.getMessage());
            assertEquals(
                "expected boolean expression but got non-boolean expression", 
                e.getMessage());
        }
    }
    
    public void testExpectedNonBooleanGotBoolean() throws Exception {
        lookForExpressionGetCondition("5 = x");
        lookForExpressionGetCondition(
            "foo.a = bar.a and (bar.a < 5 or foo.a <> bar.a)");
    }

    private void lookForExpressionGetCondition(String sql) {
        try {
            new Parser(sql).parseCondition().asNonBoolean();
            fail();
        } catch (ParserException e) {
            assertEquals(
                "expected non-boolean expression but got boolean expression", 
                e.getMessage());
//          assertEquals("expected non-boolean expression but got 5 = x", e.getMessage());
        }
    }
    
    public void testLiteralString() throws Exception {
        Parser parser = new Parser("'hi'");
        assertEquals(new QuotedString("'hi'"), parser.parsePrimary().asNonBoolean());
        assertEquals("", parser.remainingTokens());
    }

    public void testExpressionLocation() throws Exception {
        checkExpression(NullExpression.class, 3, 7, "  null  ");
        checkExpression(IntegerLiteral.class, 2, 4, " 43  ");
        checkExpression(LongLiteral.class, 2, 12, " 4555666777  ");
        checkExpression(QuotedString.class, 2, 7, " 'foo' ");
        checkExpression(DecimalLiteral.class, 3, 10, "  3.14159 ");
        checkExpression(DecimalLiteral.class, 1, 3, ".5");
        checkExpression(DecimalLiteral.class, 1, 3, "5.");
        checkExpression(DecimalLiteral.class, 2, 7, " + 0.5\n");
        checkExpression(IntegerLiteral.class, 2, 6, " - 43  ");
        checkExpression(LongLiteral.class, 2, 13, " -4555666777  ");
        checkExpression(Plus.class, 2, 12, " -5 + 8 / 2  ");

        /* Should be 2,15.  But that turns out to be hard (how to
           modify the ParserExpression with the right location,
           or some such). 
           */  
        checkExpression(Plus.class, 3, 13, " (-5 + 8 / 2 ) ");

        checkExpression(Maximum.class, 2, 11, " max ( x ) ");
        checkExpression(Minimum.class, 2, 11, " min ( x ) ");
        checkExpression(CountAll.class, 2, 11, " count (*) ");
        checkExpression(Count.class, 2, 13, " count (yyy) ");
        checkExpression(Average.class, 2, 11, " avg ( x ) ");
        checkExpression(Sum.class, 1, 9, "sum( x )");

        checkExpression(SingleColumn.class, 1, 2, "x ");
        checkExpression(SingleColumn.class, 2, 9, " foo . x ");

        checkExpression(NullExpression.class, 2, 6, " null ");
    }
    
    public void testNullExceptionHasLocation() throws Exception {
        Parser parser = new Parser(" null ");
        try {
            parser.parseExpression();
            fail();
        }
        catch (FoundNullLiteral e) {
            MayflyAssert.assertLocation(2, 6, e.location());
        }
    }
    
    public void testRethrownNullExceptionHasLocation() throws Exception {
        Parser parser = new Parser(" 5 + null + 7 ");
        try {
            parser.parseExpressionOrNull();
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "Specify a null literal rather than an expression containing one",
                e.getMessage());
            
            /* This is currently from the start of the expression until the
               first null.  Would be a bit more friendly to parse the whole
               expression and provide the location of the whole expression,
               I guess, but maybe this location is close enough.  */
            MayflyAssert.assertLocation(2, 10, e.location());
        }
    }
    
    public void testBinaryLocation() throws Exception {
        TextToken original = new TextToken(TokenType.PARAMETER, "?", 5, 73, 5, 74);
        List tokens = new ArrayList();
        tokens.add(
            new BinaryToken(
                new ImmutableByteArray(((byte)42)), original.location));
        tokens.add(new EndOfFileToken(6, 1, null));
        Parser parser = new Parser(tokens);

        CellExpression expression = (CellExpression) parser.parseExpressionOrNull();
        assertEquals(5, expression.location.startLineNumber);
        assertEquals(73, expression.location.startColumn);
        assertEquals(5, expression.location.endLineNumber);
        assertEquals(74, expression.location.endColumn);
    }
    
    private void checkExpression(Class expectedClass, 
        int expectedStartColumn, int expectedEndColumn, String input) {

        Parser parser = new Parser(input);
        Expression expression = parser.parseExpressionOrNull();
        ObjectAssert.assertInstanceOf(expectedClass, expression);

        MayflyAssert.assertLocation(
            expectedStartColumn, expectedEndColumn, expression.location);
    }

    public void testValueConstructor() throws Exception {
        Parser parser = new Parser("  values ( 33 , null ) ");
        ValueList values = parser.parseValueConstructor();
        MayflyAssert.assertLocation(3, 23, values.location);
        MayflyAssert.assertLocation(12, 14, values.location(0));
        MayflyAssert.assertLocation(17, 21, values.location(1));
    }

    public void testSyntaxErrorInValueConstructor() throws Exception {
        Parser parser = new Parser("  values ( a 'foo' ) ");
        try {
            parser.parseValueConstructor();
            fail();
        }
        catch (MayflyException e) {
            /* Probably would be nicer if this parsed the syntax frist,
               and then worried about whether there was a column reference. */
            assertEquals("values clause may not refer to column: a", 
                e.getMessage());
        }
    }

    public void testValueConstructorNoSpaces() throws Exception {
        Parser parser = new Parser("values(5,'Value')");
        ValueList values = parser.parseValueConstructor();
        MayflyAssert.assertLocation(1, 18, values.location);
        MayflyAssert.assertLocation(8, 9, values.location(0));
        MayflyAssert.assertLocation(10, 17, values.location(1));
    }

    public void testSingleColumnAsWhat() throws Exception {
        Parser parser = new Parser("select a from foo");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }

    public void testSingleColumnWithTableAsWhat() throws Exception {
        Parser parser = new Parser("foo.a");
        parser.parseWhat();
        assertEquals("", parser.remainingTokens());
    }

    public void testTwoWhatElements() throws Exception {
        Parser parser = new Parser("b, foo.a");
        parser.parseWhat();
        assertEquals("", parser.remainingTokens());
    }

    public void testAllNotLegalWithOthers() throws Exception {
        Parser parser = new Parser("a, *");
        try {
            parser.parseWhat();
            fail();
        } catch (ParserException e) {
            assertEquals("expected expression but got '*'", e.getMessage());
        }
    }

    public void testConcatenate() throws Exception {
        Parser parser = new Parser("a || b");
        parser.parseWhatElement();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testExpressionPrecedence() throws Exception {
        // What about concatenate?  It would seem like it can't be in the
        // same expression, due to differing types...

        Parser parser = new Parser("a * b + c / e - a");
        WhatElement expression = parser.parseExpression().asNonBoolean();
        assertEquals("", parser.remainingTokens());
        
        Minus minus = (Minus) expression;
 
            Plus plus = (Plus) minus.left();
    
                Multiply multiply = (Multiply) plus.left();
                    MayflyAssert.assertColumn("a", multiply.left());
                    MayflyAssert.assertColumn("b", multiply.right());
                Divide divide = (Divide) plus.right();
                    MayflyAssert.assertColumn("c", divide.left());
                    MayflyAssert.assertColumn("e", divide.right());
            
            MayflyAssert.assertColumn("a", minus.right());
    }
    
    public void testDivideAssociativity() throws Exception {
        Parser parser = new Parser("a / b * c / d");
        WhatElement expression = parser.parseExpression().asNonBoolean();
        assertEquals("", parser.remainingTokens());
        
        Divide outerDivide = (Divide) expression;
            Multiply multiply = (Multiply) outerDivide.left();
                Divide innerDivide = (Divide) multiply.left();
                    MayflyAssert.assertColumn("a", innerDivide.left());
                    MayflyAssert.assertColumn("b", innerDivide.right());
                MayflyAssert.assertColumn("c", multiply.right());
    
            MayflyAssert.assertColumn("d", outerDivide.right());
    }

    public void testMinusAssociativity() throws Exception {
        Parser parser = new Parser("a-b-c+d");
        WhatElement expression = parser.parseExpression().asNonBoolean();
        assertEquals("", parser.remainingTokens());
        
        Plus plus = (Plus) expression;
            Minus outerMinus = (Minus) plus.left();
    
                Minus innerMinus = (Minus) outerMinus.left();
                    MayflyAssert.assertColumn("a", innerMinus.left());
                    MayflyAssert.assertColumn("b", innerMinus.right());
    
                MayflyAssert.assertColumn("c", outerMinus.right());
            MayflyAssert.assertColumn("d", plus.right());
    }

    public void testConcatenateAssociativity() throws Exception {
        // Not sure it matters if concatenate associates right-to-left or left-to-right.
        // But we'll pick the same as the other operators...

        Parser parser = new Parser("a || b || c || d");
        WhatElement expression = parser.parseExpression().asNonBoolean();
        assertEquals("", parser.remainingTokens());

        Concatenate d = (Concatenate) expression;
            Concatenate c = (Concatenate) d.left();
                Concatenate b = (Concatenate) c.left();
                    MayflyAssert.assertColumn("a", b.left());
                    MayflyAssert.assertColumn("b", b.right());
                MayflyAssert.assertColumn("c", c.right());
            MayflyAssert.assertColumn("d", d.right());
    }
    
    public void testParenthesesInExpression() throws Exception {
        Parser parser = new Parser("x / (y * z)");
        WhatElement expression = parser.parseExpression().asNonBoolean();
        assertEquals("", parser.remainingTokens());
        
        Divide divide = (Divide) expression;
            MayflyAssert.assertColumn("x", divide.left());
            Multiply multiply = (Multiply) divide.right();
                MayflyAssert.assertColumn("y", multiply.left());
                MayflyAssert.assertColumn("z", multiply.right());
    }

    public void testConsumeInteger() throws Exception {
        assertEquals(23, new Parser("23").consumeInteger());
        assertEquals(2147483647, new Parser("2147483647").consumeInteger());
        try {
            new Parser("2147483648").consumeInteger();
            fail();
        }
        catch (ParserException e) {
            // Might be nice to show the context.  Kind of a bigger problem
            // (how do we show context on "expected foo got bar"?).
            assertEquals("2147483648 is out of range", e.getMessage());
        }
    }
    
    public void testNumericLiteral() throws Exception {
        checkDecimal(5.6, "5.6");
        checkDecimal(222333444555.0, "222333444555.");
        checkDecimal(0.03, ".03");
        
        IntegerLiteral smallishInteger = (IntegerLiteral) 
            new Parser("1000222333").parseNumericLiteral(Location.UNKNOWN);
        assertEquals(1000222333, smallishInteger.value);

        LongLiteral biggishInteger = (LongLiteral) 
            new Parser("9223372036854775807").parseNumericLiteral(Location.UNKNOWN);
        assertEquals(9223372036854775807L, biggishInteger.value);

        try {
            new Parser("9223372036854775808").parseNumericLiteral(Location.UNKNOWN);
            fail();
        }
        catch (UnimplementedException e) {
            assertEquals("don't yet handle BigInteger 9223372036854775808",
                e.getMessage());
        }
    }
    
    public void testLeadingPeriodInDefault() throws Exception {
        DecimalLiteral value = (DecimalLiteral) new Parser(".07").parseDefaultValue("x");
        assertEquals(0.07, value.valueAsCell().asDouble(), 0.000001);
    }

    public void testLeadingPeriodInPrimary() throws Exception {
        DecimalLiteral value = (DecimalLiteral) new Parser(".07").parsePrimary().asNonBoolean();
        assertEquals(0.07, value.valueAsCell().asDouble(), 0.000001);
    }

    private void checkDecimal(double expected, String input) {
        DecimalLiteral decimal = (DecimalLiteral) new Parser(input).parseNumericLiteral(Location.UNKNOWN);
        assertEquals(expected, decimal.value.doubleValue(), 0.0001);
    }
    
    public void testMultipleConstraints() throws Exception {
        // UNIQUE and PRIMARY KEY together don't make much sense.
        // The rule here is that constraints must be after DEFAULT
        // but can be in any order.
        new Parser("x integer unique primary key").parseColumnDefinition(
            new CreateTable("foo"));
        new Parser("x integer primary key unique").parseColumnDefinition(
            new CreateTable("foo"));
    }
    
    public void testConstraint() throws Exception {
        new Parser("unique(x)").parseTableElement(new CreateTable("foo"));

        Parser namedConstraint = new Parser("constraint foo_x_constraint unique(x)");
        namedConstraint.parseTableElement(new CreateTable("foo"));
        assertEquals("", namedConstraint.remainingTokens());

        try {
            new Parser("select integer").parseTableElement(null);
            fail();
        }
        catch (ParserException e) {
            assertEquals("expected column or table constraint but got SELECT",
                e.getMessage());
        }
    }
    
    public void testForeignKeyActions() throws Exception {
        try {
            new Parser("on earthquake run away").parseActions();
            fail();
        }
        catch (ParserException e) {
            assertEquals("expected UPDATE or DELETE but got earthquake",
                e.getMessage());
        }
        
        try {
            new Parser("on delete no action " +
                "on insert think about it").parseActions();
            fail();
        }
        catch (ParserException e) {
            assertEquals("expected UPDATE but got INSERT",
                e.getMessage());
        }
        
        checkActions("on update no action on delete no action", 
            NoAction.class, NoAction.class);
        checkActions("on delete set null on update cascade", 
            SetNull.class, Cascade.class);
        checkActions("on delete set default", 
            SetDefault.class, NoAction.class);
        checkActions("on update set default", 
            NoAction.class, SetDefault.class);
    }

    private void checkActions(String sql, 
        Class expectedOnDelete, Class expectedOnUpdate) {
        Parser parser = new Parser(sql);
        Parser.Actions actions = parser.parseActions();
        assertEquals("", parser.remainingTokens());

        ObjectAssert.assertInstanceOf(expectedOnDelete, actions.onDelete);
        ObjectAssert.assertInstanceOf(expectedOnUpdate, actions.onUpdate);
    }
    
    public void testMultipleCommands() throws Exception {
        List commands = 
            new Parser("select x from foo;;select y from foo").parseCommands();
        assertEquals(2, commands.size());
    }

    public void testMultipleCommands2() throws Exception {
        List commands = 
            new Parser("select x from foo;").parseCommands();
        assertEquals(1, commands.size());
    }
    
    public void testDataType() throws Exception {
        try {
            new Parser("  foobar").parseDataType();
            fail();
        } catch (ParserException e) {
            assertEquals("expected data type but got foobar", e.getMessage());
            assertEquals(3, e.startColumn());
            assertEquals(9, e.endColumn());
        }
    }

    private void expectFailure(String sql, String expectedMessage) {
        try {
            new Parser(sql).parseFromTable();
            fail();
        } catch (ParserException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
    
}
