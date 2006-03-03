package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.where.literal.QuotedString;

public class GroupByKeysTest extends TestCase {
    
    public void testResolveColumns() throws Exception {
        GroupByKeys keys = new GroupByKeys();
        keys.add(new GroupItem(new Concatenate(new SingleColumn("a"), new QuotedString("'abc'"))));
        Row row = new Row(new TupleBuilder().appendColumnCell("foo", "a", NullCell.INSTANCE));
        keys.resolve(row);
        
        GroupByKeys expected = new GroupByKeys();
        expected.add(new GroupItem(new Concatenate(new SingleColumn("foo", "a"), new QuotedString("'abc'"))));
        assertEquals(expected, keys);
    }
    
    // TODO: max(a) aggregateexpression
    // TODO: count(*) (does nothing)
    // TODO: error case (ambiguous column reference)

}
