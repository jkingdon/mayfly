package net.sourceforge.mayfly.acceptance;

public class AddUniqueTest extends SqlTestCase {

    public void testBasics() throws Exception {
        execute("create table foo(id integer" +
            (dialect.uniqueColumnMayBeNullable() ? "" : " not null") +
            ", x integer)");
        execute("insert into foo values(3, 10)");
        execute("insert into foo values(5, 10)");
        execute("insert into foo values(5, 20)");
        if (dialect.uniqueColumnMayBeNullable()) {
            execute("insert into foo values(null, 40)");
        }

        String add = "alter table foo add unique(id)";
        /* Perhaps "unique column id has duplicate value 5" would be
           nicer wording in that we aren't adding a 5 and conflicting
           with one there; there are two there.  But is this really
           confusing or only inaccurate in a nit-picky way?
        */
        expectExecuteFailure(add, "unique column id already has a value 5");
        
        execute("delete from foo where x = 20");
        execute(add);
        
        expectExecuteFailure("insert into foo values(5, 30)", 
            "unique column id already has a value 5");

        if (dialect.uniqueColumnMayBeNullable()) {
            String secondNull = "insert into foo values(null, 50)";
            if (dialect.allowMultipleNullsInUniqueColumn()) {
                execute(secondNull);
            }
            else {
                expectExecuteFailure(secondNull, 
                    "unique column id has duplicate value null");
            }
        }
    }
    
    public void testConstraintNames() throws Exception {
        execute("create table foo(id integer not null, x integer not null)");

        execute("alter table foo add constraint foo_unique unique(id)");
        expectExecuteFailure(
            "alter table foo add constraint foo_unique unique(x)",
            "duplicate constraint name foo_unique");
        execute("alter table foo add constraint foo_x unique(x)");
    }

}
