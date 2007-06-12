package net.sourceforge.mayfly.acceptance;

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
    
    // TODO: constraints refer to old name
    // TODO: indexes refer to old name

}
