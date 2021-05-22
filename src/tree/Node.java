package tree;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static tree.NodeType.DATA;
import static tree.NodeType.ROUTING;

public class Node {
    public int value;
    public NodeType type;
    public boolean deleted = false;
    public Node left;
    public Node right;
    public Lock lock;

    public Node(int value, NodeType type) {
        this.value = value;
        this.type = type;
        this.lock = new ReentrantLock();
    }

    public boolean insert(int value) {
        Node cur = this;
        while (true) {
            if (value < cur.value) {
                if (cur.left == null) {
                    cur.lock.lock();
                    if (cur.deleted) {
                        cur.lock.unlock();
                        cur = this;
                        continue;
                    }
                    if (cur.left == null) {
                        if (cur.type == ROUTING) {
                            cur.left = new Node(value, DATA);
                            cur.lock.unlock();
                            return true;
                        }
                        cur.type = ROUTING;
                        cur.left = new Node(value, DATA);
                        cur.right = new Node(cur.value, DATA);
                        cur.lock.unlock();
                        return true;
                    }
                    cur.lock.unlock();
                }
                cur = cur.left;
            } else if (value == cur.value && cur.type == DATA) {
                cur.lock.lock();
                boolean exists = cur.type == DATA && !cur.deleted;
                cur.lock.unlock();
                if (exists) {
                    return false;
                } else {
                    cur = cur.deleted ? this : cur.right;
                }
            } else {
                if (cur.right == null) {
                    cur.lock.lock();
                    if (cur.right == null) {
                        if (cur.deleted) {
                            cur.lock.unlock();
                            cur = this;
                            continue;
                        }
                        if (cur.type == ROUTING) {
                            cur.right = new Node(value, DATA);
                            cur.lock.unlock();
                            return true;
                        }
                        cur.type = ROUTING;
                        //
                        Node right = new Node(value, ROUTING);
                        right.left = new Node(cur.value, DATA);
                        right.right = new Node(value, DATA);
                        cur.right = right;
                        //
                        cur.lock.unlock();
                        return true;
                    }
                    cur.lock.unlock();
                }
                cur = cur.right;
            }
        }
    }

    public boolean remove(int value, Node parent, Node gparent) {
        Node cur = this;
        Node p = parent;
        Node g = gparent;
        while (true) {
            if (value < cur.value) {
                if (cur.left == null) {
                    return false;
                } else {
                    g = p;
                    p = cur;
                    cur = cur.left;
                }
            } else if (value > cur.value || cur.type == ROUTING) {
                if (cur.right == null) {
                    return false;
                } else {
                    g = p;
                    p = cur;
                    cur = cur.right;
                }
            } else {
                // Если близко к корню дерева (родитель всегда есть -- вспомогательная вершина)
                if (g == null) {
                    cur.lock.lock();
                    p.lock.lock();
                    // Если уже удалили, пробуем еще раз
                    if (cur.deleted) {
                        cur = this;
                        continue;
                    }
                    if (value < p.value) {
                        p.left = null;
                    } else {
                        p.right = null;
                    }
                    p.lock.unlock();
                    cur.lock.unlock();
                    return true;
                }
                // Лочим
                cur.lock.lock();
                p.lock.lock();
                g.lock.lock();
                // Если кто-то из трех удален, запускаем заново
                if (g.deleted || p.deleted || cur.deleted) {
                    g.lock.unlock();
                    p.lock.unlock();
                    cur.lock.unlock();
                    cur = this;
                    continue;
                }
                // Перекидываем ссылки
                if (p.value < g.value) {
                    g.left = value < p.value ? p.right : p.left;
                } else {
                    g.right = value < p.value ? p.right : p.left;
                }
                // Помечаем удаленными
                cur.deleted = true;
                p.deleted = true;
                // Разлочим
                g.lock.unlock();
                p.lock.unlock();
                cur.lock.unlock();
                return true;
            }
        }
    }

    public boolean contains(int value) {
        Node cur = this;
        while (value != cur.value) {
            if (value < cur.value) {
                cur = cur.left;
            } else {
                cur = cur.right;
            }
            if (cur == null) return false;
        }
        return true;
    }

    public List<Integer> getData() {
        if (this.type == DATA) return of(this.value);
        List<Integer> left = this.left == null ? of() : this.left.getData();
        List<Integer> right = this.right == null ? of() : this.right.getData();
        return concat(left.stream(), right.stream()).collect(toList());
    }
}
