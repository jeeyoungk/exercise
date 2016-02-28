package com.kimjeeyoung;

import com.google.common.base.Strings;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation of Fibonacci Heap
 * Created by jeeyoungk on 2/27/16.
 */
public class FibHeap<T extends Comparable<T>> {
    /**
     * used to store mutable variables for lambdas.
     */
    private static final class Counter {
        int counter = 0;
    }

    private final static class Node<T extends Comparable<T>> {
        /**
         * left sibling. forms a doubly linked list. can be "this".
         */
        Node<T> left;
        /**
         * right sibling. forms a doubly linked list. can be "this".
         */
        Node<T> right;
        /**
         * reference to a child. can be null if it is a leaf node.
         */
        Node<T> child;
        /**
         * Assigned value. cannot be changed.
         */
        private final T value;

        private Node(T value) {
            this.value = value;
            this.left = this;
            this.right = this;
            this.child = null;
        }

        public void appendSibling(Node<T> other) {
            Node<T> prevThisRight = this.right;
            Node<T> prevOtherLeft = other.left;
            this.right = other;
            other.left = this;
            prevOtherLeft.right = prevThisRight;
            prevThisRight.left = prevOtherLeft;
        }

        public void appendChild(Node<T> other) {
            checkArgument(value.compareTo(other.value) < 0);
            if (child == null) {
                child = other;
            } else {
                other.appendSibling(child);
            }
        }

        /**
         * remove the current node from the linked list, return the new node to the linked list.
         */
        public Node<T> remove() {
            if (this.left == this) {
                return null;
            }
            // FROM:
            // prevLeft <-> (this) <-> prevRight
            // TO:
            // prevLeft <-> prevRight
            Node<T> prevRight = this.right;
            Node<T> prevLeft = this.left;
            prevRight.left = prevLeft;
            prevLeft.right = prevRight;
            // detach "this".
            this.left = this.right = this;
            return prevLeft;
        }

        /**
         * # of all nodes at the given level (transitive siblings and children).
         */
        public int getSize() {
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
        public int getSiblingCount() {
            Counter ctx = new Counter();
            iterateSibling((node) -> ctx.counter++);
            return ctx.counter;
        }

        /**
         * # of direct children.
         */
        public int degree() {
            return child == null ? 0 : this.child.getSiblingCount();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, 1);
            return sb.toString();
        }

        /**
         * iterate through the siblings.
         * Restrictions:
         * CAN modify the node.
         * CAN remove the node.
         * SHOULD NOT modify its siblings.
         */
        private void iterateSibling(Consumer<Node<T>> consumer) {
            Node<T> node = this;
            do {
                Node<T> curNode = node;
                node = node.right; // increment the pointer first so that curNode can be safely removed.
                consumer.accept(curNode);
            } while (node != this);
        }

        private void toString(StringBuilder sb, int indent) {
            String prefix = Strings.repeat("-", indent);
            iterateSibling((node) -> {
                sb.append(prefix).append(node.value).append("\n");
                if (node.child != null) {
                    node.child.toString(sb, indent + 1);
                }
            });
        }
    }

    private Node<T> root = null;
    private boolean active = true;

    public void insert(T t) {
        checkArgument(this.active);
        Node<T> node = new Node<>(t);
        if (root == null) {
            root = node;
        }
        root.appendSibling(node);
        if (root.value.compareTo(t) > 0) {
            root = node;
        }
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
        Node<T> oldRoot = root;
        root = root.remove();
        if (root == null && oldRoot.child == null) {
            return oldRoot.value; // heap is empty after pop. no further actions needed.
        } else if (root == null) {
            root = oldRoot.child; // previously no sibling, so elevate children to top level nodes.
        } else if (oldRoot.child != null) {
            root.appendChild(oldRoot.child); // append children to top level nodes.
        }

        // consolidate children.
        @SuppressWarnings("unchecked")
        Node<T>[] collapsed = new Node[root.getSiblingCount() + 1];
        root.iterateSibling((curNode) -> {
            int d = curNode.degree();
            while (collapsed[d] != null) {
                final Node<T> child;
                if (collapsed[d].value.compareTo(curNode.value) < 0) {
                    child = curNode;
                    curNode = collapsed[d];
                } else {
                    child = collapsed[d];
                }
                child.remove();
                curNode.appendChild(child);
                collapsed[d] = null;
                d++;
            }
            collapsed[d] = curNode;
        });

        // set the new root node to the new minimum.
        root.iterateSibling((node) -> {
            if (root.value.compareTo(node.value) > 0) {
                root = node;
            }
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

    public int size() {
        checkArgument(this.active);
        return root == null ? 0 : root.getSize();
    }
}
