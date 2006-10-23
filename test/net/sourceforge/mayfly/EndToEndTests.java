package net.sourceforge.mayfly;

import junitx.framework.ArrayAssert;
import junitx.framework.StringAssert;

import net.sourceforge.mayfly.acceptance.DataTypeTest;
import net.sourceforge.mayfly.acceptance.MayflyDialect;
import net.sourceforge.mayfly.acceptance.SqlTestCase;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tests that could be acceptance tests, but the behavior seems
 * so unimportant that I don't really feel like testing
 * every other database to see what it does.
 * 
 * Also includes some tests which are end-to-end but rely
 * on Mayfly-specific features like {@link MayflySqlException}.
 */
public class EndToEndTests extends SqlTestCase {
    
    public void testThisIsMayfly() throws Exception {
        assertTrue(
            "When testing a non-mayfly database, " +
                "please just run the tests in the acceptance package",
            dialect instanceof MayflyDialect);
    }

    public void testUnaryPlus() throws Exception {
        execute("create table foo (x integer default -5, " +
            "y integer, " +
            "z integer default +44000)");
        execute("insert into foo(y) values(0)");
        assertResultSet(new String[] { " -5, 44000 " }, 
            query("select x, z from foo"));
    }
    
    public void testQueryVersusUpdate() throws Exception {
        /* Might be interesting to see what other databases do
           (for example, I think hypersonic 1.8.x will throw
           exceptions such as these but 1.7.x will be lenient). */
        execute("create table foo (x integer)");
        expectExecuteFailure("select x from foo", 
            "SELECT is only available with query, not update");
        expectQueryFailure("insert into foo(x) values(5)", 
            "expected SELECT but got INSERT");
    }
    
    public void testTableEngine() throws Exception {
        /* MySQL compatibility.  In the future perhaps the engine will
           do something like give an error if you specify myisam
           and try to use features it doesn't support, like
           transactions or foreign keys.  For now the engine is a noop.  */
        execute("create table countries (id integer) engine=innodb");
        execute("create table mixedcase (id integer) engine = InnoDB");
        execute("create table cities (id integer) engine=myisam");
        expectExecuteFailure("create table cities (id integer) engine=DataSinkHole", 
            "unrecognized table type DataSinkHole");
    }
    
    public void testCharacterSet() throws Exception {
        /* Mayfly can store any unicode string.  So it seems
         * best to ignore the character set, I guess.  I suppose
         * if it is being set to ISO8859-15, for example, we
         * could complain about other characters being inserted.
         * But I'm not sure how useful that would be.  */
        execute("create table foo (id integer) engine=InnoDB character set utf8");
        execute("create table bar (id integer) character set utf8");
        execute("create table baz (id integer) character set klingonEncoding1");
    }

    /**
     * @internal
     * In {@link DataTypeTest#testCharacterStream()} we test
     * setting parameters with a length, and results via
     * an index.  So here we test setting parameters without
     * a length (which should work, as described there),
     * and results via a name.
     */
    public void testCharacterStream() throws Exception {
        execute("create table foo (x text)");

        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        // Derby requires that the correct length be passed in.  That is bogus,
        // because there is no way to get that length when reading from, say, a
        // UTF-8 file, short of reading the whole file.
        // MySQL, Postgres and Hypersonic do fine with a length of "1000".
        insert.setCharacterStream(1, new StringReader("value"), 0);
        assertEquals(1, insert.executeUpdate());
        insert.close();
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        Reader stream = results.getCharacterStream("x");
        String contents = IOUtils.toString(stream);
        assertEquals("value", contents);
        stream.close(); // Check JDBC documentation: should I close it?

        assertFalse(results.next());
        results.close();
    }
    
    public void testBinaryStreamWithoutSize() throws Exception {
        execute("create table foo (x blob)");

        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        byte[] data = { (byte)0xff, (byte)0xef, 7 };
        insert.setBinaryStream(1, new ByteArrayInputStream(data), 0);
        assertEquals(1, insert.executeUpdate());
        insert.close();
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        InputStream stream = results.getBinaryStream("x");
        byte[] contents = IOUtils.toByteArray(stream);
        ArrayAssert.assertEquals(data, contents);
        stream.close(); // Check JDBC documentation: should I close it?

        assertFalse(results.next());
        results.close();
    }

    public void testTimestamp() throws Exception {
        execute("create table foo (x timestamp)");
        try {
            execute("insert into foo(x) values('something')");
            fail();
        }
        catch (MayflySqlException expected) {
            assertEquals("data type timestamp is not implemented", 
                expected.getMessage());
        }
    }

    public void testCurentTimestamp() throws Exception {
        execute("create table foo (" +
            "x timestamp default Current_Timestamp, y integer)");
        try {
            execute("insert into foo(y) values(5)");
            assertResultSet(new String[] { "0" }, query("select x from foo"));
            fail();
        }
        catch (MayflySqlException expected) {
            // Either one of these would be OK.
            assertEquals("data type timestamp is not implemented", 
                expected.getMessage());
//            assertEquals("Current_Timestamp is not implemented", 
//                expected.getMessage());
        }
    }
    
    public void testLineNumbersOnForeignKeyViolations() throws Exception {
        /* Line numbers are (at least sometimes) provided by 
         * Oracle in an all_errors table, by SQL Server in the
         * SQLException#getMessage(), and by Sybase and Postgres
         * in a subclass of SQLException.  The SQLException
         * subclass seems logical.
         */
        execute("create table countries (id integer primary key, " +
            "name varchar(255))");
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            ")");

        /* We're just trying to indicate the line number of the statement
        which violated the constraint.  Pointing to the constraint itself
        might be interesting too but that raises many more issues because
        it was in a separate statement, so do we try to indicate a
        file name?  Or the Java stack trace where the constraint was
        created?  Constraint names, of course, are a more conventional
        solution (although we might want solutions for people who
        prefer not to use constraint names).  */
        expectExecuteFailure(
            "  insert into cities(name, country) values ('Dhaka', 3)  ",
            "foreign key violation: countries has no id 3",
            1, 3/*54*/, 1, 56/*55*/);
    }

    /*
       It might be interesting to see what other databases do.
       But for now I'm just wanting to make sure that Mayfly's
       data structures don't end up in an inconsistent state.
     */
    public void testMultipleColumnPrimaryKey() throws Exception {
        execute("create table foo(a integer, b integer, c integer, " +
            "primary key(a, c) )");
        expectExecuteFailure("alter table foo drop column a", 
            "attempt to drop column a from multi-column primary key a,c");
        execute("alter table foo drop column b");
    }
    
    /*
      It might be interesting to see what other databases do.
      But for now I'm just wanting to make sure that Mayfly's
      data structures don't end up in an inconsistent state.
    */
    public void testForeignKey() throws Exception {
        execute("create table currency(id integer, name varchar(255))");
        execute("create table balance(" +
            "amount decimal(10,3), " +
            "currency integer, " +
            "foreign key(currency) references currency(id)" +
            ")");
        expectExecuteFailure("alter table currency drop column id", 
            "the column id is referenced by a foreign key in table balance, column currency");
        expectExecuteFailure("alter table balance drop column currency", 
            "mayfly does not currently allow dropping a column with a foreign key " +
            "(table balance, column currency)");
        execute("alter table currency drop column name");
        execute("alter table balance drop column amount");
    }
    
    public void testTransactionLevel() throws Exception {
        // TODO: What does "NONE" mean?  Check JDBC documentation.
        assertEquals(
            Connection.TRANSACTION_NONE,
            connection.getTransactionIsolation());
    }
    
    public void testExceptionAsRuntime() throws Exception {
        try {
            query("select * from foo");
            fail();
        }
        catch (SQLException checkedException) {
            assertEquals("no table foo", checkedException.getMessage());
            MayflySqlException mayflyException = 
                (MayflySqlException) checkedException;

            RuntimeException unchecked = mayflyException.asRuntimeException();
            assertEquals("no table foo", unchecked.getMessage());
            assertNull(unchecked.getCause());

            StringWriter trace = new StringWriter();
            unchecked.printStackTrace(new PrintWriter(trace));
            String traceString = trace.toString();

            StringAssert.assertContains("testExceptionAsRuntime", traceString);
            StringAssert.assertContains("query", traceString);
        }
    }
    
    public void testFailingCommand() throws Exception {
        try {
            execute("create table drop(x integer)");
            fail();
        }
        catch (MayflySqlException e) {
            assertEquals("expected identifier but got DROP", e.getMessage());
            assertEquals("create table drop(x integer)", e.failingCommand());
        }
    }
    
    public void testCommandInPreparedStatement() throws Exception {
        execute("create table foo(x integer)");
        try {
            connection.prepareStatement(
                "select table from foo where x < ?");
            fail();
        }
        catch (MayflySqlException e) {
            assertEquals("expected primary but got TABLE", e.getMessage());
            assertEquals("select table from foo where x < ?", 
                e.failingCommand());
        }
    }
    
}
