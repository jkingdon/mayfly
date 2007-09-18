import static org.junit.Assert.assertEquals;
import net.sourceforge.mayfly.Database;

import org.junit.Test;



public class FooTest {
    
    @Test public void database() {
        Database database = new Database();
        database.execute("create table foo(x integer, name varchar(255))");
        database.execute("insert into foo(x, name) values(5, 'five')");
        FooPersistence persistence = 
            new FooPersistence(database.openConnection());
        Foo fetched = persistence.getFoo(5);
        assertEquals("five", fetched.name);
    }

}
