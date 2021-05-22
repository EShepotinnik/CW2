package executor;

import tree.Tree;

import java.util.Random;

import static java.lang.System.nanoTime;

public class SpeedCheckExecutor extends Thread {
    private final Tree tree;
    private final double x;
    public long counter = 0;
    public String label;

    public SpeedCheckExecutor(Tree tree, double x, String label) {
        this.tree = tree;
        this.x = x;
        this.label = label;
    }

    @Override
    public void run() {
        long start = nanoTime();
        while (nanoTime() < start + 5_000_000_000L) {
            ++counter;
            int key = new Random().nextInt(100_000);
            double p = Math.random();
            if (p < x) {
                tree.insert(key);
            } else if (x <= p && p <= 2.0 * x) {
                tree.remove(key);
            } else {
                tree.contains(key);
            }
        }
    }
}
