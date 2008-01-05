package net.sourceforge.mayfly.acceptance;

import java.sql.SQLException;



public class DropForeignKeyTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
            dialect.tableTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "constraint city_country " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.tableTypeForForeignKeys());

        String countrylessCity = "insert into cities values ('Monaco', 99)";
        expectExecuteFailure(countrylessCity,
            "foreign key violation: countries has no id 99");
        
        String dropForeignKey = 
            "alter table cities drop foreign key city_country";
        if (dialect.haveDropForeignKey()) {
            execute(dropForeignKey);
            execute(countrylessCity);
        }
        else {
            expectExecuteFailure(dropForeignKey, 
                "expected COLUMN but got FOREIGN");
        }
    }
    
    public void testDropConstraintCommand() throws Exception {
        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
            dialect.tableTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "constraint city_country " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.tableTypeForForeignKeys());

        String countrylessCity = "insert into cities values ('Monaco', 99)";
        expectExecuteFailure(countrylessCity,
            "foreign key violation: countries has no id 99");
        
        String dropConstraint = 
            "alter table cities drop constraint city_country";
        if (dialect.haveDropConstraint()) {
            execute(dropConstraint);
            execute(countrylessCity);
        }
        else {
            expectExecuteFailure(dropConstraint, 
                "expected alter table drop action but got CONSTRAINT");
        }
    }
    
    // alter table cities drop constraint city_country restrict
    // (or CASCADE).
    
    public void testConfusionAboutUniqueAndForeign() throws Exception {
        if (!dialect.haveDropForeignKey()) {
            return;
        }

        execute("create table foo(" +
            "id integer not null, constraint uniq_id unique(id))" +
            dialect.tableTypeForForeignKeys());
        expectExecuteFailure("alter table foo drop foreign key uniq_id",
            "constraint uniq_id is not a foreign key");
    }
    
    public void testErrorCases() throws Exception {
        if (!dialect.haveDropForeignKey()) {
            return;
        }

        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
            dialect.tableTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "constraint city_country " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.tableTypeForForeignKeys());

        expectExecuteFailure("alter table cities drop foreign key citycountry", 
            "no constraint citycountry");
        expectExecuteFailure(
            "alter table countries drop foreign key city_country",
            /* Maybe the message should say "on table countries"?
               Seems like the kind of thing which would be noise,
               except sometimes, when you would really want it.
             */
            "no constraint city_country");
        if (!dialect.constraintNamesMightBeCaseSensitive()) {
            execute("alter table cities drop foreign key CITY_country");
        }
    }
    
    public void testDropSomeKeepSome() throws Exception {
        if (!dialect.haveDropForeignKey()) {
            return;
        }

        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
            dialect.tableTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)," +
            "colonial_power integer," +
            "constraint colonial_constraint " +
            "foreign key (colonial_power) references countries(id)" +
            ")" +
            dialect.tableTypeForForeignKeys());
        execute("insert into countries values(1, 'Portugal')");
        execute("insert into countries values(2, 'India')");
        execute("insert into cities values('Goa', 2, 1)");
        String insertCityWithoutColonialPower = 
            "insert into cities values('Delhi', 2, 99)";
        expectExecuteFailure(insertCityWithoutColonialPower, 
            "foreign key violation: countries has no id 99");
        String insertCityWithoutCountry = 
            "insert into cities values('Monaco', 7, 1)";
        expectExecuteFailure(insertCityWithoutCountry, 
            "foreign key violation: countries has no id 7");
        
        execute("alter table cities drop foreign key colonial_constraint");
        execute(insertCityWithoutColonialPower);
        expectExecuteFailure(insertCityWithoutCountry, 
            "foreign key violation: countries has no id 7");
    }
    
    public void testDropUnnamedForeignKey() throws Exception {
        /*
         * A cleaner way, perhaps, would be if there were a syntax to
         * drop the key based on which column it is from/to or something
         * like that.  But existing practice (such as it is) seems to be
         * that the database assigns a constraint name (kind of a problem
         * for scripting this stuff, but at least mysql assigns the name
         * in a predictable way, which doesn't depend on the contents of
         * other tables, the time, database internal state, etc).
         */
        if (!dialect.haveDropForeignKey()) {
            return;
        }

        execute("create table refd(id integer primary key)" + 
            dialect.tableTypeForForeignKeys());
        execute("create table refr(a integer, b integer, c integer," +
            "foreign key(a) references refd(id)," +
            "foreign key(b) references refd(id)" +
            ")" + 
            dialect.tableTypeForForeignKeys());

        String dropForeignKeyB = "alter table refr drop foreign key refr_ibfk_2";
        if (dialect.nameForeignKeysWithIbfk()) {
            execute(dropForeignKeyB);

            checkWeDeletedBConstraintNotA();
        }
        else {
            /* Derby names them like "SQL071109113329810" which seems
             * to be based on the time (2007-11-09T11:33:29.10 local time zone, 
             * or some such).
             */
            /* postgres and hypersonic probably name them some other way,
             * but they don't have drop foreign key, so we don't worry about it.
             */
            expectExecuteFailure(dropForeignKeyB, "no foreign key refr_ibfk_2");
        }

    }
    
    public void testForeignKeyNamesAndOrder() throws Exception {
        if (!dialect.haveDropForeignKey() || 
            !dialect.nameForeignKeysWithIbfk()) {
            return;
        }

        execute("create table refd(id integer primary key)" + 
            dialect.tableTypeForForeignKeys());
        execute("create table refr(a integer, b integer, c integer," +
            "foreign key(b) references refd(id)," +
            "foreign key(a) references refd(id)" +
            ")" + 
            dialect.tableTypeForForeignKeys());

        execute("alter table refr drop foreign key refr_ibfk_1");

        checkWeDeletedBConstraintNotA();
    }

    public void testForeignKeyNamesWhereSomeKeysExplicitlyNamed() throws Exception {
        if (!dialect.haveDropForeignKey() || 
            !dialect.nameForeignKeysWithIbfk()) {
            return;
        }

        execute("create table refd(id integer primary key)" + 
            dialect.tableTypeForForeignKeys());
        execute("create table refr(a integer, b integer, c integer," +
            "constraint a_constraint foreign key(a) references refd(id)," +
            "foreign key(b) references refd(id)" +
            ")" + 
            dialect.tableTypeForForeignKeys());

        execute("alter table refr drop foreign key refr_ibfk_1");

        checkWeDeletedBConstraintNotA();
    }

    private void checkWeDeletedBConstraintNotA() throws SQLException {
        execute("insert into refd(id) values(5)");
        execute("insert into refr(a,b,c) values(5,7,9)");
        expectExecuteFailure("insert into refr(a,b,c) values(7,5,9)",
            dialect.wishThisWereTrue() ?
                /* The wording here is awkward, but it is useful to know
                   which column, right? */
                "foreign key violation: column a references non-present " +
                    "value 7 in table refd, column id" :
                "foreign key violation: refd has no id 7");
    }
    
}
