package net.sourceforge.mayfly.acceptance.definition;

import net.sourceforge.mayfly.acceptance.SqlTestCase;

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
        expectExecuteFailure(add, 
            dialect.wishThisWereTrue() ?
                "attempt to add foreign key from table bar, column foo_id " +
                "where existing value 6 references non-present value " +
                "in table foo, column id, " :

                "foreign key violation: attempt in table bar, column foo_id " +
                "to reference non-present value 6 in table foo, column id");
        
        execute("delete from bar where foo_id = 6");
        execute(add);
        
        expectExecuteFailure("insert into bar values(6)", 
            "foreign key violation: attempt in table bar, column foo_id " +
            "to reference non-present value 6 in table foo, column id");
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
    
    public void testRemoveCreatesGap() throws Exception {
        if (!dialect.haveDropForeignKey() || !dialect.nameForeignKeysWithIbfk()) {
            return;
        }

        execute("create table foo(id integer primary key)" +
            dialect.tableTypeForForeignKeys());
        execute("create table bar(f1 integer, f2 integer, f3 integer)" +
            dialect.tableTypeForForeignKeys());
        
        execute("alter table bar add foreign key(f1) references foo(id)");
        execute("alter table bar add foreign key(f2) references foo(id)");
        execute("alter table bar drop foreign key bar_ibfk_1");
        execute("alter table bar add foreign key(f3) references foo(id)");
        execute("alter table bar drop foreign key bar_ibfk_2");
        execute("alter table bar drop foreign key bar_ibfk_3");
        
        // verify all foreign keys are gone
        execute("insert into bar(f1, f2, f3) values(4, 5, 6)");
    }
    
}
