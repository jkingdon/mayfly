package net.sourceforge.mayfly.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @internal
 * This is basically a topological sort class.  It is relatively general
 * but does have one assumption baked in: that one can order the nodes
 * (with no two being equal), and that one desires to use this order
 * to decide which nodes to select.
 */
public class Graph {
    
    SortedMap/*<Node,SortedSet<Node>>*/ predecessors = 
        new TreeMap(new NodeComparator());
    SortedMap/*<Node,SortedSet<Node>>*/ successors = 
        new TreeMap(new NodeComparator());

    public int nodeCount() {
        int count = predecessors.size();
        if (count != successors.size()) {
            throw new RuntimeException(
                "have " + count + " predecessors but " + 
                successors.size() + " successors");
        }
        return count;
    }

    public void addNode(Node node) {
        Object oldPredecessors = 
            predecessors.put(node, new TreeSet(new NodeComparator()));
        if (oldPredecessors != null) {
            throw new RuntimeException("already have node " + node);
        }
        successors.put(node, new TreeSet(new NodeComparator()));
    }

    public void addEdge(Node from, Node to) {
        ((SortedSet) predecessors.get(to)).add(from);
        ((SortedSet) successors.get(from)).add(to);
    }

    public SortedSet predecessors(Node to) {
        return (SortedSet) predecessors.get(to);
    }

    public SortedSet successors(Node from) {
        return (SortedSet) successors.get(from);
    }

    static class NodeComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            Node first = (Node) arg0;
            Node second = (Node) arg1;
            return first.backupOrdering(second);
        }
    }

    public void removeEdge(Node from, Node to) {
        {
            boolean wasPresent = 
                ((SortedSet) predecessors.get(to)).remove(from);
            if (!wasPresent) {
                throw new RuntimeException(
                    "there was no edge from " + from + " to " + to);
            }
        }
        
        {
            boolean wasPresent = 
                ((SortedSet) successors.get(from)).remove(to);
            if (!wasPresent) {
                throw new RuntimeException(
                    "there was no edge from " + from + " to " + to);
            }
        }
    }

    public SortedSet nodesWithNoPredecessor() {
        SortedSet result = new TreeSet(new NodeComparator());
        for (Iterator iter = predecessors.entrySet().iterator(); 
            iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Node node = (Node) entry.getKey();
            SortedSet predecessors = (SortedSet) entry.getValue();
            if (predecessors.isEmpty()) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * This operation destroys the graph that it is operating on.
     */
    public List topologicalSort() {
        List result = new ArrayList();
        List queue = new ArrayList(nodesWithNoPredecessor());
        int nodeCount = nodeCount();
        while (result.size() < nodeCount) {
            sortStep(result, queue);
        }
        return result;
    }

    void sortStep(List result, List queue) {
        if (queue.isEmpty()) {
            throw new CycleDetectedException();
        }
        
        Node head = (Node) queue.remove(0);
        result.add(head);
        
        SortedSet headSuccessors = new TreeSet(new NodeComparator());
        headSuccessors.addAll(successors(head));
        Iterator iterator = headSuccessors.iterator();
        while (iterator.hasNext()) {
            Node next = (Node) iterator.next();
            removeEdge(head, next);
            if (predecessors(next).size() == 0) {
                queue.add(next);
            }
        }
    }

    public void addNodes(List nodes) {
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Node table = (Node) iter.next();
            addNode(table);
        }
    }

}
