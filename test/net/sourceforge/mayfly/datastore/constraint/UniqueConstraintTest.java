package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import java.util.Arrays;

import net.sourceforge.mayfly.datastore.Columns;

public class UniqueConstraintTest extends TestCase {
    
    public void testRename() throws Exception {
        UniqueConstraint constraint = new UniqueConstraint(
            Columns.fromColumnNames(
                Arrays.asList(new String[] { "a", "b", "c" })),
            "abc_constraint"
        );
        UniqueConstraint result = 
            (UniqueConstraint) constraint.renameColumn("B", "bb");
        assertEquals(3, result.names.size());
        assertEquals("a", result.names.name(0));
        assertEquals("bb", result.names.name(1));
        assertEquals("c", result.names.name(2));
        
        assertEquals("abc_constraint", result.constraintName);
    }

}
