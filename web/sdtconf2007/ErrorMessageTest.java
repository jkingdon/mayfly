import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sourceforge.mayfly.Database;

import org.junit.Test;



public class ErrorMessageTest {

    @Test public void duplicatePrimaryKeyValue() {
        Database database = new Database();
        database.execute("create table foo(id integer primary key)");
        database.execute("insert into foo(id) values(5)");
        try {
            database.execute("insert into foo(id) values(5)");
            fail();
        }
        catch (Exception e) {
            // Hypersonic: Violation of unique constraint $$: duplicate value(s) for column(s) $$: SYS_PK_47
            // MySQL 5.0: Duplicate entry '5' for key 1
            // Postgres: ERROR: duplicate key violates unique constraint "foo_pkey"
            // Derby: The statement was aborted because it would have caused a 
            //  duplicate key value in a unique or primary key constraint 
            //  or unique index identified by 'SQL071201111516360' defined on 'FOO'.

            assertEquals("primary key id already has a value 5", e.getMessage());
            
            // Vote: should "id" (column name) be in this message?
            // Vote: should "foo" (table name) be in this message?
            // Vote: should "5" (duplicated value) be in this message?
        }
    }

    @Test public void duplicateColumn() {
        Database database = new Database();
        try {
            database.execute("create table foo(id integer, x integer, id integer)");
            fail();
        }
        catch (Exception e) {
            // Tells us which column (hard to see by inspection if there are dozens).
            assertEquals("duplicate column id", e.getMessage());
        }
    }
    
    @Test public void mismatch() {
        Database database = new Database();
        database.execute("create table foo(x integer)");
        database.execute("insert into foo(x) values(5, 6)");
    }

}
