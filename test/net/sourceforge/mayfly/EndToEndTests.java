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
import java.sql.Statement;

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
        // I guess this should be saying "insert" instead of "this command".
        expectQueryFailure("insert into foo(x) values(5)", 
            "This command is only available with update, not query");
    }
    
    /**
     * @internal
     * In {@link net.sourceforge.mayfly.acceptance.StringTest#testCharacterStream()} we test
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
            "attempt to drop column a from multi-column " +
            "primary key in table foo, columns a,c");
        execute("alter table foo drop column b");
    }
    
    /*
      It might be interesting to see what other databases do.
      But for now we just worry about ourselves.
    */
    public void testDropColumnWithForeignKey() throws Exception {
        execute("create table currency(" +
            "id integer primary key, name varchar(255))");
        execute("create table balance(" +
            "amount decimal(10,3), " +
            "currency integer, " +
            "foreign key(currency) references currency(id)" +
            ")");
        String dropId = "alter table currency drop column id";
        expectExecuteFailure(dropId, 
            "the column id is referenced by a foreign key in table balance, column currency");
        execute("alter table balance drop column currency");
        execute(dropId);
        ((MayflyDialect)dialect).checkDump(
            "CREATE TABLE balance(\n" +
            "  amount DECIMAL(10,3)\n" +
            ");\n\n" +
            "CREATE TABLE currency(\n" +
            "  name VARCHAR(255)\n" +
            ");\n\n");
    }

    public void testDropColumnFromTableWithForeignKey() throws Exception {
        execute("create table currency(" +
            "id integer primary key)");
        execute("create table balance(" +
            "amount decimal(10,3), " +
            "currency integer, " +
            "foreign key(currency) references currency(id)" +
            ")");
        execute("alter table balance drop column amount");
        ((MayflyDialect)dialect).checkDump(
            "CREATE TABLE currency(\n" +
            "  id INTEGER,\n" +
            "  PRIMARY KEY(id)\n" +
            ");\n\n" +
            "CREATE TABLE balance(\n" +
            "  currency INTEGER,\n" +
            "  CONSTRAINT balance_ibfk_1 FOREIGN KEY(currency) REFERENCES currency(id)\n" +
            ");\n\n");
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
            assertEquals("expected expression but got TABLE", e.getMessage());
            assertEquals("select table from foo where x < ?", 
                e.failingCommand());
        }
    }
    
    public void testForeignKeyBadColumn() throws Exception {
        execute("create table foo(x integer primary key)");
        expectExecuteFailure(
            "create table bar(y integer, foreign key(z) references foo(x))",
            "no column z",
            1, 29, 1, 61);
    }
    
    public void testBinaryAsPrimaryKey() throws Exception {
        /* At the moment I'm most interested in line numbers
           on certain UnimplementedException.  I don't know
           whether those methods will eventually be incapable
           of throwing exceptions, or whether the exceptions
           will just morph to "can't compare this data type"
           or some such.  */
        execute("create table foo(x " + dialect.binaryTypeName() + 
            " primary key)");
        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        insert.setBytes(1, new byte[] { 0x1, 0x3, (byte)0xff, (byte)0x90 });
        assertEquals(1, insert.executeUpdate());
        insert.setBytes(1, new byte[] { 0x1 });
        try {
            assertEquals(1, insert.executeUpdate());
            fail();
        }
        catch (MayflySqlException e) {
            dialect.assertMessage(
                "This feature is not yet implemented in Mayfly", 
                e,
                1, 20, 1, 29);
        }
        insert.close();
    }
    
    /**
       Test we can have a big binary data type, where big > 4Gibyte.
       While we're at it, same for varchar (and who else? CLOB?)
     */
    public void testBigSizes() throws Exception {
        execute("create table foo(a blob(8111222333))");
        execute("create table bar(a varchar(8111222333))");
    }
    
    public void testIdentityDoesNotAcceptStrings() throws Exception {
        execute("create table foo(a identity)");
        expectExecuteFailure("insert into foo(a) values('bad')",
            "attempt to store string 'bad' into integer column a");
    }
    
    public void testAutoIncrementStartsWith() throws Exception {
        /* 
           Different databases have different ways of specifying the
           value that an auto-increment starts with, if it can be
           done at all.  For now, Mayfly does it with DEFAULT.
         */
        execute("create table foo(a integer default 6 auto_increment)");
        execute("insert into foo() values()");
        assertResultSet(new String[] { "6" }, query("select a from foo"));
    }
    
    public void testGetMaxRows() throws Exception {
        int noLimit = 0;

        Statement statement = connection.createStatement();
        assertEquals(noLimit, statement.getMaxRows());
        PreparedStatement prepared = 
            connection.prepareStatement("create table foo(x integer)");
        assertEquals(noLimit, prepared.getMaxRows());
    }
    
    public void testQueryTimeout() throws Exception {
        int noLimit = 0;

        Statement statement = connection.createStatement();
        assertEquals(noLimit, statement.getQueryTimeout());
        PreparedStatement prepared = 
            connection.prepareStatement("create table foo(x integer)");
        assertEquals(noLimit, prepared.getQueryTimeout());
    }
    
    public void testBadTimestamp() throws Exception {
        execute("create table foo(x timestamp)");
        expectExecuteFailure(
            "insert into foo(x) values('0000-00-00 00:00:00')", 
            "Value 0 for monthOfYear must be in the range [1,12]",
            1, 27, 1, 48);
        expectExecuteFailure(
            "insert into foo(x) values('2004-17-01 01:01:01')", 
            "Value 17 for monthOfYear must be in the range [1,12]",
            1, 27, 1, 48);
    }

    public void testAfterErrorHandling() throws Exception {
        execute("create table foo(a integer, c integer)");
        execute("insert into foo values(1, 100)");
        expectExecuteFailure(
            "alter table foo add column b integer after qqq",
            "no column qqq", 
            1, 44, 1, 47);
    }
    
    public void testAddAutoIncrementNoRows() throws Exception {
        execute("create table foo(a integer)");
        execute("alter table foo modify column " +
            "a integer auto_increment not null");
        execute("insert into foo() values()");
        assertResultSet(new String[] { "1" }, query("select a from foo"));
    }

}
