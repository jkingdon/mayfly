package net.sourceforge.mayfly.acceptance.definition;

import net.sourceforge.mayfly.acceptance.SqlTestCase;

public class RenameColumnTest extends SqlTestCase {
    
    public void testRename() throws Exception {
        execute("create table buildings(hiehgt integer)");
        execute("insert into buildings(hiehgt) values(100)");
        String rename = 
            "alter table buildings change column hiehgt height integer";
        if (dialect.haveModifyColumn()) {
            execute(rename);
            expectQueryFailure("select hiehgt from buildings", "no column hiehgt");
            assertResultSet(new String[] { "100" }, 
                query("select height from buildings"));
        }
        else {
            expectExecuteFailure(rename, 
                "expected alter table action but got change");
        }
    }
    
    /**
     * Here we ape the MySQL (5.0.27, I think) behavior.
     * 
     * It would, I suppose, make more sense to just rename the column
     * both in the foreign key and the table itself.  But if we
     * implement that, might want a flag so that people can detect
     * usages which won't work in MySQL.
     */
    public void testConstraintRefersToOldName() throws Exception {
        if (!dialect.haveModifyColumn()) {
            return;
        }
        execute("create table foo(id integer primary key)");
        execute("create table bar(fu_id integer," +
            "  constraint fu_constraint " +
            "  foreign key(fu_id) references foo(id))");
        String rename = "alter table bar change column fu_id foo_id integer";

        /* I'm not seeing an exception in MySQL 5.0.45 here.
           Haven't investigated in detail, though.
         */
        expectExecuteFailure(
            rename, 
            "cannot rename column fu_id because a constraint refers to it");
        execute("alter table bar drop foreign key fu_constraint");
        execute(rename);
        execute("alter table bar add foreign key(foo_id) references foo(id)");
    }
    
    // TODO: are other constraints (besides foreign keys) a different case?
    // TODO: indexes refer to old name

}
