package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.parser.*;

import java.util.*;

public class RowMask extends Enumerable {


    private List masks = new ArrayList();

    public RowMask() {
    }

    public RowMask(List masks) {
        this.masks = masks;
    }


    protected Object createNew(Iterable items) {
        return new RowMask(asList(items));
    }

    public Iterator iterator() {
        return masks.iterator();
    }

    public RowMask add(RowMaskElement maskElement) {
        masks.add(maskElement);
        return this;
    }


}
