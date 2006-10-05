package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

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
        
        Constraints newConstraints = constraints.dropColumn(null, "id");
        assertNull(newConstraints.primaryKey);
        assertEquals(0, newConstraints.constraints.size());
    }

    private Columns makeColumns(String name) {
        return new Columns(ImmutableList.singleton(new Column(name)));
    }

}
