package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.util.ImmutableList;

public class ConstraintsTest extends TestCase {
    
    public void testDropColumn() throws Exception {
        Constraints constraints = new Constraints(
            new PrimaryKey(makeColumns("id")),
            ImmutableList.singleton(new UniqueConstraint(makeColumns("id"))),
            new ImmutableList()
        );
        assertEquals(2, constraints.constraints.size());
        
        Constraints newConstraints = constraints.dropColumn(null, "id");
        assertEquals(0, newConstraints.constraints.size());
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

}
