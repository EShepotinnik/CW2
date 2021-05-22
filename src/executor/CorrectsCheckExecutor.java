package executor;

import operation.Operation;
import tree.Tree;

import java.util.List;

public class CorrectsCheckExecutor extends Thread {
    private final List<Operation> operations;
    private final Tree tree;
    private final int step;
    private final int offset;

    public CorrectsCheckExecutor(Tree tree, List<Operation> operations, int step, int offset) {
        this.tree = tree;
        this.operations = operations;
        this.step = step;
        this.offset = offset;
    }

    @Override
    public void run() {
        for (int i = offset; i < operations.size(); i += step) {
            switch (operations.get(i).type) {
                case INSERT:
                    tree.insert(operations.get(i).value);
                    break;
                case DELETE:
                    tree.remove(operations.get(i).value);
                    break;
                case CONTAINS:
                    tree.contains(operations.get(i).value);
                    break;
            }
        }
    }
}
