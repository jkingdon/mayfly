package net.sourceforge.mayfly.acceptance;

import net.sourceforge.mayfly.acceptance.expression.ValueTest;


/**
 * @internal
 * See {@link ValueTest} for insert cases not involving subselects
 */
public class InsertSubselectTest extends SqlTestCase {
    
    public void testSubselect() throws Exception {
        execute("create table src(d integer, e integer, f integer)");
        execute("insert into src(d, e, f) values(1, 2, 3)");
        execute("insert into src(d, e, f) values(11, 22, 33)");
        execute("create table dest(a integer, b integer, c integer)");

        execute("insert into dest(a, b) select f, e from src");
        
        assertResultSet(new String[] { " 33, 22, null ", " 3, 2, null " }, 
            query("select a, b, c from dest"));
    }
    
    public void testTooManyValues() throws Exception {
        execute("create table src(d integer, e integer, f integer)");
        execute("create table dest(a integer, b integer, c integer)");

        expectExecuteFailure(
            "insert into dest(a, b) select d, e, f from src",

            "Too many values.\n" +
            "Columns and values from subselect were:\n" +
            "a d\n" +
            "b e\n" +
            "(none) f\n");
    }

    public void testTooFewValues() throws Exception {
        execute("create table src(d integer, e integer, f integer)");
        execute("create table dest(a integer, b integer, c integer)");

        expectExecuteFailure(
            "insert into dest(a, b) select d from src",
            
            "Too few values.\n" +
            "Columns and values from subselect were:\n" +
            "a d\n" +
            "b (none)\n");
    }

    public void testImplicitDestinationColumns() throws Exception {
        execute("create table src(d integer, e integer, f integer)");
        execute("insert into src(d, e, f) values(1, 2, 3)");
        execute("create table dest(a integer, b integer, c integer)");

        execute("insert into dest select d, e, f from src");
        assertResultSet(new String[] { " 1, 2, 3 " }, 
            query("select a, b, c from dest"));
    }
    
    public void testStillCheckCountWhenDestinationColumnsAreImplicit() throws Exception {
        execute("create table src(d integer, e integer, f integer)");
        execute("create table dest(a integer, b integer, c integer)");

        expectExecuteFailure("insert into dest select d, e from src",
            "Too few values.\n" +
            "Columns and values from subselect were:\n" +
            "a d\n" +
            "b e\n" +
            "c (none)\n");
    }

    public void testImplicitSourceColumns() throws Exception {
        execute("create table src(d integer, e integer, f integer)");
        execute("insert into src(d, e, f) values(1, 2, 3)");
        execute("create table dest(a integer, b integer, c integer)");

        execute("insert into dest(a, b, c) select * from src");
        assertResultSet(new String[] { " 1, 2, 3 " }, 
            query("select a, b, c from dest"));
    }
    
    public void testExpression() throws Exception {
        execute("create table src(d integer, e integer)");
        execute("insert into src(d, e) values(23, 37)");
        execute("insert into src(d, e) values(7, 47)");
        execute("create table dest(a integer)");

        execute("insert into dest(a) select d + e from src");
        
        assertResultSet(new String[] { " 60 ", " 54 " }, 
            query("select a from dest"));
    }

}
