package net.sourceforge.mayfly.acceptance;

public class TableOptionTest extends SqlTestCase {

    public void testTableEngine() throws Exception {
        /* MySQL compatibility.  In the future perhaps the engine will
           do something like give an error if you specify myisam
           and try to use features it doesn't support, like
           transactions or foreign keys.  For now the engine is a noop.  */
        String sql = "create table countries (id integer) engine=innodb";
        if (dialect.haveEngine()) {
            execute(sql);
            execute("create table mixedcase (id integer) engine = InnoDB");
            execute("create table cities (id integer) engine=myisam");
            expectExecuteFailure("create table cities (id integer) engine=DataSinkHole",
                "unrecognized table type DataSinkHole");
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got engine");
        }
    }

    public void testAlterEngine() throws Exception {
        execute("create table foo(x integer)");
        String sql = "alter table foo engine=innodb";
        if (dialect.haveEngine()) {
            execute(sql);
            dialect.checkDump(
                "CREATE TABLE foo(\n" +
                "  x INTEGER\n" +
                ");\n\n");
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got engine");
        }
    }

    public void testCharacterSet() throws Exception {
        /* Mayfly can store any unicode string.  So it seems
         * best to ignore the character set, I guess.  I suppose
         * if it is being set to ISO8859-15, for example, we
         * could complain about other characters being inserted.
         * But I'm not sure how useful that would be.  */
        String sql = "create table foo (id integer) engine=InnoDB character set utf8";
        if (dialect.haveEngine()) {
            execute(sql);
            execute("create table bar (id integer) character set utf8");
            String bogusCharacterSet = "create table baz (id integer) character set klingonEncoding1";
            if (dialect.expectMayflyBehavior()) {
                execute(bogusCharacterSet);
            }
            else {
                expectExecuteFailure(bogusCharacterSet, "invalid character set klingonEncoding1");
            }
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got engine");
        }
    }

    public void testAlterCharacterSet() throws Exception {
        execute("create table foo(x integer)");
        String sql = "alter table foo character set utf8";
        if (dialect.haveEngine()) {
            execute(sql);
            dialect.checkDump(
                "CREATE TABLE foo(\n" +
                "  x INTEGER\n" +
                ");\n\n");
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got engine");
        }
    }

    public void testCollation() throws Exception {
        /* Collation is a real-life feature which we would like to support.
           I'm a bit reluctant to just ignore the collation (and mis-collate strings).
           I guess the status quo mis-collates (it uses Java string operations, which
           I guess is binary unicode order although I don't know about surrogate pairs).
           So perhaps ignoring a specified collation would be no worse, I don't know. */
        // TODO: why is this failing on MySQL?  Is the syntax right?
        expectExecuteFailure("create table foo(x integer) character set utf8 collation utf8_unicode_ci",
            "expected end of file but got collation");
    }
    
    public void testAlterSchema() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        
        execute(dialect.createEmptySchemaCommand("mars"));
        String sql = "alter schema mars character set utf8";
        if (dialect.haveEngine()) {
            execute(sql);
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got character");
        }
    }

}
