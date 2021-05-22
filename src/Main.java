import executor.CorrectsCheckExecutor;
import executor.SpeedCheckExecutor;
import operation.Operation;
import tree.Tree;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static operation.OperationType.DELETE;
import static operation.OperationType.INSERT;

public class Main {
    private static final int OPERATION_COUNT = 10000;
    private static final int BOUND = 100000;
    private static final int THREAD_COUNT = 1;

    public static final int K = 100_000;

    public static void main(String[] args) throws InterruptedException{
        speedMeasure();
    }

    private static void speedMeasure() throws InterruptedException{
        Tree tree = new Tree();
        prepopulate(tree);
        double x = 0.1;
        List<SpeedCheckExecutor> executors = range(0, THREAD_COUNT)
                .mapToObj(i -> new SpeedCheckExecutor(tree, x, "" + i))
                .collect(toList());
        System.out.println("--- START ---");
        executors.forEach(Thread::start);
        for (Thread e : executors) e.join();
        long operationCount = executors.stream()
                .map(executor -> executor.counter)
                .reduce(0L, Long::sum);
        System.out.println(operationCount / 5L);
    }

    private static void prepopulate(Tree tree) {
        for (int i = 0; i < K; ++i) {
            if (Math.random() > 0.5) tree.insert(i);
        }
    }

    private static void check() throws InterruptedException {
        Tree tree = new Tree();

        List<Integer> valuesI = new Random()
                .ints(OPERATION_COUNT, -BOUND, BOUND)
                .boxed()
                .collect(toList());

        List<Integer> valuesR = new Random()
                .ints(OPERATION_COUNT, -BOUND, BOUND)
                .boxed()
                .collect(toList());

        List<Operation> inserts = range(0, OPERATION_COUNT)
                .mapToObj(i -> new Operation(INSERT, valuesI.get(i)))
                .collect(toList());
        List<Operation> removes = range(0, OPERATION_COUNT)
                .mapToObj(i -> new Operation(DELETE, valuesR.get(i)))
                .collect(toList());

        List<CorrectsCheckExecutor> executors = range(0, THREAD_COUNT)
                .mapToObj(i -> new CorrectsCheckExecutor(tree, inserts, THREAD_COUNT, i))
                .collect(toList());
        executors.forEach(CorrectsCheckExecutor::start);
        for (CorrectsCheckExecutor e : executors) e.join();

        executors = range(0, THREAD_COUNT)
                .mapToObj(i -> new CorrectsCheckExecutor(tree, removes, THREAD_COUNT, i))
                .collect(toList());
        executors.forEach(CorrectsCheckExecutor::start);
        for (CorrectsCheckExecutor e : executors) e.join();

        // CHECK
        Set<Integer> set = new HashSet<>();
        inserts.forEach(operation -> set.add(operation.value));
        removes.forEach(operation -> set.remove(operation.value));

        var data = tree.getData().stream().sorted().collect(toList());
        var setList = set.stream().sorted().collect(toList());
        boolean ok = data.size() == setList.size();
        for (int i = 0; i < data.size(); ++i) {
            ok = ok && data.get(i).equals(setList.get(i));
            if (!ok) break;
        }
        System.out.println(ok ? "CORRECT" : "INCORRECT");
    }
}
