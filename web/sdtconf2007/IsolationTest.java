import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.datastore.DataStore;

import org.junit.Before;
import org.junit.Test;



public class IsolationTest {
    
    // immutable, shared
    private static DataStore common = makeStore();

    private static DataStore makeStore() {
        Database commonDatabase = new Database();
        commonDatabase.execute("create table foo(x integer, name varchar(255))");
        commonDatabase.execute("insert into foo(x, name) values(5, 'master row')");
        return commonDatabase.dataStore();
    }

    // mutable, set up for each test
    Database database;

    @Before public void setUp() {
        database = new Database(common);
    }
    
    @Test public void readMasterData() throws Exception {
        ResultSet results = database.query("select name from foo where x = 5");
        assertTrue(results.next());
        assertEquals("master row", results.getString("name"));
        assertFalse(results.next());
    }
    
    @Test public void addRows() throws Exception {
        database.execute("insert into foo(x, name) values(77, 'test-specific data')");
        assertEquals(2, database.rowCount("foo"));
    }
    
    @Test public void isUnpolluted() throws Exception {
        assertEquals(1, database.rowCount("foo"));
    }

}
