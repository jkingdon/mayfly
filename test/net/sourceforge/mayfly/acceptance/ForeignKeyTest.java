package net.sourceforge.mayfly.acceptance;

public class ForeignKeyTest extends SqlTestCase {
    
    public void testInsertAndDelete() throws Exception {
        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
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

        expectExecuteFailure("delete from countries",
            "foreign key violation: table cities refers to id 1 in countries");
        execute("delete from cities");
        execute("delete from countries");
    }
    
    public void testNull() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            "on delete cascade)" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into cities values ('Bombay', null)");
        assertResultSet(new String[] { " 'Bombay', null " }, 
            query("select * from cities"));
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

        /* Most databases mention "cities" in this message.  But is it always clear
           from the sql?  If so, putting it in the message is clutter.  */
        expectExecuteFailure("update cities set country = 4", 
            "foreign key violation: countries has no id 4");
        assertResultSet(new String[] { " 'Berlin', 'West Germany' " },
            query("select cities.name, countries.name from cities " +
                "inner join countries on cities.country = countries.id")
        );
        
        expectExecuteFailure(
            "update countries set id = 3 where name = 'West Germany'", 
            "foreign key violation: " +
            "table cities refers to id 2 in countries");
        execute("update countries set id = 4 where name = 'East Germany'");
        execute(
            "update countries set id = 2 where name = 'West Germany'");
    }
    
    public void testDropTable() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());
        
        expectExecuteFailure("drop table countries",
            "cannot drop countries because " +
            "a foreign key in table cities refers to it");
        execute("drop table cities");
        execute("drop table countries");
    }
    
    public void testDropTableWithSchemas() throws Exception {
        if (dialect.schemasMissing() ||
            !dialect.foreignKeyCanReferToAnotherSchema()) {
            return;
        }
        
        createEmptySchema("landoj");
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        createEmptySchema("urboj");
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references landoj.countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());
        
        execute("set schema landoj");
        expectExecuteFailure("drop table countries",
            "cannot drop countries because " +
            "a foreign key in table urboj.cities refers to it");
        execute("set schema urboj");
        String sql = "drop table landoj.countries";
        if (dialect.wishThisWereTrue()) {
            expectExecuteFailure(sql,
                "cannot drop countries because " +
                "a foreign key in table cities refers to it");
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got '.'");
        }
        execute("drop table cities");
        execute("set schema landoj");
        execute("drop table countries");
    }
    
    public void testReferenceNonexistentTable() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        expectExecuteFailure(
            "create table cities (name varchar(255), country integer, " +
            "foreign key (country) references bad_table(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys(),
            "no table bad_table");
    }

    public void testNoAction() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            "on delete no action on update no action)" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into countries values (1, 'India')");
        execute("insert into cities values ('Bombay', 1)");
        expectExecuteFailure("delete from countries", 
            "foreign key violation: table cities refers to id 1 in countries");
        expectExecuteFailure(
            "update countries set id = 999 where id = 1",
            "foreign key violation: table cities refers to id 1 in countries");

        assertResultSet(
            new String[] { " 1, 'India' " }, 
            query("select * from countries"));
        assertResultSet(new String[] { " 'Bombay', 1 " }, 
            query("select * from cities"));
    }

    public void testOnDeleteCascade() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            "on delete cascade)" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into countries values (1, 'India')");
        execute("insert into cities values ('Bombay', 1)");
        execute("delete from countries");
        assertResultSet(new String[] { }, query("select * from cities"));
    }

    public void testOnDeleteSetNull() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), " +
            "country integer default 1, " +
            "foreign key (country) references countries(id)" +
            "on delete set null)" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into countries values (1, 'The World')");
        execute("insert into countries values (2, 'India')");
        execute("insert into cities values ('Bombay', 2)");

        /* Although the row in the cities table is changed, 2 out of
         * 2 databases surveyed say that it doesn't count as
         * an affected row in the rowsAffected return value.
         * So we just have the affected row in countries. */
        assertEquals(1, execute("delete from countries where id = 2"));

        assertResultSet(new String[] { " 'Bombay', null " }, 
            query("select * from cities"));
    }

    public void testOnDeleteSetDefault() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());

        String createCities = "create table cities (name varchar(255), " +
            "country integer default 1, " +
            "foreign key (country) references countries(id)" +
            "on delete set default)" +
            dialect.databaseTypeForForeignKeys();
        if (dialect.onDeleteSetDefaultMissing(true)) {
            expectExecuteFailure(createCities, "errno 150");
            return;
        }
        execute(createCities);

        execute("insert into countries values (1, 'The World')");
        execute("insert into countries values (2, 'India')");
        execute("insert into cities values ('Bombay', 2)");
        String sql = "delete from countries where id = 2";
        if (dialect.onDeleteSetDefaultMissing(false)) {
            expectExecuteFailure(sql, "foreign key violation");
        }
        else {
            execute(sql);
            assertResultSet(new String[] { " 'Bombay', 1 " }, 
                query("select * from cities"));
        }
    }

    public void testOnUpdateNoAction() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)" +
            "on update no action on delete cascade)" +
            dialect.databaseTypeForForeignKeys());
        execute("insert into countries values (1, 'USSR')");
        execute("insert into countries values (2, 'Russia')");
        execute("insert into cities values ('Moscow', 1)");
        expectExecuteFailure(
            "update countries set id = 999 where id = 1",
            "foreign key violation: table cities refers to id 1 in countries");
        assertResultSet(
            new String[] { " 1, 'USSR' ", " 2, 'Russia' " }, 
            query("select * from countries"));
        assertResultSet(new String[] { " 'Moscow', 1 " }, 
            query("select * from cities"));
    }

    public void testOnUpdateCascade() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        String createCities = "create table cities (name varchar(255), country integer, " +
                    "foreign key (country) references countries(id)" +
                    "on update cascade)" +
                    dialect.databaseTypeForForeignKeys();
        if (dialect.onUpdateSetNullAndCascadeMissing()) {
            expectExecuteFailure(createCities, 
                "ON UPDATE CASCADE not implemented");
        }
        else {
            execute(createCities);
            execute("insert into countries values (1, 'USSR')");
            execute("insert into countries values (2, 'Russia')");
            execute("insert into cities values ('Moscow', 1)");
    
            execute("update countries set id = 999 where id = 1");
    
            assertResultSet(
                new String[] { " 999, 'USSR' ", " 2, 'Russia' " }, 
                query("select * from countries"));
            assertResultSet(new String[] { " 'Moscow', 999 " }, 
                query("select * from cities"));
            
            // The action shouldn't affect the following two cases:
            execute("update cities set country = 2");
            expectExecuteFailure("update cities set country = 5",
                "foreign key violation: countries has no id 5");
        }
    }

    public void testOnUpdateSetNull() throws Exception {
        execute(
            "create table countries " +
            "(id integer primary key, name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        String createCities = "create table cities (name varchar(255), country integer, " +
                    "foreign key (country) references countries(id)" +
                    "on update set null)" +
                    dialect.databaseTypeForForeignKeys();
        if (dialect.onUpdateSetNullAndCascadeMissing()) {
            expectExecuteFailure(createCities, "ON UPDATE SET NULL not implemented");
        }
        else {
            execute(createCities);
            execute("insert into countries values (1, 'USSR')");
            execute("insert into countries values (2, 'Russia')");
            execute("insert into cities values ('Moscow', 1)");
            execute("update countries set id = 999 where id = 1");
            assertResultSet(
                new String[] { " 999, 'USSR' ", " 2, 'Russia' " }, 
                query("select * from countries"));
            assertResultSet(new String[] { " 'Moscow', null " }, 
                query("select * from cities"));
        }
    }

    // ON UPDATE SET DEFAULT
    // two level cascade: X REFERS TO Y ON DELETE NO ACTION
    //   and Y REFERS TO Z ON DELETE CASCADE
    //   Now delete a row in Z - should fail because X still refers to it.
    
    public void testSelfReference() throws Exception {
        execute("create table person(id integer primary key," +
            "mother integer," +
            "foreign key(mother) references person(id)" +
            ")"
            + dialect.databaseTypeForForeignKeys()
        );
        execute("insert into person (id) values(1)");
        execute("insert into person (id, mother) values(2, 1)");
        expectExecuteFailure("insert into person (id, mother) values(3, 7)",
            "foreign key violation: person has no id 7");
        
        execute("insert into person(id, mother) values(10, 10)");
        /* Are there extra self-reference cases?  Does the
           delete check in update always work? */
        // update person set id = 11, mother = 11 where id = 10
    }
    
    public void xtestCircularReference() throws Exception {
        // Neither Hypersonic nor Derby allow a foreign key
        // to reference a table which isn't yet created.
        // Do you need to add
        // the constraints later with ALTER TABLE?  Is there
        // a syntax for forward-declaring a table?  Is the
        // whole idea of circular foreign keys silly?
        execute("create table team(id integer primary key," +
            "captain integer," +
            "foreign key(captain) references player(id)" +
            ")"
        );
        execute("create table player(id integer primary key," +
            "team integer," +
            "foreign key(team) references team(id)" +
        ")");
        expectExecuteFailure("insert into team(id, captain) values(1, 10)",
            "foreign key violation: table player has no id 10"
        );
        // One has to go through convolutions to just insert,
        // unless one has enforce-constraints-on-commit.
        execute("insert into team(id) values(1)");
        execute("insert into player(id, team) values(10, 1)");
        execute("update team set captain = 10 where id = 1");
    }
    
    // multiple referencing columns
    // multiple referencing columns where one is NULL (but others are not)
    // "references foo" (omitting the '(' column... ')')
    // reference to something other than a primary key (an error, right?)

}
