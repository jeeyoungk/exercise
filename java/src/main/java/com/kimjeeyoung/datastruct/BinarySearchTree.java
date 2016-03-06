// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.kimjeeyoung.datastruct.ComparatorUtil.compareTo;

/**
 * Implementation of binary search tree.
 */
public class BinarySearchTree<K, V> implements Tree<K, V> {
  private static class Node<K, V> {
    private final K key;
    private final V value;
    private Node<K, V> left;
    private Node<K, V> right;

    private Node(K key, V value) {
      this.key = key;
      this.value = value;
    }

    private void add(BinarySearchTree<K, V> tree, Node<K, V> node) {
      if (compareTo(tree.comparator, key, node.key) <= 0) {
        if (left == null) {
          left = node;
        } else {
          left.add(tree, node);
        }
      } else {
        if (right == null) {
          right = node;
        } else {
          right.add(tree, node);
        }
      }
    }

    private void iterate(BiConsumer<K, V> consumer) {
      if (left != null) {
        left.iterate(consumer);
      }
      consumer.accept(key, value);
      if (right != null) {
        right.iterate(consumer);
      }
    }

    private Node<K, V> findNode(BinarySearchTree<K, V> tree, K key) {
      int comparison = compareTo(tree.comparator, this.key, key);
      if (comparison == 0) {
        return this;
      } else if (comparison < 0) {
        return left == null ? null : left.findNode(tree, key);
      } else {
        return right == null ? null : right.findNode(tree, key);
      }
    }

    private Node<K, V> remove(BinarySearchTree<K, V> tree, K key, Consumer<Node<K, V>> parentSetter) {
      int comparison = compareTo(tree.comparator, this.key, key);
      if (comparison == 0) {
        rebalance(parentSetter);
        return null;
      } else if (comparison < 0) {
        return left == null ? null : left.remove(tree, key, (n) -> left = n);
      } else {
        return right == null ? null : right.remove(tree, key, (n) -> right = n);
      }
    }

    private void rebalance(Consumer<Node<K, V>> parentSetter) {
      if (left == null && right == null) {
        parentSetter.accept(null);
      } else if (left == null) {
        parentSetter.accept(right);
      } else if (right == null) {
        parentSetter.accept(left);
      } else {
        Node<K, V> oldParent = this;
        Node<K, V> newParent = this.left;
        parentSetter.accept(newParent);
        rebalance((n) -> newParent.left = n);
        newParent.right = oldParent.right;
      }
    }
  }

  private Node<K, V> root;

  private Comparator<? super K> comparator;

  public BinarySearchTree() {
  }

  public BinarySearchTree(Comparator<? super K> comparator) {
    this.comparator = comparator;
  }

  @Override
  public void add(K key, V value) {
    Node<K, V> newNode = new Node<>(key, value);
    if (root == null) {
      root = newNode;
    } else {
      root.add(this, newNode);
    }
  }

  @Override
  public boolean contains(K key) {
    return root != null && root.findNode(this, key) != null;
  }

  @Override
  public V remove(K key) {
    if (root != null) {
      Node<K, V> node = root.remove(this, key, (n) -> this.root = n);
      if (node != null) {
        return node.value;
      }
    }
    return null;
  }

  @Override
  public V get(K key) {
    if (root != null) {
      Node<K, V> node = root.findNode(this, key);
      if (node != null) {
        return node.value;
      }
    }
    return null;
  }

  @Override
  public void iterate(BiConsumer<K, V> consumer) {
    if (root != null) {
      root.iterate(consumer);
    }
  }
}
