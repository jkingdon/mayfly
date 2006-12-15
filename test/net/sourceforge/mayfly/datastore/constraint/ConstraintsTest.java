package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.select.StoreEvaluator;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;
import java.util.List;

public class ConstraintsTest extends TestCase {
    
    public void testDropColumn() throws Exception {
        Constraints constraints = new Constraints(
            ImmutableList.fromArray(new Constraint[] {
                new PrimaryKey(makeColumns("id")),
                new UniqueConstraint(makeColumns("id"))
            })
        );
        assertEquals(2, constraints.constraintCount());
        
        Constraints newConstraints = constraints.dropColumn(null, "id");
        assertEquals(0, newConstraints.constraintCount());
    }

    private Columns makeColumns(String name) {
        return new Columns(ImmutableList.singleton(new Column(name)));
    }
    
    public void testWrongType() throws Exception {
        Constraints constraints = new Constraints(
            ImmutableList.singleton(
                new PrimaryKey(makeColumns("id"), "my_constraint")));
        try {
            constraints.dropForeignKey("my_constraint");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("constraint my_constraint is not a foreign key", 
                e.getMessage());
        }
    }
    
    public void testMultiplePrimaryKeys() throws Exception {
        try {
            new Constraints(
                ImmutableList.fromArray(new Constraint[] {
                    new PrimaryKey(Columns.singleton(new Column("a"))),
                    new PrimaryKey(Columns.singleton(new Column("b")))
                })
            );
        }
        catch (MayflyInternalException e) {
            assertEquals("attempt to define 2 primary keys", e.getMessage());
        }
    }
    
    public void testRefersTo() throws Exception {
        Database database = new Database();
        database.execute("create table aa(a integer primary key)");
        database.execute("create table bb(a_id integer," +
            "foreign key(a_id) references aa(a))");
        DataStore dataStore = database.dataStore();
        Evaluator evaluator = new StoreEvaluator(
            dataStore, DataStore.ANONYMOUS_SCHEMA_NAME);

        assertTrue(
            dataStore.table("bb").constraints.refersTo("aa", evaluator));
        Constraints aaConstraints = dataStore.table("aa").constraints;
        assertFalse(aaConstraints.refersTo("bb", evaluator));
        try {
            aaConstraints.refersTo("nonexist", evaluator);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no table nonexist", e.getMessage());
        }
    }
    
    public void testReferencedTables() throws Exception {
        Database database = new Database();
        database.execute("create table aa(a integer primary key)");
        database.execute("create table bb(b integer primary key," +
            "a_id integer," +
            "foreign key(a_id) references aa(a))");
        database.execute("create table cc(a_id integer," +
            "foreign key(a_id) references aa(a)," +
            "b_id integer," +
            "foreign key(b_id) references bb(b))");
        DataStore dataStore = database.dataStore();
        Evaluator evaluator = new StoreEvaluator(
            dataStore, DataStore.ANONYMOUS_SCHEMA_NAME);

        Constraints aaConstraints = dataStore.table("aa").constraints;
        List aaRefersTo = aaConstraints.referencedTables(evaluator);
        assertEquals(0, aaRefersTo.size());
        
        Constraints bbConstraints = dataStore.table("bb").constraints;
        Iterator bbRefersTo = 
            bbConstraints.referencedTables(evaluator).iterator();
        assertEquals("aa", bbRefersTo.next());
        assertFalse(bbRefersTo.hasNext());
        
        Constraints ccConstraints = dataStore.table("cc").constraints;
        Iterator ccRefersTo = 
            ccConstraints.referencedTables(evaluator).iterator();
        assertEquals("aa", ccRefersTo.next());
        assertEquals("bb", ccRefersTo.next());
        assertFalse(ccRefersTo.hasNext());
    }

}
