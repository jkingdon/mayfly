package net.sourceforge.mayfly.acceptance;

public class ForeignKeyTest extends SqlTestCase {
    
    public void testInsertAndDelete() throws Exception {
        execute("create table countries (id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into countries values (1, 'Australia')");
        execute("insert into cities values ('Perth', 1)");

        expectExecuteFailure("insert into cities values ('Dhaka', 3)",
            "foreign key violation: countries has no id 3");
        expectExecuteFailure("insert into cities(name, country) values ('Dhaka', 3)", 
            "foreign key violation: countries has no id 3");

        String sql = "delete from countries";
        if (dialect.wishThisWereTrue()) {
            expectExecuteFailure(sql,
                "foreign key violation: table cities refers to id 1 in countries");
            execute("delete from cities");
            execute("delete from countries");
        }
        else {
            execute(sql);
        }
    }
    
    public void testWithSchemas() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        
        createEmptySchema("jupiter");
        execute("create table foo (id integer primary key)");
        execute("insert into foo(id) values(1899)");
        execute("create table bar (name varchar(255), foo_id integer," +
            "foreign key (foo_id) references foo(id)" +
            ")");

        createEmptySchema("saturn");
        execute("create table foo (id integer primary key)");
        execute("insert into foo(id) values(569)");
        expectExecuteFailure("insert into jupiter.bar(name, foo_id) values ('x', 569)",
            "foreign key violation: foo has no id 569");
        execute("insert into jupiter.bar(name, foo_id) values ('x', 1899)");
        
        String createBaz = "create table baz (name varchar(255), foo_id integer," +
            "foreign key (foo_id) references jupiter.foo(id)" +
            ")";
        if (dialect.foreignKeyCanReferToAnotherSchema()) {
            execute(createBaz);
            expectExecuteFailure("insert into baz(name, foo_id) values ('x', 569)",
                "foreign key violation: jupiter.foo has no id 569");
            execute("insert into baz(name, foo_id) values ('x', 1899)");
        }
        else {
            expectExecuteFailure(createBaz, "I don't like schema jupiter");
        }
    }

    public void testUpdate() throws Exception {
        execute("create table countries (id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into countries values (1, 'East Germany')");
        execute("insert into countries values (2, 'West Germany')");
        execute("insert into cities values ('Berlin', 1)");

        execute("update cities set country = 2");

        String sql = "update cities set country = 4";
        if (dialect.wishThisWereTrue()) {
            /* Most databases mention "cities" in this message.  But is it always clear
               from the sql?  If so, putting it in the message is clutter.  */
            expectExecuteFailure(sql, "foreign key violation: countries has no id 4");
            assertResultSet(new String[] { " 'Berlin', 'West Germany' " },
                query("select cities.name, countries.name from cities " +
                    "inner join countries on cities.country = countries.id")
            );
            
            expectExecuteFailure("update countries set id = 3 where name = 'West Germany'", 
                "foreign key violation: the cities table refers to id 2 in countries");
            execute("update countries set id = 4 where name = 'East Germany'");
        }
        else {
            execute(sql);
        }
    }
    
    // dropping tables
    // ON DELETE, ON UPDATE
    // NO ACTION, CASCADE, SET NULL, SET DEFAULT
    // multiple referencing columns
    // "references foo" (omitting the '(' column... ')')

}
