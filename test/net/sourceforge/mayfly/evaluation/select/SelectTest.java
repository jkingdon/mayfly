package net.sourceforge.mayfly.evaluation.select;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.condition.And;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.evaluation.condition.Greater;
import net.sourceforge.mayfly.evaluation.condition.Or;
import net.sourceforge.mayfly.evaluation.condition.True;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.from.FromTable;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.MayflyAssert;

public class SelectTest extends TestCase {

    public void testExecuteSimpleJoin() throws Exception {
        L fooColumns = new L().append("colA").append("colB");
        L barColumns = new L().append("colX").append("colY");
        Evaluator evaluator =
            new Evaluator(
                new Schema()
                    .createTable("foo", fooColumns)
                    .addRow("foo", fooColumns, makeValues("1a", "1b"))
                    .addRow("foo", fooColumns, makeValues("2a", "2b"))

                    .createTable("bar", barColumns)
                    .addRow("bar", barColumns, makeValues("1a", "1b"))
                    .addRow("bar", barColumns, makeValues("2a", "2b"))
                    .addRow("bar", barColumns, makeValues("3a", "3b"))
            );

        ResultRows rows = query(evaluator, "select * from foo, bar");
        assertEquals(6, rows.size());
        assertRow("foo", "colA", "1a", "foo", "colB", "1b", "bar", "colX", "1a", "bar", "colY", "1b", rows.row(0));
        assertRow("foo", "colA", "1a", "foo", "colB", "1b", "bar", "colX", "2a", "bar", "colY", "2b", rows.row(1));
        assertRow("foo", "colA", "1a", "foo", "colB", "1b", "bar", "colX", "3a", "bar", "colY", "3b", rows.row(2));
        assertRow("foo", "colA", "2a", "foo", "colB", "2b", "bar", "colX", "1a", "bar", "colY", "1b", rows.row(3));
        assertRow("foo", "colA", "2a", "foo", "colB", "2b", "bar", "colX", "2a", "bar", "colY", "2b", rows.row(4));
        assertRow("foo", "colA", "2a", "foo", "colB", "2b", "bar", "colX", "3a", "bar", "colY", "3b", rows.row(5));
    }

    private void assertRow(String alias1, String column1, String value1, 
        String alias2, String column2, String value2, 
        String alias3, String column3, String value3, 
        String alias4, String column4, String value4, 
        ResultRow row) {
        assertEquals(4, row.size());
        assertRowElement(alias1, column1, value1, row.element(0));
        assertRowElement(alias2, column2, value2, row.element(1));
        assertRowElement(alias3, column3, value3, row.element(2));
        assertRowElement(alias4, column4, value4, row.element(3));
    }

    private void assertRow(String alias1, String column1, String value1, 
        String alias2, String column2, String value2, 
        ResultRow row) {
        assertEquals(2, row.size());
        assertRowElement(alias1, column1, value1, row.element(0));
        assertRowElement(alias2, column2, value2, row.element(1));
    }

    private void assertRowElement(String alias, String column, String value, ResultRow.Element element) {
        assertEquals(alias, element.column().tableOrAlias());
        assertEquals(column, element.column().columnName());
        assertEquals(value, element.value.asString());
    }

    private ResultRows query(Evaluator evaluator, String sql) {
        Select select = (Select) Command.fromSql(sql);
        select.optimize();
        return select.query(evaluator, new Selected());
    }

    public void testSmallerJoin() throws Exception {
        Evaluator evaluator =
            new Evaluator(
                new Schema()
                    .createTable("foo", new L().append("colA"))
                    .addRow("foo", new L().append("colA"), ValueList.singleton(new StringCell("1a")))

                    .createTable("bar", new L().append("colX"))
                    .addRow("bar", new L().append("colX"), ValueList.singleton(new StringCell("barXValue")))
                );


        ResultRows rows = query(evaluator, "select * from foo, bar");
        assertEquals(1, rows.size());
        assertRow("foo", "colA", "1a", "bar", "colX", "barXValue", rows.row(0));
    }

    public void testSimpleWhere() throws Exception {
        L columnNames = new L().append("colA").append("colB");
        Evaluator evaluator =
            new Evaluator(
                new Schema()
                    .createTable("foo", columnNames)
                    .addRow("foo", columnNames, makeValues("1a", "1b"))
                    .addRow("foo", columnNames, makeValues("2a", "xx"))
                    .addRow("foo", columnNames, makeValues("3a", "xx"))
            );

        ResultRows rows = query(evaluator, "select * from foo where colB = 'xx'");
        assertEquals(2, rows.size());
        assertRow("foo", "colA", "2a", "foo", "colB", "xx", rows.row(0));
        assertRow("foo", "colA", "3a", "foo", "colB", "xx", rows.row(1));
    }

    private ValueList makeValues(String firstStringValue, String secondStringValue) {
        return ValueList
            .singleton(new StringCell(firstStringValue))
            .with(new Value(new StringCell(secondStringValue)));
    }
    
    public void testMakeJoinsExplicit() throws Exception {
        Select select = (Select) Select.fromSql("select * from foo, bar");
        select.optimize();
        InnerJoin join = (InnerJoin) select.from().soleElement();

        assertEquals("foo", ((FromTable) join.left).tableName);
        assertEquals("bar", ((FromTable) join.right).tableName);
    }

    public void testLeftAssociative() throws Exception {
        // At least for now, we don't try to pick the optimal order of
        // joins; we just take the listed order in a left-associative way.
        Select select = (Select) Select.fromSql("select * from foo, bar, baz");
        select.optimize();
        InnerJoin join = (InnerJoin) select.from().soleElement();

        InnerJoin firstJoin = (InnerJoin) join.left;
        assertEquals("foo", ((FromTable) firstJoin.left).tableName);
        assertEquals("bar", ((FromTable) firstJoin.right).tableName);

        assertEquals("baz", ((FromTable) join.right).tableName);
    }
    
    public void testTransformWhereToOn() throws Exception {
        Select select = (Select) Select.fromSql(
            "select * from foo, bar, baz where foo.id = bar.id");
        select.optimize(
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("id"))
                .createTable("bar", ImmutableList.singleton("id"))
                .createTable("baz", ImmutableList.singleton("id"))
            ));

        InnerJoin join = (InnerJoin) select.from().soleElement();
        InnerJoin firstJoin = (InnerJoin) join.left;

        Equal on = (Equal) firstJoin.condition;
        MayflyAssert.assertColumn("foo", "id", on.leftSide);
        MayflyAssert.assertColumn("bar", "id", on.rightSide);
        
        ObjectAssert.assertInstanceOf(True.class, select.where);
    }
    
    public void testCanMove() throws Exception {
        assertTrue(Select.canMove(
            new Equal(
                new SingleColumn("foo", "a"), 
                new SingleColumn("b")), 
            new FromTable("foo"), new FromTable("bar"), 
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("a"))
                .createTable("bar", ImmutableList.singleton("b")))
            ));
    }
    
    public void testCannotMoveNoColumn() throws Exception {
        assertFalse(Select.canMove(
            new Equal(
                new SingleColumn("foo", "a"), 
                new SingleColumn("c")), 
            new FromTable("foo"), new FromTable("bar"), 
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("a"))
                .createTable("bar", ImmutableList.singleton("b"))) 
            ));
    }
    
    public void testCannotMoveNoTable() throws Exception {
        assertFalse(Select.canMove(
            new Equal(
                new SingleColumn("foo", "a"), 
                new SingleColumn("baz", "c")), 
            new FromTable("foo"), new FromTable("bar"), 
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("a"))
                .createTable("bar", ImmutableList.singleton("b"))) 
            ));
    }
    
    public void testCheckComplexExpressionUnmovable() throws Exception {
        assertFalse(Select.canMove(
            new Or(
                new Equal(new IntegerLiteral(5), new IntegerLiteral(5)),
                new Equal(
                    new SingleColumn("foo", "a"), 
                    new SingleColumn("baz", "c"))
            ), 
            new FromTable("foo"), new FromTable("bar"), 
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("a"))
                .createTable("bar", ImmutableList.singleton("b"))) 
            ));
    }
    
    public void testCheckComplexExpressionMovable() throws Exception {
        assertTrue(Select.canMove(
            new Or(
                new Equal(new IntegerLiteral(5), new IntegerLiteral(5)),
                new Equal(
                    new SingleColumn("foo", "a"), 
                    new SingleColumn("bar", "b"))
            ), 
            new FromTable("foo"), new FromTable("bar"), 
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("a"))
                .createTable("bar", ImmutableList.singleton("b"))) 
            ));
    }
    
    public void testCannotMoveAggregate() throws Exception {
        // The problem is that we don't yet have fully functional
        // machinery for taking max(a) and knowing that "a" is
        // foo.a and not some other a.
        assertFalse(Select.canMove(
            new Equal(
                new Maximum(new SingleColumn("a"), "max", false),
                new IntegerLiteral(5)), 
            new FromTable("foo"), new FromTable("bar"), 
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("a"))
                .createTable("bar", ImmutableList.singleton("b"))) 
            ));
    }
    
    public void testFullDummyRow() throws Exception {
        Select select = (Select) Select.fromSql(
            "select * from foo, bar, baz");
        ResultRow dummyRow = select.dummyRow(
            0,
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("id"))
                .createTable("bar", ImmutableList.singleton("id"))
                .createTable("baz", ImmutableList.singleton("id"))
            ));
        assertEquals(3, dummyRow.size());
        MayflyAssert.assertColumn("foo", "id", dummyRow.expression(0));
    }
    
    public void testMoveLeftSideOfAnd() throws Exception {
        Select select = (Select) Select.fromSql(
            "select * from foo, bar, baz " +
            "where foo.id = bar.id and (bar.id = 5 or baz.id = 7)");
        select.optimize(
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("id"))
                .createTable("bar", ImmutableList.singleton("id"))
                .createTable("baz", ImmutableList.singleton("id"))
            ));

        InnerJoin join = (InnerJoin) select.from().soleElement();
        InnerJoin firstJoin = (InnerJoin) join.left;

        Equal on = (Equal) firstJoin.condition;
        MayflyAssert.assertColumn("foo", "id", on.leftSide);
        MayflyAssert.assertColumn("bar", "id", on.rightSide);

        Or movedToLastJoin = (Or) join.condition;
        Equal barEquals5 = (Equal) movedToLastJoin.leftSide;
        MayflyAssert.assertColumn("bar", "id", barEquals5.leftSide);
        Equal bazEquals7 = (Equal) movedToLastJoin.rightSide;
        MayflyAssert.assertColumn("baz", "id", bazEquals7.leftSide);
        
        ObjectAssert.assertInstanceOf(True.class, select.where);
    }
    
    public void testMoveRightSideOfAnd() throws Exception {
        Select select = (Select) Select.fromSql(
            "select * from foo, bar, baz " +
            "where foo.id = baz.id and (bar.id = 5 or foo.id = 7)");
        select.optimize(
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("id"))
                .createTable("bar", ImmutableList.singleton("id"))
                .createTable("baz", ImmutableList.singleton("id"))
            ));

        InnerJoin join = (InnerJoin) select.from().soleElement();
        InnerJoin firstJoin = (InnerJoin) join.left;

        Or on = (Or) firstJoin.condition;
        MayflyAssert.assertColumn("bar", "id", ((Equal)on.leftSide).leftSide);
        MayflyAssert.assertColumn("foo", "id", ((Equal)on.rightSide).leftSide);

        Equal movedToLastJoin = (Equal) join.condition;
        MayflyAssert.assertColumn("foo", "id", movedToLastJoin.leftSide);
        MayflyAssert.assertColumn("baz", "id", movedToLastJoin.rightSide);
        
        ObjectAssert.assertInstanceOf(True.class, select.where);
    }
    
    public void testMoveAlmostEverything() throws Exception {
        Select select = (Select) Select.fromSql(
            "select * from foo, bar, baz " +
            "where foo.id = bar.id and bar.id > 5 and baz.id = 9 and foo.id > 7");
        select.optimize(
            new Evaluator(new Schema()
                .createTable("foo", ImmutableList.singleton("id"))
                .createTable("bar", ImmutableList.singleton("id"))
                .createTable("baz", ImmutableList.singleton("id"))
            ));

        InnerJoin join = (InnerJoin) select.from().soleElement();
        InnerJoin firstJoin = (InnerJoin) join.left;

        And on = (And) firstJoin.condition;
        MayflyAssert.assertColumn("foo", "id", ((Greater)on.leftSide).leftSide);
        And secondLevel = (And) on.rightSide;
        MayflyAssert.assertColumn("foo", "id", ((Equal)secondLevel.leftSide).leftSide);
        MayflyAssert.assertColumn("bar", "id", ((Greater)secondLevel.rightSide).leftSide);

        Equal movedToLastJoin = (Equal) join.condition;
        MayflyAssert.assertColumn("baz", "id", movedToLastJoin.leftSide);
        
        ObjectAssert.assertInstanceOf(True.class, select.where);
    }
    
}
