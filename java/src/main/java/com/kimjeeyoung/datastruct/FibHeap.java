package com.kimjeeyoung.datastruct;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.kimjeeyoung.datastruct.ComparatorUtil.compareTo;

/**
 * An implementation of Fibonacci Heap, according to CLRS.
 *
 * @param <T> Contained type
 */
public class FibHeap<T> implements Heap<T> {

  /**
   * Minimum element of the heap.
   */
  private FibNodeImpl<T> root = null;

  /**
   * True if the heap can be used. Set to <code>false</code> after {@link #union(FibHeap)} is
   * called.
   */
  private boolean active = true;

  /**
   * Optional parameter.
   */
  private Comparator<? super T> comparator;

  public FibHeap() {
    this.comparator = null;
  }

  public FibHeap(Comparator<? super T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public HeapNode<T> insert(T t) {
    checkArgument(this.active);
    FibNodeImpl<T> node = new FibNodeImpl<>(t, this);
    if (root == null) {
      root = node;
    } else {
      root.appendSibling(node);
      if (compareTo(comparator, root.value, t) > 0) {
        root = node;
      }
    }
    return node;
  }

  @Override
  public T getMinimum() {
    checkArgument(this.active);
    if (root == null) {
      throw new NoSuchElementException();
    }
    return root.value;
  }

  @Override
  public T popMinimum() {
    checkArgument(this.active);
    if (root == null) {
      throw new NoSuchElementException();
    }
    FibNodeImpl<T> oldRoot = root;
    root = root.remove();
    if (root == null && oldRoot.child == null) {
      return oldRoot.value; // heap is empty after pop. no further actions needed.
    } else if (root == null) {
      oldRoot.child.iterateSibling((node) -> node.parent = null);
      root = oldRoot.child; // previously no sibling, so elevate children to top level nodes.
    } else if (oldRoot.child != null) {
      oldRoot.child.iterateSibling((node) -> node.parent = null);
      root.appendSibling(oldRoot.child); // append children to top level nodes.
    }

    // set the new root node to the new minimum.
    // this guarantees that we'll never relocate root in consolidation step.
    root.iterateSibling((node) -> {
      checkArgument(node.parent == null);
      if (compareTo(comparator, root.value, node.value) > 0) {
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
        if (compareTo(comparator, collapsed.get(d).value, curNode.value) < 0) {
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
        if (compareTo(comparator, this.root.value, other.root.value) < 0) {
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
   */
  private final static class FibNodeImpl<T> implements HeapNode<T> {
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
    public void decrement(T t) {
      if (compareTo(heap.comparator, value, t) <= 0) {
        throw new IllegalArgumentException("Cannot decrement to a greater value.");
      }
      value = t;
      if (parent != null && compareTo(heap.comparator, parent.value, t) > 0) {
        // heap invariant broken - elevate the node to top.
        FibNodeImpl<T> oldParent = parent;
        oldParent.cut(this);
        oldParent.cascadeCut();
      }

      if (compareTo(heap.comparator, heap.root.value, t) > 0) {
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
     * Note: this method does NOT handle setting {@link #parent} link.
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
      checkArgument(compareTo(heap.comparator, value, other.value) < 0);
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
     * iterate through the siblings. Restrictions: CAN modify the node. CAN remove the node. SHOULD
     * NOT modify its siblings. SHOULD NOT detach the root.
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

  /**
   * validate the internal state of the heap.
   */
  @VisibleForTesting
  void validateState() {
    if (root != null) {
      root.iterateSibling((node) -> {
        // invariants for being a sibling of the root node.
        checkArgument(node.parent == null);
        checkArgument(compareTo(comparator, root.value, node.value) <= 0);
        // doubly linked list is correctly formed.
        checkArgument(node.left.right == node);
        checkArgument(node.right.left == node);
        // recursion
        validateState(node);
      });
      validateState(root);
    }
  }

  private void validateState(FibNodeImpl<T> parent) {
    if (parent.child != null) {
      parent.child.iterateSibling((node) -> {
        // invariants regarding parent <-> child relation.
        checkArgument(node.parent == parent);
        checkArgument(compareTo(comparator, node.parent.value, node.value) <= 0);
        // doubly linked list is correctly formed.
        checkArgument(node.left.right == node);
        checkArgument(node.right.left == node);
      });
    }
  }
}
