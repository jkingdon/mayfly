package net.sourceforge.mayfly.acceptance;

public class AddForeignKeyTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table foo(id integer primary key)" +
            dialect.tableTypeForForeignKeys());
        execute("create table bar(foo_id integer)" +
            dialect.tableTypeForForeignKeys());
        execute("insert into bar values(5)");
        execute("insert into bar values(6)");
        execute("insert into foo values(5)");

        String add = "alter table bar add foreign key (foo_id) references foo(id)";
        expectExecuteFailure(add, "foreign key violation: foo has no id 6");
        
        execute("delete from bar where foo_id = 6");
        execute(add);
        
        expectExecuteFailure("insert into bar values(6)", 
            "foreign key violation: foo has no id 6");
    }
    
    public void testConstraintNames() throws Exception {
        execute("create table foo(id integer primary key)" +
            dialect.tableTypeForForeignKeys());
        execute("create table bar(first_foo integer, second_foo integer)" +
            dialect.tableTypeForForeignKeys());

        execute("alter table bar add constraint bar_foo " +
            "foreign key (first_foo) references foo(id)");
        expectExecuteFailure("alter table bar add constraint bar_foo " +
            "foreign key (second_foo) references foo(id)",
            "duplicate constraint name bar_foo");
        execute("alter table bar add constraint bar_second_foo " +
            "foreign key (second_foo) references foo(id)");
    }
    
}
