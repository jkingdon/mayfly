package net.sourceforge.mayfly.acceptance;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultTest extends SqlTestCase {
    
    public void testImplicitInsert() throws Exception {
        execute("create table foo (x integer default 111222333, y integer)");
        execute("insert into foo(y) values(0)");
        assertResultSet(new String[] { "111222333" }, query("select x from foo"));
    }
    
    public void testImplicitAndNotNull() throws Exception {
        execute(
            "create table foo (" +
                "id integer, " +
                "x integer not null, " +
                "description varchar(255) not null" +
            ")");
        String sql = "insert into foo(id) values(1)";
        if (dialect.notNullImpliesDefaults()) {
            execute(sql);
            assertResultSet(new String[] { " 1, 0, '' " }, 
                query("select id, x, description from foo"));
        }
        else {
            expectExecuteFailure(sql, "column x cannot be null");
        }
    }

    public void testExplicitInsert() throws Exception {
        execute("create table foo (x integer default 111222333)");
        String sql = "insert into foo(x) values(default)";
        if (dialect.canUpdateToDefault()) {
            execute(sql);
            assertResultSet(new String[] { "111222333" }, 
                query("select x from foo"));
        }
        else {
            expectExecuteFailure(sql, "no column default");
        }
    }

    public void testNull() throws Exception {
        execute("create table foo (x integer default null, y integer)");
        execute("insert into foo(y) values(0)");
        assertResultSet(new String[] { " null, 0 " }, query("select x, y from foo"));
    }

    public void testSignedLiterals() throws Exception {
        /** Unary plus is so obscure, and so little used, that it is tested
           in {@link net.sourceforge.mayfly.EndToEndTests} instead of here.
           (It also is not supported by all databases).
         */

        execute("create table foo (x integer default -5, y integer)");
        execute("insert into foo(y) values(0)");
        assertResultSet(new String[] { " -5 " }, query("select x from foo"));
    }

    public void testJdbcParameter() throws Exception {
        // Interestingly enough, the SQL92 grammar doesn't seem to
        // allow this syntax.
        
        String sql = "create table foo (x integer default ?, y integer)";
        if (dialect.allowJdbcParameterAsDefault()) {
            PreparedStatement prepared = connection.prepareStatement(sql);
            prepared.setInt(1, 70);
            assertEquals(0, prepared.executeUpdate());
            prepared.close();

            execute("insert into foo(y) values(0)");
            assertResultSet(new String[] { " 70 " }, query("select x from foo"));
        }
        else {
            try {
                // Different databases vary about whether the exception
                // happens in the prepareStatement or the setInt.
                PreparedStatement prepared = connection.prepareStatement(sql);
                prepared.setInt(1, 70);
                prepared.close();
            }
            catch (SQLException e) {
                assertMessage("expected default value for column x but got '?'", e);
            }
        }
    }

    public void testUpdate() throws Exception {
        execute("create table foo (x smallint default 31000)");
        execute("insert into foo(x) values(0)");
        assertResultSet(new String[] { "0" }, query("select x from foo"));
        String sql = "update foo set x = default";
        if (dialect.canUpdateToDefault()) {
            execute(sql);
            assertResultSet(new String[] { "31000" }, query("select x from foo"));
        }
        else {
            expectExecuteFailure(sql, "no column default");
        }
    }
    
    public void testExpression() throws Exception {
        String sql = "create table foo (x integer default 2 + 2, y integer)";
        if (dialect.defaultValueCanBeExpression()) {
            execute(sql);
            execute("insert into foo(y) values(0)");
            assertResultSet(new String[] { " 4 " }, query("select x from foo"));
        }
        else {
            expectExecuteFailure(sql, "expected ')' but got '+'");
        }
    }
    
    public void testExpressionReferencesValues() throws Exception {
        // Postgres gives a nice error that we can't reference columns
        // That seems sane I guess - what about circular references and
        // other pathological cases?
        expectExecuteFailure("create table foo (x integer, y integer default x + 1)", 
            "expected default value for column y but got x");
    }
    
    public void testCombineWithConstraint() throws Exception {
        // Mostly a syntax test, but while we're at it, test semantics
        execute("create table foo (x varchar(80) default 'zippo' not null, y integer)");
        execute("insert into foo(y) values(0)");
        expectExecuteFailure("insert into foo(x) values(null)", "column x cannot be null");
        assertResultSet(new String[] { " 'zippo' " }, query("select x from foo"));
    }

}
