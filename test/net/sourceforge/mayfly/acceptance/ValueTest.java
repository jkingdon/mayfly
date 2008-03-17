package net.sourceforge.mayfly.acceptance;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @internal
 * See {@link InsertSubselectTest} for insert cases involving subselects
 */
public class ValueTest extends SqlTestCase {

    public void testNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (null)");
        assertResultSet(new String[] { }, query("select a from foo where a = 5"));

        {
            ResultSet results = query("select a from foo");
            assertTrue(results.next());
    
            assertEquals(0, results.getInt(1));
            assertTrue(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

        checkWrongWayToLookForNull();

        {
            // Right way to look for null.
            ResultSet results = query("select a from foo where a is null");
            assertTrue("got null row", results.next());
    
            assertEquals(0, results.getInt(1));
            assertTrue(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select a from foo where a is not null");
            assertFalse(results.next());
            results.close();
        }

    }

    public void testNonNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (5)");
        
        {
            ResultSet results = query("select a from foo");
            assertTrue(results.next());

            assertEquals(5, results.getInt(1));
            assertFalse(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

        checkWrongWayToLookForNull();

        {
            ResultSet results = query("select a from foo where a is null");
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select a from foo where a is not null");
            assertTrue(results.next());

            assertEquals(5, results.getInt(1));
            assertFalse(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

    }
    
    public void testWasNullGetsClearedForNextColumn() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (null, 5)");
        ResultSet results = query("select a, b from foo");
        assertTrue(results.next());

        assertEquals(0, results.getInt("a"));
        assertTrue(results.wasNull());

        assertEquals(5, results.getInt("b"));
        assertFalse(results.wasNull());
        
        assertFalse(results.next());
    }

    public void testWasNullGetsClearedForNextRow() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (null)");
        execute("insert into foo (a) values (7)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());

        assertEquals(0, results.getInt("a"));
        assertTrue(results.wasNull());

        assertTrue(results.next());

        assertEquals(7, results.getInt("a"));
        assertFalse(results.wasNull());

        assertFalse(results.next());
    }

    private void checkWrongWayToLookForNull() throws SQLException {
        String wrongWayToLookForNull = "select a from foo where a = null";
        if (dialect.disallowNullsInExpressions()) {
            expectQueryFailure(wrongWayToLookForNull, 
                "To check for null, use IS NULL or IS NOT NULL, not a null literal"
            );
        } else {
            // Hypersonic behavior.  I think SQL specifies that "a = null"
            // evaluates to null, which then means false, but is this
            // really useful or just a trap?  Until proven otherwise,
            // I'm going with "trap".
            ResultSet results = query(wrongWayToLookForNull);
            assertFalse(results.next());
            results.close();
        }
    }
    
    public void testAssertResultSetAndNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (null)");
        
        assertResultSet(
            new String[] {
                " 5 ",
                " null "
            },
            query("select a from foo")
        );
    }

    public void testEmptyStringAsNull() throws Exception {
        /* Oracle treats empty string as null (although the documentation
           claims this may change in some future version of Oracle).
           Here we test for the standard behavior - '' and null are different. */
        execute("create table foo (a varchar(255), b varchar(255))");
        execute("insert into foo(a, b) values ('', 'empty string')");
        execute("insert into foo(a, b) values (null, 'a null')");
        assertResultSet(new String[] { " 'a null' "}, query("select b from foo where a is null"));
    }
    
    public void testExpressionInInsert() throws Exception {
        execute("create table foo (a varchar(255))");

        if (dialect.verticalBarsMeanConcatenation()) {
            execute("insert into foo(a) values ('cat' || 'e' || 'gory')");
        }
        else {
            execute("insert into foo(a) values (concat('cat', 'e', 'gory'))");
        }

        assertResultSet(
            new String[] { " 'category' " },
            query("select a from foo")
        );
    }
    
    public void testNullInInsertExpression() throws Exception {
        execute("create table foo (a integer)");
        String insertNullExpression = "insert into foo(a) values (5 + null)";
        if (dialect.disallowNullsInExpressions()) {
            expectExecuteFailure(insertNullExpression, 
                "Specify a null literal rather than an expression containing one");
            assertResultSet(new String[] { }, query("select a from foo"));
        }
        else {
            execute(insertNullExpression);
            assertResultSet(new String[] { " null " }, query("select a from foo"));
        }
    }
    
    public void testNullInUpdateExpression() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values(10)");
        String nullExpression = "update foo set a = 5 + null";
        if (dialect.disallowNullsInExpressions()) {
            expectExecuteFailure(nullExpression, 
                "Specify a null literal rather than an expression containing one");
            assertResultSet(new String[] { " 10 " }, query("select a from foo"));
        }
        else {
            execute(nullExpression);
            assertResultSet(new String[] { " null " }, query("select a from foo"));
        }
    }
    
    public void testInsertSomeColumns() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (b) values (5)");
        assertResultSet(
            new String[] { " null, 5 " },
            query("select a, b from foo")
        );
    }
    
    public void testInsertAllColumns() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo values (5, 7)");
        assertResultSet(
            new String[] { " 5, 7 " },
            query("select a, b from foo")
        );
    }
    
    public void testInsertBadColumnName() throws Exception {
        execute("create table foo (a integer)");
        // Do we want to know about all of them, or just the first?
        // Just the first might be better in terms of avoiding
        // information overload.
        expectExecuteFailure("insert into foo (b, c) values (5, 7)", "no column b");
    }
    
    public void testInsertIntoNonexistentTable() throws Exception {
        expectExecuteFailure("INSERT INTO FOO (b) values (5)", "no table FOO");
    }
    
    public void testInsertSetSyntax() throws Exception {
        execute("create table t(x integer, y integer)");
        String sql = "insert into t set x=123, y=456";
        if (dialect.haveInsertSetSyntax()) {
            execute(sql);
            assertResultSet(
                new String[] { " 123, 456 " },
                query("select x, y from t")
            );
        }
        else {
            expectExecuteFailure(sql, "expected VALUES but got SET");
        }
    }
    
    public void testReferenceToColumn() throws Exception {
        execute("create table foo (a integer, b integer)");
        String referToValueWeInsert = "insert into foo(a, b) values (5, a + 3)";
        String referToUnsetColumn = "insert into foo(a) values (foo.b)";
        if (dialect.valuesClauseCanReferToColumn()) {
            execute(referToValueWeInsert);
            execute(referToUnsetColumn);
            assertResultSet(new String[] { "5, 8", "null, null" }, 
                query("select a,b from foo"));
        }
        else {
            expectExecuteFailure(referToValueWeInsert,
                "values clause may not refer to column: a",
                1, 34, 1, 35);
            expectExecuteFailure(referToUnsetColumn, 
                "values clause may not refer to column: foo.b"); 
        }
    }
    
    public void testTooManyValues() throws Exception {
        execute("create table foo (a integer)");
        // Given a long list of column names, and a long list of values,
        // it might not be obvious which value/name is missing/surplus.
        // So printing out the table of both is an attempt to make this
        // easy to figure out.
        expectExecuteFailure("insert into foo (a) values (5, 7)",
            "Too many values.\n" +
            "Columns and values were:\n" +
            "a 5\n" +
            "(none) 7\n",
            1, 21, 1, 34
        );
    }
    
    public void testTooFewValues() throws Exception {
        execute("create table foo (a integer, b integer)");
        // Given a long list of column names, and a long list of values,
        // it might not be obvious which value/name is missing/surplus.
        // So printing out the table of both is an attempt to make this
        // easy to figure out.
        expectExecuteFailure("insert into foo (a, b) values (5)",
            "Too few values.\n" +
            "Columns and values were:\n" +
            "a 5\n" +
            "b (none)\n",
            1, 24, 1, 34
        );
    }
    
    public void testInsertAllColumnsChecksForNumberOfValues() throws Exception {
        execute("create table foo (a integer, b integer)");
        String insertSql = "insert into foo values (5)";
        if (dialect.numberOfValuesMustMatchNumberOfColumns()) {
            expectExecuteFailure(insertSql,
                "Too few values.\n" +
                "Columns and values were:\n" +
                "a 5\n" +
                "b (none)\n"
            );
        }
        else {
            execute(insertSql);
            assertResultList(new String[] { " 5, null " }, query("select * from foo"));
        }
    }
    
    public void testDuplicateColumnName() throws Exception {
        execute("create table foo (Id integer)");
        expectExecuteFailure("insert into foo (Id, Id) values (5, 7)", 
            "duplicate column Id");
    }
    
    public void testNegativeNumber() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (-5)");
        execute("insert into foo (x) values (-3)");
        execute("insert into foo (x) values (+7)");
        
        assertResultSet(new String[] { " -5 " }, query("select x from foo where x < -4"));
    }
    
    public void testReadInteger() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("x"));
        assertEquals("5", results.getString("x"));
        assertEquals(5.0, results.getDouble("x"), 0.00001);
        assertFalse(results.next());
    }
    
    /**
     * @internal
     * In allowing duplicate rows, SQL does not follow the relational model.
     * But we are probably stuck with allowing the duplicates, I suspect.
     */
    public void testIdenticalRows() throws Exception {
        execute("create table foo(x integer, y varchar(255))");
        execute("insert into foo(x, y) values(5, 'dup')");
        execute("insert into foo(x, y) values(5, 'dup')");
        assertResultList(new String[] { " 5, 'dup' ", " 5, 'dup' " }, 
            query("select x,y from foo"));
    }
    
}
