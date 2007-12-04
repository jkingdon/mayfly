import static org.junit.Assert.assertEquals;
import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.dump.SqlDumper;

import org.junit.Test;

public class DumperTest {

    @Test public void upgrade() {
        Database upgrader = new Database();
        upgrader.execute("create table foo(x integer)");
        upgrader.execute("alter table foo add column name varchar(255)");
        
        Database freshInstall = new Database();
        freshInstall.execute("create table foo(x integer, name varchar(255))");
        
        assertEquals(
            new SqlDumper().dump(freshInstall.dataStore()),
            new SqlDumper().dump(upgrader.dataStore()));

        // Now demonstrate a failure
//        freshInstall.execute("create table other(y integer)");
//        assertEquals(
//            new SqlDumper().dump(freshInstall.dataStore()),
//            new SqlDumper().dump(upgrader.dataStore()));
   }

}
