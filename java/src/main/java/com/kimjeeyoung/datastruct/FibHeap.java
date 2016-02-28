package com.kimjeeyoung.datastruct;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An implementation of Fibonacci Heap.
 *
 * @param <T> Contained type
 */
public class FibHeap<T extends Comparable<T>> {
    public interface FibNode<T extends Comparable<T>> {
        /**
         * Decrement the key of the given node.
         *
         * @param t value decremented to.
         * @throws IllegalArgumentException if the current value is smaller than <code>t</code>.
         */
        void decrement(T t);

        /**
         * Key of the given node.
         */
        T getKey();
    }

    /**
     * Minimum element of the heap.
     */
    private FibNodeImpl<T> root = null;

    /**
     * True if the heap can be used. Set to <code>false</code> after {@link #union(FibHeap)} is called.
     */
    private boolean active = true;

    public FibNode<T> insert(T t) {
        checkArgument(this.active);
        FibNodeImpl<T> node = new FibNodeImpl<>(t, this);
        if (root == null) {
            root = node;
        } else {
            root.appendSibling(node);
            if (root.value.compareTo(t) > 0) {
                root = node;
            }
        }
        return node;
    }

    public T getMinimum() {
        checkArgument(this.active);
        return root == null ? null : root.value;
    }

    public T popMinimum() {
        checkArgument(this.active);
        if (root == null) {
            return null;
        }
        FibNodeImpl<T> oldRoot = root;
        root = root.remove();
        if (root == null && oldRoot.child == null) {
            return oldRoot.value; // heap is empty after pop. no further actions needed.
        } else if (root == null) {
            root = oldRoot.child; // previously no sibling, so elevate children to top level nodes.
        } else if (oldRoot.child != null) {
            oldRoot.child.iterateSibling((node) -> node.parent = null);
            root.appendSibling(oldRoot.child); // append children to top level nodes.
        }

        // set the new root node to the new minimum.
        // this guarantees that we'll never relocate root in consolidation step.
        root.iterateSibling((node) -> {
            checkArgument(node.parent == null);
            if (root.value.compareTo(node.value) > 0) {
                root = node;
            }
        });

        // consolidate children.
        final ArrayList<FibNodeImpl<T>> collapsed = new ArrayList<>();
        root.iterateSibling((curNode) -> {
            int d = curNode.degree();
            for (int i = collapsed.size(); i <= d; i++) {
                collapsed.add(null);
            }
            while (collapsed.get(d) != null) {
                final FibNodeImpl<T> child;
                if (collapsed.get(d).value.compareTo(curNode.value) < 0) {
                    child = curNode;
                    curNode = collapsed.get(d);
                } else {
                    child = collapsed.get(d);
                }
                child.remove();
                curNode.appendChild(child);
                collapsed.set(d, null);
                d++;
                for (int i = collapsed.size(); i <= d; i++) {
                    collapsed.add(null);
                }
            }
            collapsed.set(d, curNode);
        });
        return oldRoot.value;
    }

    /**
     * combine two heaps into one.
     */
    public FibHeap<T> union(FibHeap<T> other) {
        checkArgument(this.active && other.active);
        this.active = false;
        other.active = false;
        FibHeap<T> heap = new FibHeap<>();
        if (this.root != null || other.root != null) {
            if (this.root == null) {
                heap.root = other.root;
            } else if (other.root == null) {
                heap.root = this.root;
            } else {
                this.root.appendSibling(other.root);
                if (this.root.value.compareTo(other.root.value) < 0) {
                    heap.root = this.root;
                } else {
                    heap.root = other.root;
                }
            }
        }
        return heap;
    }

    /**
     * Returns the size of the Fibonacci heap.
     */
    public int size() {
        checkArgument(this.active);
        return root == null ? 0 : root.getSize();
    }

    /**
     * Private structure used to store mutable variables for lambdas.
     */
    private static final class Counter {
        int counter = 0;
    }

    /**
     * Internal nodes of Fibonacci Heap.
     *
     * @param <T>
     */
    private final static class FibNodeImpl<T extends Comparable<T>> implements FibNode<T> {
        /**
         * underlying heap.
         */
        private final FibHeap<T> heap;
        /**
         * left sibling. forms a doubly linked list. can be "this".
         */
        private FibNodeImpl<T> left;
        /**
         * right sibling. forms a doubly linked list. can be "this".
         */
        private FibNodeImpl<T> right;
        /**
         * reference to a child. since children form a doubly linked list, all children can be obtained
         * via traversing its siblings. can be null if it is a leaf node.
         */
        private FibNodeImpl<T> child;
        /**
         * reference to the parent. can be null if it is the root node.
         */
        private FibNodeImpl<T> parent;
        /**
         * Assigned value. cannot be changed.
         */
        private T value;
        /**
         * true if a child ever has been removed.
         */
        private boolean marked;

        private FibNodeImpl(T value, FibHeap<T> heap) {
            this.value = value;
            this.left = this;
            this.right = this;
            this.child = null;
            this.marked = false;
            this.heap = heap;
        }

        @Override
        public T getKey() {
            return value;
        }

        @Override
        public void decrement(T t) {
            if (value.compareTo(t) <= 0) {
                throw new IllegalArgumentException("Cannot decrement to a greater value.");
            }
            value = t;
            if (parent != null && parent.value.compareTo(t) > 0) {
                // heap invariant broken - elevate the node to top.
                FibNodeImpl<T> oldParent = parent;
                oldParent.cut(this);
                oldParent.cascadeCut();
            }

            if (heap.root.value.compareTo(t) > 0) {
                heap.root = this;
            }
        }

        private void cut(FibNodeImpl<T> child) {
            child.remove();
            heap.root.appendSibling(child);
            child.marked = false;
        }

        private void cascadeCut() {
            if (parent != null) {
                if (!this.marked) {
                    this.marked = true;
                } else {
                    FibNodeImpl<T> oldParent = parent;
                    oldParent.cut(this);
                    oldParent.cascadeCut();
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, 1);
            return sb.toString();
        }

        /**
         * this method does NOT handle parents.
         * @param other
         */
        void appendSibling(FibNodeImpl<T> other) {
            FibNodeImpl<T> prevThisRight = this.right;
            FibNodeImpl<T> prevOtherLeft = other.left;
            this.right = other;
            other.left = this;
            prevOtherLeft.right = prevThisRight;
            prevThisRight.left = prevOtherLeft;
        }
;
        void appendChild(FibNodeImpl<T> other) {
            checkArgument(value.compareTo(other.value) < 0);
            other.iterateSibling((node) -> node.parent = this);
            if (child == null) {
                child = other;
            } else {
                child.appendSibling(other);
            }
        }

        /**
         * remove the current node from the linked list, return the new node to the linked list.
         */
        FibNodeImpl<T> remove() {
            if (parent != null && parent.child == this) {
                // ensure that parent.child is updated accordingly.
                if (this.left == this) {
                    parent.child = null;
                } else {
                    parent.child = this.left;
                }
            }
            parent = null;
            if (this.left == this) {
                return null;
            }
            // FROM:
            // prevLeft <-> (this) <-> prevRight
            // TO:
            // prevLeft <-> prevRight
            FibNodeImpl<T> prevRight = this.right;
            FibNodeImpl<T> prevLeft = this.left;
            prevRight.left = prevLeft;
            prevLeft.right = prevRight;
            // detach "this".
            this.left = this.right = this;
            return prevLeft;
        }

        /**
         * # of all nodes at the given level (transitive siblings and children).
         */
        int getSize() {
            Counter ctx = new Counter();
            iterateSibling((node) -> {
                ctx.counter++;
                if (node.child != null) {
                    node.child.getSize();
                }
            });
            return ctx.counter;
        }

        /**
         * # of sibling nodes of the given node, including itself.
         */
        int getSiblingCount() {
            Counter ctx = new Counter();
            iterateSibling((node) -> ctx.counter++);
            return ctx.counter;
        }

        /**
         * # of direct children.
         */
        int degree() {
            return child == null ? 0 : this.child.getSiblingCount();
        }

        /**
         * iterate through the siblings.
         * Restrictions:
         * CAN modify the node.
         * CAN remove the node.
         * SHOULD NOT modify its siblings.
         * SHOULD NOT detach the root.
         */
        private void iterateSibling(Consumer<FibNodeImpl<T>> consumer) {
            FibNodeImpl<T> node = this;
            do {
                FibNodeImpl<T> curNode = node;
                node = node.right; // increment the pointer first so that curNode can be safely removed.
                consumer.accept(curNode);
            } while (node != this);
        }

        private void toString(StringBuilder sb, int indent) {
            String prefix = Strings.repeat("-", indent);
            iterateSibling((node) -> {
                // checkArgument(node.parent == this);
                sb.append(prefix).append(node.value);
                if (node.marked) {
                    sb.append(" X");
                }
                sb.append("\n");
                if (node.child != null) {
                    node.child.toString(sb, indent + 1);
                }
            });
        }
    }
}
