package net.sourceforge.mayfly.acceptance;

import net.sourceforge.mayfly.EndToEndTests;

import org.apache.commons.io.IOUtils;

import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StringTest extends SqlTestCase {

    public void testStrings() throws Exception {
        execute("create table foo (color varchar(80), size varchar(80))");
        execute("insert into foo (color, size) values ('red', 'medium')");

        {
            ResultSet results = query("select size, color from foo");
            assertTrue(results.next());
            assertEquals("medium", results.getString(1));
            assertEquals("red", results.getString("color"));
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select size, color from foo");
            assertTrue(results.next());
            assertEquals("medium", results.getObject(1));
            assertEquals("red", results.getObject("color"));
            assertFalse(results.next());
            results.close();
        }
    }

    public void testAsciiPunctuation() throws Exception {
        execute("create table foo (value varchar(255))");
        execute("insert into foo (value) values (' !\"#$%&''()*+,-./:;<=>?@[]^_`{|}~')");

        ResultSet results = query("select value from foo where value = ' !\"#$%&''()*+,-./:;<=>?@[]^_`{|}~'");
        assertTrue(results.next());
        assertEquals(" !\"#$%&'()*+,-./:;<=>?@[]^_`{|}~", results.getString(1));
        assertFalse(results.next());
    }
    
    public void testBackSlash() throws Exception {
        execute("create table foo (value varchar(255))");

        String insertSql = "insert into foo (value) values ('\\')";
        String selectSql = "select value from foo where value = '\\'";

        if (dialect.backslashInAStringIsAnEscape()) {
            expectExecuteFailure(insertSql, "unterminated string literal");
            expectExecuteFailure(selectSql, "unterminated string literal");
        }
        else {
            execute(insertSql);
    
            ResultSet results = query(selectSql);
            assertTrue(results.next());
            assertEquals("\\", results.getString(1));
            assertFalse(results.next());
        }
    }
    
    public void testTrailingSpaces() throws Exception {
        execute("create table foo(a varchar(4000))");
        execute("insert into foo(a) values('hi  ')");
        execute("insert into foo(a) values('a')");

        assertResultSet(
            new String[] { " 'hi  ' ", " 'a' " },
            query("select a from foo"));

        String equals = "select a from foo where a = 'hi'";
        if (dialect.trailingSpacesConsultedInComparisons()) {
            assertResultSet(new String[] {}, query(equals));
        }
        else {
            assertResultSet(new String[] { " 'hi  ' " }, query(equals));
        }

        String greater = "select a from foo where a > 'hi'";
        if (dialect.trailingSpacesConsultedInComparisons()) {
            assertResultSet(new String[] { " 'hi  ' " }, query(greater));
        }
        else {
            assertResultSet(new String[] {}, query(greater));
        }

        assertResultSet(
            new String[] { " 'a' " },
            query("select a from foo where a < 'hi'"));
    }
    
    public void testInternalAndLeadingSpaces() throws Exception {
        execute("create table foo(a varchar(255))");
        execute("insert into foo(a) values(' a')");
        execute("insert into foo(a) values('a b')");
        
        assertResultSet(new String[] { "' a'", "'a b'" }, 
            query("select a from foo"));
        assertResultSet(new String[] { "' a'" }, 
            query("select a from foo where a = ' a'"));
        assertResultSet(new String[] { }, 
            query("select a from foo where a = 'a'"));
        assertResultSet(new String[] { }, 
            query("select a from foo where a = '  a'"));
        assertResultSet(new String[] { }, 
            query("select a from foo where a = 'a  b'"));
        assertResultSet(new String[] { "'a b'" },
            query("select a from foo where a = 'a b'"));
    }
    
    /**
     * @internal
     * Also see {@link EndToEndTests#testCharacterStream()}
     * which checks a few more cases.
     */
    public void testCharacterStream() throws Exception {
        execute("create table foo (x varchar(255))");

        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        String data = "value";
        // Derby requires that the correct length be passed in.  That is bogus,
        // because there is no way to get that length when reading from, say, a
        // UTF-8 file, short of reading the whole file.  MySQL, Postgres and Hypersonic
        // do fine with a length of "1000".
        insert.setCharacterStream(1, new StringReader(data), data.length());
        assertEquals(1, insert.executeUpdate());
        insert.close();
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        Reader stream = results.getCharacterStream(1);
        String contents = IOUtils.toString(stream);
        assertEquals("value", contents);
        stream.close(); // Check JDBC documentation: should I close it?

        assertFalse(results.next());
        results.close();
    }
    
}
