package tree;

import java.util.List;

import static tree.NodeType.ROUTING;

public class Tree {
    public Node root;

    public Tree() {
        root = new Node(0, ROUTING);
    }

    public boolean insert(int value) {
        return root.insert(value);
    }

    public boolean remove(int value) {
        return root.remove(value, null, null);
    }

    public boolean contains(int value) {
        return root.contains(value);
    }

    public List<Integer> getData() {
        return root.getData();
    }
}
