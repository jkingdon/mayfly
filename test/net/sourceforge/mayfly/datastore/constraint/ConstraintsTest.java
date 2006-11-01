package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.util.ImmutableList;

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

}
