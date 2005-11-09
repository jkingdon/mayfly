package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.util.*;

public class CreateTableTest extends TestCase {
    
    public void testParse() throws Exception {
        //assertEquals("", Tree.parse("create table foo (a integer, b varchar(40))").toString());

        CreateTable create = CreateTable.createTableFromTree(Tree.parse("create table foo (a integer, b varchar(40))"));
        assertNotNull(create);
        assertEquals("foo", create.table());
        assertEquals(new L().append("a").append("b"), create.columnNames());
    }

}
