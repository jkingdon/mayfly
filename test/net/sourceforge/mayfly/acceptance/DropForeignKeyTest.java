package net.sourceforge.mayfly.acceptance;


public class DropForeignKeyTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "constraint city_country " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());

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
    
    public void testErrorCases() throws Exception {
        if (!dialect.haveDropForeignKey()) {
            return;
        }

        execute("create table countries (id integer primary key, " +
            "name varchar(255))" +
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "constraint city_country " +
            "foreign key (country) references countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());

        expectExecuteFailure("alter table cities drop foreign key citycountry", 
            "no foreign key citycountry");
        expectExecuteFailure(
            "alter table countries drop foreign key city_country",
            /* Maybe the message should say "on table countries"?
               Seems like the kind of thing which would be noise,
               except sometimes, when you would really want it.
             */
            "no foreign key city_country");
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
            dialect.databaseTypeForForeignKeys());
        execute("create table cities (name varchar(255), country integer, " +
            "foreign key (country) references countries(id)," +
            "colonial_power integer," +
            "constraint colonial_constraint " +
            "foreign key (colonial_power) references countries(id)" +
            ")" +
            dialect.databaseTypeForForeignKeys());
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
    
}
