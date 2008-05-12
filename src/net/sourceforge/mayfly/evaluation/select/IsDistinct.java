package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.GroupByCells;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.what.Selected;

import java.util.LinkedHashSet;
import java.util.Set;

public class IsDistinct extends Distinct {

    @Override
    public ResultRows distinct(Selected selected, ResultRows rows) {
        Set distinctRows = constructDistinctRows(selected, rows);
        
        return distinctRowsToResultRows(selected, distinctRows);
    }

    private ResultRows distinctRowsToResultRows(
        Selected selected, Set<GroupByCells> distinctRows) {
        ResultRows result = new ResultRows();
        for (GroupByCells cells : distinctRows) {
            result = result.with(selected.toRow(cells));
        }
        return result;
    }

    private Set constructDistinctRows(Selected selected, ResultRows rows) {
        Set distinctRows = new LinkedHashSet();
        for (ResultRow row : rows) {
            GroupByCells cells = selected.evaluateAll(row);
            distinctRows.add(cells);
        }
        return distinctRows;
    }

}
