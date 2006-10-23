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

}
