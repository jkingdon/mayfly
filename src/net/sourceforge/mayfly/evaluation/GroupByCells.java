package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.Arrays;
import java.util.List;

/**
 * @internal
 * Note that the kind of equality in equals/hashCode 
 * is the kind we want for GROUP BY or
 * DISTINCT, in which all nulls are equal.  That is, it is different
 * from {@link Cell#sqlEquals(Cell)}, in which null != null.
 * 
 * See also {@link Cell}'s equals/hashCode
 */
public class GroupByCells extends ValueObject {
    
    private final ImmutableList<Cell> cells;
    
    public GroupByCells(Cell... array) {
        this(Arrays.asList(array));
    }
    
    public GroupByCells(List<Cell> list) {
        for (Cell cell : list) {
            if (cell == null) {
                throw new NullPointerException("cell is null in GroupByCells");
            }
        }
        cells = new ImmutableList<Cell>(list);
    }

    public Cell firstKey() {
        return get(0);
    }

    public int size() {
        return cells.size();
    }

    public Cell get(int index) {
        return cells.get(index);
    }

}
