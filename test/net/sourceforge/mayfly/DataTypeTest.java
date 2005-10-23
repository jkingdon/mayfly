package net.sourceforge.mayfly;


public class DataTypeTest extends SqlTestCase {

    public void testTypes() throws Exception {
        execute("create table foo (a integer)");
    }

}
