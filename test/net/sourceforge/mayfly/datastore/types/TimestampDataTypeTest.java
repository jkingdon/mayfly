package net.sourceforge.mayfly.datastore.types;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;
import junitx.framework.StringAssert;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TimestampCell;
import net.sourceforge.mayfly.evaluation.command.CreateTable;
import net.sourceforge.mayfly.evaluation.expression.TestTimeSource;
import net.sourceforge.mayfly.parser.Lexer;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampDataTypeTest extends TestCase {
    
    public void testCoerce() throws Exception {
        TimestampCell cell = (TimestampCell) MayflyAssert.coerce(
            new TimestampDataType(), new StringCell("2038-12-01 03:43:07"));
        assertEquals(2038, cell.year());
    }
    
    public void testCallsGenericCoerce() throws Exception {
        Cell coerced = 
            MayflyAssert.coerce(new TimestampDataType(), NullCell.INSTANCE);
        ObjectAssert.assertInstanceOf(NullCell.class, coerced);
    }
    
    public void testCoerceFromTimestamp() throws Exception {
        TimestampCell in = new TimestampCell(2003, 1, 7, 13, 45, 00);
        TimestampCell coerced = (TimestampCell) MayflyAssert.coerce(
            new TimestampDataType(), in);
        assertEquals(13, coerced.hour());
    }

    public void testStringToDate() throws Exception {
        checkFailure("");
        checkFailure("123");
        checkFailure("12345");
        checkFailure("12345-01-01 00:00:00");
        checkFailure("x1234-01-01 00:00:00");
        checkFailure("1234-01-01 00:00:00x");
        check(1234, 1, 1, 0, 0, 0, "1234-01-01 00:00:00");
        checkFailure("1066");
        checkFailure("1066x12-25");
        checkFailure("106612-25");
        check(1066, 12, 25, 0, 0, 0, "1066-12-25");
        checkFailure("1066-12-25T00:00:00"); // Should allow this?
        checkFailure("1066-12-25 0:00:00");
        checkFailure("1066-12-25 00:00:0");
        check(1066, 12, 25, 23, 59, 43, "1066-12-25 23:59:43");
        try {
            new TimestampDataType().stringToDate("1890-02-31 00:00:01");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("Value 31 for dayOfMonth must be in the range [1,28]", 
                e.getMessage());
        }
    }

    public void testQuotingInMessage() throws Exception {
        try {
            new TimestampDataType().stringToDate("don't");
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "'don''t' is not in format yyyy-mm-dd hh:mm:ss", 
                e.getMessage());
        }
    }

    private void check(int expectedYear, int expectedMonth, int expectedDay,
        int expectedHour, int expectedMinute, int expectedSecond,
        String input) {
        TimestampCell stamp = new TimestampDataType().stringToDate(input);
        assertEquals(expectedYear, stamp.year());
        assertEquals(expectedMonth, stamp.month());
        assertEquals(expectedDay, stamp.day());
        assertEquals(expectedHour, stamp.hour());
        assertEquals(expectedMinute, stamp.minute());
        assertEquals(expectedSecond, stamp.second());
    }

    private void checkFailure(String input) {
        try {
            new TimestampDataType().stringToDate(input);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("'" +
                input +
                "' is not in format yyyy-mm-dd hh:mm:ss", e.getMessage());
        }
    }
    
    public void testEvaluationIsDelayed() throws Exception {
        TestTimeSource timeSource = new TestTimeSource();
        String sql = "create table foo(a timestamp default current_timestamp)";
        Parser parser = new Parser(
            new Lexer(sql).tokens(), 
            false, timeSource);
        timeSource.advanceTo(1976, 7);
        CreateTable command = (CreateTable) parser.parse();
        timeSource.advanceTo(1976, 8);

        Database database = new Database();
        database.executeUpdate(command, DataStore.ANONYMOUS_SCHEMA_NAME);
        timeSource.advanceTo(1976, 9);
        database.execute("insert into foo() values()");
        timeSource.advanceTo(1976, 10);
        database.execute("insert into foo() values()");
        timeSource.advanceTo(1976, 11);

        ResultSet results = database.query("select a from foo order by a");
        assertTrue(results.next());
        StringAssert.assertStartsWith("1976-09", getStamp(results));
        assertTrue(results.next());
        StringAssert.assertStartsWith("1976-10", getStamp(results));
        assertFalse(results.next());
    }

    private String getStamp(ResultSet results) throws SQLException {
        Timestamp timestamp = results.getTimestamp(1);
        return new LocalDateTime(timestamp.getTime())
            .toString(TimestampCell.FORMATTER);
    }

}
